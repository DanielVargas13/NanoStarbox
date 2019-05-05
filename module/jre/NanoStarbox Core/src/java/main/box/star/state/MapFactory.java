package box.star.state;

import java.io.Serializable;
import java.util.Map;

public interface MapFactory<K extends Serializable, V> {
  Map<K, V> createMap();
}
