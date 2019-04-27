package box.star.bin.sh.promise;

import box.star.bin.sh.Function;
import box.star.bin.sh.SharedMap;
import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.Map;

public interface FunctionProvider<HOST> {
  HOST defineFunction(String name, Function function);
  HOST applyFunctions(@Nullable Map<String, Function> functions);
  HOST clearFunctions();
  HOST removeFunction(String name);
  Function getFunction(String name);
  List<String> functions();
  boolean haveFunction(String key);
  SharedMap<String, Function> exportFunctions();
}
