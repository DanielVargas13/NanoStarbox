package box.star.shell;

import box.star.shell.Environment;
import box.star.shell.Main;
import box.star.shell.Plugin;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

/**
 * A plugin or function can implement this to offer object constructor services.
 * @param <Kind>
 */
public interface PluginFactory<Kind> {
  /**
   * Custom Plugin Object Constructor
   * @param context the context
   * @param origin the source location of the command
   * @param overlay the local environment for the plugin object
   * @param key
   * @param io
   * @param parameters
   * @return
   */
 Kind constructObject(Main context, String origin, Environment overlay, StreamTable io, Object... parameters);

}
