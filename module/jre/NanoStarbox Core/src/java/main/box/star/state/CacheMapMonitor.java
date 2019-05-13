package box.star.state;

/**
 * Provides a way to synchronize operations with external cache management
 * tools.
 *
 * @param <K>
 * @param <V>
 */
public interface CacheMapMonitor<K, V> {
    enum CacheEvent {
        CREATE, UPDATE, EXPIRE, REMOVE, RENEW
    }
    void onCacheEvent(CacheEvent action, long timeStamp, K key, V value);
}
