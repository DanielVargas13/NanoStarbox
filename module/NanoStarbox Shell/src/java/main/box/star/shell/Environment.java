package box.star.shell;

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
public class Environment extends ConcurrentHashMap<String, Variable> implements EnvironmentExporter {

  public final static String defaultCurrentDirectoryKey = "PWD";
  public final static String defaultCurrentDirectory = System.getProperty("user.dir");

  protected String currentDirectoryKey;

  /**
   * <p>Creates an export copy of this Environment</p>
   * <br>
   * <p>Each variable marked for export is copied to the new environment.</p>
   * @return a newly exported Environment
   */
  public Environment getExports(){
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
  public Map<String, String> getSerializedExports() {
    Map<String, String> serialized = new Hashtable<>();
    for(String k: keySet()){
      Variable v = get(k);
      if (v.export) serialized.put(k, v.toString());
    }
    return null;
  }
  public void export(String name, boolean value){
    Variable var = get(name);
    var.export = value;
  }
  public boolean exporting(String name){
    Variable var = get(name);
    return var.export;
  }
  public void mapAllObjects(Map<String, Object> map){
    for (String k: map.keySet()) put(k, new Variable(map.get(k)));
  }
  public void mapAllStrings(Map<String, String> map){
    for (String k: map.keySet()) put(k, new Variable(map.get(k)));
  }
  public void removeAllKeys(List<String> keys){
    for (String k: keys) remove(k);
  }
  List<String> keyList(){
    return new ArrayList<>(keySet());
  }
  {
    mapAllStrings(System.getenv());
  }

  public void setSystemDirectoryKey(String currentDirectoryKey) {
    this.currentDirectoryKey = currentDirectoryKey;
  }

  public String getCurrentDirectory() {
    return get(defaultCurrentDirectoryKey).toString();
  }

  public void setCurrentDirectory(String currentDirectory){
    if (new File(currentDirectory).exists())
      throw new RuntimeException(new FileNotFoundException(currentDirectory));
    Variable var = new Variable(currentDirectory);
    var.export = true;
    put(defaultCurrentDirectoryKey, var);
    if (currentDirectoryKey != null) put(currentDirectoryKey, var);
  }

  {
    setCurrentDirectory(defaultCurrentDirectory);
  }

  public File getRelativeFile(String name){
    return new File(getCurrentDirectory(), name);
  }

}
