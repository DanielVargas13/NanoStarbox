package box.star.shell;

import box.star.state.Configuration;
import box.star.state.EnumSettings;
import box.star.text.basic.Scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Stack;

public class Main {

  final static public boolean systemConsoleMode = System.console() != null;

  public static enum Settings {}

  protected EnumSettings.Manager<Settings, Serializable> settings;
  protected Configuration<Settings, Serializable> configuration;

  protected Environment environment;
  protected StreamTable streams;
  protected Stack<String> parameters;
  protected int exitValue, shellLevel;
  protected Main parent;

  private String origin;

  /**
   * Classic start main shell
   * @param parameters
   */
  Main(String... parameters){
    settings = new EnumSettings.Manager<>(this.getClass().getSimpleName());
    configuration = settings.getConfiguration();
    shellLevel++;
    Stack<String> p = new Stack();
    p.addAll(Arrays.asList(parameters));
    // TODO: process main parameters
  }

  /**
   * <p>API: create child shell</p>
   * @param parent
   * @param source
   */
  Main(Main parent, Scanner source) {
    shellLevel = parent.shellLevel + 1;
    this.origin = source.claim();
    settings = new EnumSettings.Manager<>("shell["+shellLevel+"]"+getOrigin(), parent.getConfiguration());
    configuration = settings.getConfiguration();
    this.parent = parent;
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
