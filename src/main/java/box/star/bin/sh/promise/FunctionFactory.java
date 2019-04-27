package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;

public interface FunctionFactory<Host extends ShellHost> {
  String getName();
  boolean matchName(String name);
  FactoryFunction createFunction(Host host, SharedMap<String, String> locals);
}
