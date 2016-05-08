package andrey.yandex;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by andrey on 08.05.16.
 */

public class LRUCacheTest {

    @Test
    public void lru() throws Exception {
        LRUCache<Integer, Integer> lru = new LRUCache<>(35);
        lru.put(1, 1);
        int a = lru.get(1);
        assertEquals(1, a);
    }

    @Test
    public void get() throws Exception {
        LRUCache<Integer, Integer> lru = new LRUCache<>(3);
        lru.put(1, 1);
        lru.put(2, 5);
        lru.put(3, 9);
        lru.get(2);
        lru.get(3);
        lru.get(1);
        lru.put(4, 11);
        lru.get(2);
        assertEquals(1, lru.getMisses());
    }
}