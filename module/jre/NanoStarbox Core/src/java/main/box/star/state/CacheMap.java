package box.star.state;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Runtime Object Cache capable of synchronizing with disk based cache services
 * through the {@link CacheMapMonitor} interface.
 *
 * @param <K>
 * @param <V>
 */
public class CacheMap<K, V> implements CacheMapMonitor<K, V> {

  protected final static CacheMapMonitor<Object, Object> defaultCacheMapMonitor = new CacheMapMonitor<Object, Object>() {
    @Override
    public void onCacheEvent(CacheEvent action, long timeStamp, Object key, Object value) {
    }
  };

  /**
   * Default: self-monitoring/null-driver
   */
  private CacheMapMonitor<K, V> cacheMapMonitor = (CacheMapMonitor<K, V>) defaultCacheMapMonitor;

  /**
   *
   * @param monitor
   * @throws IllegalStateException if monitor already set
   */
  public void setMonitor(CacheMapMonitor<K, V> monitor) {
    if (this.cacheMapMonitor != defaultCacheMapMonitor){
      throw new IllegalStateException("cache monitor already set");
    }
    this.cacheMapMonitor = monitor;
  }

  private long maxAge;
  private Map<K, Entry<V>> map;

  // Clear out old entries every few queries
  private int queryCount;
  private int queryOverflow = 300;
  private int MAX_ENTRIES = 200;

  private boolean updateExpirationByRequest;

  /**
   * Creates a shareable runtime state cache where entries expire after an elapsed time.
   *
   * When an entry expires it is removed from the cache.
   *
   * @param cacheDuration how long to keep items in the cache.
   * @param updateExpirationByRequest if true, updates the expiration timestamp for every get request.
   *
   */
  @SuppressWarnings("serial")
  public CacheMap(long cacheDuration, boolean updateExpirationByRequest) {
    this.maxAge = cacheDuration;
    this.updateExpirationByRequest = updateExpirationByRequest;

    map = new LinkedHashMap<K, Entry<V>>() {
      protected boolean removeEldestEntry(Map.Entry<K, Entry<V>> eldest) {
        return size() > MAX_ENTRIES;
      }
    };
  }

  public synchronized V get(K key) {
    if (++queryCount >= queryOverflow) {
      cleanup();
    }
    Entry<V> entry = entryFor(key);
    if (entry != null) {
      if (updateExpirationByRequest) {
        entry.setTimestamp(System.currentTimeMillis());
        cacheMapMonitor.onCacheEvent(CacheEvent.RENEW, entry.timestamp, key, entry.val);
      }
      return entry.val();
    }
    return null;
  }

  public synchronized void put(K key, V val) {
    if (++queryCount >= queryOverflow) {
      cleanup();
    }
    Entry<V> entry = entryFor(key);
    if (entry != null) {
      entry.setTimestamp(System.currentTimeMillis());
      entry.setVal(val);
      cacheMapMonitor.onCacheEvent(CacheEvent.UPDATE, entry.timestamp, key, val);
    } else {
      entry = new Entry<V>(System.currentTimeMillis(), val);
      map.put(key, entry);
      cacheMapMonitor.onCacheEvent(CacheEvent.CREATE, entry.timestamp, key, val);
    }
  }

  public synchronized void clear() {
    map.clear();
  }

  private Entry<V> entryFor(K key) {
    Entry<V> entry = map.get(key);
    if (entry != null) {
      long delta = System.currentTimeMillis() - entry.timestamp();
      if (delta < 0 || delta >= maxAge) {
        cacheMapMonitor.onCacheEvent(CacheEvent.EXPIRE, entry.timestamp, key, entry.val);
        map.remove(key);
        entry = null;
      }
    }
    return entry;
  }

  private void cleanup() {
    Set<K> keySet = map.keySet();
    // Avoid ConcurrentModificationExceptions
    Object[] keys = new Object[keySet.size()];
    int i = 0;
    for (K key : keySet) {
      keys[i++] = key;
    }
    for (int j = 0; j < keys.length; j++) {
      entryFor((K) keys[j]);
    }
    queryCount = 0;
  }

  private static class Entry<T> {

    private long timestamp;
    private T val;

    Entry(long timestamp, T val) {
      this.timestamp = timestamp;
      this.val = val;
    }

    long timestamp() { return timestamp; }

    void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    T val() {
      return val;
    }

    void setVal(T val) { this.val = val; }
  }

  public boolean containsKey(Object key) {return map.containsKey(key);}

  public synchronized V remove(Object key) {
    if (map.containsKey(key)) {
      Entry<V> entry = map.get(key);
      cacheMapMonitor.onCacheEvent(CacheEvent.REMOVE, entry.timestamp, (K)key, map.get(key).val);
      return map.remove(key).val;
    }
    return null;
  }

  @Override
  public void onCacheEvent(CacheEvent action, long timeStamp, K key, V value) {}

}
