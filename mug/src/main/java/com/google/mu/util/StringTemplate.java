package com.google.mu.util;

import static com.google.mu.util.InternalCollectors.toImmutableList;
import static com.google.mu.util.Optionals.optional;
import static com.google.mu.util.stream.MoreCollectors.onlyElements;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.google.mu.function.Quarternary;
import com.google.mu.function.Quinary;
import com.google.mu.function.Senary;
import com.google.mu.function.Ternary;
import com.google.mu.util.stream.BiStream;

/**
 * A utility class to extract placeholder values from input strings based on a template.
 *
 * <p>If you have a simple template with placeholder names like "{recipient}", "{question}",
 * you can then reverse-engineer the placeholder values from a formatted string such as
 * "To Charlie: How are you?":
 *
 * <pre>{@code
 * StringTemplate template = new StringTemplate("To {recipient}: {question}?", is('?').not());
 * Map<String, String> placeholderValues = template.parse("To Charlie: How are you?").toMap();
 * assertThat(placeholderValues)
 *     .containsExactly("{recipient}", "Charlie", "{question}", "How are you");
 * }</pre>
 *
 * <p>More fluently, instead of parsing into a {@code Map} keyed by the placeholder variable names,
 * directly collect the placeholder values using lambda:
 * "To Charlie: How are you?":
 *
 * <pre>{@code
 * StringTemplate template = new StringTemplate("To {recipient}: {question}?", is('?').not());
 * return template.parse("To Charlie: How are you?", (recipient, question) -> ...));
 * }</pre>
 *
 * <p>Note that other than the placeholders, characters in the template and the input must match
 * exactly, case sensitively, including whitespaces, punctuations and everything.
 *
 * <p>This class doesn't perform formatting. For formatting the template with placeholder
 * variable values, it's trivial to do {@code
 * spanningInOrder("{", "}").repeatedly().replaceAllFrom(template, placeholder -> ...)}
 *
 * @since 6.6
 */
public final class StringTemplate {
  private final String pattern;
  private final List<Substring.Match> placeholders;
  private final List<String> placeholderVariableNames;
  private final CharPredicate placeholderValueCharMatcher;

  /**
   * Constructs a StringTemplate
   *
   * @param pattern the template pattern with placeholders in the format of {@code "{placeholder_name}"}
   * @param placeholderValueCharMatcher
   *     the characters that are allowed in each matched placeholder value
   */
  public StringTemplate(String pattern, CharPredicate placeholderValueCharMatcher) {
    this(pattern, Substring.spanningInOrder("{", "}"), placeholderValueCharMatcher);
  }

  /**
   * Constructs a StringTemplate
   *
   * @param pattern the template pattern with placeholders
   * @param placeholderVariablePattern
   *     the pattern of the placeholder variables such as {@code Substring.spanningInOrder("[", "]")}.
   * @param placeholderValueCharMatcher
   *     the characters that are allowed in each matched placeholder value
   */
  public StringTemplate(
      String pattern,
      Substring.Pattern placeholderVariablePattern,
      CharPredicate placeholderValueCharMatcher) {
    this.pattern = pattern;
    this.placeholders =
        placeholderVariablePattern.repeatedly().match(pattern).collect(toImmutableList());
    this.placeholderVariableNames =
        placeholders.stream().map(Substring.Match::toString).collect(toImmutableList());
    this.placeholderValueCharMatcher = requireNonNull(placeholderValueCharMatcher);
  }

  /**
   * Parses {@code input} and extracts all placeholder name-value pairs in a BiStream,
   * in the same order as {@link #placeholders}.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template
   */
  public BiStream<String, String> parse(String input) {
    return BiStream.zip(placeholderVariableNames.stream(),  parsePlaceholderValues(input));
  }

  /**
   * Parses {@code input} and apply {@code function} with the two placeholder name-value pairs
   * in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template or the template
   *     doesn't have two placeholders.
   */
  public <R> R parse(String input, BiFunction<? super String, ? super String, R> function) {
    return parsePlaceholderValues(input).collect(onlyElements(function));
  }

  /**
   * Parses {@code input} and apply {@code function} with the 3 placeholder name-value pairs
   * in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template or the template
   *     doesn't have 3 placeholders.
   */
  public <R> R parse(String input, Ternary<? super String,  R> function) {
    return parsePlaceholderValues(input).collect(onlyElements(function));
  }

  /**
   * Parses {@code input} and apply {@code function} with the 4 placeholder name-value pairs
   * in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template or the template
   *     doesn't have 4 placeholders.
   */
  public <R> R parse(String input, Quarternary<? super String,  R> function) {
    return parsePlaceholderValues(input).collect(onlyElements(function));
  }

  /**
   * Parses {@code input} and apply {@code function} with the 5 placeholder name-value pairs
   * in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template or the template
   *     doesn't have 5 placeholders.
   */
  public <R> R parse(String input, Quinary<? super String,  R> function) {
    return parsePlaceholderValues(input).collect(onlyElements(function));
  }

  /**
   * Parses {@code input} and apply {@code function} with the 6 placeholder name-value pairs
   * in this template.
   *
   * @throws IllegalArgumentException if {@code input} doesn't match the template or the template
   *     doesn't have 6 placeholders.
   */
  public <R> R parse(String input, Senary<? super String,  R> function) {
    return parsePlaceholderValues(input).collect(onlyElements(function));
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
    Substring.Pattern placeholderValuePattern =
        Substring.leading(placeholderValueCharMatcher).or(Substring.BEGINNING);
    List<Substring.Match> builder = new ArrayList<>();
    int templateIndex = 0;
    int inputIndex = 0;
    for (Substring.Match placeholder : placeholders) {
      int preludeLength = placeholder.index() - templateIndex;
      if (!input.regionMatches(inputIndex, pattern, templateIndex, preludeLength)) {
        return Optional.empty();
      }
      templateIndex += preludeLength;
      inputIndex += preludeLength;
      Substring.Match placeholderValue = placeholderValuePattern.match(input, inputIndex);
      builder.add(placeholderValue);
      templateIndex += placeholder.length();
      inputIndex += placeholderValue.length();
    }
    int remaining = pattern.length() - templateIndex;
    return optional(
        remaining == input.length() - inputIndex
            && input.regionMatches(inputIndex, pattern, templateIndex, remaining),
        Collections.unmodifiableList(builder));
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

  /** Returns the immutable list of placeholder variable names in this template, in occurrence order. */
  public List<String> placeholderVariableNames() {
    return placeholderVariableNames;
  }

  /** Returns the template pattern. */
  @Override public String toString() {
    return pattern;
  }

  private Stream<String> parsePlaceholderValues(String input) {
    return match(input)
        .orElseThrow(() -> new IllegalArgumentException("Input doesn't match template (" + pattern + ")"))
        .stream()
        .map(Substring.Match::toString);
  }
}
