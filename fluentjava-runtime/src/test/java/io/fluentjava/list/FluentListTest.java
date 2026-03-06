package io.fluentjava.list;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FluentList exhaustive tests")
class FluentListTest {

    @Test
    void firstAndLastHelpers() {
        assertEquals("a", FluentList.firstOrNull(List.of("a", "b")));
        assertNull(FluentList.firstOrNull(List.<String>of()));
        assertNull(FluentList.firstOrNull(null));
        assertEquals("b", FluentList.firstOrNull(List.of("a", "b"), s -> s.equals("b")));
        assertNull(FluentList.firstOrNull(List.of("a", "b"), s -> s.equals("x")));
        assertNull(FluentList.firstOrNull(null, s -> true));

        assertEquals("b", FluentList.lastOrNull(List.of("a", "b")));
        assertNull(FluentList.lastOrNull(List.<String>of()));
        assertNull(FluentList.lastOrNull(null));
        assertEquals("b", FluentList.lastOrNull(List.of("a", "b", "c"), s -> s.equals("b")));
        assertNull(FluentList.lastOrNull(List.of("a", "b"), s -> s.equals("x")));
        assertNull(FluentList.lastOrNull(null, s -> true));
    }

    @Test
    void nullEmptyAndIndexHelpers() {
        assertTrue(FluentList.isNullOrEmpty(null));
        assertTrue(FluentList.isNullOrEmpty(List.of()));
        assertFalse(FluentList.isNullOrEmpty(List.of(1)));

        assertEquals(List.of(), FluentList.orEmpty(null));
        List<Integer> source = List.of(1, 2, 3);
        assertSame(source, FluentList.orEmpty(source));

        assertEquals(2, FluentList.getOrNull(source, 1));
        assertNull(FluentList.getOrNull(source, -1));
        assertNull(FluentList.getOrNull(source, 3));
        assertNull(FluentList.getOrNull(null, 0));

        assertEquals(9, FluentList.getOrDefault(source, 5, 9));
        assertEquals(1, FluentList.getOrDefault(source, 0, 9));
    }

    @Test
    void filterAndMapOperations() {
        assertEquals(List.of(2, 4), FluentList.filterBy(List.of(1, 2, 3, 4), n -> n % 2 == 0));
        assertEquals(List.of(), FluentList.filterBy(null, n -> true));

        List<String> mapped = FluentList.mapTo(List.of(1, 2), Object::toString);
        assertEquals(List.of("1", "2"), mapped);
        assertThrows(UnsupportedOperationException.class, () -> mapped.add("3"));
        assertEquals(List.of(), FluentList.mapTo(null, Object::toString));

        assertEquals(List.of("a", "b", "c"),
                FluentList.flatMap(List.of("a,b", "c"), s -> List.of(s.split(","))));
        assertEquals(List.of(), FluentList.flatMap(null, s -> List.of(s)));
        assertEquals(List.of("a"), FluentList.flatMap(List.of("a", "b"), s -> s.equals("a") ? List.of(s) : null));

        assertEquals(List.of(1, 2), FluentList.mapNotNull(List.of("1", "x", "2"), s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        assertEquals(List.of(), FluentList.mapNotNull(null, Object::toString));

        assertEquals(List.of("a", "b"), FluentList.filterNotNull(Arrays.asList("a", null, "b")));
        assertEquals(List.of(), FluentList.filterNotNull(null));
    }

    @Test
    void distinctSortAndGroup() {
        assertEquals(List.of("ax", "by"),
                FluentList.distinctBy(List.of("ax", "ay", "by"), s -> s.charAt(0)));
        assertEquals(List.of(), FluentList.distinctBy(null, s -> s));

        assertEquals(List.of("a", "b", "c"),
            FluentList.sortedBy(List.of("b", "c", "a"), s -> s));
        assertEquals(List.of(), FluentList.<String, String>sortedBy(null, s -> s));

        assertEquals(List.of("c", "b", "a"),
            FluentList.sortedByDescending(List.of("b", "c", "a"), s -> s));
        assertEquals(List.of(), FluentList.<String, String>sortedByDescending(null, s -> s));

        Map<Integer, List<String>> grouped = FluentList.groupBy(List.of("a", "bb", "c"), String::length);
        assertEquals(List.of("a", "c"), grouped.get(1));
        assertEquals(List.of("bb"), grouped.get(2));
        assertThrows(UnsupportedOperationException.class, () -> grouped.put(3, List.of("x")));
        assertThrows(UnsupportedOperationException.class, () -> grouped.get(1).add("x"));
        assertEquals(Map.of(), FluentList.groupBy(null, String::length));

        Map<Integer, String> associated = FluentList.associateBy(List.of("a", "bb", "cc"), String::length);
        assertEquals("a", associated.get(1));
        assertEquals("cc", associated.get(2));
        assertThrows(UnsupportedOperationException.class, () -> associated.put(3, "ddd"));
        assertEquals(Map.of(), FluentList.associateBy(null, String::length));
    }

    @Test
    void partitionChunkAndNumericAggregation() {
        Map.Entry<List<Integer>, List<Integer>> parts = FluentList.partition(List.of(1, 2, 3), n -> n % 2 == 0);
        assertEquals(List.of(2), parts.getKey());
        assertEquals(List.of(1, 3), parts.getValue());
        assertThrows(UnsupportedOperationException.class, () -> parts.getKey().add(4));
        assertEquals(Map.entry(List.of(), List.of()), FluentList.partition(null, n -> true));

        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)),
                FluentList.chunked(List.of(1, 2, 3, 4, 5), 2));
        assertEquals(List.of(), FluentList.chunked(List.of(1, 2), 0));
        assertEquals(List.of(), FluentList.chunked(List.of(), 2));
        assertEquals(List.of(), FluentList.chunked(null, 2));

