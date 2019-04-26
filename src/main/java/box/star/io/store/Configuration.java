package box.star.io.store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Configuration extends HashMap<String, Serializable> {

  private static final long serialVersionUID = 1978528942625555272L;
  private File file;
  private boolean modified = false;
  private long modificationTime = 0;
  public Configuration(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }
  public Configuration(int initialCapacity) {
    super(initialCapacity);
  }
  public Configuration(Map<? extends String, ? extends Serializable> m) {
    super(m);
    this.setModified(true);
  }

  public Configuration() {}

  public Configuration(File file) {
    this.file = file;
    this.setModified(true);
  }

  protected static Configuration load(File source) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(source))) {
      return loadDataStream(ois, null);
    }
    catch (Exception e) {throw new RuntimeException(e);}
  }

  protected static Configuration loadDataStream(ObjectInputStream ois, ObjectSerializer customObjectSerializer) {
    if (customObjectSerializer != null)
      try {
        return (Configuration) customObjectSerializer.loadStreamObject(ois);
      }
      catch (Exception e) { throw new RuntimeException(e); }
    try { return (Configuration) ois.readObject(); }
    catch (Exception e) { throw new RuntimeException(e); }
  }

  protected static void save(Configuration cfg) {
    if (cfg == null) return;
    File destination = cfg.file;
    if (!destination.exists()) {
      File parent = destination.getParentFile();
      if (parent != null && !parent.exists()) parent.mkdirs();
    }
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(destination))) {
      cfg.setModified(false);
      saveDataStream(cfg, oos, null);
    }
    catch (Exception e) {throw new RuntimeException(e);}
  }

  protected static void saveDataStream(Configuration cfg, ObjectOutputStream oos, ObjectSerializer customObjectSerializer) {
    if (customObjectSerializer != null) {
      customObjectSerializer.saveStreamObject(cfg, oos);
      return;
    }
    try { oos.writeObject(cfg); }
    catch (IOException e) { throw new RuntimeException(e); }
  }

  @Override
  public Serializable put(String key, Serializable value) {
    this.setModified(true);
    return super.put(key, value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Serializable> m) {
    this.setModified(true);
    super.putAll(m);
  }

  @Override
  public Serializable remove(Object key) {
    this.setModified(true);
    return super.remove(key);
  }

  @Override
  public void clear() {
    this.setModified(true);
    super.clear();
  }

  @Override
  public Serializable putIfAbsent(String key, Serializable value) {
    this.setModified(true);
    return super.putIfAbsent(key, value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    this.setModified(true);
    return super.remove(key, value);
  }

  @Override
  public boolean replace(String key, Serializable oldValue, Serializable newValue) {
    this.setModified(true);
    return super.replace(key, oldValue, newValue);
  }

  @Override
  public Serializable replace(String key, Serializable value) {
    this.setModified(true);
    return super.replace(key, value);
  }

  @Override
  public Serializable merge(String key, Serializable value, BiFunction<? super Serializable, ? super Serializable, ? extends Serializable> remappingFunction) {
    this.setModified(true);
    return super.merge(key, value, remappingFunction);
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super Serializable, ? extends Serializable> function) {
    this.setModified(true);
    super.replaceAll(function);
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {

    this.file = file;

  }

  public long getFileSize() {
    return (file != null) ? file.length() : 0;
  }

  public boolean isModifiedInMemory() {
    return modified;
  }

  public boolean isModifiedInStorage() {
    return (file != null) && file.exists() && file.lastModified() > modificationTime;
  }

  public long getModificationTime() {
    return modificationTime;
  }

  /**
   * Setting this value to true, will cause the configuration to save to the
   * defined file from the constructor.
   *
   * @param modified
   */
  public void setModified(boolean modified) {
    if (modified) this.modificationTime = new java.util.Date().getTime();
    this.modified = modified;
  }

  public boolean isUpdateable() {
    return (file != null && this.isModifiedInMemory());
  }

  public long getNumber(String key) {
    return (long) get(key);
  }

  public Integer getInt(String key) {
    return (Integer) get(key);
  }

  public String getString(String key) {
    return (String) get(key);
  }

  public boolean getBoolean(String key) {
    return (boolean) get(key);
  }

  public void save() {
    if (this.isUpdateable()) save(this);
  }
}
