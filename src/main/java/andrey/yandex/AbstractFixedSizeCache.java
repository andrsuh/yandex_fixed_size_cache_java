package andrey.yandex;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base class for LRUCache, LFUCache, FIFOCache
 */

public abstract class AbstractFixedSizeCache<K, V> implements FixedSizeCache<K, V> {
    protected static final int DEFAULT_CAPACITY = 64;

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Map<K, V> cache;
    protected final int capacity;
    protected final AtomicLong hits = new AtomicLong(0);  // number of successful attempts to get value for a key
    protected final AtomicLong misses = new AtomicLong(0); // number of failed attempts


    protected AbstractFixedSizeCache(Map<K, V> cache, int capacity) {
        this.cache = cache;
        this.capacity = capacity;
    }

    @Override
    public V get(K key) {
        lock.writeLock().lock();
        try {
            if (cache.containsKey(key)) {
                hits.incrementAndGet();
                return cache.get(key);
            }

            misses.incrementAndGet();
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(K key) {
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            hits.set(0);
            misses.set(0);
            cache.clear();
        } finally {
            lock.writeLock().unlock();
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
}
