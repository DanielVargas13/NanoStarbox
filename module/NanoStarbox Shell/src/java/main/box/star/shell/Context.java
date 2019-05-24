package box.star.shell;

import box.star.shell.io.Stream;
import box.star.shell.io.StreamTable;
import box.star.text.basic.Scanner;

import java.util.Stack;

public class Context {

  final static public boolean systemConsoleMode = System.console() != null;

  Context parent;
  Scanner source;
  String origin;
  int shellLevel;

  protected Environment environment;
  protected StreamTable io;
  protected Stack<String> parameters;
  protected int exitValue;

  Context(){}

  Context(Context parent){
    this.parent = parent;
  }

  Context(String origin, StreamTable io){
    this.origin = origin;
    this.io = io;
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

  final public String get(String name){
    return environment.getString(name);
  }

  final public Stream get(int stream){
    return io.get(stream);
  }

  final public void set(String name, Object value, boolean export){
    environment.put(name, new Variable(value, export));
  }

  final public void set(String name, Object value){
    environment.put(name, new Variable(value));
  }

  final public void set(int number, Stream value){
    io.put(number, value);
  }

  final public boolean have(String key){
    return environment.containsKey(key);
  }

  final public boolean have(int stream){
    return io.containsKey(stream);
  }

  final public boolean have(String key, Class type){
    return environment.containsKey(key) && environment.get(key).isObjectOfClass(type);
  }

  final public void defineFunction(Function userFunction, boolean export){
    environment.put(userFunction.name, new Variable(userFunction, export));
  }

  public void assembleObject(Assembler plugin, String origin, String key, boolean export, StreamTable io, Object... parameters) {
    // TODO: evaluation routine
    Object objNewInstance = plugin.compile(this, origin, io, parameters);
    set(key, objNewInstance, export);
    return;
  }

}
