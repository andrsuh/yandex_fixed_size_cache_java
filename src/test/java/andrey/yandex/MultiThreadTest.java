package andrey.yandex;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

public class MultiThreadTest {
    private static final long parameter = 30;
    private static CountDownLatch latch;
    private static long correctResult;
    private final List<CalculateFibonacci> threads = new ArrayList<>();

    @BeforeClass
    public static void init() {
        latch = new CountDownLatch(100);
        correctResult = uncachedFibonacci(parameter);
    }

    private static long uncachedFibonacci(long n) {
        if (n < 2) {
            return 1;
        }
        return uncachedFibonacci(n - 1) + uncachedFibonacci(n - 2);
    }

    @Test
    public void testLRUCache() {
        FixedSizeCache<Long, Long> cache = new LRUCache<>(20);
        assertThat(test(cache)).isTrue();
        assertThat(cache.getHits()).isGreaterThan(0);
        System.out.println(cache.size());
        assertThat(cache.size()).isLessThanOrEqualTo(20);
    }

    @Test
    public void testLFUCache() {
        FixedSizeCache<Long, Long> cache = new LFUCache<>(20);
        assertThat(test(cache)).isTrue();
        assertThat(cache.getHits()).isGreaterThan(0);
        assertThat(cache.size()).isLessThanOrEqualTo(20);
    }

    @Test
    public void testFIFOCache() {
        FixedSizeCache<Long, Long> cache = new FIFOCache<>(20);
        assertThat(test(cache)).isTrue();
        assertThat(cache.getHits()).isGreaterThan(0);
        assertThat(cache.size()).isLessThanOrEqualTo(20);
    }

    @Test
    public void testAdvancedLFUCache() {
        FixedSizeCache<Long, Long> cache = new AdvancedLFUCache<>(20);
        assertThat(test(cache)).isTrue();
        assertThat(cache.getHits()).isGreaterThan(0);
        assertThat(cache.size()).isLessThanOrEqualTo(20);    }

    private boolean test(FixedSizeCache<Long, Long> cache) {
        for (int i = 0; i < 100; i++) {
            CalculateFibonacci thread = new CalculateFibonacci(parameter, cache);
            threads.add(thread);
            thread.start();
        }

        boolean result = true;
        for (CalculateFibonacci thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            result = result & (thread.getResult() == correctResult);
        }

        return result;
    }

    private static class CalculateFibonacci extends Thread {
        private final FixedSizeCache<Long, Long> cache;
        private long parameter;
        private long result;

        private CalculateFibonacci(long parameter, FixedSizeCache cache) {
            this.parameter = parameter;
            this.cache = cache;
        }

        private long getResult() {
            return result;
        }

        @Override
        public void run() {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            result = cachedFibonacci(parameter);
        }

        private long cachedFibonacci(long n) {
            if (n < 2) {
                return 1;
            }

            Long result;
            if ((result = cache.get(n)) == null) {
                long a = cachedFibonacci(n - 1);
                long b = cachedFibonacci(n - 2);

                cache.put(n, a + b);

                return a + b;
            } else {
                return result;
            }
        }
    }
}
