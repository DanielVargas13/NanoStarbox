package box.star.shell.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>An implementation of map that can be used concurrently</p>
 * <br>
 * <p>This map can store objects as variables, and convert them to string on export.</p>
 * <br>
 * <p>This map supports system local present working directory environment variable configuration,
 * and relative file path look-ups.</p>
 * <br>
 * <p>This map can mark variables for export (independent-copy) and can export them
 * for internal use (with objects), or in serialized form (string-only).</p>
 * <br>
 */
public class Environment extends ConcurrentHashMap<String, Environment.Variable> {

  public final static String defaultCurrentDirectoryKey = "PWD";
  public final static String defaultCurrentDirectory = System.getProperty("user.dir");

  protected String currentDirectoryKey;

  /**
   * <p>Creates an export copy of this Environment</p>
   * <br>
   * <p>Each variable marked for export is copied to the new environment.</p>
   * @return a newly exported Environment
   */
  Environment getExports(){
    Environment exports = new Environment();
    for(String k: keySet()){
      Variable v = get(k);
      if (v.export) exports.put(k, v.clone());
    }
    return null;
  }
  /**
   * <p>Creates a serialized export copy of this Environment</p>
   * <br>
   *   <p>Objects will be converted to string using their toString method.</p>
   * @return the newly serialized export copy
   */
  Map<String, String> getSerializedExports() {
    Map<String, String> serialized = new Hashtable<>();
    for(String k: keySet()){
      Variable v = get(k);
      if (v.export) serialized.put(k, v.toString());
    }
    return null;
  }

  public String getString(String name){
    if (! containsKey(name)) return null;
    return get(name).toString();
  }
  public <T> T getObject(Class<? extends T> type, String name){
    if (! containsKey(name)) return null;
    return get(name).getObject(type);
  }
  public void export(String name, boolean value){
    Variable var = get(name);
    var.export = value;
  }
  public boolean exporting(String name){
    Variable var = get(name);
    return var.export;
  }
  public void mapAllObjects(Environment map, boolean export){
    for (String k: map.exportList()){
      Variable v = map.get(k).clone();
      v.export = export;
      this.put(k, v);
    }
  }
  public void mapAllObjects(Map<String, Object> map, boolean export){
    for (String k: map.keySet()) put(k, new Variable(map.get(k), export));
  }
  public void mapAllStrings(Map<String, String> map, boolean export){
    for (String k: map.keySet()) put(k, new Variable(map.get(k), export));
  }
  public void removeAllKeys(List<String> keys){
    for (String k: keys) remove(k);
  }
  public List<String> keyList(){
    return new ArrayList<>(keySet());
  }
  public List<String> exportList(){
    List<String> out = new ArrayList<>();
    for (String k: keyList()){
      Variable v = get(k);
      if (v.export) out.add(k);
    }
    return out;
  }
  public void setSystemDirectoryKey(String currentDirectoryKey) {
    this.currentDirectoryKey = currentDirectoryKey;
  }

  public String getCurrentDirectory() {
    return get(defaultCurrentDirectoryKey).toString();
  }

  public void setCurrentDirectory(String currentDirectory){
    if (!new File(currentDirectory).exists())
      throw new RuntimeException(new FileNotFoundException(currentDirectory));
    Variable var = new Variable(currentDirectory);
    var.export = true;
    this.put(defaultCurrentDirectoryKey, var);
    if (currentDirectoryKey != null) put(currentDirectoryKey, var);
  }

  {
    setCurrentDirectory(defaultCurrentDirectory);
  }

  public File getRelativeFile(String name){
    return new File(getCurrentDirectory(), name);
  }

  public Environment loadFactoryEnvironment(boolean export)  {
    this.mapAllStrings(System.getenv(), export);
    return this;
  }

  public static class Variable implements Cloneable {
    boolean
        export, // aka enumerable
        constant // aka writable
    ;
    Object value;
    public Variable(Object value){
      this.value = value;
    }
    public Variable(Object value, boolean export){
      this.value = value;
      this.export = export;
    }
    @Override
    public String toString() {
      if (this.isObjectOfClass(String.class)) return (String) value;
      return this.value.toString();
    }
    public void setValue(Object value) {
      if (this.constant == false) this.value = value;
    }
    public <ANY> ANY getValue(){
      return (ANY) this.value;
    }

    public boolean isObject(){
      return isObjectOfClass(Object.class);
    }
    public Class<?> getObjectClass(){
      return getValue().getClass();
    }
    public boolean isObjectOfClass(Class type){
      return type.isInstance(this.value);
    }
    public <T> T getObject(Class<? extends T> type){
      return type.cast(getValue());
    }
    @Override
    protected Variable clone() {
      try /*  throwing runtime exceptions with closure */ {
        return (Variable) super.clone();
      } catch (Exception e){throw new RuntimeException(e);}
      finally /*  complete */ {
        ;
      }
    }
  }
}