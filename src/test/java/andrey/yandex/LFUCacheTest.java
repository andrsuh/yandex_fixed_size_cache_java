package andrey.yandex;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by andrey on 08.05.16.
 */

public class LFUCacheTest {
    @Test
    public void put() throws Exception {
        LFUCache<Integer, Integer> lfu = new LFUCache<>(35);
        lfu.put(1, 1);
        int a = lfu.get(1);
        assertEquals(1, a);
    }

//    @Test
//    public void removeTheLeastFrequent() throws Exception {
//        LFUCache<Integer, Integer> lfu = new LFUCache<>(35);
//        lfu.put(1, 1);
//        lfu.put(2, 5);
//        lfu.put(3, 9);
//        lfu.put(4, 11);
//        lfu.get(1);
//        lfu.get(2);
//        lfu.get(1);
//        lfu.get(3);
//        lfu.get(3);
////        lfu.removeTheLeastFrequent();
//        lfu.get(4);
//        assertEquals(1, lfu.getMisses());
//    }

    @Test
    public void get() throws Exception {
        LFUCache<Integer, Integer> lfu = new LFUCache<>(3);
        lfu.put(1, 1);
        lfu.put(2, 5);
        lfu.put(3, 9);
        lfu.get(1);
        lfu.get(2);
        lfu.get(1);
        lfu.get(3);
        lfu.get(3);
        lfu.put(4, 11);
        lfu.get(2);
        assertEquals(1, lfu.getMisses());
    }

    @Test
    public void get2() throws Exception {
        LFUCache<Integer, Integer> lfu = new LFUCache<>(3);
        lfu.put(1, 1);
        lfu.put(2, 5);
        lfu.put(3, 9);
        lfu.put(4, 11);
        lfu.get(1);
        assertEquals(1, lfu.getMisses());
    }

}