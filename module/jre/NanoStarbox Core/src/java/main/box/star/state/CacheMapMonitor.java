package box.star.state;

/**
 * Provides a way to synchronize operations with external cache management
 * tools.
 *
 * @param <K>
 * @param <V>
 */
public interface CacheMapMonitor<K, V> {
    void onCreate(K key, V value);
    void onUpdate(K key, V value);
    void onExpire(K key, V value);
    void onRemove(K key, V value);
    void onRenew(K key, V value);
}
