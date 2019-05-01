package box.star;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Settings implements Serializable {

  private static final long serialVersionUID = -1068114640999220388L;

  private ConcurrentHashMap<Enum, Serializable> map;

  private void init(){ map = new ConcurrentHashMap<>(); }

  {
    init();
  }

  public Settings(){}

  public void set(Enum k, Serializable v){ map.put(k, v); }

  public <ANY> ANY get(Enum k){ return (ANY) map.get(k); }

  public int size() {return map.size();}

  public boolean isEmpty() {return map.isEmpty();}

  public boolean containsKey(Enum key) {return map.containsKey(key);}

  public boolean containsValue(Serializable value) {return map.containsValue(value);}

  public Serializable remove(Enum key) {return map.remove(key);}

  public void clear() {map.clear();}

  public ConcurrentHashMap.KeySetView<Enum, Serializable> keySet() {return map.keySet();}

  public Collection<Serializable> values() {return map.values();}

  public Serializable putIfAbsent(Enum key, Serializable value) {return map.putIfAbsent(key, value);}

  public Serializable getOrDefault(Enum key, Serializable defaultValue) {return map.getOrDefault(key, defaultValue);}

}
