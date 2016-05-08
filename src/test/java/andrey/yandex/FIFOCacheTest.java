package andrey.yandex;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by andrey on 08.05.16.
 */
public class FIFOCacheTest {
    @Test
    public void lru() throws Exception {
        FIFOCache<Integer, Integer> fifo = new FIFOCache<>(35);
        fifo.put(1, 1);
        int a = fifo.get(1);
        assertEquals(1, a);
    }

    @Test
    public void get() throws Exception {
        FIFOCache<Integer, Integer> fifo = new FIFOCache<>(3);
        fifo.put(1, 1);
        fifo.put(2, 5);
        fifo.put(3, 9);
        fifo.get(1);
        fifo.get(2);
        fifo.get(1);
        fifo.get(2);
        fifo.put(4, 11);
        fifo.get(1);
        assertEquals(1, fifo.getMisses());
    }

}