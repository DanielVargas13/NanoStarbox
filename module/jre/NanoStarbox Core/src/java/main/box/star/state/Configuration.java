package box.star.state;

import box.star.contract.NotNull;
import box.star.contract.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration<K extends Serializable, V extends Serializable> implements Serializable {
  private static final long serialVersionUID = 6990681513366432002L;

  public static class Entry<V> implements Serializable {
    private static final long serialVersionUID = 7036263767117650059L;
    private boolean enumerable = true, writable = true, configurable = true;
    private long creationTime, modificationTime;
    private V value;
    public Entry(V value){
      this.creationTime = this.modificationTime = System.currentTimeMillis();
      this.value = value;
    }
    public void setConfigurable(boolean configurable) {
      this.update();
      this.configurable = configurable;
    }
    public Class getValueClass(){
      return value.getClass();
    }
    public long getCreationTime() {
      return creationTime;
    }
    public long getModificationTime() {
      return modificationTime;
    }
    public boolean isConfigurable() {
      return configurable;
    }
    public void setEnumerable(boolean enumerable) {
      this.update();
      this.enumerable = enumerable;
    }
    public boolean isEnumerable() {
      return enumerable;
    }
    public void setWritable(boolean writable) {
      this.update();
      this.writable = writable;
    }
    public boolean isWritable() {
      return writable;
    }
    private void update(){
      this.modificationTime = System.currentTimeMillis();
    }
    public void setValue(V value) {
      this.update();
      this.value = value;
    }
    public V getValue() {
      return value;
    }
  }

  public static class Manager<K extends Serializable, V extends Serializable> implements Serializable {
    private static final long serialVersionUID = -7347640081767571074L;
    private ConcurrentHashMap<K, Entry<V>> map;
    private String name;
    private Configuration<K, V>parent;
    public Manager(String name){
      this.name = name;
      map = new ConcurrentHashMap<>();
    }
    public Manager(String name, Configuration<K, V>parent){
      this(name);
      this.parent = parent;
    }
    public Entry<V> get(K key){
      return map.get(key);
    }
    public Entry<V> create(V value){
      return new Entry<>(value);
    }
    public void set(K key, V value){
      map.put(key, create(value));
    }
    public void set(K key, Entry<V>entry){
      map.put(key, entry);
    }
    public Entry<V> delete(K key){
      return map.remove(key);
    }
    public Configuration<K, V> getConfiguration(){
      Configuration<K, V> configuration = new Configuration<>(this);
      return configuration;
    }
    public boolean containsKey(K key) {
      return map.containsKey(key);
    }
    public List<K> keyList(){
      return new ArrayList<>(map.keySet());
    }
    public List<K> resolveKeys() {
      List<K> keys = new ArrayList<>();
      resolveKeys(keys);
      return keys;
    }
    private void resolveKeys(List<K>keyList){
      for (K key: map.keySet()){
        if (! keyList.contains(key)) {
          Entry<V> e = map.get(key);
          if (e.isEnumerable()) keyList.add(key);
        }
      }
      if (parent != null) parent.manager.resolveKeys(keyList);
    }
    private boolean resolveKey(K key){
      if (map.containsKey(key)) return true;
      else if (parent != null) return parent.manager.resolveKey(key);
      else return false;
    }
    private Entry<V> resolve(K key) {
      if (map.containsKey(key)) return map.get(key);
      else if (parent != null) return parent.manager.resolve(key);
      else return null;
    }
    public Entry<V> remove(K key){
      if (map.containsKey(key)) {
        Entry<V> e = map.get(key);
        if (e.isConfigurable()) {
          map.remove(key);
          return e;
        }
        return null;
      }
      if (parent != null) return parent.manager.remove(key);
      return null;
    }
    public Configuration<K, V> getParent() {
      return parent;
    }
  }

  private Manager<K, V> manager;

  Configuration(@NotNull Manager<K, V> manager){
    this.manager = manager;
  }

  public String toString(){return this.manager.name;}

  public void set(@NotNull K k, @Nullable V v){
    Entry<V> entry = manager.resolve(k);
    if (entry != null) {
      if (entry.isWritable()) entry.setValue(v);
    } else {
      manager.map.put(k, new Entry<>(v));
    }
  }

  @Nullable public Class classOf(@NotNull K k){
    Entry<V> entry = manager.resolve(k);
    if (entry != null) return entry.getValueClass();
    else return null;
  }

  @Nullable public <ANY> ANY get(@NotNull K k){
    Entry<V> e = manager.resolve(k);
    if (e == null) return null;
    return (ANY) e.getValue();
  }

  public int size() {return keyList().size();}

  public void addAll(@NotNull Map<K, V> map){
    for (K k:map.keySet()) set(k, map.get(k));
  }

  public boolean isEmpty() {return keyList().size() == 0;}

  public boolean containsKey(@NotNull K key) {
    return manager.resolveKey(key);
  }

  @Nullable public V remove(@NotNull K key) {
    Entry<V> e = manager.remove(key);
    if (e != null){ return e.getValue(); }
    else return null;
  }

  public void clear() {
    for (K k:keyList()) remove(k);
  }

  @NotNull public List<K> keyList() {
    return manager.resolveKeys();
  }

  @NotNull public List<V> valueList() {
    List<K> all = keyList();
    List<V>out = new ArrayList<>();
    for(K k: all) {
      Entry<V> e = manager.resolve(k);
      out.add(e.getValue());
    }
    return out;
  }

}
