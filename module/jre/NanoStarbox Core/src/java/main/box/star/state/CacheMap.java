package box.star.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CacheMap<K, V> {

  private long millisUntilExpiration;
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
    this.millisUntilExpiration = cacheDuration;
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
      if (updateExpirationByRequest) entry.setTimestamp(System.currentTimeMillis());
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
    } else {
      map.put(key, new Entry<V>(System.currentTimeMillis(), val));
    }
  }

  public synchronized void clear() {
    map.clear();
  }

  private Entry<V> entryFor(K key) {
    Entry<V> entry = map.get(key);
    if (entry != null) {
      long delta = System.currentTimeMillis() - entry.timestamp();
      if (delta < 0 || delta >= millisUntilExpiration) {
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

  public synchronized Entry<V> remove(Object key) {return map.remove(key);}

}