        assertEquals(6d, FluentList.sumOf(List.of(1, 2, 3), Integer::doubleValue));
        assertEquals(0d, FluentList.sumOf(List.of(), Integer::doubleValue));
        assertEquals(0d, FluentList.sumOf(null, Integer::doubleValue));

        assertEquals(3d, FluentList.averageOf(List.of(2, 3, 4), Integer::doubleValue));
        assertEquals(0d, FluentList.averageOf(List.of(), Integer::doubleValue));
        assertEquals(0d, FluentList.averageOf(null, Integer::doubleValue));

        assertEquals(2L, FluentList.countBy(List.of(1, 2, 3, 4), n -> n % 2 == 0));
        assertEquals(0L, FluentList.countBy(List.of(), n -> true));
        assertEquals(0L, FluentList.countBy(null, n -> true));
    }

    @Test
    void minMaxAndPredicates() {
        assertEquals("ccc", FluentList.maxByOrNull(List.of("a", "bb", "ccc"), String::length));
        assertNull(FluentList.maxByOrNull(List.<String>of(), String::length));
        assertNull(FluentList.maxByOrNull(null, String::length));
        assertEquals("a", FluentList.maxByOrNull(List.of("a", "b"), s -> (Integer) null));

        assertEquals("a", FluentList.minByOrNull(List.of("a", "bb", "ccc"), String::length));
        assertNull(FluentList.minByOrNull(List.<String>of(), String::length));
        assertNull(FluentList.minByOrNull(null, String::length));
        assertEquals("a", FluentList.minByOrNull(List.of("a", "b"), s -> (Integer) null));

        assertTrue(FluentList.none(List.of(1, 3, 5), n -> n % 2 == 0));
        assertFalse(FluentList.none(List.of(1, 2), n -> n % 2 == 0));

        assertTrue(FluentList.any(List.of(1, 2), n -> n % 2 == 0));
        assertFalse(FluentList.any(List.of(1, 3), n -> n % 2 == 0));

        assertTrue(FluentList.all(List.of(2, 4), n -> n % 2 == 0));
        assertFalse(FluentList.all(List.of(2, 3), n -> n % 2 == 0));
        assertTrue(FluentList.all(List.of(), n -> false));
        assertTrue(FluentList.all(null, n -> false));
    }

    @Test
    void slicingPagingAndSetOperations() {
        assertEquals(List.of(1, 2), FluentList.takeWhile(List.of(1, 2, 0, 3), n -> n > 0));
        assertEquals(List.of(), FluentList.takeWhile(List.of(), n -> true));
        assertEquals(List.of(), FluentList.takeWhile(null, n -> true));

        assertEquals(List.of(0, 3), FluentList.dropWhile(List.of(1, 2, 0, 3), n -> n > 0));
        assertEquals(List.of(), FluentList.dropWhile(List.of(), n -> true));
        assertEquals(List.of(), FluentList.dropWhile(null, n -> true));

        assertEquals(List.of(3, 4), FluentList.paginate(List.of(1, 2, 3, 4, 5), 2, 2));
        assertEquals(List.of(), FluentList.paginate(List.of(1, 2, 3), 4, 2));
        assertEquals(List.of(), FluentList.paginate(List.of(1, 2), 1, 0));
        assertEquals(List.of(), FluentList.paginate(List.of(1, 2), 0, 2));
        assertEquals(List.of(), FluentList.paginate(List.of(), 1, 2));
        assertEquals(List.of(), FluentList.paginate(null, 1, 2));

        assertEquals(List.of(Map.entry("a", 1), Map.entry("b", 2)),
                FluentList.zip(List.of("a", "b", "c"), List.of(1, 2)));
        assertEquals(List.of(), FluentList.zip(List.of(), List.of(1)));
        assertEquals(List.of(), FluentList.zip(null, List.of(1)));
        assertEquals(List.of(), FluentList.zip(List.of("a"), null));

        assertEquals(List.of(2, 3), FluentList.intersect(List.of(1, 2, 2, 3), List.of(2, 3, 4)));
        assertEquals(List.of(), FluentList.intersect(List.of(), List.of(1)));
        assertEquals(List.of(), FluentList.intersect(null, List.of(1)));
        assertEquals(List.of(), FluentList.intersect(List.of(1), null));

        assertEquals(List.of(1, 3), FluentList.subtract(List.of(1, 2, 3), List.of(2, 4)));
        assertEquals(List.of(1, 2), FluentList.subtract(List.of(1, 2), null));
        assertEquals(List.of(1, 2), FluentList.subtract(List.of(1, 2), List.of()));
        assertEquals(List.of(), FluentList.subtract(List.of(), List.of(1)));
        assertEquals(List.of(), FluentList.subtract(null, List.of(1)));

        assertEquals(List.of(1, 2, 3), FluentList.union(List.of(1, 2), List.of(2, 3)));
        assertEquals(List.of(1, 2), FluentList.union(List.of(1, 2), null));
        assertEquals(List.of(3, 4), FluentList.union(null, List.of(3, 4)));
        assertEquals(List.of(), FluentList.union(null, null));
    }

    @Test
    void utilityClassConstructorThrows() throws Exception {
        var ctor = FluentList.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(AssertionError.class, ex.getCause());
    }
}
