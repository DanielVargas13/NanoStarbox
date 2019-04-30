package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import box.star.contract.Nullable;

import java.util.List;
import java.util.Map;

public interface FunctionCatalog<Host> {
  Host defineFunction(FunctionFactory factory);
  Host applyFunctions(@Nullable Map<String, FunctionFactory> factories);
  Host clearFunctions();
  Host removeFunction(String name);
  FunctionFactory getFunctionFactory(String name);
  List<String> functions();
  boolean haveFunction(String key);
  SharedMap<String, FunctionFactory> exportFunctions();
}
