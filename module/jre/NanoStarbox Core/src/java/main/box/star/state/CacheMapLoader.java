package box.star.state;

import java.io.Serializable;
import java.util.Map;

/**
 * The key points to note about the cache map loader is that,
 * primarily your task is to create a serializable version of
 * any {@link CacheMap.Entry#value()} that is not serializable,
 * and to inflate these objects when the cache is loaded from a
 * serializable source.
 * @param <K>
 * @param <V>
 */
public class CacheMapLoader<K, V> {

  /**
   * Convert each map Entry.value to serializable, and return the map.
   *
   * @param map
   * @return
   */
  public Serializable saveMap(Map<K, CacheMap.Entry<V>>map){
    return null;
  }

  /**
   * Conver each map Entry.value to runtime object form, and return the map.
   * @param output
   * @return
   */
  Map<K, CacheMap.Entry<V>> loadMap(Map<K, CacheMap<K, V>> output) {
    return null;
  };

}
