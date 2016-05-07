import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 05.05.16.
 */
public class Main {

    private static class Test implements Runnable {
        private long threadNum;
        final private FixedSizeCache cache;

        public Test(long threadNum, FixedSizeCache cache) {
            this.threadNum = threadNum;
            this.cache = cache;
        }

        @Override
        public void run() {
            int result = fibbWithCache(70, cache);
//            System.out.println("Thread: " + threadNum + " result: " + result);
        }


        public int fibbWithCache(int n, FixedSizeCache<Integer, Integer> cache) {
            if (n < 2) {
                return 1;
            }

            Integer result;
            if ((result = cache.get(n)) == null) {
                int a = fibbWithCache(n - 1, cache);
                int b = fibbWithCache(n - 2, cache);

                cache.put(n, a + b);

                return a + b;
            } else {
                return result;
            }

        }
    }

    public static int cachingFibb(int n, FixedSizeCache<Integer, Integer> cache) {
        if (n < 2) {
            return 1;
        }

        Integer result;
        if ((result = cache.get(n)) == null) {
            int a = cachingFibb(n - 1, cache);
            int b = cachingFibb(n - 2, cache);

            cache.put(n, a + b);

            return a + b;
        } else {
            return result;
        }
    }


    public int fibbWithoutCache(int n) {
        if (n < 2) {
            return 1;
        }

        int a = fibbWithoutCache(n - 1);
        int b = fibbWithoutCache(n - 2);

        return a + b;
    }

    public static void testCache(FixedSizeCache cache) {
        long start = System.currentTimeMillis();
        int res = cachingFibb(70, cache);
        long end = System.currentTimeMillis();
        System.out.println("Execute time with caching ---> " + (end - start));

        System.out.println("Result --> " + res);

    }

    public static void test(FixedSizeCache cache) {
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Thread newThread = new Thread(new Test(i, cache));
            threadList.add(newThread);
            newThread.start();
        }

        threadList.stream()
                .forEach(thread -> {
                    try {
                        thread.join();
                    } catch(InterruptedException e){}
                });

    }

    public static void main(String ... args) {
        LRUCache<Integer, Integer> lrucache = new LRUCache<>(35);
        FIFOCache<Integer, Integer> fifocache = new FIFOCache<>(35);
        LFUCache<Integer, Integer> lfucache = new LFUCache<>(35);
        AdvancedLFUCache<Integer, Integer> alfucache = new AdvancedLFUCache<>(35);

//        testCache(lrucache);
        test(lrucache);
        System.out.println("LRU: hits - " + lrucache.getHits() + " misses - " + lrucache.getMisses());

//        testCache(fifocache);
        test(fifocache);
        System.out.println("FIFO: hits - " + fifocache.getHits() + " misses - " + fifocache.getMisses());

//        testCache(alfucache);
        test(alfucache);
        System.out.println("AdvLFU: hits - " + alfucache.getHits() + " misses - " + alfucache.getMisses());

//        testCache(lfucache);
        test(lfucache);
        System.out.println("LFU: hits - " + lfucache.getHits() + " misses - " + lfucache.getMisses());


    }
}
