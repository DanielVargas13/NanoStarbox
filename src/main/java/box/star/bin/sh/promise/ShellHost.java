package box.star.bin.sh.promise;

import box.star.bin.sh.Executive;
import box.star.bin.sh.SharedMap;
import box.star.bin.sh.Streams;
import box.star.contract.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @param <Shell> the class using this interface
 */
public interface ShellHost<Shell> extends FunctionCatalog<Shell>, StreamCatalog<Shell>, VariableCatalog<Shell> {
  /**
   *
   * @return the exit status of the last command run.
   */
  int getStatus();

  /**
   *
   * @return this ShellHost
   */
  String getCurrentDirectory();

  /**
   *
   * @param directory the current directory of the environment.
   * @return this ShellHost
   */
  Shell setCurrentDirectory(String directory);

  /**
   *
   * @param parameters the command and parameters to execute.
   * @return the exit status of the command
   */
  int run(String... parameters);
  int run(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, String... parameters);

  /**
   *
   * @param parameters the command and parameters to execute.
   * @return the Executive of the command
   */
  Executive exec(String... parameters);
  Executive exec(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, String... parameters);
  int runPipe(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, List<String[]>commands);
  Executive execPipe(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, List<String[]>commands);
  int spawn(String... parameters);
  int spawn(@Nullable Map<String, String> variables, String... parameters);

  File getFile(String file);

}
