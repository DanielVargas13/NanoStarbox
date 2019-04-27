package box.star.bin.sh.promise;

import box.star.bin.sh.Executive;
import box.star.bin.sh.SharedMap;
import box.star.bin.sh.Streams;

import java.util.Map;

public interface Standard<Shell, Executive> extends StandardStreamProvider<Shell>, VariableProvider<Shell> {
  int getStatus();
  String getCurrentDirectory();
  Shell setCurrentDirectory(String directory);
  int run(String... parameters);
  int run(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, String... parameters);
  Executive exec(String... parameters);
  Executive exec(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, String... parameters);
  int spawn(String... parameters);
  int spawn(@Nullable Map<String, String> variables, String... parameters);
}
