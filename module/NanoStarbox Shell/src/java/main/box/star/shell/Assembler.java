package box.star.shell;

import box.star.shell.Environment;
import box.star.shell.Main;
import box.star.shell.Plugin;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

/**
 * Any object can implement this to offer object constructor services.
 * @param <Kind>
 */
public interface Assembler<Kind> {
  /**
   * Custom Assembler
   * @param context the context
   * @param origin the source location of the command
   * @param overlay the local environment for the object
   * @param io the stream table for the object
   * @param parameters the parameters for the construction
   * @return the newly compiled object
   */
 Kind compile(Main context, String origin, Environment overlay, StreamTable io, Object... parameters);

}
