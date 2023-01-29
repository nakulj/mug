package com.google.mu.util;

import static com.google.mu.util.InternalCollectors.toImmutableList;
import static com.google.mu.util.Optionals.optional;
import static com.google.mu.util.Substring.before;
import static com.google.mu.util.Substring.first;
import static com.google.mu.util.Substring.spanningInOrder;
import static com.google.mu.util.Substring.suffix;
import static com.google.mu.util.stream.MoreCollectors.combining;
import static com.google.mu.util.stream.MoreCollectors.onlyElement;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.google.mu.function.Quarternary;
import com.google.mu.function.Quinary;
import com.google.mu.function.Senary;
import com.google.mu.function.Ternary;
import com.google.mu.util.stream.BiStream;
import com.google.mu.util.stream.MoreStreams;

/**
 * A (lossy) reverse operation of {@link String#format} to extract placeholder values from input
 * strings according to a format string. For example:
 *
 * <pre>{@code
 * return new StringFormat("Dear %s: %s?")
 *     .parse(input, (recipient, question) -> ...);
 * }</pre>
 *
 * <p>Placeholders can also be named:
 *
 * <pre>{@code
 * ImmutableMap<String, String> placeholderValues =
 *     new StringFormat("Dear {recipient}: {question}?")
 *         .parse("Dear Charlie: How are you?")
 *         .toMap();
 * }</pre>
 *
 * <p>If the placeholder auto detection doesn't work for you, for example, your format uses named
 * placeholders but also needs to include the {@code %s} as literal characters, specify the
 * placeholder explicitly as in:
 *
 * <pre>{@code
 * new StringFormat("I use {placeholder}, not %s", spanningInOrder("{", "}").repeatedly())
 * }</pre>
 *
 * <p>Note that other than the placeholders, characters in the format string are treated as
 * literals. This works better if your format string is close to free-form text with characters like
 * '.', '?', '(', '|' and what not because you won't have to escape them. On the other hand, it
 * won't work for more sophisticated patterns where regex modifiers and quantifiers are needed.
 *
 * <p>In the face of ambiguity, the {@code parse()} methods can be lossy. Consider the format string
 * of {@code String.format("I bought %s and %s", "apples and oranges", "chips")}, it returns {@code
 * "I bought apples and oranges and chips"}; but the following parsing code will incorrectly return
 * {@code Map.of("{fruits}", "apples", "{snacks}", "oranges and chips")}:
 *
 * <pre>{@code
 * new StringFormat("I bought {fruits} and {snacks}")
 *     .parse("I bought apples and oranges and chips", (fruits, snacks) -> ...);
 * }</pre>
 *
 * As such, only use this class on trusted input strings (i.e. not user inputs).
 * And use regex instead to better deal with ambiguity.
 *
 * <p>All the {@code parse()} and {@code match()} methods attempt to match the entire input string,
 * with the {@code parse()} methods throwing and {@code match()} returning {@code Optional.empty()}
 * upon mismatch.
 *
 * <p>If you need to find the string format anywhere in the input string, or need to find repeated
 * occurrences from the input string, use the {@code scan()} methods instead. Tack on {@code
 * .findFirst()} on the returned lazy stream if you only care to find a single occurrence.
 *
 * <p>This class is immutable and pre-compiles the format string at constructor time so that the
 * {@code parse()} methods will be more efficient.
 *
 * @since 6.6
 */
public final class StringFormat {
  private final String format;
  private final List<Substring.Match> placeholders;

  /** In the input string "key: %s value: %s", "key: " and " value: " are the literals. */
  private final List<String> literals;

  /**
   * Constructs a StringFormat using either {@code "%s"} as placeholder,
   * or placeholders with curly braces. For example:
   *
   * <pre>{@code
   * new StringFormat("Dear %s, your confirmation number is %s");
   * new StringFormat("Dear {person}, your confirmation number is {confirmation_number}");
   * }</pre>
   *
   * @param format the template format with placeholders
   * @throws IllegalArgumentException if {@code format} is invalid
   *     (e.g. a placeholder immediately followed by another placeholder)
   */
  public StringFormat(String format) {
    this(format, (format.contains("%s") ? first("%s") : spanningInOrder("{", "}")).repeatedly());
  }

