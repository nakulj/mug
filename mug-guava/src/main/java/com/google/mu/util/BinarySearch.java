package com.google.mu.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.math.DoubleMath;

/**
 * Generic algorithm to fluently binary search for a target value.
 *
 * <p>For example: <pre>{@code
 * BinarySearch.inSortedArray([10, 20, 30, 40]).find(20) => Optional.of(1)
 * BinarySearch.inSortedArray([10, 20, 30, 40]).insertionPointFor(22) => InsertionPoint.before(2)
 * BinarySearch.inSortedArrayWithTolerance([1.1, 2.1, 2.2, 2.3, 3.3, 4.4], 0.5).rangeOf(2D)
 *     => Range.closed(1, 3)
 * }</pre>
 *
 * <p>For {@code inRangeInclusive()} and the primitive array search methods, no boxing is performed in the
 * O(logn) search operation. The input search key and the output result are however boxed and wrapped
 * in one of {@code Optional}, {@code Range} or {@code InsertionPoint} objects, which is O(1).
 *
 * @param <K> the search key
 * @param <C> the comparable search result (typically a numeric index)
 * @since 6.4
 */
public abstract class BinarySearch<K, C extends Comparable<C>> {
  /** Returns a {@link BinarySearch} for indexes with the given sorted {@code list}. */
  public static <E extends Comparable<E>> BinarySearch<E, Integer> inSortedList(List<? extends E> list) {
    return inRangeInclusive(0, list.size() - 1)
        .keyedBy(key -> {
          checkNotNull(key);
          return (l, i, h) -> key.compareTo(list.get(i));
        });
  }

  /** Returns a {@link BinarySearch} for indexes with the given sorted {@code list} according to {@code comparator. */
  public static <E> BinarySearch<E, Integer> inSortedList(
      List<? extends E> list, Comparator<? super E> comparator) {
    checkNotNull(comparator);
    return inRangeInclusive(0, list.size() - 1)
        .keyedBy(key -> (l, i, h) -> comparator.compare(key, list.get(i)));
  }

  /** Returns a {@link BinarySearch} for indexes with the given {@code list} sorted by the {@code sortBy} function. */
  public static <K extends Comparable<K>, E> BinarySearch<K, Integer> inSortedList(
      List<? extends E> list, Function<? super E, ? extends K> sortedBy) {
    return inSortedList(Lists.transform(list, sortedBy::apply));
  }

  /** Returns a {@link BinarySearch} for indexes with the given sorted int {@code array}. */
  public static BinarySearch<Integer, Integer> inSortedArray(int[] array) {
    return inRangeInclusive(0, array.length - 1)
        .keyedBy(key -> {
          int intValue = key.intValue();
          return (l, i, h) -> Integer.compare(intValue, array[i]);
        });
  }

  /** Returns a {@link BinarySearch} for indexes with the given sorted long {@code array}. */
  public static BinarySearch<Long, Integer> inSortedArray(long[] array) {
    return inRangeInclusive(0, array.length - 1)
        .keyedBy(key -> {
          long longValue = key.longValue();
          return (l, i, h) -> Long.compare(longValue, array[i]);
        });
  }

  /** Returns a {@link BinarySearch} for indexes with the given sorted double {@code array}. */
  public static BinarySearch<Double, Integer> inSortedListWithTolerance(List<Double> list, double tolerance) {
    checkNotNegative(tolerance);
    return inRangeInclusive(0, list.size() - 1)
        .keyedBy(key -> {
          double target = key.doubleValue();
          return (l, i, h) -> DoubleMath.fuzzyCompare(target, list.get(i), tolerance);
        });
  }

  /** Returns a {@link BinarySearch} for indexes with the given sorted double {@code array}. */
  public static BinarySearch<Double, Integer> inSortedArrayWithTolerance(double[] array, double tolerance) {
    checkNotNegative(tolerance);
    return inRangeInclusive(0, array.length - 1)
        .keyedBy(key -> {
          double target = key.doubleValue();
          return (l, i, h) -> DoubleMath.fuzzyCompare(target, array[i], tolerance);
        });
  }

  /**
   * Returns a {@link BinarySearch} over the inclusive range of {@code [from, to]}.
   * The range is empty if {@code from == to + 1}.
   *
   * <p>This is the most generic binary search algorithm, supporting flexible target
   * matching criterion.
   */
  public static BinarySearch<IndexedSearchTarget, Integer> inRangeInclusive(int from, int to) {
    if (from > to) {
      checkArgument(from - to == 1, "Invalid from (%s) vs. to (%s)", from, to);
      return always(InsertionPoint.before(from));
    }
    return new BinarySearch<IndexedSearchTarget, Integer>() {
      @Override public InsertionPoint<Integer> insertionPointFor(IndexedSearchTarget target) {
        checkNotNull(target);
        for (int low = from, high = to; ;) {
          int mid = safeMid(low, high);
          int where = target.locate(low, mid, high);
          if (where > 0) {
            if (mid == high) { // mid is the floor
              return InsertionPoint.after(mid);
            }
            low = mid + 1;
          } else if (where < 0) {
            if (mid == low) { // mid is the ceiling
              return InsertionPoint.before(mid);
            }
            high = mid - 1;
          } else {
            return InsertionPoint.at(mid);
          }
        }
      }
      @Override public InsertionPoint<Integer> insertionPointBefore(IndexedSearchTarget target) {
        return insertionPointFor(before(target));
      }
      @Override public InsertionPoint<Integer> insertionPointAfter(IndexedSearchTarget target) {
        return insertionPointFor(after(target));
      }
    };
  }

