package andrey.yandex;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by andrey on 06.05.16.
 */

public class AdvancedLFUCache<K, V> extends AbstractFixedSizeCache<K, V> {
    private final Map<K, Map.Entry<V, FreqNode<K>>> lfuCache = new HashMap<>();

    private FreqNode<K> headFrequency;

    public AdvancedLFUCache() {
        this(DEFAULT_CAPACITY);
    }

    public AdvancedLFUCache(int capacity) {
        super(null, capacity);
        headFrequency = new FreqNode<>();
    }

    @Override
    public V get(K key) {
        lock.writeLock().lock();

        try {
            if (!lfuCache.containsKey(key)) {
                misses.incrementAndGet();
                return null;
            }

            hits.incrementAndGet();

            Map.Entry<V, FreqNode<K>> value = lfuCache.get(key);
            FreqNode<K> currentKeyFrequency = value.getValue();
            FreqNode<K> nextFrequency = currentKeyFrequency.next;

            if (nextFrequency == null || nextFrequency.value != currentKeyFrequency.value + 1) { // nextFrequency == headFrequency
                nextFrequency = new FreqNode<>(currentKeyFrequency.value + 1, currentKeyFrequency, nextFrequency);
            }
            nextFrequency.keys.add(key);

            Map.Entry<V, FreqNode<K>> newValue = new AbstractMap.SimpleEntry<>(value.getKey(), nextFrequency);
            lfuCache.put(key, newValue);

            currentKeyFrequency.keys.remove(key);

            if (currentKeyFrequency.keys.isEmpty()) {
                currentKeyFrequency.removeNode();
            }

            return value.getKey();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            hits = new AtomicLong(0);
            misses = new AtomicLong(0);
            headFrequency = new FreqNode<>();
            lfuCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (lfuCache.containsKey(key)) {
                return;
            }

            if (lfuCache.size() >= capacity) {
                remove();
            }

            FreqNode<K> frequency = headFrequency.next;
            if (frequency == null || frequency.value != 1) {
                frequency = new FreqNode<>(1, headFrequency, frequency);
            }

            frequency.keys.add(key);
            lfuCache.put(key, new AbstractMap.SimpleEntry<>(value, frequency));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        return lfuCache.size();
    }

    private void remove() {
//        System.out.println("Size before: " + lfuCache.size());
        if (headFrequency.next == null) {
            return;
        }

        K v = null;
        for (K k : headFrequency.next.keys) { // its a bad way to get access to key with smallest freq
            v = k;
            break;
        }
//                System.out.println("Remove: " + v);
        headFrequency.next.keys.remove(v);
        lfuCache.remove(v);
//                System.out.println("Size after: " + lfuCache.size());

        if (headFrequency.next.keys.isEmpty()) {
            headFrequency.next.removeNode();
        }
    }

    private static class FreqNode<K> {
        public long value;
        public Set<K> keys;
        public FreqNode<K> prev;
        public FreqNode<K> next;

        public FreqNode() {
            value = 0;
            keys = new LinkedHashSet<>();
        }

        public FreqNode(long value, FreqNode<K> prev, FreqNode<K> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;

            keys = new LinkedHashSet<>();

            if (prev != null) {
                prev.next = this;
            }
            if (next != null) {
                next.prev = this;
            }
        }

        public void removeNode() {
            prev.next = next;
            next.prev = prev;
        }
    }

    public static int fibbWithCache(int n, AbstractFixedSizeCache<Integer, Integer> cache) {
        if (n < 2) {
            return 1;
        }

        if (cache.get(n) == null) {
            int a = fibbWithCache(n - 1, cache);
            int b = fibbWithCache(n - 2, cache);

            cache.put(n, a + b);
        }

        return cache.get(n);
    }

    public static int fibbWithoutCache(int n) {
        if (n < 2) {
            return 1;
        }

        int a = fibbWithoutCache(n - 1);
        int b = fibbWithoutCache(n - 2);

        return a + b;
    }

    public static void main(String[] args) {
        AdvancedLFUCache<Integer, Integer> cache = new AdvancedLFUCache<>(35);

        long start = System.currentTimeMillis();
        int res = fibbWithCache(40, cache);
        long end = System.currentTimeMillis();
        System.out.println("Execute time with caching ---> " + (end - start));

        System.out.println("Result --> " + res);

        start = System.currentTimeMillis();
        res = fibbWithoutCache(40);
        end = System.currentTimeMillis();
        System.out.println("Execute time without caching ---> " + (end - start));
        System.out.println("Result --> " + res);

    }
}
