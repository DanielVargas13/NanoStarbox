package box.star.bin.sh;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SharedMap<K,V> extends ConcurrentHashMap<K, V> {

  public SharedMap() {
    super();
  }

  public SharedMap(int initialCapacity) {
    super(initialCapacity);
  }

  public SharedMap(Map<? extends K, ? extends V> m) {
    super(m);
  }

  public SharedMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public SharedMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
    super(initialCapacity, loadFactor, concurrencyLevel);
  }

  public SharedMap<K, V> copy(){
    return new SharedMap<>(this);
  }

  public void merge(Map<K,V>locals){
    if (locals != null) putAll(locals);
  }

  public SharedMap<K, V> createLayer(SharedMap<K, V> top){
    SharedMap<K, V> copy = new SharedMap<>(this);
    copy.putAll(top);
    return copy;
  }

  final String[] compileEnvirons(){
    return compileEnvirons(null);
  }

  final String[] compileEnvirons(Map<? extends K, ? extends V> local) {
    Map<K, V> build = new HashMap<K, V>(this);
    if (local != null) build.putAll(local);
    Set<K> keys = build.keySet();
    String[] out = new String[keys.size()];
    int i = 0;
    for (K key : keys) {
      out[i++] = key + "=" + build.get(key);
    }
    return out;
  }

}
