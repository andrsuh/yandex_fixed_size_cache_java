package andrey.yandex;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by andrey on 04.05.16.
 */
public class FIFOCache<K, V> extends AbstractFixedSizeCache<K, V> {

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