package box.star.shell;

import box.star.state.Configuration;
import box.star.state.EnumSettings;
import box.star.text.basic.Scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

public class Main {

  final static public boolean systemConsoleMode = System.console() != null;

  public static enum Settings {
  }

  private static class SettingsManager extends EnumSettings.Manager<Settings, Serializable> {
    public SettingsManager() {
      super(SettingsManager.class.getSimpleName());
    }
    public SettingsManager(String name, Configuration<Settings, Serializable> parent) {
      super(name, parent);
    }
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
    settings = new SettingsManager();
    configuration = settings.getConfiguration();
    environment = new Environment();
    Stack<String> p = new Stack();
    p.addAll(Arrays.asList(parameters));
    processMainParameters(parameters);
    // TODO: start scanning, store result
  }

  // TODO: process main parameters
  private void processMainParameters(String[] parameters) {
    Scanner scanner = null;
    StreamTable io = null;
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
    settings = new SettingsManager("shell["+shellLevel+"]", parent.getConfiguration());
    configuration = settings.getConfiguration();
    environment = parent.environment.getExports();
    this.parent = parent;
    contextInit(new Scanner(origin, source), io);
    // TODO: start scanning, store result
  }

  public String getOrigin() {
    return this.origin;
  }

  // TODO: expandParameter to stack with environment overlay
  Stack<String> expandTextParameter(Environment overlay, String origin, int number, String text){
    return null;
  }

  // TODO: expandText with environment overlay
  String expandText(Environment overlay, String origin, String text){
    return null;
  }

  public Configuration<Settings, Serializable> getConfiguration() {
    return configuration;
  }

}
