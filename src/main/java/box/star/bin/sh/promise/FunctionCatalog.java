package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import box.star.contract.Nullable;

import java.util.List;
import java.util.Map;

public interface FunctionCatalog<HOST> {
  HOST defineFunction(String name, FunctionFactory<HOST> function);
  HOST applyFunctions(@Nullable Map<String, FunctionFactory<HOST>> functions);
  HOST clearFunctions();
  HOST removeFunction(String name);
  FunctionFactory<HOST> getFunctionFactory(String name);
  List<String> functions();
  boolean haveFunction(String key);
  SharedMap<String, FunctionFactory<HOST>> exportFunctions();
}
