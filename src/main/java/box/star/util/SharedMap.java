package box.star.util;

import java.io.Closeable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared Map Prototype
 *
 * @param <K>
 * @param <V>
 */
public class SharedMap<K, V> extends ConcurrentHashMap<K, V> {

    private SharedMap<K, V> parent;

    public SharedMap() { super(); }

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

    @Override
    public V get(Object key) {
        V data = super.get(key);
        if (data == null && parent != null) return parent.get(key);
        return data;
    }

    private void exportKeySetView(KeySetView<K,V> child){
        KeySetView<K, V> ksv = super.keySet();
        for (K k:ksv){
            if (child.contains(k)) continue;
            child.add(k);
        }
        if (parent != null) parent.exportKeySetView(child);
    }

    @Override
    public KeySetView<K, V> keySet() {
        KeySetView<K, V> ksv = super.keySet();
        if (parent != null) parent.exportKeySetView(ksv);
        return ksv;
    }

    @Override
    public Enumeration<K> keys() {
        return new Enumeration<K>() {
            Iterator<K> iterator = keySet().iterator();
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            @Override
            public K nextElement() {
                return iterator.next();
            }
        };
    }

    public SharedMap<K, V> getLink(){
        SharedMap<K, V> map = new SharedMap<K, V>();
        map.parent = this;
        return map;
    }

}
