package io.fluentjava.map;

import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FluentMap {

    private FluentMap() {
        throw new AssertionError("FluentMap is a utility class and cannot be instantiated");
    }

    /**
     * Checks if the map is null or empty.
     *
     * @param map the map to check (may be {@code null})
     * @return {@code true} if the map is null or empty
     */
    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Returns the map itself, or an empty map if it is null.
     *
     * @param map the map to check (may be {@code null})
     * @return the original map, or an empty map if null
     */
    public static <K, V> Map<K, V> orEmpty(Map<K, V> map) {
        return map == null ? Collections.<K, V>emptyMap() : map;
    }

    /**
     * Returns the value for the key, or an empty string if not found or null.
     *
     * @param map the map to search (may be {@code null})
     * @param key the key to look up
     * @return the value for the key, or an empty string if not found or null
     */
    public static <K> String getOrEmpty(Map<K, String> map, K key) {
        if (map == null) {
            return "";
        }
        String value = map.get(key);
        return value == null ? "" : value;
    }

    /**
     * Returns the value for the key, or null if not found or map is null.
     *
     * @param map the map to search (may be {@code null})
     * @param key the key to look up
     * @return the value for the key, or null if not found
     */
    public static <K, V> V getOrNull(Map<K, V> map, K key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Returns a map containing only the entries whose keys match the predicate.
     *
     * @param map the map to filter (may be {@code null})
     * @param pred the predicate to test keys (must not be {@code null})
     * @return an unmodifiable map of matching entries
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, Predicate<K> pred) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> out = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (pred.test(entry.getKey())) {
                out.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Returns a map containing only the entries whose values match the predicate.
     *
     * @param map the map to filter (may be {@code null})
     * @param pred the predicate to test values (must not be {@code null})
     * @return an unmodifiable map of matching entries
     */
    public static <K, V> Map<K, V> filterValues(Map<K, V> map, Predicate<V> pred) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> out = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (pred.test(entry.getValue())) {
                out.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Returns a map with the same keys and values mapped by the given function.
     *
     * @param map the map to transform (may be {@code null})
     * @param fn the function to apply to values (must not be {@code null})
     * @return an unmodifiable map with mapped values
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> fn) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, R> out = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            out.put(entry.getKey(), fn.apply(entry.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Returns a map with the same values and keys mapped by the given function.
     *
     * @param map the map to transform (may be {@code null})
     * @param fn the function to apply to keys (must not be {@code null})
     * @return an unmodifiable map with mapped keys
     */
    public static <K, V, R> Map<R, V> mapKeys(Map<K, V> map, Function<K, R> fn) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<R, V> out = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            out.put(fn.apply(entry.getKey()), entry.getValue());
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Checks if any entry in the map matches the predicate.
     *
     * @param map the map to check (may be {@code null})
     * @param pred the predicate to test entries (must not be {@code null})
     * @return {@code true} if any entry matches
     */
    public static <K, V> boolean any(Map<K, V> map, Predicate<Map.Entry<K, V>> pred) {
        return count(map, pred) > 0;
    }

    /**
     * Checks if all entries in the map match the predicate.
     *
     * @param map the map to check (may be {@code null})
     * @param pred the predicate to test entries (must not be {@code null})
     * @return {@code true} if all entries match or map is empty/null
     */
    public static <K, V> boolean all(Map<K, V> map, Predicate<Map.Entry<K, V>> pred) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!pred.test(entry)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if no entry in the map matches the predicate.
     *
     * @param map the map to check (may be {@code null})
     * @param pred the predicate to test entries (must not be {@code null})
     * @return {@code true} if no entry matches
     */
    public static <K, V> boolean none(Map<K, V> map, Predicate<Map.Entry<K, V>> pred) {
        return count(map, pred) == 0;
    }

    /**
     * Counts the number of entries in the map matching the predicate.
     *
     * @param map the map to check (may be {@code null})
     * @param pred the predicate to test entries (must not be {@code null})
     * @return the number of matching entries
     */
    public static <K, V> long count(Map<K, V> map, Predicate<Map.Entry<K, V>> pred) {
        if (map == null || map.isEmpty()) {
            return 0L;
        }
        long c = 0L;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (pred.test(entry)) {
                c++;
            }
        }
        return c;
    }

    /**
     * Returns a list of all entries in the map as Map.Entry objects.
     *
     * @param map the map to convert (may be {@code null})
     * @return a list of entries, or empty if map is null/empty
     */
    public static <K, V> List<Map.Entry<K, V>> toList(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map.Entry<K, V>> out = new ArrayList<>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            out.add(new AbstractMap.SimpleImmutableEntry<K, V>(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Returns a new map containing all entries from both maps (map2 overrides map1 on key collision).
     *
     * @param map1 the first map (may be {@code null})
     * @param map2 the second map (may be {@code null})
     * @return an unmodifiable merged map
     */
    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
        if ((map1 == null || map1.isEmpty()) && (map2 == null || map2.isEmpty())) {
            return Collections.emptyMap();
        }
        Map<K, V> out = new LinkedHashMap<>();
        if (map1 != null) {
            out.putAll(map1);
        }
        if (map2 != null) {
            out.putAll(map2);
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * Returns a new map with keys and values swapped.
     *
     * @param map the map to invert (may be {@code null})
     * @return an unmodifiable inverted map
     */
    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<V, K> out = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            out.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(out);
    }
}
