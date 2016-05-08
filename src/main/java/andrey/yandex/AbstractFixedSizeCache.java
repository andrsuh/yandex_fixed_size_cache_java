package andrey.yandex;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by andrey on 03.05.16.
 */

public abstract class AbstractFixedSizeCache<K, V> implements FixedSizeCache<K, V> {
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected static final int DEFAULT_CAPACITY = 64;

    protected final Map<K, V> cache;
    protected final int capacity;

    protected AtomicLong hits = new AtomicLong(0);  // number of successful attempts to get value for a key
    protected AtomicLong misses = new AtomicLong(0); // number of failed attempts


    protected AbstractFixedSizeCache(int capacity) {
        this(null, capacity);
    }

    protected AbstractFixedSizeCache(Map<K, V> cache) {
        this(cache, DEFAULT_CAPACITY);
    }

    protected AbstractFixedSizeCache(Map<K, V> cache, int capacity) {
        this.cache = cache;
        this.capacity = capacity;
    }

    @Override
    public V get(K key) {
        lock.readLock().lock();
        try {
            if (cache.containsKey(key)) {
                hits.incrementAndGet();
                return cache.get(key);
            }

            misses.incrementAndGet();
            return null;
        } finally {
            lock.readLock().unlock();
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
            hits = new AtomicLong(0);
            misses = new AtomicLong(0);
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
