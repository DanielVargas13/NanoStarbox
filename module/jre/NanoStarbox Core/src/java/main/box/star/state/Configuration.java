package box.star.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration<K extends Serializable, V extends Serializable> implements Serializable {

  private static final long serialVersionUID = 6990681513366432002L;
  
  private ConcurrentHashMap<K, V> map;

  private void init(){ map = new ConcurrentHashMap<>(); }

  {
    init();
  }

  private String name;
  public Configuration(String name){this.name = name;}

  public String toString(){return this.name;}

  public void set(K k, V v){ map.put(k, v); }

  public <ANY> ANY get(K k){ return (ANY) map.get(k); }

  public int size() {return map.size();}

  public void addAll(Map<K, V> map){
    for (K k:map.keySet()) this.map.put(k, map.get(k));
  }

  public boolean isEmpty() {return map.isEmpty();}

  public boolean containsKey(K key) {return map.containsKey(key);}

  public boolean containsValue(V value) {return map.containsValue(value);}

  public V remove(K key) {return map.remove(key);}

  public void clear() {map.clear();}

  public List<K> keyList() {return new ArrayList<K>(map.keySet());}

  public Collection<V> values() {return map.values();}

  public V putIfAbsent(K key, V value) {return map.putIfAbsent(key, value);}

  public V getOrDefault(K key, V defaultValue) {return map.getOrDefault(key, defaultValue);}

}