  /**
   * Constructs a StringFormat. By default, {@code new StringFormat(format)} automatically detects
   * placeholders with either {@code "%s"} or curly braces. If you need different placeholder
   * syntax, for example, to use square brackets instead of curly braces:
   *
   * <pre>{@code
   * new StringFormat("Hi [person], my name is [me]", spanningInOrder("[", "]").repeatedly());
   * }</pre>
   *
   * @param format the template format with placeholders
   * @param placeholderVariablePattern placeholders in {@code format}.
   *     For example: {@code first("%s").repeatedly()}.
   * @throws IllegalArgumentException if {@code format} is invalid
   *     (e.g. a placeholder immediately followed by another placeholder)
   */
  public StringFormat(String format, Substring.RepeatingPattern placeholderVariablesPattern) {
    this.format = format;
    this.placeholders = placeholderVariablesPattern.match(format).collect(toImmutableList());
    this.literals = extractLiteralsFromFormat(format, placeholders);
    for (int i = 1; i < placeholders.size(); i++) {
      if (literals.get(i).isEmpty()) {
        throw new IllegalArgumentException(
            "invalid pattern with '" + placeholders.get(i - 1) + placeholders.get(i) + "'");
      }
    }
  }

  /**
   * Parses {@code input} and applies {@code reducer} with the single placeholder value
   * in this template.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("Job failed (job id: %s)").parse(input, jobId -> ...);
   * }</pre>
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly one placeholder.
   */
  public <R> R parse(String input, Function<? super String, R> reducer) {
    return parsePlaceholderValues(input).collect(onlyElement(reducer));
  }

  /**
   * Parses {@code input} and applies {@code reducer} with the two placeholder values
   * in this template.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("Job failed (job id: '%s', error code: %s)")
   *     .parse(input, (jobId, errorCode) -> ...);
   * }</pre>
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly two placeholders.
   */
  public <R> R parse(String input, BiFunction<? super String, ? super String, R> reducer) {
    return parsePlaceholderValues(input).collect(combining(reducer));
  }

  /**
   * Similar to {@link #parse(String, BiFunction}, but parses {@code input} and applies {@code
   * reducer} with the <em>3</em> placeholder values in this template.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("Job failed (job id: '%s', error code: %s, error details: %s)")
   *     .parse(input, (jobId, errorCode, errorDetails) -> ...);
   * }</pre>
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly 3 placeholders.
   */
  public <R> R parse(String input, Ternary<? super String,  R> reducer) {
    return parsePlaceholderValues(input).collect(combining(reducer));
  }

  /**
   * Similar to {@link #parse(String, BiFunction}, but parses {@code input} and applies {@code
   * reducer} with the <em>4</em> placeholder values in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly 4 placeholders.
   */
  public <R> R parse(String input, Quarternary<? super String,  R> reducer) {
    return parsePlaceholderValues(input).collect(combining(reducer));
  }

  /**
   * Similar to {@link #parse(String, BiFunction}, but parses {@code input} and applies {@code
   * reducer} with the <em>5</em> placeholder values in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly 5 placeholders.
   */
  public <R> R parse(String input, Quinary<? super String,  R> reducer) {
    return parsePlaceholderValues(input).collect(combining(reducer));
  }

  /**
   * Similar to {@link #parse(String, BiFunction}, but parses {@code input} and applies {@code
   * reducer} with the <em>6</em> placeholder values in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format or the template
   *     doesn't have exactly 6 placeholders.
   */
  public <R> R parse(String input, Senary<? super String,  R> reducer) {
    return parsePlaceholderValues(input).collect(combining(reducer));
  }

  /**
   * Parses {@code input} and extracts all placeholder name-value pairs in a BiStream,
   * in the same order as {@link #placeholders}.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the format
   */
  public BiStream<String, String> parse(String input) {
    return BiStream.zip(
        placeholders.stream().map(Substring.Match::toString), parsePlaceholderValues(input));
  }

