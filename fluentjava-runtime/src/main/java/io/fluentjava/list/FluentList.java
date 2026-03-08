package io.fluentjava.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Fluent utility methods for {@link java.util.List}.
 *
 * <p>All methods are {@code public static}, null-safe, and return
 * unmodifiable lists (where applicable). They are the compile-time rewrite
 * targets for FluentJava's javac plugin: the plugin rewrites
 * {@code users.firstOrNull()} into {@code FluentList.firstOrNull(users)}
 * in the AST before type resolution.</p>
 *
 * <h3>Fluent usage (with javac plugin):</h3>
 * <pre>{@code
 *   users.firstOrNull()                       // â†’ FluentList.firstOrNull(users)
 *   users.filterBy(u -> u.getAge() > 18)      // â†’ FluentList.filterBy(users, u -> ...)
 *   users.mapTo(User::getName)                 // â†’ FluentList.mapTo(users, User::getName)
 *   users.lastOrNull()                         // â†’ FluentList.lastOrNull(users)
 * }</pre>
 *
 * <h3>Security:</h3>
 * <ul>
 *   <li>Zero reflection</li>
 *   <li>Zero internal JDK API usage at runtime</li>
 *   <li>Zero mutable state</li>
 *   <li>Zero external dependencies</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class FluentList {

    /** Utility class â€” cannot be instantiated. */
    private FluentList() {
        throw new AssertionError("FluentList is a utility class and cannot be instantiated");
    }

    /**
     * Returns the first element of the list, or {@code null} if the list is
     * {@code null} or empty.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentList.firstOrNull(List.of("a","b","c")) // "a"
     *   FluentList.firstOrNull(List.of())            // null
     *   FluentList.firstOrNull(null)                 // null
     * }</pre>
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return the first element, or {@code null}
     */
    public static <T> T firstOrNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * Returns {@code true} if the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return {@code true} if the list is {@code null} or empty
     */
    public static <T> boolean isNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Returns an empty unmodifiable list when the input is {@code null}.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return the original list if not {@code null}, otherwise an empty list
     */
    public static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    /**
     * Returns the first element matching the predicate, or {@code null}
     * when none match or the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return the first matching element, or {@code null}
     */
    public static <T> T firstOrNull(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (T item : list) {
            if (pred.test(item)) {
                return item;
            }
        }
        return null;
    }


    /**
     * Returns the last element of the list, or {@code null} if the list is
     * {@code null} or empty.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentList.lastOrNull(List.of("a","b","c")) // "c"
     *   FluentList.lastOrNull(List.of())            // null
     *   FluentList.lastOrNull(null)                 // null
     * }</pre>
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return the last element, or {@code null}
     */
    public static <T> T lastOrNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(list.size() - 1);
    }

    /**
     * Returns the last element matching the predicate, or {@code null}
     * when none match or the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return the last matching element, or {@code null}
     */
    public static <T> T lastOrNull(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            T item = list.get(i);
            if (pred.test(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the element at the given index, or {@code null} if the index
     * is out of bounds or the list is {@code null}.
     *
     * @param <T>   the element type
     * @param list  the source list (may be {@code null})
     * @param index the element index
     * @return the element at {@code index}, or {@code null}
     */
    public static <T> T getOrNull(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * Returns the element at the given index or the provided default when
     * the index is out of bounds or the list is {@code null}.
     *
     * @param <T>          the element type
     * @param list         the source list (may be {@code null})
     * @param index        the element index
     * @param defaultValue the default value to return when not present
     * @return the element at {@code index} or {@code defaultValue}
     */
    public static <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        T value = getOrNull(list, index);
        return value == null ? defaultValue : value;
    }

    /**
     * Filters the list elements matching the given predicate.
     *
     * <p>Returns an unmodifiable list. If the source list is {@code null},
     * returns an empty list.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentList.filterBy(List.of(1,2,3,4), n -> n % 2 == 0) // [2, 4]
     *   FluentList.filterBy(List.of(), n -> true)               // []
     *   FluentList.filterBy(null, n -> true)                    // []
     * }</pre>
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the filtering predicate (must not be {@code null})
     * @return an unmodifiable list of matching elements
     */
    public static <T> List<T> filterBy(List<T> list, Predicate<T> pred) {
        if (list == null) {
            return List.of();
        }
        return list.stream().filter(pred).toList();
    }


    /**
     * Maps each element of the list using the given function.
     *
     * <p>Returns an unmodifiable list. If the source list is {@code null},
     * returns an empty list.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentList.mapTo(List.of(1,2,3), Object::toString) // ["1","2","3"]
     *   FluentList.mapTo(List.of(), Object::toString)       // []
     *   FluentList.mapTo(null, Object::toString)            // []
     * }</pre>
     *
     * @param <T>  the source element type
     * @param <R>  the result element type
     * @param list the source list (may be {@code null})
     * @param fn   the mapping function (must not be {@code null})
     * @return an unmodifiable list of mapped elements
     */
    public static <T, R> List<R> mapTo(List<T> list, Function<T, R> fn) {
        if (list == null) {
            return List.of();
        }
        return list.stream().map(fn).toList();
    }

    /**
     * Applies the mapping function to each element and flattens the results
     * into a single unmodifiable list.
     *
     * @param <T>  the source element type
     * @param <R>  the result element type
     * @param list the source list (may be {@code null})
     * @param fn   the mapping function producing lists (must not be {@code null})
     * @return an unmodifiable flattened list
     */
    public static <T, R> List<R> flatMap(List<T> list, Function<T, List<R>> fn) {
        if (list == null) {
            return List.of();
        }
        List<R> out = new ArrayList<>();
        for (T item : list) {
            List<R> sub = fn.apply(item);
            if (sub != null) {
                out.addAll(sub);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Maps elements using the function and excludes {@code null} results.
     *
     * @param <T>  the source element type
     * @param <R>  the result element type
     * @param list the source list (may be {@code null})
     * @param fn   the mapping function (must not be {@code null})
     * @return an unmodifiable list of non-null mapped values
     */
    public static <T, R> List<R> mapNotNull(List<T> list, Function<T, R> fn) {
        if (list == null) {
            return List.of();
        }
        return list.stream().map(fn).filter(Objects::nonNull).toList();
    }

    /**
     * Returns an unmodifiable list containing only non-null elements.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return an unmodifiable list of non-null elements
     */
    public static <T> List<T> filterNotNull(List<T> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream().filter(Objects::nonNull).toList();
    }

    /**
     * Returns a list of elements with distinct keys computed by the given
     * function, preserving original order.
     *
     * @param <T>  the element type
     * @param <K>  the key type
     * @param list the source list (may be {@code null})
     * @param fn   the function to extract the key (must not be {@code null})
     * @return an unmodifiable list with distinct elements by key
     */
    public static <T, K> List<T> distinctBy(List<T> list, Function<T, K> fn) {
        if (list == null) {
            return List.of();
        }
        Set<K> seen = new LinkedHashSet<>();
        List<T> out = new ArrayList<>();
        for (T item : list) {
            if (seen.add(fn.apply(item))) {
                out.add(item);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Returns a new list sorted by the provided key function in ascending
     * order (nulls first).
     *
     * @param <T>  the element type
     * @param <U>  the key type (must be comparable)
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return an unmodifiable sorted list
     */
    public static <T, U extends Comparable<? super U>> List<T> sortedBy(List<T> list, Function<T, U> fn) {
        if (list == null) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(fn, Comparator.nullsFirst(Comparator.naturalOrder())));
        return List.copyOf(copy);
    }

    /**
     * Returns a new list sorted by the provided key function in descending
     * order (nulls last).
     *
     * @param <T>  the element type
     * @param <U>  the key type (must be comparable)
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return an unmodifiable sorted list in descending order
     */
    public static <T, U extends Comparable<? super U>> List<T> sortedByDescending(List<T> list, Function<T, U> fn) {
        if (list == null) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(fn, Comparator.nullsLast(Comparator.reverseOrder())));
        return List.copyOf(copy);
    }

    /**
     * Groups elements by the key produced by the function and returns an
     * unmodifiable map of key to unmodifiable lists.
     *
     * @param <T>  the element type
     * @param <K>  the key type
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return an unmodifiable map grouping keys to lists of elements
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> fn) {
        if (list == null) {
            return Map.of();
        }
        Map<K, List<T>> tmp = new LinkedHashMap<>();
        for (T item : list) {
            K key = fn.apply(item);
            tmp.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        Map<K, List<T>> out = new LinkedHashMap<>();
        for (Map.Entry<K, List<T>> entry : tmp.entrySet()) {
            out.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Associates elements by keys produced by the function. Later elements
     * overwrite earlier ones for the same key.
     *
     * @param <T>  the element type
     * @param <K>  the key type
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return an unmodifiable map associating keys to elements
     */
    public static <T, K> Map<K, T> associateBy(List<T> list, Function<T, K> fn) {
        if (list == null) {
            return Map.of();
        }
        Map<K, T> out = new LinkedHashMap<>();
        for (T item : list) {
            out.put(fn.apply(item), item);
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Partitions the list into a pair of lists (accepted, rejected) based on
     * the predicate.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return a map entry whose key is the matching list and value is the non-matching list
     */
    public static <T> Map.Entry<List<T>, List<T>> partition(List<T> list, Predicate<T> pred) {
        if (list == null) {
            return Map.entry(List.of(), List.of());
        }
        List<T> yes = new ArrayList<>();
        List<T> no = new ArrayList<>();
        for (T item : list) {
            if (pred.test(item)) {
                yes.add(item);
            } else {
                no.add(item);
            }
        }
        return Map.entry(List.copyOf(yes), List.copyOf(no));
    }

    /**
     * Splits the list into consecutive sublists of the given size.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param size the chunk size (must be &gt; 0)
     * @return an unmodifiable list of unmodifiable chunks
     */
    public static <T> List<List<T>> chunked(List<T> list, int size) {
        if (list == null || list.isEmpty() || size <= 0) {
            return List.of();
        }
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            out.add(List.copyOf(list.subList(i, Math.min(i + size, list.size()))));
        }
        return List.copyOf(out);
    }

    /**
     * Returns the sum of values produced by applying the function to each
     * element. Returns {@code 0d} for {@code null} or empty lists.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param fn   the function producing double values (must not be {@code null})
     * @return the sum of produced values
     */
    public static <T> double sumOf(List<T> list, ToDoubleFunction<T> fn) {
        if (list == null || list.isEmpty()) {
            return 0d;
        }
        double sum = 0d;
        for (T item : list) {
            sum += fn.applyAsDouble(item);
        }
        return sum;
    }

    /**
     * Returns the element with the maximum key as determined by the
     * function, or {@code null} if the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param <U>  the key type (must be comparable)
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return the element with maximum key, or {@code null}
     */
    public static <T, U extends Comparable<? super U>> T maxByOrNull(List<T> list, Function<T, U> fn) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        T best = null;
        U bestKey = null;
        for (T item : list) {
            U key = fn.apply(item);
            if (best == null || Comparator.nullsFirst(Comparator.<U>naturalOrder()).compare(key, bestKey) > 0) {
                best = item;
                bestKey = key;
            }
        }
        return best;
    }

    /**
     * Returns the element with the minimum key as determined by the
     * function, or {@code null} if the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param <U>  the key type (must be comparable)
     * @param list the source list (may be {@code null})
     * @param fn   the key extractor function (must not be {@code null})
     * @return the element with minimum key, or {@code null}
     */
    public static <T, U extends Comparable<? super U>> T minByOrNull(List<T> list, Function<T, U> fn) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        T best = null;
        U bestKey = null;
        for (T item : list) {
            U key = fn.apply(item);
            if (best == null || Comparator.nullsLast(Comparator.<U>naturalOrder()).compare(key, bestKey) < 0) {
                best = item;
                bestKey = key;
            }
        }
        return best;
    }

    /**
     * Returns the average of values produced by the function, or {@code 0d}
     * when the list is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param fn   the function producing double values (must not be {@code null})
     * @return the average of produced values
     */
    public static <T> double averageOf(List<T> list, ToDoubleFunction<T> fn) {
        if (list == null || list.isEmpty()) {
            return 0d;
        }
        return sumOf(list, fn) / list.size();
    }

    /**
     * Counts elements matching the predicate.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return the number of matching elements
     */
    public static <T> long countBy(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return 0L;
        }
        long count = 0L;
        for (T item : list) {
            if (pred.test(item)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if no elements match the predicate.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return {@code true} when no elements match
     */
    public static <T> boolean none(List<T> list, Predicate<T> pred) {
        return countBy(list, pred) == 0;
    }

    /**
     * Returns {@code true} if any element matches the predicate.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return {@code true} when at least one element matches
     */
    public static <T> boolean any(List<T> list, Predicate<T> pred) {
        return countBy(list, pred) > 0;
    }

    /**
     * Returns {@code true} if all elements match the predicate (vacuously
     * {@code true} for {@code null} or empty lists).
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return {@code true} when all elements match
     */
    public static <T> boolean all(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        for (T item : list) {
            if (!pred.test(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the leading elements that match the predicate.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return an unmodifiable list of leading matching elements
     */
    public static <T> List<T> takeWhile(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<T> out = new ArrayList<>();
        for (T item : list) {
            if (!pred.test(item)) {
                break;
            }
            out.add(item);
        }
        return List.copyOf(out);
    }

    /**
     * Drops leading elements that match the predicate and returns the rest.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements (must not be {@code null})
     * @return an unmodifiable list with leading matching elements removed
     */
    public static <T> List<T> dropWhile(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        int idx = 0;
        while (idx < list.size() && pred.test(list.get(idx))) {
            idx++;
        }
        return List.copyOf(list.subList(idx, list.size()));
    }

    /**
     * Returns a page of the list (1-based page index) with given size.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param page the 1-based page number
     * @param size the page size (must be &gt; 0)
     * @return an unmodifiable list representing the page
     */
    public static <T> List<T> paginate(List<T> list, int page, int size) {
        if (list == null || list.isEmpty() || size <= 0 || page <= 0) {
            return List.of();
        }
        int from = (page - 1) * size;
        if (from >= list.size()) {
            return List.of();
        }
        int to = Math.min(from + size, list.size());
        return List.copyOf(list.subList(from, to));
    }

    /**
     * Zips two lists into a list of pairs up to the length of the shorter
     * list.
     *
     * @param <T>   the first list element type
     * @param <U>   the second list element type
     * @param list  the first list (may be {@code null})
     * @param other the second list (may be {@code null})
     * @return an unmodifiable list of pairs
     */
    public static <T, U> List<Map.Entry<T, U>> zip(List<T> list, List<U> other) {
        if (list == null || other == null || list.isEmpty() || other.isEmpty()) {
            return List.of();
        }
        int size = Math.min(list.size(), other.size());
        List<Map.Entry<T, U>> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(Map.entry(list.get(i), other.get(i)));
        }
        return List.copyOf(out);
    }

    /**
     * Returns the intersection of two lists, preserving order of the first
     * list and removing duplicates.
     *
     * @param <T>   the element type
     * @param list  the first list (may be {@code null})
     * @param other the second list (may be {@code null})
     * @return an unmodifiable list with elements present in both lists
     */
    public static <T> List<T> intersect(List<T> list, List<T> other) {
        if (list == null || other == null || list.isEmpty() || other.isEmpty()) {
            return List.of();
        }
        Set<T> right = new LinkedHashSet<>(other);
        List<T> out = new ArrayList<>();
        for (T item : list) {
            if (right.contains(item) && !out.contains(item)) {
                out.add(item);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Returns elements from the first list that are not present in the
     * second list.
     *
     * @param <T>   the element type
     * @param list  the source list (may be {@code null})
     * @param other the list of elements to remove (may be {@code null})
     * @return an unmodifiable list of elements from {@code list} not in {@code other}
     */
    public static <T> List<T> subtract(List<T> list, List<T> other) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        if (other == null || other.isEmpty()) {
            return List.copyOf(list);
        }
        Set<T> right = new LinkedHashSet<>(other);
        return list.stream().filter(item -> !right.contains(item)).toList();
    }

    /**
     * Returns the union of two lists preserving insertion order of elements
     * and removing duplicates.
     *
     * @param <T>   the element type
     * @param list  the first list (may be {@code null})
     * @param other the second list (may be {@code null})
     * @return an unmodifiable list containing the union of both lists
     */
    public static <T> List<T> union(List<T> list, List<T> other) {
        if ((list == null || list.isEmpty()) && (other == null || other.isEmpty())) {
            return List.of();
        }
        LinkedHashSet<T> set = new LinkedHashSet<>();
        if (list != null) {
            set.addAll(list);
        }
        if (other != null) {
            set.addAll(other);
        }
        return List.copyOf(set);
    }

    // ────────────────────────────────────────────────────────────────
    // New methods
    // ────────────────────────────────────────────────────────────────

    /**
     * Returns the second element of the list, or {@code null} if the list
     * has fewer than 2 elements or is {@code null}.
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return the second element, or {@code null}
     */
    public static <T> T second(List<T> list) {
        return (list == null || list.size() < 2) ? null : list.get(1);
    }

    /**
     * Returns the third element of the list, or {@code null} if the list
     * has fewer than 3 elements or is {@code null}.
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return the third element, or {@code null}
     */
    public static <T> T third(List<T> list) {
        return (list == null || list.size() < 3) ? null : list.get(2);
    }

    /**
     * Converts the list to a {@link LinkedHashSet} preserving insertion order.
     *
     * @param <T>  the element type
     * @param list the list (may be {@code null})
     * @return a {@link Set} preserving order, or an empty set if null
     */
    public static <T> Set<T> toSet(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(list));
    }

    /**
     * Builds a {@link Map} from the list using the given key and value extractors.
     * Later elements overwrite earlier ones for the same key.
     *
     * <p>Inspired by Kotlin's {@code associate} function.</p>
     *
     * @param <T>     the element type
     * @param <K>     the key type
     * @param <V>     the value type
     * @param list    the source list (may be {@code null})
     * @param keyFn   the key extractor function
     * @param valueFn the value extractor function
     * @return an unmodifiable map
     */
    public static <T, K, V> Map<K, V> associate(List<T> list, Function<T, K> keyFn, Function<T, V> valueFn) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        Map<K, V> out = new LinkedHashMap<>();
        for (T item : list) {
            out.put(keyFn.apply(item), valueFn.apply(item));
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Returns the index of the first element matching the predicate,
     * or {@code -1} if none match.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements
     * @return the index of the first matching element, or -1
     */
    public static <T> int indexOfFirst(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            if (pred.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last element matching the predicate,
     * or {@code -1} if none match.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param pred the predicate to test elements
     * @return the index of the last matching element, or -1
     */
    public static <T> int indexOfLast(List<T> list, Predicate<T> pred) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            if (pred.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Flattens a list of lists into a single list.
     *
     * <p>Inspired by Kotlin's {@code flatten}.</p>
     *
     * @param <T>  the element type
     * @param list the list of lists (may be {@code null})
     * @return an unmodifiable flattened list
     */
    public static <T> List<T> flatten(List<List<T>> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<T> out = new ArrayList<>();
        for (List<T> inner : list) {
            if (inner != null) {
                out.addAll(inner);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Returns a random element from the list, or {@code null} if the list
     * is {@code null} or empty.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return a random element, or {@code null}
     */
    public static <T> T randomOrNull(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Iterates over the list with index, invoking the action with
     * {@code (index, element)} for each entry.
     *
     * <p>Inspired by Kotlin's {@code forEachIndexed}.</p>
     *
     * @param <T>    the element type
     * @param list   the source list (may be {@code null})
     * @param action the action to invoke for each element
     */
    public static <T> void forEachIndexed(List<T> list, BiConsumer<Integer, T> action) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            action.accept(i, list.get(i));
        }
    }

    /**
     * Joins list elements using {@code ", "} separator (calling {@code toString()}).
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return a comma-separated string, or {@code ""} if null/empty
     */
    public static <T> String toCsv(List<T> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns a frequency map counting occurrences of each element.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return an unmodifiable map of element to count
     */
    public static <T> Map<T, Long> frequencies(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        Map<T, Long> freq = new LinkedHashMap<>();
        for (T item : list) {
            freq.merge(item, 1L, Long::sum);
        }
        return Collections.unmodifiableMap(freq);
    }

    /**
     * Returns a new shuffled list without modifying the original.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @return a new shuffled unmodifiable list
     */
    public static <T> List<T> shuffled(List<T> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return List.copyOf(copy);
    }

    /**
     * Returns {@code n} random elements from the list without repetition.
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param n    the number of elements to sample
     * @return an unmodifiable list of sampled elements
     */
    public static <T> List<T> sample(List<T> list, int n) {
        if (list == null || list.isEmpty() || n <= 0) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return List.copyOf(copy.subList(0, Math.min(n, copy.size())));
    }

    /**
     * Returns sliding windows of the given size over the list.
     *
     * <p>Inspired by Kotlin's {@code windowed}.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentList.windowed(List.of(1,2,3,4), 2)  // [[1,2],[2,3],[3,4]]
     * }</pre>
     *
     * @param <T>  the element type
     * @param list the source list (may be {@code null})
     * @param size the window size (must be &gt; 0)
     * @return an unmodifiable list of unmodifiable windows
     */
    public static <T> List<List<T>> windowed(List<T> list, int size) {
        if (list == null || list.isEmpty() || size <= 0 || size > list.size()) {
            return List.of();
        }
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i <= list.size() - size; i++) {
            out.add(List.copyOf(list.subList(i, i + size)));
        }
        return List.copyOf(out);
    }

    /**
     * Returns the sum of int values produced by the mapper function.
     *
     * @param <T>    the element type
     * @param list   the source list (may be {@code null})
     * @param mapper the function to extract int values
     * @return the sum, or 0 if null/empty
     */
    public static <T> int sumOfInt(List<T> list, ToIntFunction<T> mapper) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (T item : list) {
            sum += mapper.applyAsInt(item);
        }
        return sum;
    }

    /**
     * Returns the sum of long values produced by the mapper function.
     *
     * @param <T>    the element type
     * @param list   the source list (may be {@code null})
     * @param mapper the function to extract long values
     * @return the sum, or 0L if null/empty
     */
    public static <T> long sumOfLong(List<T> list, ToLongFunction<T> mapper) {
        if (list == null || list.isEmpty()) {
            return 0L;
        }
        long sum = 0L;
        for (T item : list) {
            sum += mapper.applyAsLong(item);
        }
        return sum;
    }
}
