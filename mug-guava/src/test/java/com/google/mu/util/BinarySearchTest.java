package com.google.mu.util;

import static com.google.common.collect.Range.all;
import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.atMost;
import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.closedOpen;
import static com.google.common.collect.Range.greaterThan;
import static com.google.common.collect.Range.lessThan;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.google.mu.util.BinarySearch.inSortedArray;
import static com.google.mu.util.BinarySearch.inSortedArrayWithTolerance;
import static com.google.mu.util.BinarySearch.inSortedList;
import static com.google.mu.util.BinarySearch.inSortedListWithTolerance;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThrows;

import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.testing.ClassSanityTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.Expect;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;

@RunWith(TestParameterInjector.class)
public class BinarySearchTest {
  @Rule public final Expect expect = Expect.create();

  @Test
  public void inRangeInclusive_invalidIndex() {
    assertThrows(IllegalArgumentException.class, () -> BinarySearch.inRange(closed(2, 0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inRange(closed(Integer.MAX_VALUE, Integer.MAX_VALUE - 2)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inRange(closed(Integer.MIN_VALUE + 2, Integer.MIN_VALUE)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inRange(closed(Integer.MAX_VALUE, Integer.MIN_VALUE)));
  }

  @Test
  public void inLongRangeInclusive_invalidIndex() {
    assertThrows(IllegalArgumentException.class, () -> BinarySearch.inLongRange(closed(2L, 0L)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inLongRange(closed(Long.MAX_VALUE, Long.MAX_VALUE - 2)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inLongRange(closed(Long.MIN_VALUE + 2, Long.MIN_VALUE)));
    assertThrows(
        IllegalArgumentException.class,
        () -> BinarySearch.inLongRange(closed(Long.MAX_VALUE, Long.MIN_VALUE)));
  }

  @Test
  public void inRange_empty() {
    assertThat(BinarySearch.inRange(closedOpen(0, 0)).find((l, i, h) -> 0)).isEmpty();
    assertThat(BinarySearch.inRange(closedOpen(0, 0)).findRangeOf((l, i, h) -> 0))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(BinarySearch.inRange(closedOpen(0, 0)).insertionPointFor((l, i, h) -> 0))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(closedOpen(0, 0)).insertionPointBefore((l, i, h) -> 0))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(closedOpen(0, 0)).insertionPointAfter((l, i, h) -> 0))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(greaterThan(Integer.MAX_VALUE)).findRangeOf((l, i, h) -> 0))
        .isEqualTo(Range.closedOpen(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(greaterThan(Integer.MAX_VALUE)).find((l, i, h) -> 0))
        .isEmpty();
    assertThat(BinarySearch.inRange(lessThan(Integer.MIN_VALUE)).find((l, i, h) -> 0))
        .isEmpty();
  }

  @Test
  public void inLongRange_empty() {
    assertThat(BinarySearch.inLongRange(closedOpen(0L, 0L)).find((l, i, h) -> 0)).isEmpty();
    assertThat(BinarySearch.inLongRange(closedOpen(0L, 0L)).findRangeOf((l, i, h) -> 0))
        .isEqualTo(Range.closedOpen(0L, 0L));
    assertThat(BinarySearch.inLongRange(closedOpen(0L, 0L)).insertionPointFor((l, i, h) -> 0))
        .isEqualTo(InsertionPoint.before(0L));
    assertThat(BinarySearch.inLongRange(closedOpen(0L, 0L)).insertionPointBefore((l, i, h) -> 0))
        .isEqualTo(InsertionPoint.before(0L));
    assertThat(BinarySearch.inLongRange(closedOpen(0L, 0L)).insertionPointAfter((l, i, h) -> 0))
    .isEqualTo(InsertionPoint.before(0L));
    assertThat(BinarySearch.inLongRange(greaterThan(Long.MAX_VALUE)).findRangeOf((l, i, h) -> 0))
        .isEqualTo(Range.closedOpen(Long.MAX_VALUE, Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(greaterThan(Long.MAX_VALUE)).find((l, i, h) -> 0))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(lessThan(Long.MIN_VALUE)).find((l, i, h) -> 0))
        .isEmpty();
  }


  @Test
  public void inRange_singleCandidateRange_found() {
    assertThat(BinarySearch.inRange(closed(1, 1)).find((l, i, h) -> Integer.compare(i, 1)))
        .hasValue(1);
    assertThat(BinarySearch.inRange(closed(1, 1)).findRangeOf((l, i, h) -> Integer.compare(i, 1)))
        .isEqualTo(Range.closed(1, 1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointFor((l, i, h) -> Integer.compare(i, 1)))
        .isEqualTo(InsertionPoint.at(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointBefore((l, i, h) -> Integer.compare(i, 1)))
        .isEqualTo(InsertionPoint.before(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointAfter((l, i, h) -> Integer.compare(i, 1)))
        .isEqualTo(InsertionPoint.after(1));
  }


  @Test
  public void inLongRange_singleCandidateRange_found() {
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).find((l, i, h) -> Long.compare(i, 1)))
        .hasValue(1L);
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).findRangeOf((l, i, h) -> Long.compare(i, 1)))
        .isEqualTo(Range.closed(1L, 1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointFor((l, i, h) -> Long.compare(i, 1)))
        .isEqualTo(InsertionPoint.at(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointBefore((l, i, h) -> Long.compare(i, 1)))
        .isEqualTo(InsertionPoint.before(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointAfter((l, i, h) -> Long.compare(i, 1)))
        .isEqualTo(InsertionPoint.after(1L));
  }

  @Test
  public void inRange_singleCandidateRange_shouldBeBefore() {
    assertThat(BinarySearch.inRange(closed(1, 1)).find((l, i, h) -> Integer.compare(0, i)))
        .isEmpty();
    assertThat(BinarySearch.inRange(closed(1, 1)).findRangeOf((l, i, h) -> Integer.compare(0, i)))
        .isEqualTo(Range.closedOpen(1, 1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointFor((l, i, h) -> Integer.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointBefore((l, i, h) -> Integer.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointAfter((l, i, h) -> Integer.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void inRange_singleCandidateRange_shouldBeAfter() {
    assertThat(BinarySearch.inRange(closed(1, 1)).find((l, i, h) -> Integer.compare(10, i)))
        .isEmpty();
    assertThat(BinarySearch.inRange(closed(1, 1)).findRangeOf((l, i, h) -> Integer.compare(10, i)))
        .isEqualTo(Range.closedOpen(2, 2));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointFor((l, i, h) -> Integer.compare(10, i)))
        .isEqualTo(InsertionPoint.after(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointBefore((l, i, h) -> Integer.compare(10, i)))
        .isEqualTo(InsertionPoint.after(1));
    assertThat(BinarySearch.inRange(closed(1, 1)).insertionPointAfter((l, i, h) -> Integer.compare(10, i)))
        .isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void inLongRange_singleCandidateRange_shouldBeBefore() {
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).find((l, i, h) -> Long.compare(0, i)))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).findRangeOf((l, i, h) -> Long.compare(0, i)))
        .isEqualTo(Range.closedOpen(1L, 1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointFor((l, i, h) -> Long.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointBefore((l, i, h) -> Long.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointAfter((l, i, h) -> Long.compare(0, i)))
        .isEqualTo(InsertionPoint.before(1L));
  }

  @Test
  public void inLongRange_singleCandidateRange_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).find((l, i, h) -> Long.compare(3, i)))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).findRangeOf((l, i, h) -> Long.compare(3, i)))
        .isEqualTo(Range.closedOpen(2L, 2L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointFor((l, i, h) -> Long.compare(3, i)))
        .isEqualTo(InsertionPoint.after(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointBefore((l, i, h) -> Long.compare(3, i)))
        .isEqualTo(InsertionPoint.after(1L));
    assertThat(BinarySearch.inLongRange(closed(1L, 1L)).insertionPointAfter((l, i, h) -> Long.compare(3, i)))
        .isEqualTo(InsertionPoint.after(1L));
  }

  @Test
  public void inRange_preventsUderflow_shouldBeBefore() {
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Integer.MIN_VALUE, Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
  }

  @Test
  public void inRange_preventsUnderflow_shouldBeAfter() {
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 1));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(atMost(Integer.MIN_VALUE)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MIN_VALUE));
  }

  @Test
  public void inLongRange_preventsUderflow_shouldBeBefore() {
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Long.MIN_VALUE, Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
  }

  @Test
  public void inLongRange_preventsUnderflow_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Long.MIN_VALUE + 1, Long.MIN_VALUE + 1));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(atMost(Long.MIN_VALUE)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MIN_VALUE));
  }

  @Test
  public void inRange_preventsOverflow_shouldBeBefore() {
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MAX_VALUE));
  }

