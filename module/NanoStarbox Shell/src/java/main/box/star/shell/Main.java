package box.star.shell;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

  final static public boolean systemConsoleMode = System.console() != null;

  // TODO: expandText with environment overlay

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

}
