package box.star.js;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ArchiveLoader extends URLClassLoader {

  public static class Context implements Serializable {

    private static final long serialVersionUID = -6175607046241228282L;

    public List<String> runtimePackages;
    public List<String> runtimeClasses = new ArrayList<>();

    // file: entries
    public ConcurrentHashMap<String, ArchiveEntries> archiveEntries = new ConcurrentHashMap<>();

    // package: classes
    public ConcurrentHashMap<String, String[]> ownPackages = new ConcurrentHashMap<>();

    // classes: file
    public ConcurrentHashMap<String, String> ownClasses = new ConcurrentHashMap<>();

  }

  private Context context = new Context();

  private void loadRuntimePackages(){
    context.runtimePackages = new ArrayList<>();
    for (Package p: getRuntime()) context.runtimePackages.add(p.getName());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class v = super.loadClass(name, resolve);
    if (v != null && ! context.runtimeClasses.contains(name)){
      context.runtimeClasses.add(name);
    }
    return v;
  }

  private ArchiveLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
    loadRuntimePackages();
  }

  public ArchiveLoader(URL[] urls) {
    this(urls, ArchiveLoader.class.getClassLoader());
  }

  public List<String> getRuntimeClasses() {
    return context.runtimeClasses;
  }

  public List<String> getRuntimePackages() {
    return context.runtimePackages;
  }

  private ArchiveLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
    loadRuntimePackages();
  }

  private void addArchiveEntries(ArchiveEntries entries){
    context.archiveEntries.put(entries.getSource(), entries);
  }

  private void processDirectory(File directory)  {
    if ("META-INF".equals(directory.getName())) return;
    for (File file: Objects.requireNonNull(directory.listFiles())){
      if (file.isDirectory()) processDirectory(file);
      else {
        try {
          processFile(file.toURI().toURL());
        }
        catch (MalformedURLException e) {

        }
      }
    }
  }

  private void processFile(URL url) {
    String pkgPath = url.getPath();
    super.addURL(url);
    ArchiveEntries record = context.archiveEntries.get(pkgPath);
    if (record == null){
      record = new ArchiveEntries(url);
      addArchiveEntries(record);
      for (String pkgRoot:record.roots.keySet()){
        String[]pkgClasses = record.roots.get(pkgRoot);
        context.ownPackages.put(pkgRoot, pkgClasses);
        for(String pkgClass:pkgClasses) {
          context.ownClasses.put(pkgClass, pkgPath);
        }
      }
    }
  }

  public void addURL(String url){
    try {
      addURL(new File(url).toURI().toURL());
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addURL(URL url) {
    File f = new File(url.getPath());
    if (f.isDirectory()) processDirectory(f);
    else processFile(url);
  }

  public List<String> getOwnPackages(){
    return new ArrayList<>(context.ownPackages.keySet());
  }

  public List<String> getOwnSources(){
    return new ArrayList<>(context.archiveEntries.keySet());
  }

  public List<String> getOwnClasses(){
    return new ArrayList<>(context.ownClasses.keySet());
  }

  public boolean havePackage(String name) {
    return context.ownPackages.containsKey(name) || context.runtimePackages.contains(name);
  }

  public boolean haveClass(String name){
    if (context.ownClasses.containsKey(name) || context.runtimeClasses.contains(name)) return true;
    Class c = findLoadedClass(name);
    if (c != null) {
      context.runtimeClasses.add(name);
      return true;
    }
    return false;
  }

  private List<Package> getRuntime(){
    return new ArrayList<>(Arrays.asList(this.getPackages()));
  }

  public List<String> getURIs(){
    List<URL>s=new ArrayList<>(Arrays.asList(super.getURLs()));
    List<String>out = new ArrayList<>(s.size());
    for (URL u:s)out.add(u.getFile());
    return out;
  }

  public Object get(Scriptable global, String name) throws ClassNotFoundException {
    org.mozilla.javascript.Context cx =  org.mozilla.javascript.Context.getCurrentContext();
      WrapFactory wrapFactory = cx.getWrapFactory();
      Object newValue = wrapFactory.wrapJavaClass(cx, global, loadClass(name));
      return newValue;
  }

}
