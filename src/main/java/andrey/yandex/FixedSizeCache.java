package andrey.yandex;

/**
 * Created by andrey on 08.05.16.
 */

public interface FixedSizeCache<K, V> {
    int DEFAULT_CAPACITY = 64;

    V get(K key);
    void put(K key, V value);
    int size();
    boolean contains(K key);
    void clear();
    long getHits();
    long getMisses();
}
