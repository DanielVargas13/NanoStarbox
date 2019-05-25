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

  final protected Context AndOriginOf(String origin){
    if (this.origin != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.origin = origin;
    return this;
  }

  final protected Context WithParentOf(Context parent){
    if (this.parent != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    importContext(parent);
    initialized = true;
    return this;
  }

  final void importContext(Context impl){
    this.parent = impl;
    importEnvironment(impl.environment);
    importStreamTable(impl.io);
  }

  final protected Context importEnvironment(Environment environment){
    if (environment == null){
      this.environment = environment.getExports();
    } else this.environment.putAll(environment.getExports());
    return this;
  }

  final protected Context importStreamTable(StreamTable io){
    if (io == null){
      this.io = new StreamTable();
      this.io.putAll(parent.io);
    } else this.io.putAll(io);
    return this;
  }

  /**
   * <p>For the definitions phase, and String reporting</p>
   * <br>
   *   <p>The main context must, use this field to create the default stream table io.</p>
   *   <br>
   */
  Map<Integer, String> redirects;

  public interface Shell {
    abstract class MainClass extends Context {
      protected Stack<String> parameters;
      protected Scanner scanner;
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
    }
    abstract class ScriptClass extends /* source ... */ MainClass {
      ScriptClass(Context parent, String origin) {
        super(parent, origin);
      }
    }
    abstract class FunctionClass extends /* function NAME() {} */ Context implements Cloneable {
      private String name;
//      protected List<box.star.shell.Command> commandList;
//      final protected Context WithCommandListOf(List<Command> commands){
//        if (this.commandList != null)
//          throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
//        this.commandList = commands;
//        return this;
//      }
      FunctionClass(String origin, String name) {
        super(null, origin);
        this.name = name;
      }
      public String getName() {
        return name;
      }
      private final String redirectionText(){
        return " redirection text here";
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
       *
       * @param context
       * @return the newly created function instance
       */
      protected Function createRuntimeInstance(Context context) {
        try /* never throwing runtime exceptions with closure */ {
          if (this.parent != null)
            throw new IllegalStateException("trying to create function instance from function instance");
          Function newInstance = (Function) super.clone();
          newInstance.parent = context;
          return newInstance;
        } catch (Exception e){throw new RuntimeException(e);}
        // finally /* never complete */ { ; }
      }
      final public int invoke(String... parameters){
        if (parent == null)
          throw new IllegalStateException("trying to invoke function definition");
        Stack<String> params = new Stack<>();
        params.add(getName());
        params.addAll(Arrays.asList(parameters));
        return exec(params);
      }
      final public int invoke(String name, String... parameters){
        if (parent == null)
          throw new IllegalStateException("trying to invoke function definition");
        Stack<String> params = new Stack<>();
        params.add(name);
        params.addAll(Arrays.asList(parameters));
        return exec(params);
      }
    }
    abstract class PluginClass extends /* virtual function NAME() {} */ FunctionClass {
      PluginClass(String origin, String name) {
        super(origin, name);
      }
      final protected Scanner getScanner(){
        return ((MainClass)getMain()).scanner;
      }
      @Override
      final protected int exec(Stack<String> parameters) {
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
       * <p>A plugin object may access the context scanner.</p>
       * <br>
       * @param parameters
       * @return
       */
      protected <ANY> ANY call(Stack<java.lang.Object> parameters) {
        return (ANY) null;
      }
    }
    class CommandShellContext extends /* [$](COMMAND...) */ Context {
      CommandShellContext(Context parent, String origin) {
        super(parent, origin);
      }
    }
    class CommandGroupContext extends /* { COMMAND... } */ Context {
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
