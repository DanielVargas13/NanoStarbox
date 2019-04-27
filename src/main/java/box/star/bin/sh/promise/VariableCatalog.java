package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import box.star.contract.Nullable;

import java.util.List;
import java.util.Map;

public interface VariableCatalog<Host> {
  Host applyVariables(@Nullable Map<String, String> variables);
  Host clearVariables();
  Host resetVariables();
  String get(String key);
  Host set(String key, String value);
  Host remove(String key);
  List<String> variables();
  boolean haveVariable(String key);
  SharedMap<String, String> exportVariables();
}
