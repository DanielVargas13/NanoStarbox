package box.star.state;

import box.star.io.Streams;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Runtime Object Cache
 *
 * Capable of synchronizing serializable values to disk.
 *
 * Capable of synchronizing with disk based cache services
 * through the {@link CacheMapMonitor} interface.
 *
 * @param <K> a serializable key
 * @param <V> an object
 */
public class CacheMap<K, V> implements CacheMapMonitor<K, V> {

  protected final static CacheMapMonitor<Object, Object> defaultCacheMapMonitor = new CacheMapMonitor<Object, Object>() {
    @Override
    public void onCacheEvent(CacheEvent action, long timeStamp, Object key, Object value) {}
  };

  /**
   * Default: self-monitoring/null-driver
   */
  private CacheMapMonitor<K, V> cacheMapMonitor = (CacheMapMonitor<K, V>) defaultCacheMapMonitor;
  private CacheMapLoader<K, V> cacheMapLoader;
  private File synchronizationFile;

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

  /**
   * Disk synchronization setup function.
   *
   * Make sure you set the monitor before calling this function, so you can
   * synchronize with external cache handlers during the cleanup phase.
   *
   * @param synchronization a file in which to synchronize data.
   * @param loader a class to transform non serializable values during load and save.
   */
  public void setSynchronization(File synchronization, CacheMapLoader<K, V> loader){
    if (cacheMapLoader != null)
      throw new IllegalStateException("cache map loader already set");
    cacheMapLoader = loader;
    synchronizationFile = synchronization;
    if (synchronizationFile.exists()){
      try {
        FileInputStream fis = new FileInputStream(synchronizationFile);
        map = loader.loadMap((Map<K, CacheMap.Entry<V>>) Streams.readSerializable(fis));
        fis.close();
        cleanup();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Run the synchronization function.
   *
   * Culls expired entries and optionally saves the data to disk in a reloadable
   * format.
   *
   * If there is no cache map loader set, then only expired entries
   * are culled from the cache.
   *
   * This feature does not notify the monitor of any events. It is assumed
   * that the monitor and loader are a feature-set, quite possibly the same
   * object.
   *
   */
  synchronized public void synchronize() {
    cleanup();
    if (cacheMapLoader == null) return;
    try {
      FileOutputStream os = new FileOutputStream(synchronizationFile);
      Streams.writeSerializable(os, cacheMapLoader.saveMap(map));
      os.close();
    } catch (Exception e){
      throw new RuntimeException(e);
    }
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
        cacheMapMonitor.onCacheEvent(CacheEvent.RENEW, entry.timestamp, key, entry.value);
      }
      return entry.value();
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
      entry.setValue(val);
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
        cacheMapMonitor.onCacheEvent(CacheEvent.EXPIRE, entry.timestamp, key, entry.value);
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

  /**
   * This class is serializable but it's values may not be. The {@link CacheMapLoader}
   * class allows transformation of the cache to a serializable format.
   * @param <T>
   */
  public static class Entry<T> implements Serializable {

    private static final long serialVersionUID = 6164182392342985833L;

    private long timestamp;
    private T value;

    private Entry(long timestamp, T value) {
      this.timestamp = timestamp;
      this.value = value;
    }

    long timestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public T value() {
      return value;
    }

    public void setValue(T val) { this.value = val; }

  }

  public boolean containsKey(Object key) {return map.containsKey(key);}

  public synchronized V remove(Object key) {
    if (map.containsKey(key)) {
      Entry<V> entry = map.get(key);
      cacheMapMonitor.onCacheEvent(CacheEvent.REMOVE, entry.timestamp, (K)key, map.get(key).value);
      return map.remove(key).value;
    }
    return null;
  }

  @Override
  public void onCacheEvent(CacheEvent action, long timeStamp, K key, V value) {}

}