  /**
   * Returns a {@link BinarySearch} over the inclusive range of {@code [from, to]}.
   * The range is empty if {@code from == to + 1}.
   *
   * <p>This is the most generic binary search algorithm, supporting flexible target
   * matching criterion.
   */
  public static BinarySearch<LongIndexedSearchTarget, Long> inRangeInclusive(long from, long to) {
    if (from > to) {
      checkArgument(from - to == 1, "Invalid from (%s) vs. to (%s)", from, to);
      return always(InsertionPoint.before(from));
    }
    return new BinarySearch<LongIndexedSearchTarget, Long>() {
      @Override public InsertionPoint<Long> insertionPointFor(LongIndexedSearchTarget target) {
        checkNotNull(target);
        for (long low = from, high = to; ;) {
          long mid = safeMid(low, high);
          int where = target.locate(low, mid, high);
          if (where > 0) {
            if (mid == high) { // mid is the floor
              return InsertionPoint.after(mid);
            }
            low = mid + 1;
          } else if (where < 0) {
            if (mid == low) { // mid is the ceiling
              return InsertionPoint.before(mid);
            }
            high = mid - 1;
          } else {
            return InsertionPoint.at(mid);
          }
        }
      }
      @Override public InsertionPoint<Long> insertionPointBefore(LongIndexedSearchTarget target) {
        return insertionPointFor(before(target));
      }
      @Override public InsertionPoint<Long> insertionPointAfter(LongIndexedSearchTarget target) {
        return insertionPointFor(after(target));
      }
    };
  }

  /**
   * Searches for the index of {@code target}.
   *
   * <p>
   * If target is found, returns the matching integer; otherwise returns empty.
   *
   * <p>
   * Prefer using {@link java.util.Arrays#binarySearch} and
   * {@link java.util.Collections#binarySearch} when possible.
   *
   * <p>
   * While the common use cases of binary search is to search in sorted arrays and
   * lists, there are diverse contexts where the algorithm is also applicable
   * (think of the Guess the Number game). As a more realistic example, you can
   * binary search a rotated, otherwise strictly-ordered array, using the
   * following code:
   *
   * <pre>
   * {@code
   * Optional<Integer> binarySearchRotated(int[] rotated, int target) {
   *   return BinarySearch.inRangeInclusive(0, rotated.length - 1)
   *       find((low, mid, high) -> {
   *         int probe = rotated[mid];
   *         if (target < probe) {
   *           return rotated[low] <= probe && target < rotated[low] ? 1 : -1;
   *         } else if (target > probe) {
   *            return probe <= rotated[high] && target > rotated[high] ? -1 : 1;
   *         } else {
   *           return 0;
   *         }
   *       });
   * }
   * }
   * </pre>
   */
  public final Optional<C> find(@Nullable K key) {
    return insertionPointFor(key).exact();
  }

  /**
   * Finds the sub-range of elements that match {@code target}.
   *
   * <p>
   * If there is a single match at index `i`, {@code [i, i]} is returned. For more
   * than one matches, the returned range is closed at both ends. If no match is
   * found, an empty range is returned with the open {@link Range#upperEndpoint}
   * being the insertion point, except if the insertion point should have been
   * after {@code MAX_VALUE}, in which case the open upper bound is saturated at
   * {@code MAX_VALUE} even though it's not a valid insertion point.
   */
  public final Range<C> rangeOf(@Nullable K key) {
    InsertionPoint<C> left = insertionPointBefore(key);
    InsertionPoint<C> right = insertionPointAfter(key);
    if (!left.equals(right)) {
      return Range.closed(left.ceiling(), right.floor());
    }
    C insertAt = right.isAboveAll() ? right.floor() : right.ceiling();
    return Range.closedOpen(insertAt, insertAt);
  }

  /**
   * Finds the {@link InsertionPoint if {@code target} were to be added <em>in order</em>.
   *
   * <p>Specifically, if {@code target} is found, the insertion point is at the its index;
   * while if not found, the insertion point is between the two adjacent indexes where
   * it could be inserted.
   *
   * <p>
   * Imagine in a Google Doc page, if you have two columns of
   * texts to be rendered into a two-column table, and you want to split the two
   * columns as evenly as possible such that it takes the fewest number of lines
   * overall. you can implement it with binary search:
   *
   * <pre>{@code
   *   InsertionPoint optimal = BinarySearch.inRangeInclusive(1, tableWidth - 1)
   *       .insertionPointFor(
   *           (low, w, high) ->
   *               Integer.compare(
   *                   renderWithColumnWidth(text1, w),
   *                   renderWithColumnWidth(text2, tableWidth - w)));
   *   return optimal.exact()
   *       .orElseGet(
   *           () -> {
   *             int lines1 = max(
   *                 renderWithColumnWidth(text1, optimal.floor()),
   *                 renderWithColumnWidth(text2, tableWidth - optimal.floor()));
   *             int lines2 = max(
   *                 renderWithColumnWidth(text1, optimal.ceiling()),
   *                 renderWithColumnWidth(text2, tableWidth - optimal.ceiling()));
   *             return lines1 < lines2 ? floor : ceiling;
   *           });
   * }
   * </pre>
   */
  public abstract InsertionPoint<C> insertionPointFor(@Nullable K key);