  /**
   * Matches {@code input} against the pattern.
   *
   * <p>Returns an immutable list of placeholder values in the same order as {@link #placeholders},
   * upon success; otherwise returns empty.
   *
   * <p>The {@link Substring.Match} result type allows caller to inspect the characters around each
   * match, or to access the raw index in the input string.
   */
  public Optional<List<Substring.Match>> match(String input) {
    List<Substring.Match> builder = new ArrayList<>(placeholders.size());
    if (!input.startsWith(literals.get(0))) {  // first literal is the prefix
      return Optional.empty();
    }
    int inputIndex = literals.get(0).length();
    for (int i = 1; i < literals.size(); i++) {
      // subsequent literals are searched; last literal is the suffix.
      Substring.Pattern literalLocator =
          i < literals.size() - 1 ? first(literals.get(i)) : suffix(literals.get(i));
      Substring.Match placeholder = before(literalLocator).match(input, inputIndex);
      if (placeholder == null) {
        return Optional.empty();
      }
      builder.add(placeholder);
      inputIndex = placeholder.index() + placeholder.length() + literals.get(i).length();
    }
    return optional(inputIndex == input.length(), unmodifiableList(builder));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the single placeholder value to {@code reducer}
   * function for each iteration.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("/home/usr/myname/%s\n")
   *     .scan(multiLineInput, fileName -> ...);
   * }</pre>
   *
   * <p>unlike {@link StringFormat#parse(String, Function)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, Function<? super String, ? extends R> reducer) {
    return scanAndCollect(input, onlyElement(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the two placeholder values to {@code reducer}
   * function for each iteration.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("[key=%s, value=%s]")
   *     .repeatedly()
   *     .parse(input, (key, value) -> ...);
   * }</pre>
   *
   * <p>unlike {@link StringFormat#parse(String, BiFunction)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, BiFunction<? super String, ? super String, ? extends R> reducer) {
    return scanAndCollect(input, combining(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the three placeholder values to {@code reducer}
   * function for each iteration.
   *
   * <p>For example: <pre>{@code
   * new StringFormat("[%s + %s = %s]")
   *     .repeatedly()
   *     .parse(input, (lhs, rhs, result) -> ...);
   * }</pre>
   *
   * <p>unlike {@link StringFormat#parse(String, Ternary)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, Ternary<? super String, ? extends R> reducer) {
    return scanAndCollect(input, combining(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the 4 placeholder values to {@code reducer}
   * function for each iteration.
   *
   * <p>unlike {@link StringFormat#parse(String, Quarternary)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, Quarternary<? super String, ? extends R> reducer) {
    return scanAndCollect(input, combining(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the 5 placeholder values to {@code reducer}
   * function for each iteration.
   *
   * <p>unlike {@link StringFormat#parse(String, Quinary)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, Quinary<? super String, ? extends R> reducer) {
    return scanAndCollect(input, combining(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matches of this string format.
   * Returns the lazy stream of results from passing the 6 placeholder values to {@code reducer}
   * function for each iteration.
   *
   * <p>unlike {@link StringFormat#parse(String, Senary)}, the input string isn't matched
   * entirely: the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public <R> Stream<R> scan(String input, Senary<? super String, ? extends R> reducer) {
    return scanAndCollect(input, combining(reducer));
  }

  /**
   * Scans the {@code input} string and extracts all matched placeholders in this string format.
   *
   * <p>unlike {@link StringFormat#match(String)}, the input string isn't matched entirely:
   * the pattern doesn't have to start from the beginning, and if there are some remaining
   * characters that don't match the pattern any more, the stream stops. In particular, if there
   * is no match, empty stream is returned.
   */
  public Stream<List<Substring.Match>> scan(String input) {
    requireNonNull(input);
    return MoreStreams.whileNotNull(
        new Supplier<List<Substring.Match>>() {
          private int inputIndex = 0;

          @Override public List<Substring.Match> get() {
            inputIndex = input.indexOf(literals.get(0), inputIndex);
            if (inputIndex < 0) {
              return null;
            }
            inputIndex += literals.get(0).length();
            List<Substring.Match> builder = new ArrayList<>(placeholders.size());
            for (int i = 1; i < literals.size(); i++) {
              String literal = literals.get(i);
              Substring.Match placeholder = before(first(literal)).match(input, inputIndex);
              if (placeholder == null) {
                return null;
              }
              builder.add(placeholder);
              inputIndex = placeholder.index() + placeholder.length() + literal.length();
            }
            return unmodifiableList(builder);
          }
        });
  }

  /**
   * Returns the immutable list of placeholders in this template, in occurrence order.
   *
   * <p>Each placeholder is-a {@link CharSequence} with extra accessors to the index in this
   * template string. Callers can also use, for example, {@code .skip(1, 1)} to easily strip away
   * the '{' and '}' characters around the placeholder names.
   */
  public List<Substring.Match> placeholders() {
    return placeholders;
  }

  /** Returns the string format. */
  @Override public String toString() {
    return format;
  }

  private Stream<String> parsePlaceholderValues(String input) {
    return match(input)
        .orElseThrow(
            () -> new IllegalArgumentException("input doesn't match template (" + format + ")"))
        .stream()
        .map(Substring.Match::toString);
  }

  private <R> Stream<R> scanAndCollect(String input, Collector<? super String, ?, R> collector) {
    return scan(input).map(values -> values.stream().map(Substring.Match::toString).collect(collector));
  }

  private static List<String> extractLiteralsFromFormat(
      String format, List<Substring.Match> placeholders) {
    List<String> builder = new ArrayList<>(placeholders.size() + 1);
    int index = 0;
    for (Substring.Match placeholder : placeholders) {
      builder.add(substr(format, index, placeholder.index()));
      index = placeholder.index() + placeholder.length();
    }
    builder.add(substr(format, index, format.length()));
    return unmodifiableList(builder);
  }

  /** Same as {@link String#substring} but short-circuits when substring is empty. */
  private static String substr(String s, int begin, int end) {
    return begin == end ? "" : s.substring(begin, end);
  }
}