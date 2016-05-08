package andrey.yandex;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by andrey on 06.05.16.
 */

public class AdvancedLFUCache<K, V> implements FixedSizeCache<K, V> {
    private final Map<K, Map.Entry<V, FreqNode<K>>> cache = new HashMap<>();
    private int capacity;
    private FreqNode<K> headFrequency;

    private AtomicLong hits = new AtomicLong(0);  // number of successful attempts to get value for a key
    private AtomicLong misses = new AtomicLong(0); // number of failed attempts

    private Lock lock = new ReentrantLock();

    public AdvancedLFUCache() {
        this(DEFAULT_CAPACITY);
    }

    public AdvancedLFUCache(int capacity) {
        this.capacity = capacity;
        headFrequency = new FreqNode<>();
    }

    @Override
    public V get(K key) {
        lock.lock();

        try {
            if (!cache.containsKey(key)) {
                misses.incrementAndGet();
                return null;
            }

            hits.incrementAndGet();

            Map.Entry<V, FreqNode<K>> value = cache.get(key);
            FreqNode<K> currentKeyFrequency = value.getValue();
            FreqNode<K> nextFrequency = currentKeyFrequency.next;

            if (nextFrequency == null || nextFrequency.value != currentKeyFrequency.value + 1) { // nextFrequency == headFrequency
                nextFrequency = new FreqNode<>(currentKeyFrequency.value + 1, currentKeyFrequency, nextFrequency);
            }
            nextFrequency.keys.add(key);

            Map.Entry<V, FreqNode<K>> newValue = new AbstractMap.SimpleEntry<>(value.getKey(), nextFrequency);
            cache.put(key, newValue);

            currentKeyFrequency.keys.remove(key);

            if (currentKeyFrequency.keys.isEmpty()) {
                currentKeyFrequency.removeNode();
            }

            return value.getKey();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            hits = new AtomicLong(0);
            misses = new AtomicLong(0);
            headFrequency = new FreqNode<>();
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                return;
            }

            if (cache.size() >= capacity) {
                remove();
            }

            FreqNode<K> frequency = headFrequency.next;
            if (frequency == null || frequency.value != 1) {
                frequency = new FreqNode<>(1, headFrequency, frequency);
            }

            frequency.keys.add(key);
            cache.put(key, new AbstractMap.SimpleEntry<>(value, frequency));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean contains(K key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
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
        headFrequency.next.keys.remove(v);
        cache.remove(v);

        if (headFrequency.next.keys.isEmpty()) {
            headFrequency.next.removeNode();
        }
    }

    @Override
    public long getHits() {
        return hits.longValue();
    }

    @Override
    public long getMisses() {
        return misses.longValue();
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
}
