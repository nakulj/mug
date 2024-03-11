package com.google.mu.examples;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.mu.util.Substring.first;
import static com.google.mu.util.Substring.last;
import static java.util.stream.Collectors.toList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.mu.safesql.SafeQuery;
import com.google.mu.util.StringFormat;
import com.google.mu.util.Substring;

@RunWith(JUnit4.class)
public class HowToUseStringFormatTest {

  @Test public void parseExample() {
    StringFormat timeFormat = new StringFormat("{hour}:{minute}:{second}.{millis}");
    String input = "12:10:01.234";
    assertThat(timeFormat.parse(input, (hour, minute, second, millis) -> minute))
        .hasValue("10");
  }

  @Test public void parseGreedyExample() {
    StringFormat filenameFormat = new StringFormat("{path}/{name}.{ext}");
    String input = "/usr/tom/my.file.txt";
    assertThat(filenameFormat.parseGreedy(input, (path, name, ext) -> name))
        .hasValue("my.file");
  }

  @Test public void scanExample() {
    StringFormat boldFormat = new StringFormat("<b>{bolded}</b>");
    String input = "Please come back to the <b>front desk</b> at <b>12:00</b>.";
    assertThat(boldFormat.scan(input, bolded -> bolded))
        .containsExactly("front desk", "12:00");
  }

  @Test public void safeQueryExample() {
    String id = "foo";
    StringFormat.To<SafeQuery> whereClause = SafeQuery.template("WHERE id = '{id}'");
    assertThat(whereClause.with(id))
        .isEqualTo(SafeQuery.of("WHERE id = 'foo'"));
  }

  @Test public void parse2dArray() {
    String x = "{ {F, 40 , 40 , 2000},{L, 60 , 60 , 1000},{F, 40 , 40 , 2000}}";
    Substring.Pattern braced = Substring.between(first('{'), last('}'));
    System.out.println(
        braced.repeatedly()
            .from(braced.from(x).get())
            .map(first(',').repeatedly()::splitThenTrim)
            .map(elements -> elements.collect(toList()))
            .collect(toList()));
  }

  @SuppressWarnings("StringUnformatArgsCheck")
  String failsBecauseTwoLambdaParametersAreExpected() {
	  return new StringFormat("{key}:{value}").parseOrThrow("k:v", key -> key);
  }

  @SuppressWarnings("StringUnformatArgsCheck")
  String failsBecauseLambdaParameterNamesAreOutOfOrder() {
    return new StringFormat("{key}:{value}").parseOrThrow("k:v", (value, key) -> key);
  }

  @SuppressWarnings("StringFormatPlaceholderNamesCheck")
  String failsDueToBadPlaceholderName() {
    return new StringFormat("{?}:{-}").parseOrThrow("k:v", (k, v) -> k);
  }

  @SuppressWarnings("StringFormatArgsCheck")
  SafeQuery mismatchingPlaceholderInSafeQueryTemplate(String name) {
    return SafeQuery.template("WHERE id = '{id}'").with(name);
  }
}
