package box.star.state;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>An implementation of map for storing {@link Enum enum} keyed objects</p>
 * <br>
 * <p>Features object class introspection and enum mash-ups using
 * an {@link IdentityHashMap}.</p>
 * <br>
 */
public class MachineStorage implements Map<Enum, Object> {

  IdentityHashMap<Enum, Object> objectTable = new IdentityHashMap<>();

  @Override
  public boolean equals(Object o) {return objectTable.equals(o);}

  @Override
  public int hashCode() {return objectTable.hashCode();}

  @Override
  public Object get(Object key) {return objectTable.get(key);}

  public <T extends Object> T get(Class<T> type, Enum key){
    return type.cast(get(key));
  }

  public boolean isObjectOfType(Class type, Enum key){
    return type.isInstance(get(key));
  }

  public Class getType(Enum key){
    Object val = get(key);
    if (val == null) return null;
    return val.getClass();
  }

  @Override
  public Object getOrDefault(Object key, Object defaultValue) {return objectTable.getOrDefault(key, defaultValue);}

  public int size() {return objectTable.size();}

  public boolean isEmpty() {return objectTable.isEmpty();}

  public boolean containsValue(Object value) {return objectTable.containsValue(value);}

  public boolean containsKey(Object key) {return objectTable.containsKey(key);}

  public Object remove(Object key) {return objectTable.remove(key);}

  public void clear() {objectTable.clear();}

  public Set<Enum> keySet() {return objectTable.keySet();}

  public Set<Map.Entry<Enum, Object>> entrySet() {return objectTable.entrySet();}

  public Collection<Object> values() {return objectTable.values();}

  public Object replace(Enum key, Object value) {return objectTable.replace(key, value);}

  public Object put(Enum key, Object value) {return objectTable.put(key, value);}

  public void putAll(Map<? extends Enum, ?> t) {objectTable.putAll(t);}

  public void forEach(BiConsumer<? super Enum, ? super Object> action) {objectTable.forEach(action);}

  public void replaceAll(BiFunction<? super Enum, ? super Object, ?> function) {objectTable.replaceAll(function);}

  public Object putIfAbsent(Enum key, Object value) {return objectTable.putIfAbsent(key, value);}

  public boolean replace(Enum key, Object oldValue, Object newValue) {return objectTable.replace(key, oldValue, newValue);}

  public Object computeIfAbsent(Enum key, Function<? super Enum, ?> mappingFunction) {return objectTable.computeIfAbsent(key, mappingFunction);}

  public Object computeIfPresent(Enum key, BiFunction<? super Enum, ? super Object, ?> remappingFunction) {return objectTable.computeIfPresent(key, remappingFunction);}

  public Object compute(Enum key, BiFunction<? super Enum, ? super Object, ?> remappingFunction) {return objectTable.compute(key, remappingFunction);}

  public Object merge(Enum key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {return objectTable.merge(key, value, remappingFunction);}


}
