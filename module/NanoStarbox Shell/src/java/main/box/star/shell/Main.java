package box.star.shell;

import box.star.shell.runtime.Environment;
import box.star.shell.runtime.Shell;
import box.star.shell.runtime.etc.SettingsManager;
import box.star.shell.runtime.io.StreamTable;
import box.star.state.Configuration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

/**
 * Product Spec: code name: System Commander
 */
public class Main extends Shell.MainContext {

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
    Environment environment = new Environment().loadFactoryEnvironment(true);
    importEnvironment(environment);
    importStreamTable(null);
    settings = new SettingsManager(environment);
    configuration = settings.getConfiguration();
    processParameters(parameters);
    // TODO: start scanning, store result
  }

  private void processParameters(String[] parameters) {
    Stack<String> p = new Stack();
    p.add(getClass().getName());
    p.addAll(Arrays.asList(parameters));
    WithParametersOf(p);
    // TODO: actually process parameters
    StreamTable io = new StreamTable().loadFactoryStreams();
  }

}
