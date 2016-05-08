package andrey.yandex;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

/**
 * Created by andrey on 04.05.16.
 */

public class LFUCache<K, V> extends AbstractFixedSizeCache<K, V> {
    private final Map<K, Integer> frequencies = new HashMap<>(); // store key and its actually frequency
    private final Queue<Map.Entry<Integer, K>> frequencies_queue;

    public LFUCache() {
        this(DEFAULT_CAPACITY);
    }

    public LFUCache(int capacity) {
        super(new HashMap<>(), capacity);

        frequencies_queue = new PriorityQueue<>(
                capacity, (o1, o2) -> o1.getKey().compareTo(o2.getKey()) // will store key and its freq. sorted by freq. min
        );
    }

    @Override
    public V get(K key) {
        lock.writeLock().lock();
        try {
            if (!cache.containsKey(key)) {
                misses.incrementAndGet();
                return null;
            }

            hits.incrementAndGet();

            int currentFrequency = frequencies.get(key); // get actually frequency for key

            frequencies_queue.remove(new SimpleEntry<>(currentFrequency, key)); // remove from queue old values
            frequencies_queue.offer(new SimpleEntry<>(currentFrequency + 1, key)); // set actually values in queue
            frequencies.put(key, currentFrequency + 1); // and update it in out map

            return cache.get(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (cache.size() >= capacity) {
                remove();
            }

            cache.put(key, value);
            frequencies.put(key, 1); // initially key has frequency == 1
            frequencies_queue.offer(new SimpleEntry<>(1, key));
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void remove() {
        Map.Entry<Integer, K> pair = frequencies_queue.poll(); // get key with smallest freq.

        cache.remove(pair.getValue());
        frequencies.remove(pair.getValue());
    }
}