  @Test
  public void inRange_preventsOverflow_shouldBeAfter() {
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(Integer.MAX_VALUE)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
  }

  @Test
  public void inLongRange_preventsOverflow_shouldBeBefore() {
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Long.MAX_VALUE, Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MAX_VALUE));
  }

  @Test
  public void inLongRange_preventsOverflow_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Long.MAX_VALUE, Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(Long.MAX_VALUE)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
  }

  @Test
  public void inRange_maxRange_shouldBeBefore() {
    assertThat(BinarySearch.inRange(all()).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inRange(all()).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Integer.MIN_VALUE, Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Integer.MIN_VALUE));
  }


  @Test
  public void inRange_maxRange_shouldBeAfter() {
    assertThat(BinarySearch.inRange(all()).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inRange(all()).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(all()).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
  }

  @Test
  public void inLongRange_maxRange_shouldBeBefore() {
    assertThat(BinarySearch.inLongRange(all()).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(all()).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Long.MIN_VALUE, Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
  }


  @Test
  public void inLongRange_maxRange_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(all()).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(all()).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Long.MAX_VALUE, Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(all()).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
  }

  @Test
  public void inRange_maxPositiveRange_shouldBeBefore() {
    assertThat(BinarySearch.inRange(atLeast(0)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atLeast(0)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(0));
  }


  @Test
  public void inRange_maxPositiveRange_shouldBeAfter() {
    assertThat(BinarySearch.inRange(atLeast(0)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inRange(atLeast(0)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Integer.MAX_VALUE));
  }


  @Test
  public void inLongRange_maxPositiveRange_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(atLeast(0L)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atLeast(0L)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(Long.MAX_VALUE, Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(Long.MAX_VALUE));
  }

  @Test
  public void inRange_maxNegativeRange_shouldBeAfter() {
    assertThat(BinarySearch.inRange(Range.lessThan(0)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inRange(Range.lessThan(0)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1));
  }

  @Test
  public void inLongRange_maxNegativeRange_shouldBeBefore() {
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).find((l, i, h) -> -1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).findRangeOf((l, i, h) -> -1))
        .isEqualTo(Range.closedOpen(Long.MIN_VALUE, Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointFor((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointBefore((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointAfter((l, i, h) -> -1))
        .isEqualTo(InsertionPoint.before(Long.MIN_VALUE));
  }

  @Test
  public void inLongRange_maxNegativeRange_shouldBeAfter() {
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).find((l, i, h) -> 1))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).findRangeOf((l, i, h) -> 1))
        .isEqualTo(Range.closedOpen(0L, 0L));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointFor((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1L));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointBefore((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1L));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointAfter((l, i, h) -> 1))
        .isEqualTo(InsertionPoint.after(-1L));
  }

  @Test
  public void inRange_maxRange_found(
      @TestParameter(valuesProvider = IntValues.class) int target) {
    assertThat(
            BinarySearch.inRange(Range.<Integer>all())
                .find((l, i, h) -> Integer.compare(target, i)))
        .hasValue(target);
    assertThat(
            BinarySearch.inRange(Range.<Integer>all())
                .findRangeOf((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(
            BinarySearch.inRange(Range.<Integer>all())
                .insertionPointFor((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(
            BinarySearch.inRange(Range.<Integer>all())
                .insertionPointBefore((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(
            BinarySearch.inRange(Range.<Integer>all())
                .insertionPointAfter((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inRange_maxNegativeRange_found(
      @TestParameter(valuesProvider = NegativeValues.class) int target) {
    assertThat(BinarySearch.inRange(Range.lessThan(0)).find((l, i, h) -> Integer.compare(target, i)))
        .hasValue(target);
    assertThat(BinarySearch.inRange(Range.lessThan(0)).findRangeOf((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointFor((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointBefore((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(BinarySearch.inRange(Range.lessThan(0)).insertionPointAfter((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inLongRange_maxRange_found(
      @TestParameter(valuesProvider = LongValues.class) long target) {
    assertThat(
            BinarySearch.inLongRange(Range.<Long>all())
                .find((l, i, h) -> Long.compare(target, i)))
        .hasValue(target);
    assertThat(
            BinarySearch.inLongRange(Range.<Long>all())
                .findRangeOf((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(
            BinarySearch.inLongRange(Range.<Long>all())
                .insertionPointFor((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(
            BinarySearch.inLongRange(Range.<Long>all())
                .insertionPointBefore((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(
            BinarySearch.inLongRange(Range.<Long>all())
                .insertionPointAfter((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inLongRange_maxNegativeRange_found(
      @TestParameter(valuesProvider = NegativeLongValues.class) long target) {
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).find((l, i, h) -> Long.compare(target, i)))
        .hasValue(target);
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).findRangeOf((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointFor((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointBefore((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(BinarySearch.inLongRange(Range.lessThan(0L)).insertionPointAfter((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inRange_maxNonNegativeRange_found(
      @TestParameter(valuesProvider = NonNegativeValues.class) int target) {
    assertThat(BinarySearch.inRange(atLeast(0)).find((l, i, h) -> Integer.compare(target, i)))
        .hasValue(target);
    assertThat(BinarySearch.inRange(atLeast(0)).findRangeOf((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointFor((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointBefore((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointAfter((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inLongRange_maxNonNegativeRange_found(
      @TestParameter(valuesProvider = NonNegativeLongValues.class) long target) {
    assertThat(BinarySearch.inLongRange(atLeast(0L)).find((l, i, h) -> Long.compare(target, i)))
        .hasValue(target);
    assertThat(BinarySearch.inLongRange(atLeast(0L)).findRangeOf((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(Range.closed(target, target));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointFor((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.at(target));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointBefore((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(target));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointAfter((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.after(target));
  }

  @Test
  public void inRange_maxNonNegativeRange_negativeNotFound(
      @TestParameter(valuesProvider = NegativeValues.class) int target) {
    assertThat(BinarySearch.inRange(atLeast(0)).find((l, i, h) -> Integer.compare(target, i)))
        .isEmpty();
    assertThat(BinarySearch.inRange(atLeast(0)).findRangeOf((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointFor((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointBefore((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0));
    assertThat(BinarySearch.inRange(atLeast(0)).insertionPointAfter((l, i, h) -> Integer.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void inLongRangee_maxNonNegativeRange_negativeNotFound(
      @TestParameter(valuesProvider = NegativeValues.class) int target) {
    assertThat(BinarySearch.inLongRange(atLeast(0L)).find((l, i, h) -> Long.compare(target, i)))
        .isEmpty();
    assertThat(BinarySearch.inLongRange(atLeast(0L)).findRangeOf((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(Range.closedOpen(0L, 0L));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointFor((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0L));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointBefore((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0L));
    assertThat(BinarySearch.inLongRange(atLeast(0L)).insertionPointAfter((l, i, h) -> Long.compare(target, i)))
        .isEqualTo(InsertionPoint.before(0L));
  }

  @Test
  public void binarySearch_inSortedIntArray_found() {
    int[] sorted = new int[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(20)).hasValue(1);
    assertThat(inSortedArray(sorted).findRangeOf(20)).isEqualTo(Range.closed(1, 1));
    assertThat(inSortedArray(sorted).insertionPointFor(20)).isEqualTo(InsertionPoint.at(1));
    assertThat(inSortedArray(sorted).insertionPointBefore(20)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(20)).isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void binarySearch_inSortedIntArray_notFoundInTheMiddle() {
    int[] sorted = new int[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(19)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(19)).isEqualTo(Range.closedOpen(1, 1));
    assertThat(inSortedArray(sorted).insertionPointFor(19)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointBefore(19)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(19)).isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void binarySearch_inSortedIntArray_notFoundAtTheBeginning() {
    int[] sorted = new int[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(-1)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(-1)).isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedArray(sorted).insertionPointFor(Integer.MIN_VALUE)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArray(sorted).insertionPointBefore(-1)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArray(sorted).insertionPointAfter(0)).isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void binarySearch_inSortedIntArray_notFoundAtTheEnd() {
    int[] sorted = new int[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(41)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(Integer.MAX_VALUE)).isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedArray(sorted).insertionPointFor(50)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArray(sorted).insertionPointBefore(Integer.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArray(sorted).insertionPointAfter(Integer.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedLongArray_found() {
    long[] sorted = new long[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(20L)).hasValue(1);
    assertThat(inSortedArray(sorted).findRangeOf(20L)).isEqualTo(Range.closed(1, 1));
    assertThat(inSortedArray(sorted).insertionPointFor(20L)).isEqualTo(InsertionPoint.at(1));
    assertThat(inSortedArray(sorted).insertionPointBefore(20L)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(20L)).isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void binarySearch_inSortedLongArray_notFoundInTheMiddle() {
    long[] sorted = new long[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(19L)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(19L)).isEqualTo(Range.closedOpen(1, 1));
    assertThat(inSortedArray(sorted).insertionPointFor(19L)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointBefore(19L)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(19L)).isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void binarySearch_inSortedLongArray_notFoundAtTheBeginning() {
    long[] sorted = new long[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(-1L)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(-1L)).isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedArray(sorted).insertionPointFor(Long.MIN_VALUE)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArray(sorted).insertionPointBefore(-1L)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArray(sorted).insertionPointAfter(0L)).isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void binarySearch_inSortedLongArray_notFoundAtTheEnd() {
    long[] sorted = new long[] {10, 20, 30, 40};
    assertThat(inSortedArray(sorted).find(41L)).isEmpty();
    assertThat(inSortedArray(sorted).findRangeOf(Long.MAX_VALUE)).isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedArray(sorted).insertionPointFor(50L)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArray(sorted).insertionPointBefore(Long.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArray(sorted).insertionPointAfter(Long.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedIntArray_withDuplicates() {
    int[] sorted = new int[] {10, 20, 20, 30, 40, 40, 40};
    assertThat(inSortedArray(sorted).find(10)).hasValue(0);
    assertThat(inSortedArray(sorted).find(20).get()).isIn(ImmutableSet.of(1, 2));
    assertThat(inSortedArray(sorted).findRangeOf(20)).isEqualTo(Range.closed(1, 2));
    assertThat(inSortedArray(sorted).insertionPointBefore(20)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(20)).isEqualTo(InsertionPoint.after(2));
    assertThat(inSortedArray(sorted).findRangeOf(40)).isEqualTo(Range.closed(4, 6));
    assertThat(inSortedArray(sorted).insertionPointBefore(40)).isEqualTo(InsertionPoint.before(4));
    assertThat(inSortedArray(sorted).insertionPointAfter(40)).isEqualTo(InsertionPoint.after(6));
  }

  @Test
  public void binarySearch_inSortedLongArray_withDuplicates() {
    long[] sorted = new long[] {10, 20, 20, 30, 40, 40, 40};
    assertThat(inSortedArray(sorted).find(10L)).hasValue(0);
    assertThat(inSortedArray(sorted).find(20L).get()).isIn(ImmutableSet.of(1, 2));
    assertThat(inSortedArray(sorted).findRangeOf(20L)).isEqualTo(Range.closed(1, 2));
    assertThat(inSortedArray(sorted).insertionPointBefore(20L)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArray(sorted).insertionPointAfter(20L)).isEqualTo(InsertionPoint.after(2));
    assertThat(inSortedArray(sorted).findRangeOf(40L)).isEqualTo(Range.closed(4, 6));
    assertThat(inSortedArray(sorted).insertionPointBefore(40L)).isEqualTo(InsertionPoint.before(4));
    assertThat(inSortedArray(sorted).insertionPointAfter(40L)).isEqualTo(InsertionPoint.after(6));
  }

  @Test
  public void binarySearch_inSortedList_found() {
    ImmutableList<Integer> sorted = ImmutableList.of(10, 20, 30, 40);
    assertThat(inSortedList(sorted).find(20)).hasValue(1);
    assertThat(inSortedList(sorted).findRangeOf(20)).isEqualTo(Range.closed(1, 1));
    assertThat(inSortedList(sorted).insertionPointFor(20)).isEqualTo(InsertionPoint.at(1));
    assertThat(inSortedList(sorted).insertionPointBefore(20)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedList(sorted).insertionPointAfter(20)).isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void binarySearch_inSortedList_notFoundInTheMiddle() {
    ImmutableList<Integer> sorted = ImmutableList.of(10, 20, 30, 40);
    assertThat(inSortedList(sorted).find(19)).isEmpty();
    assertThat(inSortedList(sorted).findRangeOf(19)).isEqualTo(Range.closedOpen(1, 1));
    assertThat(inSortedList(sorted).insertionPointFor(19)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedList(sorted).insertionPointBefore(19)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedList(sorted).insertionPointAfter(19)).isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void binarySearch_inSortedList_notFoundAtTheBeginning() {
    ImmutableList<Integer> sorted = ImmutableList.of(10, 20, 30, 40);
    assertThat(inSortedList(sorted).find(-1)).isEmpty();
    assertThat(inSortedList(sorted).findRangeOf(-1)).isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedList(sorted).insertionPointFor(Integer.MIN_VALUE)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedList(sorted).insertionPointBefore(-1)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedList(sorted).insertionPointAfter(0)).isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void binarySearch_inSortedList_notFoundAtTheEnd() {
    ImmutableList<Integer> sorted = ImmutableList.of(10, 20, 30, 40);
    assertThat(inSortedList(sorted).find(41)).isEmpty();
    assertThat(inSortedList(sorted).findRangeOf(Integer.MAX_VALUE)).isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedList(sorted).insertionPointFor(50)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedList(sorted).insertionPointBefore(Integer.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedList(sorted).insertionPointAfter(Integer.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedList_withDuplicates() {
    ImmutableList<Integer> sorted = ImmutableList.of(10, 20, 20, 30, 40, 40, 40);
    assertThat(inSortedList(sorted).find(10)).hasValue(0);
    assertThat(inSortedList(sorted).find(20).get()).isIn(ImmutableSet.of(1, 2));
    assertThat(inSortedList(sorted).findRangeOf(20)).isEqualTo(Range.closed(1, 2));
    assertThat(inSortedList(sorted).insertionPointBefore(20)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedList(sorted).insertionPointAfter(20)).isEqualTo(InsertionPoint.after(2));
    assertThat(inSortedList(sorted).findRangeOf(40)).isEqualTo(Range.closed(4, 6));
    assertThat(inSortedList(sorted).insertionPointBefore(40)).isEqualTo(InsertionPoint.before(4));
    assertThat(inSortedList(sorted).insertionPointAfter(40)).isEqualTo(InsertionPoint.after(6));
  }

  @Test
  public void binarySearch_inSortedList_byKeyFunction() {
    ImmutableList<String> sorted = ImmutableList.of("x", "ab", "foo", "zerg");
    assertThat(inSortedList(sorted, String::length).find(2)).hasValue(1);
  }

  @Test
  public void binarySearch_inSortedList_byComparator() {
    List<String> sorted = asList(null, "a", "b", "c");
    assertThat(inSortedList(sorted, Comparator.nullsFirst(Comparator.naturalOrder())).find(null)).hasValue(0);
    assertThat(inSortedList(sorted, Comparator.nullsFirst(Comparator.naturalOrder())).find("b")).hasValue(2);
  }

  @Test
  public void binarySearch_inSortedDoubleArrayWithTolerance_found() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, 0.9).find(20D)).hasValue(1);
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(21D)).hasValue(1);
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(19D)).hasValue(1);
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(20D)).isEqualTo(Range.closed(1, 1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointFor(20D)).isEqualTo(InsertionPoint.at(1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(20D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(20D)).isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_notFoundInTheMiddle() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(18D)).isEmpty();
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(18D)).isEqualTo(Range.closedOpen(1, 1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointFor(18D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(18D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(18D)).isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_notFoundAtTheBeginning() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(-1D)).isEmpty();
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(-1D)).isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointFor(Double.MIN_VALUE)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointFor(Double.NEGATIVE_INFINITY)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(-1D)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(0D)).isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_notFoundAtTheEnd() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(42D)).isEmpty();
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(Double.MAX_VALUE)).isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointFor(50D)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(Double.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(Double.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArrayWithTolerance(sorted, 100).insertionPointFor(Double.NaN)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.MAX_VALUE).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.POSITIVE_INFINITY).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_withDuplicates() {
    double[] sorted = new double[] {10, 20.1, 20.2, 30, 40.1, 40.2, 40.3};
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(10D)).hasValue(0);
    assertThat(inSortedArrayWithTolerance(sorted, 1).find(20D).get()).isIn(ImmutableSet.of(1, 2));
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(20D)).isEqualTo(Range.closed(1, 2));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(20D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(20D)).isEqualTo(InsertionPoint.after(2));
    assertThat(inSortedArrayWithTolerance(sorted, 1).findRangeOf(40D)).isEqualTo(Range.closed(4, 6));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointBefore(40D)).isEqualTo(InsertionPoint.before(4));
    assertThat(inSortedArrayWithTolerance(sorted, 1).insertionPointAfter(40D)).isEqualTo(InsertionPoint.after(6));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_infinityTolerance() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(0D))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(Double.NEGATIVE_INFINITY))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(Double.POSITIVE_INFINITY))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.POSITIVE_INFINITY).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_maxTolerance() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThat(inSortedArrayWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(0D))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedArrayWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(Double.NEGATIVE_INFINITY))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedArrayWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(Double.POSITIVE_INFINITY))
        .isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedArrayWithTolerance(sorted, Double.MAX_VALUE).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedArrayWithTolerance_invalidTolerance() {
    double[] sorted = new double[] {10, 20, 30, 40};
    assertThrows(IllegalArgumentException.class, () -> inSortedArrayWithTolerance(sorted, -1));
    assertThrows(IllegalArgumentException.class, () -> inSortedArrayWithTolerance(sorted, -Double.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> inSortedArrayWithTolerance(sorted, Double.NEGATIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> inSortedArrayWithTolerance(sorted, Double.NaN));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_found() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, 0.9).find(20D)).hasValue(1);
    assertThat(inSortedListWithTolerance(sorted, 1).find(21D)).hasValue(1);
    assertThat(inSortedListWithTolerance(sorted, 1).find(19D)).hasValue(1);
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(20D)).isEqualTo(Range.closed(1, 1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointFor(20D)).isEqualTo(InsertionPoint.at(1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(20D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(20D)).isEqualTo(InsertionPoint.after(1));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_notFoundInTheMiddle() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, 1).find(18D)).isEmpty();
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(18D)).isEqualTo(Range.closedOpen(1, 1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointFor(18D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(18D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(18D)).isEqualTo(InsertionPoint.before(1));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_notFoundAtTheBeginning() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, 1).find(-1D)).isEmpty();
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(-1D)).isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointFor(Double.MIN_VALUE)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointFor(Double.NEGATIVE_INFINITY)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(-1D)).isEqualTo(InsertionPoint.before(0));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(0D)).isEqualTo(InsertionPoint.before(0));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_notFoundAtTheEnd() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, 1).find(42D)).isEmpty();
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(Double.MAX_VALUE)).isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointFor(50D)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(Double.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(Double.MAX_VALUE)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedListWithTolerance(sorted, 100).insertionPointFor(Double.NaN)).isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedListWithTolerance(sorted, Double.MAX_VALUE).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
    assertThat(inSortedListWithTolerance(sorted, Double.POSITIVE_INFINITY).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_withDuplicates() {
    ImmutableList<Double> sorted = ImmutableList.of(10.1, 20.1, 20.2, 30.1, 40.1, 40.2, 40.3);
    assertThat(inSortedListWithTolerance(sorted, 1).find(10D)).hasValue(0);
    assertThat(inSortedListWithTolerance(sorted, 1).find(20D).get()).isIn(ImmutableSet.of(1, 2));
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(20D)).isEqualTo(Range.closed(1, 2));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(20D)).isEqualTo(InsertionPoint.before(1));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(20D)).isEqualTo(InsertionPoint.after(2));
    assertThat(inSortedListWithTolerance(sorted, 1).findRangeOf(40D)).isEqualTo(Range.closed(4, 6));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointBefore(40D)).isEqualTo(InsertionPoint.before(4));
    assertThat(inSortedListWithTolerance(sorted, 1).insertionPointAfter(40D)).isEqualTo(InsertionPoint.after(6));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_infinityTolerance() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(0D))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedListWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(Double.NEGATIVE_INFINITY))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedListWithTolerance(sorted, Double.POSITIVE_INFINITY).findRangeOf(Double.POSITIVE_INFINITY))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedListWithTolerance(sorted, Double.POSITIVE_INFINITY).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_maxTolerance() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThat(inSortedListWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(0D))
        .isEqualTo(Range.closed(0, 3));
    assertThat(inSortedListWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(Double.NEGATIVE_INFINITY))
        .isEqualTo(Range.closedOpen(0, 0));
    assertThat(inSortedListWithTolerance(sorted, Double.MAX_VALUE).findRangeOf(Double.POSITIVE_INFINITY))
        .isEqualTo(Range.closedOpen(4, 4));
    assertThat(inSortedListWithTolerance(sorted, Double.MAX_VALUE).insertionPointFor(Double.NaN))
        .isEqualTo(InsertionPoint.after(3));
  }

  @Test
  public void binarySearch_inSortedListWithTolerance_invalidTolerance() {
    ImmutableList<Double> sorted = ImmutableList.of(10D, 20D, 30D, 40D);
    assertThrows(IllegalArgumentException.class, () -> inSortedListWithTolerance(sorted, -1));
    assertThrows(IllegalArgumentException.class, () -> inSortedListWithTolerance(sorted, -Double.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> inSortedListWithTolerance(sorted, Double.NEGATIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> inSortedListWithTolerance(sorted, Double.NaN));
  }

  @Test
  public void testNulls() throws Exception {
    new NullPointerTester().testAllPublicStaticMethods(BinarySearch.class);
    new ClassSanityTester().forAllPublicStaticMethods(BinarySearch.class).testNulls();
  }

  @Test
  public void binarySearchSqrt_smallNumbers() {
    assertThat(sqrt().insertionPointFor(4L).floor()).isEqualTo(2);
    assertThat(sqrt().insertionPointFor(1L).floor()).isEqualTo(1);
    assertThat(sqrt().insertionPointFor(0L).floor()).isEqualTo(0);
    assertThat(sqrt().insertionPointFor(5L).floor()).isEqualTo(2);
    assertThat(sqrt().insertionPointFor(101L).floor()).isEqualTo(10);
    assertThat(sqrt().insertionPointFor(4097L).floor()).isEqualTo(64);
  }

  @Test
  public void binarySearchSqrt_largeNumbers() {
    int[] numbers = {
      Integer.MAX_VALUE,
      Integer.MAX_VALUE - 1,
      Integer.MAX_VALUE - 2,
      Integer.MAX_VALUE / 2,
      Integer.MAX_VALUE / 10
    };
    for (int n : numbers) {
      long square = ((long) n) * n;
      assertThat(sqrt().insertionPointFor(square).floor()).isEqualTo(n);
      assertThat(sqrt().insertionPointFor(square + 1).floor()).isEqualTo(n);
      assertThat(sqrt().insertionPointFor(square - 1).floor()).isEqualTo(n - 1);
      assertThat(sqrt().find(square)).hasValue(n);
      assertThat(sqrt().find(square + 1)).isEmpty();
      assertThat(sqrt().find(square - 1)).isEmpty();
    }
  }

  @Test
  public void binarySearchRotated_empty() {
    int[] sorted = {};
    assertThat(inCircularSortedArray(sorted).find(1)).isEmpty();
  }

  @Test
  public void binarySearchRotated_singleElement() {
    int[] sorted = {1};
    assertThat(inCircularSortedArray(sorted).find(1)).hasValue(0);
    assertThat(inCircularSortedArray(sorted).find(2)).isEmpty();
  }

  @Test
  public void binarySearchRotated_twoElements() {
    int[] sorted = {1, 2};
    assertThat(inCircularSortedArray(sorted).find(1)).hasValue(0);
    assertThat(inCircularSortedArray(sorted).find(2)).hasValue(1);
    assertThat(inCircularSortedArray(sorted).find(3)).isEmpty();
  }

  @Test
  public void binarySearchRotated_twoElementsReversed() {
    int[] sorted = {20, 10};
    assertThat(inCircularSortedArray(sorted).find(10)).hasValue(1);
    assertThat(inCircularSortedArray(sorted).find(20)).hasValue(0);
    assertThat(inCircularSortedArray(sorted).find(30)).isEmpty();
  }

  @Test
  public void binarySearchRotated_notRatated() {
    int[] sorted = {10, 20, 30, 40, 50, 60, 70};
    for (int i = 0; i < sorted.length; i++) {
      assertThat(inCircularSortedArray(sorted).find(sorted[i])).hasValue(i);
    }
    assertThat(inCircularSortedArray(sorted).find(0)).isEmpty();
    assertThat(inCircularSortedArray(sorted).find(80)).isEmpty();
    assertThat(inCircularSortedArray(sorted).find(15)).isEmpty();
  }

  @Test
  public void binarySearchRotated_ratated() {
    int[] rotated = {40, 50, 60, 70, 10, 20, 30};
    for (int i = 0; i < rotated.length; i++) {
      assertThat(inCircularSortedArray(rotated).find(rotated[i])).hasValue(i);
    }
    assertThat(inCircularSortedArray(rotated).find(0)).isEmpty();
    assertThat(inCircularSortedArray(rotated).find(80)).isEmpty();
    assertThat(inCircularSortedArray(rotated).find(15)).isEmpty();
  }

  // Demo how binarySearch() can be used to implement more advanced binary search algorithms
  // such as searching within a rotated array.
  private static BinarySearch<Integer, Integer> inCircularSortedArray(int[] rotated) {
    return BinarySearch.inRange(Range.closedOpen(0, rotated.length))
        .by(key -> (low, mid, high) -> {
          int probe = rotated[mid];
          if (key < probe) {
            // target < mid value.
            // [low] <= probe means we are in the left half of [4, 5, 6, 1, 2, 3].
            // If we are in the first ascending half, it's in the right side if key <
            // rotated[lower].
            // If we are in the second ascending half, the right half is useless. Look left.
            return rotated[low] <= probe && key < rotated[low] ? 1 : -1;
          } else if (key > probe) {
            // key > mid value.
            // probe <= [high] means we are in the right half of [4, 5, 6, 1, 2, 3].
            // If we are in the second ascending half, it's in the left side if key >
            // rotated[high].
            // If we are in the first ascending half, the left side is useless. Look right.
            return probe <= rotated[high] && key > rotated[high] ? -1 : 1;
          } else {
            return 0;
          }
        });
  }

  private static BinarySearch<Long, Integer> sqrt() {
    return BinarySearch.inRange(atLeast(0))
        .by(square -> (low, mid, high) -> Long.compare(square, (long) mid * mid));
  }

  static class NegativeValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.of(
          Integer.MIN_VALUE,
          Integer.MIN_VALUE + 1,
          Integer.MIN_VALUE / 2,
          Integer.MIN_VALUE / 3,
          -3,
          -2,
          -1);
    }
  }

  static class NegativeLongValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.of(
          Long.MIN_VALUE,
          Long.MIN_VALUE + 1,
          Long.MIN_VALUE / 2,
          Long.MIN_VALUE / 3,
          -3L,
          -2L,
          -1L);
    }
  }

  static class NonNegativeValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.of(
          0,
          1,
          2,
          3,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE - 1,
          Integer.MAX_VALUE - 2,
          Integer.MAX_VALUE - 3,
          Integer.MAX_VALUE / 2,
          Integer.MAX_VALUE / 3);
    }
  }

  static class NonNegativeLongValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.of(
          0L,
          1L,
          2L,
          3L,
          Long.MAX_VALUE,
          Long.MAX_VALUE - 1,
          Long.MAX_VALUE - 2,
          Long.MAX_VALUE - 3,
          Long.MAX_VALUE / 2,
          Long.MAX_VALUE / 3);
    }
  }

  static class IntValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.builder()
          .addAll(new NegativeValues().provideValues())
          .addAll(new NonNegativeValues().provideValues())
          .build();
    }
  }

  static class LongValues implements TestParameter.TestParameterValuesProvider {
    @Override
    public List<?> provideValues() {
      return ImmutableList.builder()
          .addAll(new NegativeLongValues().provideValues())
          .addAll(new NonNegativeLongValues().provideValues())
          .build();
    }
  }
}