package box.star.js;

import box.star.io.SourceReader;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ArchiveEntries implements Serializable {

  private URL source;
  private long timestamp;

  //-> package, classes
  Hashtable<String, String[]> roots;

  public ArchiveEntries(URL source){
    this.source = source;
    Collection<String> entries = new ArrayList();
    try {
      JarInputStream jarFile = new JarInputStream(new FileInputStream(new File(source.toURI().getPath())));
      JarEntry jarEntry;
      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        entries.add(jarEntry.getName().replaceAll("/", "\\."));
      }
    } catch (Exception e){e.printStackTrace();}
    timestamp = new Date().getTime();
    Stack<String> pkgs = enumRoots(entries);
    roots = new Hashtable<>(pkgs.size());
    for (String pkg: pkgs){
      roots.put(pkg, enumClasses(entries, pkg));
    }
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean sourceExists(){
    return new File(source.getFile()).exists();
  }

  private String[] enumClasses(Collection<String>entries, String packageName){
    Stack<String> packages = new Stack<>();
    for (String s: entries) if (s.startsWith(packageName+".") && s.endsWith(".class")) packages.push(s.substring(0, s.length() - 6));
    String[] source = new String[packages.size()];
    return packages.toArray(source);
  }

  private Stack<String>enumRoots(Collection<String>entries){
    Stack<String> packages = new Stack<>();
    for (String s: entries) if (s.endsWith(".")) packages.push(s.substring(0, s.length() - 1));
    return packages;
  }

  public Stack<String> getPackageList(){
    Stack s = new Stack();
    s.addAll(roots.keySet());
    return s;
  }

  public Stack<String> getClassList(String packageName){
    Stack<String> s = new Stack();
    for (String k:roots.get(packageName)){
      s.push(k);
    }
    return s;
  }

  public String getSource() {
    return source.getPath();
  }

}
