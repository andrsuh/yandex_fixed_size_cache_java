package andrey.yandex;

/**
 * Base interface for each cache
 *
 * @param <K> Key type
 * @param <V> Value type
 */

public interface FixedSizeCache<K, V> {
    int DEFAULT_CAPACITY = 64;

    /**
     * @param key the key whose associated value is to be returned
     * @return the value to specified key,
     * or null if this cache does't contain key.
     */
    V get(K key);

    /**
     * Associates the specified value with the specified key in this cache
     * If the map previously contained key, the prev value is not replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    void put(K key, V value);

    /**
     * @return the number of key-value in this cache
     */
    int size();

    /**
     * @param key key whose presence in this cache is to be tested
     * @return true if this cache contains a specified key
     */
    boolean contains(K key);

    /**
     * Removes all key-value from this cache. Set all counts (hits, misses) = zero
     */
    void clear();

    /**
     * @return number of successful attempts to get value for a key
     */
    long getHits();

    /**
     * @return number of failed attempts to get value for a key
     */
    long getMisses();
}
