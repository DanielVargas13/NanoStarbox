package box.star.shell;

import box.star.state.Configuration;
import box.star.text.basic.Scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

/**
 * Product Spec: code name: System Commander
 */
public class Main {

  final static public boolean systemConsoleMode = System.console() != null;

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

  protected Environment environment;
  protected StreamTable io;
  protected Stack<String> parameters;
  protected int exitValue, shellLevel;
  protected Main parent;

  private Scanner source;
  private String origin;

  private void contextInit(Scanner source, StreamTable io){
    if (this.origin == null) this.origin = source.nextCharacterClaim();
    this.source = source;
    // TODO: copy local io from parent if io == null and parent doesn't, or inherit all missing stdio channels in local io from parent.
    this.io = io;
  }

  /**
   * Classic start main shell
   * @param parameters
   */
  public Main(String... parameters){
    shellLevel++;
    environment = new Environment();
    settings = new SettingsManager(environment);
    configuration = settings.getConfiguration();
    Stack<String> p = new Stack();
    p.addAll(Arrays.asList(parameters));
    processMainParameters(parameters);
    // TODO: start scanning, store result
  }

  private void processMainParameters(String[] parameters) {
    Scanner scanner = null;
    StreamTable io = null;
    // TODO: process main parameters
    contextInit(scanner, io);
  }

  /**
   * <p>API: create child shell</p>
   * @param parent
   * @param origin
   * @param source
   */
  Main(Main parent, String origin, String source, StreamTable io) {
    shellLevel = parent.shellLevel + 1;
    environment = parent.environment.getExports();
    settings = new SettingsManager("shell["+shellLevel+"]", parent.getConfiguration());
    configuration = settings.getConfiguration();
    this.parent = parent;
    contextInit(new Scanner(origin, source), io);
    // TODO: start scanning, store result
  }

  final public String getOrigin() {
    return this.origin;
  }

  Stack<String> expandTextParameter(Environment overlay, String origin, int number, String text){
    // TODO: expandParameter to stack with environment overlay
    return null;
  }

  String expandText(Environment overlay, String origin, String text){
    // TODO: expandText with environment overlay
    return null;
  }

  final public Configuration<Settings, Serializable> getConfiguration() {
    return configuration;
  }

}
