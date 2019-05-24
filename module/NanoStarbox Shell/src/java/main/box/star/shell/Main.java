package box.star.shell;

import java.io.File;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

  protected Environment environment;
  protected StreamTable streams;
  protected Stack<String> parameters;
  protected int exitValue;
  protected Main parent;

}
