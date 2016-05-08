package andrey.yandex;

import java.util.LinkedHashMap;
import java.util.Map;


public class LRUCache<K, V> extends AbstractFixedSizeCache<K, V> {
    public LRUCache() {
        this(DEFAULT_CAPACITY);
    }

    public LRUCache(int capacity) {
        super(
                new LinkedHashMap<K, V>(capacity, 0.75f, true) {
                    // the third parameter == true sets order which map entries were last accessed
                    // from least-recently accessed to most-recently (access-order)
                    @Override
                    protected boolean removeEldestEntry(final Map.Entry eldest) {
                        return size() > capacity; // will remove last accessed entry
                    }
                },
                capacity
        );
    }
}
