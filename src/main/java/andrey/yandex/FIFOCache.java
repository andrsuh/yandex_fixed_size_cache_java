package andrey.yandex;

import java.util.LinkedHashMap;
import java.util.Map;


public class FIFOCache<K, V> extends AbstractFixedSizeCache<K, V> {

    public FIFOCache() {
        this(DEFAULT_CAPACITY);
    }

    public FIFOCache(final int capacity) {
        super(
                new LinkedHashMap<K, V>(capacity) { // entries ordering by insert time
                    @Override
                    protected boolean removeEldestEntry(final Map.Entry eldest) {
                        return size() > capacity; // will remove eldest entry
                    }
                },
                capacity
        );
    }
}