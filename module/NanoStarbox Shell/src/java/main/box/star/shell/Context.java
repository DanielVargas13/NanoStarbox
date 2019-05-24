package box.star.shell;

import box.star.shell.io.Stream;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

import java.io.File;
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

  protected Stack<String> parameters;
  protected int exitValue;

  Context(){}

  Context(Context parent){
    this.parent = parent;
  }

  Context(Context parent, String origin) {
    this(parent, origin, null, null);
  }

  Context(Context parent, String origin, StreamTable io) {
    this(parent, origin, io, null);
  }

  Context(Context parent, String origin, StreamTable io, Stack<String> parameters){
    this.parent = parent;
    this.origin = origin;
    this.parameters = parameters;
    this.io = io;
  }

  final protected Context WithOriginOf(String origin){
    if (this.origin != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.origin = origin;
    return this;
  }

  final protected Context WithParentOf(Context parent){
    if (this.parent != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.parent = parent;
    return this;
  }

  final protected Context WithParametersOf(Stack<String> parameters){
    if (this.parameters != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.parameters = parameters;
    return this;
  }

  final protected Context WithStreamsOf(StreamTable io){
    if (this.io != null)
      throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
    this.io = io;
    return this;
  }

  /**
   * <p>For the definitions phase, and String reporting</p>
   * <br>
   *   <p>The main context must, use this field to create the default stream table io.</p>
   *   <br>
   */
  Map<Integer, String> redirects;

  public interface Profile {
    abstract class Main extends Context {
      private Scanner scanner;
    }
    abstract class Command extends Context {}
    abstract class Script extends Context {}
    abstract class Object extends Context {}
    abstract class Function extends Context {
      private String name;
      protected List<box.star.shell.Command> body;
      public Function(){super();}
      public Function(String origin, String name){
        this(origin, name,  null);
      }
      public Function(String origin, String name, Map<Integer, String> redirects){
        this(origin, name, null, redirects);
      }
      Function(String origin, String name, List<box.star.shell.Command> body, Map<Integer, String> redirects) {
        this.origin = origin;
        this.name = name;
        this.body = body;
        this.redirects = redirects;
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
        if (body == null)
          return "function "+getName()+"(){"+"\n\t# Native Function: "+this.origin+"\n}" + redirectionText();
        else return sourceText();
      }
      /**
       * User implementation
       * @param parameters the specified parameters, partitioned and fully-shell-expanded.
       * @return the execution status of this function
       */
      protected int exec(Stack<String> parameters){
        return 0;
      }
    }
    abstract class Plugin extends Function {
      public Plugin(String origin, String name) {
        super(origin, name);
      }
      @Override
      final protected int exec(Stack<String> parameters) {
        Stack<java.lang.Object> p = new Stack<>();
        p.addAll(parameters);
        java.lang.Object build = call(p);
        return (build == null)?1:0;
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
      final protected Scanner getScanner(){
        return getMain().scanner;
      }
    }
  }

  final protected Profile.Main getMain(){
    return null;
  }

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

  final public void newObject(Constructor plugin, String origin, String key, boolean export, StreamTable io, Object... parameters) {
    // TODO: evaluation routine
    Object objNewInstance = plugin.construct(this, origin, io, parameters);
    set(key, objNewInstance, export);
    return;
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
