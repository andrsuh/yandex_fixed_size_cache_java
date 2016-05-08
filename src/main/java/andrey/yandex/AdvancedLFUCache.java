package andrey.yandex;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LFU cache implementation based on double linked lists provides
 * O(1) speed for get, put operations
 */

public class AdvancedLFUCache<K, V> implements FixedSizeCache<K, V> {
    private final Map<K, Map.Entry<V, FreqNode<K>>> cache = new HashMap<>();
    private FreqNode<K> headFrequency = new FreqNode<>();
    private final int capacity;

    private final AtomicLong hits = new AtomicLong(0);  // number of successful attempts to get value for a key
    private final AtomicLong misses = new AtomicLong(0); // number of failed attempts

    private Lock lock = new ReentrantLock();

    /**
     * Empty constructor set maximum cache capacity = DEFAULT_CAPACITY
     */
    public AdvancedLFUCache() {
        this(DEFAULT_CAPACITY);
    }

    /**
     *
     * @param capacity maximum cache capacity
     */
    public AdvancedLFUCache(int capacity) {
        this.capacity = capacity;
    }

    /**
     * In LFU pulling strategy every times we want to get value
     * associated with key we must increase key frequency.
     *
     * @inheritDoc
     */
    @Override
    public V get(K key) {
        lock.lock();
        try {
            if (!cache.containsKey(key)) {
                misses.incrementAndGet();
                return null;
            }

            hits.incrementAndGet();

            final Map.Entry<V, FreqNode<K>> value = cache.get(key);
            final FreqNode<K> currentKeyFrequency = value.getValue();
            FreqNode<K> nextFrequency = currentKeyFrequency.next;

            if (nextFrequency == null || nextFrequency.value != currentKeyFrequency.value + 1) {
                nextFrequency = new FreqNode<>(currentKeyFrequency.value + 1, currentKeyFrequency, nextFrequency);
            }
            nextFrequency.keys.add(key);

            final Map.Entry<V, FreqNode<K>> newValue = new AbstractMap.SimpleEntry<>(value.getKey(), nextFrequency);
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

    /**
     * Before addition if current cache size great then maximal capacity
     * key with minimal frequency will remove
     *
     * @inheritDoc
     */
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
    public void clear() {
        lock.lock();
        try {
            hits.set(0);
            misses.set(0);
            headFrequency = new FreqNode<>();
            cache.clear();
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
