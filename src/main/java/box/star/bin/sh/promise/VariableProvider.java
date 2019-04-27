package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.Map;

public interface VariableProvider<HOST> {
  HOST applyVariables(@Nullable Map<String, String> variables);
  HOST clearVariables();
  HOST resetVariables();
  String get(String key);
  HOST set(String key, String value);
  HOST remove(String key);
  List<String> variables();
  boolean haveVariable(String key);
  SharedMap<String, String> exportVariables();
}