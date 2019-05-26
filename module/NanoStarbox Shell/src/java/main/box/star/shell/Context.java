package box.star.shell;

import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.shell.io.Stream;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

import java.io.File;
import java.util.Arrays;
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
    this.shellLevel = (parent.shellLevel + 1);
    initialized = true;
    return this;
  }

  void importContext(@NotNull Context impl){
    this.parent = impl;
    importEnvironment(impl.environment);
    importStreamTable(impl.io);
  }

  @NotNull final protected Context importEnvironment(@NotNull Environment environment){
    if (environment == null){
      this.environment = environment.getExports();
    } else this.environment.putAll(environment.getExports());
    return this;
  }

  @NotNull final protected Context importStreamTable(@NotNull StreamTable io){
    if (io == null){
      this.io = new StreamTable();
      this.io.putAll(parent.io);
    } else this.io.putAll(io);
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

  public interface Shell {
    abstract class MainClass extends Context {
      Stack<String> parameters;
      Scanner scanner;
      MainClass(Context parent, String origin) {
        super(parent, origin);
      }
      final protected Context WithScannerOf(Scanner scanner){
        if (this.scanner != null)
          throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
        this.scanner = scanner;
        return this;
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
    abstract class ScriptClass extends /* source ... */ MainClass {
      @Override
      void importContext(Context impl){
        this.parent = impl;
        this.environment = impl.environment;
        importStreamTable(impl.io);
        importParameters(impl);
      }
      ScriptClass(Context parent, String origin) {
        super(parent, origin);
        // todo: overwrite the inherited parameters with new parameters if available
      }
    }
    abstract class FunctionClass extends /* function NAME() {} */ Context implements Cloneable {
      Stack<String> parameters;
      private String name;
      FunctionClass(String origin, String name) {
        super(null, origin);
        this.name = name;
      }
      public String getName() {
        return name;
      }
      protected String sourceText(){
        return "function "+getName()+"(){"+"\n\t# Native Function: "+this.origin+"\n}" + redirectionText();
      }
      @Override
      public String toString() {
          return sourceText();
      }
      /**
       * User implementation
       * @param parameters the specified parameters, partitioned and fully-shell-expanded.
       * @return the execution status of this function
       */
      protected int exec(Stack<String> parameters){
        return 0;
      }
      /**
       * @param context
       * @return the newly created function instance
       */
      protected Function createContextInstance(Context context) {
        try /* never throwing runtime exceptions with closure */ {
          if (this.parent != null)
            throw new IllegalStateException("trying to create function instance from function instance");
          Function newInstance = (Function) super.clone();
          newInstance.parent = context;
          return newInstance;
        } catch (Exception e){throw new RuntimeException(e);}
        // finally /* never complete */ { ; }
      }
      @NotNull
      protected Stack<String> getParameters(){
        return parameters;
      }
      final public int invoke(String... parameters){
        if (parent == null)
          throw new IllegalStateException("trying to invoke function definition");
        this.parameters = new Stack<>();
        this.parameters.add(getName());
        this.parameters.addAll(Arrays.asList(parameters));
        return exec(this.parameters);
      }
      final public int invoke(String name, String... parameters){
        if (parent == null)
          throw new IllegalStateException("trying to invoke function definition");
        this.parameters = new Stack<>();
        this.parameters.add(name);
        this.parameters.addAll(Arrays.asList(parameters));
        return exec(this.parameters);
      }
    }
    /**
     * <p>Extended Java Operations Context with a pure Java call mechanism</p>
     * <br>
     * <p>A pure Java call, is a Java method invocation that doesn't require strings,
     * but accepts objects. In addition, the pure call does not return an int to
     * represent status, but an object which is not null to indicate a status of
     * 0=(success).</p>
     * <br>
     * <p>When a plugin is called as a function through the text interface, all
     * parameters are strings.</p>
     */
    abstract class PluginClass extends /* virtual function NAME() {} */ FunctionClass {
      Stack<String>parameters;
      PluginClass(String origin, String name) {
        super(origin, name);
      }
      /**
       * <p>Fetches the scanner from the main class</p>
       * <br>
       * <p>In theory, a plugin might read source commands, from a main
       * context, advancing its interpretation position. Can't do that without
       * access to the main scanner. This method fetches the main context, and
       * returns the scanner.</p>
       * <br>
       * <p>If the plugin does such an operation with the scanner during a source
       * read, all text following the command stream parameters, will be considered
       * source for the plugin</p>
       * <br>
       *   Disabled for now. no repeat call strategy for direct stream interpretation.
       * @return the main context scanner.
       */
//      final protected Scanner getScanner(){
//        return ((MainClass)getMain()).scanner;
//      }
      /**
       * <p>Forwards the text based function call to the object call</p>
       * <br>
       * @param parameters the specified parameters, partitioned and fully-shell-expanded.
       * @return success if the object result is not null
       */
      @Override
      final protected int exec(Stack<String> parameters) {
        this.parameters = parameters;
        Stack<java.lang.Object> p = new Stack<>();
        p.addAll(parameters);
        return (call(p) == null)?1:0;
      }
      /**
       * <p>Plugin gets Object array parameters for exec</p>
       * <br>
       * <p>Plugins operate as functions that can service object requests.</p>
       * <br>
       * <p>When called via text-script, all parameters will be strings.</p>
       * <br>
       * @param parameters
       * @return
       */
      public <ANY> ANY call(Stack<java.lang.Object> parameters) {
        return (ANY) null;
      }
      /**
       * If you are hosting a direct object call, you'll need to override
       * this method to return the string interpretation, to the text expansion
       * routines.
       * @return
       */
      @Override
      protected @NotNull Stack<String> getParameters() {
        return parameters;
      }
    }
    class CommandShellContext extends /* [$](COMMAND...) */ MainClass {
      @Override
      void importContext(Context impl) {
        super.importContext(impl);
        // spec uses a parameter copy
        importParameters(impl);
      }
      CommandShellContext(Context parent, String origin) {
        super(parent, origin);
      }
    }
    class CommandGroupContext extends /* { COMMAND... } */ Context {
      void importContext(Context impl){
        this.parent = impl;
        this.environment = impl.environment;
        importStreamTable(impl.io);
      }
      CommandGroupContext(Context parent, String origin) {
        super(parent, origin);
      }
    }
    class CommandContext extends /* COMMAND [ | COMMAND... ] */ Context {
      CommandContext(Context parent, String origin) {
        super(parent, origin);
      }
    }
    class ObjectContext extends /* UNKNOWN */ Context {
      void importContext(Context impl){
        this.parent = impl;
        this.environment = impl.environment;
        this.io = parent.io;
      }
      ObjectContext(Context parent, String origin){
        super(parent, origin);
      }
      ObjectContext(Context parent, String origin, StreamTable io){
        this(parent, origin);
        importStreamTable(io);
      }
    }
  }

  @NotNull
  final protected Context getMain(){
    if (this instanceof Shell.MainClass) return this;
    else return parent.getMain();
  }

  /**
   * Provides the $1...$N variables for text-expansion and other uses within all contexts.
   * @return
   */
  @NotNull
  final protected Stack<String> getContextParameters(){ /* current-variables: $0...$N */
    if (this instanceof Shell.MainClass) return ((Shell.MainClass)this).parameters;
    else if (this instanceof Shell.FunctionClass) return ((Shell.FunctionClass)this).parameters;
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

  final public Function getFunction(String name){
    return environment.getObject(Function.class, name);
  }

  final public Plugin getPlugin(String name){
    return environment.getObject(Plugin.class, name);
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

  final public void defineFunction(Function userFunction, boolean export){
    environment.put(userFunction.getName(), new Variable(userFunction, export));
  }

  final public boolean newObject(Constructor plugin, String origin, String key, boolean export, StreamTable io, Object... parameters) {
    Shell.ObjectContext objectContext = new Shell.ObjectContext(this, origin, io);
    Object newObjInstance = plugin.construct(objectContext, parameters);
    if (newObjInstance == null) return false;
    this.set(key, newObjInstance, export);
    return true;
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
