import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by andrey on 03.05.16.
 */
public abstract class FixedSizeCache <K, V> {
    private static final int DEFAULT_CAPACITY = 64;

    protected final Map<K, V> cache;
    protected final int capacity;

    protected AtomicLong hits = new AtomicLong(0);  // number of successful attempts to get value for a key
    protected AtomicLong misses = new AtomicLong(0); // number of failed attempts

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FixedSizeCache(int capacity) {
        this(null, capacity);
    }

    public FixedSizeCache(Map<K, V> cache) {
        this(cache, DEFAULT_CAPACITY);
    }

    public FixedSizeCache(Map<K, V> cache, int capacity) {
        this.cache = cache;
        this.capacity = capacity;
    }

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

    public void put(K key, V value){
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean contains(K key) {
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

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

    public long getHits() {
        return hits.longValue();
    }

    public long getMisses() {
        return misses.longValue();
    }
}
