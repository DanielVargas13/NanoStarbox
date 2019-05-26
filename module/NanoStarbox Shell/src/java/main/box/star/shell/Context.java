package box.star.shell;

import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.shell.io.Stream;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Context {

  final static private String PROPERTY_ACCESS_READ_ONLY =
      "you can't do that,"+
          " the corresponding property is marked read only for clients";

  final static public boolean systemConsoleMode = System.console() != null;

  Context parent;
  Environment environment;
  StreamTable io;
  String origin;
  int shellLevel;
  boolean initialized;

  protected int exitValue;
  public final long timeStamp;

  public final static long bootTimeStamp;

  static {
    bootTimeStamp = System.currentTimeMillis();
  }

  {
    this.timeStamp = System.currentTimeMillis();
  }

  Context(){}

  Context(Context parent, String origin) {
    WithParentOf(parent).AndOriginOf(origin);
  }

  public boolean isReady() {
    return initialized && origin != null;
  }

  @NotNull final protected Context AndOriginOf(@NotNull String origin){
    if (this.origin != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.origin = origin;
    return this;
  }

  @NotNull final protected Context WithParentOf(@NotNull Context parent){
    if (this.parent != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    importContext(parent);
    initialized = true;
    return this;
  }

  void importContext(@NotNull Context parent){
    if (parent == null) {
      if (this instanceof Shell.MainClass) return;
      throw new IllegalArgumentException("parent context is null");
    }
    this.parent = parent;
    this.shellLevel = parent.shellLevel;
    importEnvironment(parent.environment);
    importStreamTable(parent.io);
  }

  @NotNull final protected Context importEnvironment(@Nullable Environment environment){
    if (this.environment == null){
      this.environment = new Environment();
      if (environment == null) {
        this.environment.loadFactoryEnvironment(true);
        return this;
      }
    }
    if (environment != null) this.environment.mapAllObjects(environment, true);
    return this;
  }

  @NotNull final protected Context importStreamTable(@Nullable StreamTable io){
    if (this.io == null) {
      this.io = new StreamTable();
      if (io == null) {
        this.io.loadFactoryStreams();
        return this;
      }
    }
    if (io != null) this.io.putAll(io);
    return this;
  }

  /**
   * <p>For the definitions phase, and String reporting</p>
   */
  Map<Integer, String> redirects;
  String redirectionText(){
    StringBuilder out = new StringBuilder();
    for (Integer k:redirects.keySet()){
      String table = redirects.get(k);
      String[] fields = table.split(":", 2);
      out.append(k).append(fields[0]).append(' ').append('"').append(fields[1]).append("\" ");
    }
    return out.substring(0, Math.max(0, out.length() - 1));
  }

  private interface FirstClassExecutive {
    int exec(Stack<String> parameters);
  }

  private interface SecondClassExecutive {
    int exec(Stack<Object> parameters);
  }

  public interface Shell {
    abstract class MainClass extends Context {
      Stack<String> parameters;
      URI shellBaseDirectory;
      MainClass(Context parent, String origin) {
        super(parent, origin);
      }
      @Override
      final protected URI getShellBaseDirectory() {
        if (shellBaseDirectory == null) try /*  throwing runtime exceptions with closure */ {
          shellBaseDirectory = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (Exception e){throw new RuntimeException("failed to get main context path", e);}
        return shellBaseDirectory;
      }
      final protected Context WithParametersOf(Stack<String> parameters){
        if (this.parameters != null)
          throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
        this.parameters = parameters;
        return this;
      }
      @NotNull
      protected Stack<String> getParameters(){
        return parameters;
      }
      @NotNull final protected Context importParameters(Context parent){
        this.parameters = new Stack<>();
        this.parameters.addAll(parent.getContextParameters());
        return this;
      }
    }
    abstract class CommandClass extends /* COMMAND [ | COMMAND... ] */ Context {
      CommandClass(Context parent, String origin) {
        super(parent, origin);
      }
    }
    class SourceContext extends /* source ... */ MainClass implements FirstClassExecutive {
      private final StringBuilder script = new StringBuilder();
      Scanner scanner;
      @Override
      void importContext(Context parent){
        this.parent = parent;
        this.environment = parent.environment;
        importStreamTable(parent.io);
        importParameters(parent);
      }
      SourceContext(Context parent, String origin, String script) {
        super(parent, origin);
        this.script.append(script);
      }
      public int exec(Stack<String> parameters) {
        return 0;
      }
      public String getScript() {
        return script.toString();
      }
      boolean isFile(){
        // todo:
        return false;
      }
      long getLastModified(){
        // todo: if file or some other container providing meta-data, return container meta data
        return timeStamp;
      }
    }
    class CommandShellContext extends /* [$](COMMAND...) */ MainClass implements FirstClassExecutive {
      @Override
      void importContext(Context parent) {
        super.importContext(parent);
        this.shellLevel++;
        // spec uses a parameter copy
        importParameters(parent);
      }
      CommandShellContext(Context parent, String origin) { super(parent, origin); }
      @Override
      public int exec(Stack<String> parameters) {
        return 0;
      }
    }
    class CommandGroupContext extends /* { COMMAND... } */ Context implements FirstClassExecutive {
      void importContext(Context parent){
        this.parent = parent;
        this.environment = parent.environment;
        importStreamTable(parent.io);
      }
      CommandGroupContext(Context parent, String origin) {
        super(parent, origin);
      }
      @Override
      public int exec(Stack<String> parameters) {
        return 0;
      }
    }
  }

  @NotNull
  final protected Context getMain(){
    if (this instanceof Shell.MainClass) return this;
    else return parent.getMain();
  }

  @NotNull
  protected URI getShellBaseDirectory(){
    return getMain().getShellBaseDirectory();
  }

  /**
   * Provides the $0...$N variables for text-expansion and other uses within all contexts.
   * @return
   */
  @NotNull
  final protected Stack<String> getContextParameters(){ /* current-variables: $0...$N */
    if (this instanceof Shell.MainClass) return ((Shell.MainClass)this).getParameters();
    //else if (this instanceof Shell.FunctionClass) return ((Shell.FunctionClass)this).getParameters();
    else return parent.getContextParameters();
  }

  @Nullable
  final protected Context getParent() {
    return parent;
  }

  public int getExitValue() {
    return exitValue;
  }

  final public String getOrigin() {
    return this.origin;
  }

  final public int getShellLevel() {
    return shellLevel;
  }

  protected Environment compileEnvironmentOperations(List<String> operations){
    // todo: compile environment operations into an environment table, which can be imported on a context
    return null;
  }

  protected StreamTable compileRedirects(Map<Integer, String> redirects){
    if (this.redirects == null) this.redirects = redirects;
    else this.redirects.putAll(redirects);
    // todo: parse redirects, and return a new stream table, which can be imported on a context
    return null;
  }

  public int evaluate(String origin, String text, StreamTable io) {
    // TODO: evaluation routine
    return 0;
  }

  public Stack<String> expandTextParameter(String origin, int number, String text){
    // TODO: expandParameter to stack with environment overlay
    return null;
  }

  public String expandText(String origin, String text){
    // TODO: expandText with environment overlay
    return null;
  }

  final public <T> T getObject(Class<T> type, String name){
    return environment.getObject(type, name);
  }

  public String get(String name){
    return environment.getString(name);
  }

  final public Stream get(int stream){
    return io.get(stream);
  }

  public void set(String name, Object value, boolean export){
    environment.put(name, new Variable(value, export));
  }

  public void set(String name, Object value){
    environment.put(name, new Variable(value));
  }

  final public void set(int number, Stream value){
    io.put(number, value);
  }

  public boolean have(String key){
    return environment.containsKey(key);
  }

  final public boolean have(int stream){
    return io.containsKey(stream);
  }

  public boolean have(String key, Class type){
    return environment.containsKey(key) && environment.get(key).isObjectOfClass(type);
  }

  public void export(String name, boolean value) {environment.export(name, value);}
  public boolean exporting(String name) {return environment.exporting(name);}
  public void mapAllStrings(Map<String, String> map, boolean export) {environment.mapAllStrings(map, export);}
  public void removeAllKeys(List<String> keys) {environment.removeAllKeys(keys);}
  public List<String> keyList() {return environment.keyList();}

  final public void mapAllObjects(Map<String, Object> map, boolean export) {environment.mapAllObjects(map, export);}
  final public String getCurrentDirectory() {return environment.getCurrentDirectory();}
  final public void setCurrentDirectory(String currentDirectory) {environment.setCurrentDirectory(currentDirectory);}
  final public File getRelativeFile(String name) {return environment.getRelativeFile(name);}

}
