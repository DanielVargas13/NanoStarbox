package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;

public interface FunctionFactory {
  String getName();
  boolean matchName(String name);
  FactoryFunction createFunction(ShellHost host, SharedMap<String, String> locals);
}
