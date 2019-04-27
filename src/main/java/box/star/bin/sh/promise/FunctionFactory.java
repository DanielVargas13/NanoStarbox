package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;

public interface FunctionFactory<Host> {
  String getName();
  boolean match(String name);
  FactoryFunction createInstance(Host host, SharedMap<String, String> locals);
}
