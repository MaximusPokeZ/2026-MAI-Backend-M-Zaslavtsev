package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    @Test
    void test() {
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.set(1, 1);
        cache.set(2, 2);
        assertEquals(1, cache.get(1));
        cache.set(3, 3);
        assertNull(cache.get(2));
    }

    @Test
    void test2() {
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.set(2, 1);
        cache.set(1, 1);
        cache.set(2, 3);
        cache.set(4, 1);
        assertNull(cache.get(1));
        assertEquals(3, cache.get(2));
        assertEquals(1, cache.get(4));
    }

    @Test
    void testByExample() {
        LRUCache<String, String> cache = new LRUCache<>(100);
        cache.set("Jesse", "Pinkman");
        cache.set("Walter", "White");
        cache.set("Jesse", "James");
        assertEquals("James", cache.get("Jesse"));
        cache.rem("Walter");
        assertNull(cache.get("Walter"));
    }

}