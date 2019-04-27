package box.star.bin.sh.promise;

import box.star.bin.sh.Executive;
import box.star.bin.sh.SharedMap;
import box.star.bin.sh.Streams;
import box.star.contract.NotNull;
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
  Shell setCurrentDirectory(@NotNull String directory);

  /**
   *
   * @param parameters the command and parameters to execute.
   * @return the exit status of the command
   */
  int run(@NotNull String... parameters);

  /**
   * Calls exec and waits for the result, returning that result.
   *
   * @param locals
   * @param streams
   * @param parameters
   * @return
   */
  int run(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, @NotNull String... parameters);

  /**
   *
   * @param parameters the command and parameters to execute.
   * @return the Executive of the command
   */
  Executive exec(@NotNull String... parameters);

  /**
   * Executes the function if found, or tries to execute the system command.
   *
   * The target-command-name is the first parameter in parameters.
   *
   * If the target-command-name is: (a): function; then execution is a function or an error.
   * If the target-command-name is: (b): command; then execution is a system command or an error.
   *
   * @param locals the local process environment.
   * @param streams the stream configuration for this execution.
   * @param parameters the target-command-name and parameters for the execution.
   * @return the execution Executive
   */
  Executive exec(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, String... parameters);

  /**
   * Calls execPipe and waits for the result, returning that result.
   *
   * @param locals
   * @param streams
   * @param commands
   * @return
   */
  int runPipe(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, List<String[]>commands);

  /**
   * Executes a list of commands with the given locals and I/O streams.
   *
   * The first command is connected to the first stream.
   * The last command is connected to the second stream.
   * All commands are connected to the error stream.
   *
   * If streams are null, then the shell's streams will be used in place.
   *
   * @param locals
   * @param streams
   * @param commands
   * @return
   */
  Executive execPipe(@Nullable SharedMap<String, String> locals, @Nullable Streams streams, List<String[]>commands);

  /**
   * Locates the target-command-name and executes the command with locals
   * and parameters, returning the command's Executive.
   *
   * If the command is not found, a RuntimeException is thrown.
   *
   * @param locals
   * @param parameters
   * @return
   */
   Executive execCommand(@Nullable SharedMap<String, String> locals, String... parameters);

  /**
   * Locates the target-function-name and executes the function with locals
   * and parameters, returning the function's Executive.
   *
   * If the function is not found, a RuntimeException is thrown.
   *
   * @param locals
   * @param parameters
   * @return
   */
  Executive execFunction(@Nullable SharedMap<String, String> locals, String... parameters);

  /**
   * Creates a new shell and launches the command given within the shell, returning the result.
   *
   * @param parameters
   * @return
   */
  int spawn(@NotNull String... parameters);

  /**
   * Creates a new shell with the given environment and launches the
   * command given within the shell, returning the result.
   *
   * @param variables
   * @param parameters
   * @return
   */
  int spawn(@Nullable Map<String, String> variables, @NotNull String... parameters);

  /**
   * Locates the given file according to this shell's current directory.
   *
   * The file may or may not exist, but a not-existing-file-condition is not an error.
   *
   * @param file
   * @return a new Java File object pointing to this file reference.
   */
  File getFile(@NotNull String file);

  /**
   * Get this shell's interpretation of a "new line" character sequence.
   *
   * @return this shell's String interpretation of a line-ending.
   */
  String getLineSeparator();

  /**
   * Set this shell's interpretation of a "new line" character sequence.
   *
   * @param separator
   * @return this ShellHost
   */
  Shell setLineSeparator(String separator);

}