  /**
   * Finds the insertion point immediately before the first element that's greater than or equal to the target.
   *
   * <p>If {@code target} is absent, {@link #insertionPointBefore} and {@link #insertionPointAter} will be
   * the same point.
   *
   * <p>{@code insertionPointBefore(target).exact()} will always return empty.
   */
  public abstract InsertionPoint<C> insertionPointBefore(@Nullable K key);

  /**
   * Finds the insertion point immediately after the last element that's less than or equal to the target.
   *
   * <p>If {@code target} is absent, {@link #insertionPointBefore} and {@link #insertionPointAter} will be
   * the same point.
   *
   * <p>{@code insertionPointAfter(target).exact()} will always return empty.
   */
  public abstract InsertionPoint<C> insertionPointAfter(@Nullable K key);

  /** Represents the search target that can be found through bisecting the integer index. */
  public interface IndexedSearchTarget {
    /**
     * Given a range of {@code [low, high]} inclusively with {@code mid} as the
     * middle point of the binary search, locates the target.
     *
     * <p>
     * Returns 0 if {@code mid} is the target; negative to find it in the lower
     * range of {@code
     * [low, mid)}; or positive to find it in the upper range of
     * {@code (mid, high]}.
     *
     * <p>
     * It's guaranteed that {@code low <= mid <= high}.
     */
    int locate(int low, int mid, int high);
  }

  /** Represents the search target that can be found through bisecting the long integer index. */
  public interface LongIndexedSearchTarget {
    /**
     * Given a range of {@code [low, high]} inclusively with {@code mid} as the
     * middle point of the binary search, locates the target.
     *
     * <p>
     * Returns 0 if {@code mid} is the target; negative to find it in the lower
     * range of {@code
     * [low, mid)}; or positive to find it in the upper range of
     * {@code (mid, high]}.
     *
     * <p>
     * It's guaranteed that {@code low <= mid <= high}.
     */
    int locate(long low, long mid, long high);
  }

  private static int safeMid(int low, int high) {
    return (int) (((long) low + high) / 2);
  }

  private static long safeMid(long low, long high) {
    boolean sameSign = (low >= 0) == (high >= 0);
    return sameSign ? low + (high - low) / 2 : (low + high) / 2;
  }

  private <E> BinarySearch<E, C> keyedBy(Function<E, ? extends K> keyMapper) {
    BinarySearch<K, C> underlying = this;
    return new BinarySearch<E, C>() {
      @Override public InsertionPoint<C> insertionPointFor(@Nullable E key) {
        return underlying.insertionPointFor(keyMapper.apply(key));
      }
      @Override public InsertionPoint<C> insertionPointBefore(@Nullable E key) {
        return underlying.insertionPointBefore(keyMapper.apply(key));
      }
      @Override public InsertionPoint<C> insertionPointAfter(@Nullable E key) {
        return underlying.insertionPointAfter(keyMapper.apply(key));
      }
    };
  }

  private static IndexedSearchTarget before(IndexedSearchTarget target) {
    checkNotNull(target);
    return (low, mid, high) -> target.locate(low, mid, high) <= 0 ? -1 : 1;
  }

  private static IndexedSearchTarget after(IndexedSearchTarget target) {
    checkNotNull(target);
    return (low, mid, high) -> target.locate(low, mid, high) < 0 ? -1 : 1;
  }

  private static LongIndexedSearchTarget before(LongIndexedSearchTarget target) {
    checkNotNull(target);
    return (low, mid, high) -> target.locate(low, mid, high) <= 0 ? -1 : 1;
  }

  private static LongIndexedSearchTarget after(LongIndexedSearchTarget target) {
    checkNotNull(target);
    return (low, mid, high) -> target.locate(low, mid, high) < 0 ? -1 : 1;
  }

  private static <K, R extends Comparable<R>> BinarySearch<K, R> always(InsertionPoint<R> point) {
    return new BinarySearch<K, R>() {
      @Override public InsertionPoint<R> insertionPointFor(K key) {
        checkNotNull(key);
        return point;
      }
      @Override public InsertionPoint<R> insertionPointBefore(K key) {
        checkNotNull(key);
        return point;
      }
      @Override public InsertionPoint<R> insertionPointAfter(K key) {
        checkNotNull(key);
        return point;
      }
    };
  }

  private static void checkNotNegative(double tolerance) {
    checkArgument(tolerance >= 0.0, "tolerance (%s) cannot be negative", tolerance);
  }

  BinarySearch() {}
}
