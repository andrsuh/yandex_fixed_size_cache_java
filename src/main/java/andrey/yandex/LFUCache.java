package andrey.yandex;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

/**
 * LFU cache implementation based on priority queue provides
 * O(log n) speed for get, put operations
 */

public class LFUCache<K, V> extends AbstractFixedSizeCache<K, V> {
    private final Map<K, Integer> frequencies = new HashMap<>(); // store key and its actually frequency
    private final Queue<Map.Entry<Integer, K>> frequenciesQueue;

    /**
     * Empty constructor set maximum cache capacity = DEFAULT_CAPACITY
     */

    public LFUCache() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Create priority queue will store key and its frequency
     * Queue provides O(1) speed for getting key with min. freq
     *
     * @param capacity maximum cache capacity
     */

    public LFUCache(int capacity) {
        super(new HashMap<>(), capacity);

        frequenciesQueue = new PriorityQueue<>(
                capacity, (o1, o2) -> o1.getKey().compareTo(o2.getKey())
        );
    }

    /**
     * In LFU pulling strategy every times we want to get value
     * associated with key we must increase key frequency.
     *
     * @inheritDoc
     */

    @Override
    public V get(K key) {
        lock.writeLock().lock();
        try {
            if (!cache.containsKey(key)) {
                misses.incrementAndGet();
                return null;
            }

            hits.incrementAndGet();

            final int currentFrequency = frequencies.get(key); // get actually frequency for key

            frequenciesQueue.remove(new SimpleEntry<>(currentFrequency, key)); // remove from queue old values
            frequenciesQueue.offer(new SimpleEntry<>(currentFrequency + 1, key)); // set actually values in queue
            frequencies.put(key, currentFrequency + 1); // and update it in out map

            return cache.get(key);
        } finally {
            lock.writeLock().unlock();
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
        lock.writeLock().lock();
        try {
            if (cache.size() >= capacity) {
                remove();
            }

            cache.put(key, value);
            frequencies.put(key, 1); // initially key has frequency == 1
            frequenciesQueue.offer(new SimpleEntry<>(1, key));
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void remove() {
        final Map.Entry<Integer, K> pair = frequenciesQueue.poll(); // get key with smallest freq.

        cache.remove(pair.getValue());
        frequencies.remove(pair.getValue());
    }
}
