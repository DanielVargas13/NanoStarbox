package box.star.lang;

import java.lang.reflect.Method;

public interface Reflector {
  interface Method {
    interface PublicAccessPort {
      java.lang.reflect.Method queryMethod(String name, Class... prototype);
    }
  }
}
