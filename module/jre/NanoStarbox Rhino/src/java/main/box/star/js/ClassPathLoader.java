package box.star.js;

import box.star.contract.NotNull;
import box.star.contract.Nullable;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.io.File;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClassPathLoader extends URLClassLoader {

  ClassLoader cacheLoader;

  private Context context = new Context();

  public ClassPathLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
    loadRuntimePackages();
  }

  public ClassPathLoader(URL[] urls) {
    this(urls, ClassPathLoader.class.getClassLoader());
  }

  private ClassPathLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
    loadRuntimePackages();
  }

  private void loadRuntimePackages() {
    context.runtimePackages = new ArrayList<>();
    for (Package p : getRuntime()) context.runtimePackages.add(p.getName());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

    Class v;

    v = super.loadClass(name, resolve);

    if (v != null && !context.runtimeClasses.contains(name)) {
      context.runtimeClasses.add(name);
    }

    return v;

  }

  public List<String> getRuntimeClasses() {
    return context.runtimeClasses;
  }

  public List<String> getRuntimePackages() {
    return context.runtimePackages;
  }

  private void addTableEntries(ClassPathTable entries) {
    context.tableEntries.put(entries.getSource(), entries);
  }

  @Nullable
  public static URL toURL(@NotNull String uri){
    return toURL(new File(uri));
  }

  @Nullable
  public static URL toURL(@NotNull File file){
    return toURL(file.toURI());
  }

  @Nullable
  public static URL toURL(URI uri){
    try{
      return uri.toURL();
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  private void processDirectory(File directory) {
    if (new File(directory, "META-INF").exists()){
      processFile(toURL(directory.toURI()));
      return;
    }
    if ("META-INF".equals(directory.getName())) return;
    for (File file : Objects.requireNonNull(directory.listFiles())) {
      if (file.isDirectory()) processDirectory(file);
      else {
        processFile(toURL(file.toURI()));
      }
    }
  }

  private void processFile(URL url) {
    if (url == null) return;
    String pkgPath = url.getPath();
    File file = new File(pkgPath);
    super.addURL(url);
    ClassPathTable record = context.tableEntries.get(pkgPath);
    if (record == null) {
      if (pkgPath.endsWith(".jar") || file.isDirectory()) {
        record = new ClassPathTable(url);
        addTableEntries(record);
        for (String pkgRoot : record.roots.keySet()) {
          String[] pkgClasses = record.roots.get(pkgRoot);
          context.ownPackages.put(pkgRoot, pkgClasses);
          for (String pkgClass : pkgClasses) {
            context.ownClasses.put(pkgClass, pkgPath);
          }
        }
      } else {
        // Nop
      }
    }
  }

  public void addURL(String url) {
    addURL(toURL(new File(url).toURI()));
  }

  @Override
  public void addURL(URL url) {
    File f = new File(url.getPath());
    if (f.isDirectory()) processDirectory(f);
    else processFile(url);
  }

  public List<String> getOwnPackages() {
    return new ArrayList<>(context.ownPackages.keySet());
  }

  public List<String> getOwnSources() {
    return new ArrayList<>(context.tableEntries.keySet());
  }

  public List<String> getOwnClasses() {
    return new ArrayList<>(context.ownClasses.keySet());
  }

  public boolean havePackage(String name) {
    return context.ownPackages.containsKey(name) || context.runtimePackages.contains(name);
  }

  public boolean haveClass(String name) {
    if (context.ownClasses.containsKey(name) || context.runtimeClasses.contains(name)) return true;
    Class c = findLoadedClass(name);
    if (c != null) {
      context.runtimeClasses.add(name);
      return true;
    }
    return false;
  }

  private List<Package> getRuntime() {
    return new ArrayList<>(Arrays.asList(this.getPackages()));
  }

  public List<String> getURIs() {
    List<URL> s = new ArrayList<>(Arrays.asList(super.getURLs()));
    List<String> out = new ArrayList<>(s.size());
    for (URL u : s) out.add(u.getFile());
    return out;
  }

  public Object get(Scriptable global, String name) throws ClassNotFoundException {
    org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
    WrapFactory wrapFactory = cx.getWrapFactory();
    Object newValue = wrapFactory.wrapJavaClass(cx, global, loadClass(name));
    return newValue;
  }

  public static class Context implements Serializable {

    private static final long serialVersionUID = -6175607046241228282L;

    public List<String> runtimePackages;
    public List<String> runtimeClasses = new ArrayList<>();

    // file: entries
    public ConcurrentHashMap<String, ClassPathTable> tableEntries = new ConcurrentHashMap<>();

    // package: classes
    public ConcurrentHashMap<String, String[]> ownPackages = new ConcurrentHashMap<>();

    // classes: file
    public ConcurrentHashMap<String, String> ownClasses = new ConcurrentHashMap<>();

  }

}
