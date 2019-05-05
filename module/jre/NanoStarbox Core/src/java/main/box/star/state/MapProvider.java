package box.star.state;

import java.util.Map;

public interface MapProvider<K, V> {
  Map<K, V> createMap();
}
