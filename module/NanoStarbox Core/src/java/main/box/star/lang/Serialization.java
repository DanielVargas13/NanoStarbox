package box.star.lang;

import java.io.Serializable;

public interface Serialization {
  interface Loader<T> {
    T load(Serializable data);
  }
  interface Serializer<T> {
    byte[] serialize(T val);
  }
  interface Port<L extends Loader, S extends Serializer> {
    L getLoader();
    S getSerializer();
  }
}
