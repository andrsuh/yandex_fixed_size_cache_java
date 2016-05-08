package andrey.yandex;

import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;

/**
 * Created by andrey on 08.05.16.
 */

public class LRUCacheTest {
    @Test
    public void testDefaultConstructor() {
        FixedSizeCache<String, Integer> cache = new LRUCache<>();

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testCapacityConstructor() {
        FixedSizeCache<String, Integer> cache = new LRUCache<>(2);

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testElementsPullingPolicy() {
        FixedSizeCache<Character, Integer> cache = new LRUCache<>(5);

        for (char key = 'a'; key < 'f'; key++) {
            cache.put(key, (int)key);
        }

        assertThat(cache.size()).isEqualTo(5); // now cache contains 3 keys {a, b, c, d, e}

        for (char key = 'e'; key >= 'a'; key--) {
            cache.get(key);
        }

        cache.put('f', Integer.MAX_VALUE);

        for (char key = 'a'; key <= 'f'; key++) {
            assertThat(cache.size()).isEqualTo(5);
            if (key != 'e') {
                assertThat(cache.contains(key)).isTrue(); // cache still contains {a, b, c, d, e} and now {f}
            } else {
                assertThat(cache.contains(key)).isFalse(); // but doesnt have {e}
            }
        }
    }

    @Test
    public void testOtherMethods() {
        FixedSizeCache<Integer, Integer> cache = new LRUCache<>(10);

        for (int i = 0; i < 15; i++) {
            cache.put(i, i + 1);
            assertThat(cache.contains(i)).isTrue();
            assertThat(cache.get(i)).isEqualTo(i + 1);
        }

        assertThat(cache.size()).isEqualTo(10);

        for (int i = 0; i < 20; i++) {
            if (i < 5 || i >= 15) {
                assertThat(cache.get(i)).isNull();
                assertThat(cache.contains(i)).isFalse();
            } else {
                assertThat(cache.contains(i)).isTrue();
                assertThat(cache.get(i)).isEqualTo(i + 1);
            }
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