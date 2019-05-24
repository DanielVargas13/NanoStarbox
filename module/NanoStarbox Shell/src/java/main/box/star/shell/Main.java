package box.star.shell;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

public class Main {

  final static public boolean systemConsoleMode = System.console() != null;

  protected Environment environment;
  protected StreamTable streams;
  protected Stack<String> parameters;
  protected int exitValue, shellLevel;
  protected Main parent;

  /**
   * Classic start main shell
   * @param parameters
   */
  Main(String... parameters){
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
    this.parent = parent;
    // TODO: start scanning, store result
  }

  // TODO: expandParameter to stack with environment overlay
  Stack<String> expandTextParameter(Environment overlay, String origin, int number, String text){
    return null;
  }

  // TODO: expandText with environment overlay
  String expandText(Environment overlay, String origin, String text){
    return null;
  }

}
