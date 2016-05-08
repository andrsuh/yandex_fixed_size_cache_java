package andrey.yandex;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LFUCacheTest {
    @Test
    public void testDefaultConstructor() {
        FixedSizeCache<String, Integer> cache = new LFUCache<>();

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testCapacityConstructor() {
        FixedSizeCache<String, Integer> cache = new LFUCache<>(2);

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testElementsPullingPolicy() {
        FixedSizeCache<Character, Integer> cache = new LFUCache<>(5);

        for (char key = 'a'; key < 'f'; key++) {
            cache.put(key, (int) key);
        }

        assertThat(cache.size()).isEqualTo(5); // now cache contains 5 keys {a, b, c, d, e} with frequency = 1

        for (char key = 'e'; key > 'a'; key--) {
            cache.get(key);
            cache.get(key);
        } // now each key except 'a' has freq == 3
        cache.get('a'); // key == 'a' has freq. == 2

        cache.put('f', Integer.MAX_VALUE); // before insert we will remove key with smallest freq. ('a')

        for (char key = 'a'; key <= 'f'; key++) {
            assertThat(cache.size()).isEqualTo(5);
            if (key != 'a') {
                assertThat(cache.contains(key)).isTrue(); // cache still contains {b, c, d, e} and now {f}
            } else {
                assertThat(cache.contains(key)).isFalse(); // but doesn't have {a}
            }
        }
    }

    @Test
    public void testOtherMethods() {
        FixedSizeCache<Integer, Integer> cache = new LFUCache<>(10);

        for (int i = 0; i < 15; i++) {
            cache.put(i, i + 1);
            assertThat(cache.contains(i)).isTrue();
            assertThat(cache.get(i)).isEqualTo(i + 1);
        } // now we have 15 hits and 0 misses

        assertThat(cache.size()).isEqualTo(10);

        for (int i = 0; i < 20; i++) {
            cache.get(i);
        } // now we must have 25 hits and 10 misses

        assertThat(cache.getHits()).isEqualTo(25);
        assertThat(cache.getMisses()).isEqualTo(10);

        cache.clear();

        for (int i = 0; i < 15; i++) {
            assertThat(cache.contains(i)).isFalse();
            assertThat(cache.get(i)).isNull();
        }

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(15);
    }
}