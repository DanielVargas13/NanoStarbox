package box.star.shell;

import box.star.shell.io.StreamTable;
import box.star.state.Configuration;
import box.star.text.basic.Scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

/**
 * Product Spec: code name: System Commander
 */
public class Main extends Context.Shell.MainClass {

  public final static String

    SHELL_SYSTEM_PROFILE_VARIABLE = "SHELL_SYSTEM_PROFILE",
    SHELL_SYSTEM_PROFILE_PROPERTY = "box.star.shell.Main.system.profile",

    SHELL_USER_PROFILE_VARIABLE = "SHELL_USER_PROFILE",
    SHELL_USER_PROFILE_PROPERTY = "box.star.shell.Main.user.profile"

  ;

  public static enum Settings {
    SYSTEM_PROFILE, USER_PROFILE
  }

  protected SettingsManager settings;
  protected Configuration<Settings, Serializable> configuration;

  final public Configuration<Settings, Serializable> getConfiguration() {
    return configuration;
  }

  /**
   * Classic start main shell
   * @param parameters
   */
  public Main(String... parameters){
    super(null, null);
    environment = new Environment();
    settings = new SettingsManager(environment);
    configuration = settings.getConfiguration();
    Stack<String> p = new Stack();
    p.addAll(Arrays.asList(parameters));
    WithParametersOf(p);
    processParameters();
    // TODO: start scanning, store result
  }

  private void processParameters() {
    // TODO: actually process parameters
    scanner = null;
    StreamTable io = null;
  }

}
