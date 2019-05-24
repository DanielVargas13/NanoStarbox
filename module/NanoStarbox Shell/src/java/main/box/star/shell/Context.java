package box.star.shell;

import box.star.shell.io.Stream;
import box.star.shell.io.StreamTable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Context {

  final static private String PROPERTY_ACCESS_READ_ONLY =
      "you can't do that, the corresponding property is marked read only for clients";

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
  
  final public Context getParent() {
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
    environment.put(userFunction.name, new Variable(userFunction, export));
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
