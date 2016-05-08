package andrey.yandex;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class AdvancedLFUCacheTest {

    @Test
    public void testDefaultConstructor() {
        FixedSizeCache<String, Integer> cache = new AdvancedLFUCache<>();

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testCapacityConstructor() {
        FixedSizeCache<String, Integer> cache = new AdvancedLFUCache<>(2);

        assertThat(cache.size()).isEqualTo(0);
        assertThat(cache.get("a")).isNull();
        assertThat(cache.contains("a")).isFalse();
        assertThat(cache.getHits()).isEqualTo(0);
        assertThat(cache.getMisses()).isEqualTo(1);
    }

    @Test
    public void testElementsPullingPolicy() {
        FixedSizeCache<Character, Integer> cache = new AdvancedLFUCache<>(5);

        for (char key = 'a'; key < 'f'; key++) {
            cache.put(key, (int) key);
        }

        assertThat(cache.size()).isEqualTo(5); // now cache contains 5 keys {a, b, c, d, e}

        cache.put('f', Integer.MAX_VALUE); // before insert we will remove eldest key ('a')
        cache.put('g', Integer.MAX_VALUE); // remove 'b' key

        for (char key = 'c'; key <= 'g'; key++) {
            assertThat(cache.contains(key)).isTrue(); // cache still contains {b, c, d, e} and now {f}
        }
    }

    @Test
    public void testOtherMethods() {
        FixedSizeCache<Integer, Integer> cache = new AdvancedLFUCache<>(10);

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