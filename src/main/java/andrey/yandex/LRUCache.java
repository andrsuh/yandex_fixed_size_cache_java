import java.util.*;

/**
 * Created by andrey on 03.05.16.
 */
public class LRUCache<K, V> extends FixedSizeCache<K, V> {

    public LRUCache(int capacity) {
        super(
            new LinkedHashMap<K, V>(capacity, 0.75f, true) {
                // the third parameter == true sets order which map entries were last accessed
                // from least-recently accessed to most-recently (access-order)
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldrest) {
                    return size() > capacity; // will remove last accessed entry
                }
            },
            capacity
        );
    }
}
