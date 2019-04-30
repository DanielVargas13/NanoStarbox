package box.star.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.io.File;
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

  public interface EventPort {
    void onNewDirectory(File uri);
    void onNewArchive(String uri);
    void onNewPackage(String uri, String packageName);
    void onNewClass(String uri, String packageName, String className);
  }

  EventPort eventPort = new EventPort() {

    @Override
    public void onNewDirectory(File uri) {}

    @Override
    public void onNewArchive(String uri) {
    }

    @Override
    public void onNewPackage(String uri, String packageName) {}

    @Override
    public void onNewClass(String uri, String packageName, String className) {
      System.err.println(uri+"?&class="+className);
    }

  };

//  private ConcurrentHashMap<String, Object> loadedClasses = new ConcurrentHashMap<>();

  // file: entries
  private ConcurrentHashMap<String, ArchiveEntries> archiveEntries = new ConcurrentHashMap<>();

  // package: classes
  private ConcurrentHashMap<String, String[]> knownPackages = new ConcurrentHashMap<>();

  // classes: file
  private ConcurrentHashMap<String, String> knownClasses = new ConcurrentHashMap<>();

  private ArchiveLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public ArchiveLoader(URL[] urls) {
    this(urls, ArchiveLoader.class.getClassLoader());
  }

  private ArchiveLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
  }

  private void addArchiveEntries(ArchiveEntries entries){
    archiveEntries.put(entries.getSource(), entries);
  }

  private void processDirectory(File directory)  {
    if ("META-INF".equals(directory.getName())) return;
    //eventPort.onNewDirectory(directory);
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
    ArchiveEntries record = archiveEntries.get(pkgPath);
    if (record == null){
      //eventPort.onNewArchive(pkgPath);
      record = new ArchiveEntries(url);
      addArchiveEntries(record);
      for (String pkgRoot:record.roots.keySet()){
        String[]pkgClasses = record.roots.get(pkgRoot);
        knownPackages.put(pkgRoot, pkgClasses);
        //eventPort.onNewPackage(pkgPath, pkgRoot);
        for(String pkgClass:pkgClasses) {
          knownClasses.put(pkgClass, pkgPath);
          //eventPort.onNewClass(pkgPath, pkgRoot, pkgClass);
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

  public List<String> getKnownPackages(){
    return new ArrayList<>(knownPackages.keySet());
  }

  public List<String> getKnownSources(){
    return new ArrayList<>(archiveEntries.keySet());
  }

  public List<String>getKnownClasses(){
    return new ArrayList<>(knownClasses.keySet());
  }

  public boolean havePackage(String name) {
    return knownPackages.containsKey(name);
  }

  public boolean haveClass(String name){
    return knownClasses.containsKey(name);
  }

  public List<Package> getRuntime(){
    return new ArrayList<>(Arrays.asList(this.getPackages()));
  }

  public List<String> getURIs(){
    List<URL>s=new ArrayList<>(Arrays.asList(super.getURLs()));
    List<String>out = new ArrayList<>(s.size());
    for (URL u:s)out.add(u.getFile());
    return out;
  }

  public Object get(Scriptable global, String name) throws ClassNotFoundException {
    Context cx =  Context.getCurrentContext();
      WrapFactory wrapFactory = cx.getWrapFactory();
      Object newValue = wrapFactory.wrapJavaClass(cx, global, loadClass(name));
      return newValue;
  }

}
