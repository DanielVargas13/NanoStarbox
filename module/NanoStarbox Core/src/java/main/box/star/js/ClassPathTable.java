package box.star.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 */
public class ClassPathTable implements Serializable {

  //-> package, classes
  Hashtable<String, String[]> roots;
  private URL source;
  private long timestamp;

  public ClassPathTable(URL source) {
    this.source = source;
    File sourceFile = new File(source.getFile());
    Collection<String> entries;
    if (sourceFile.getName().endsWith(".jar")) entries = processJar(sourceFile);
    else if (sourceFile.isDirectory()) entries = processDirectory(sourceFile);
    else throw new RuntimeException("URL not supported: " + source);
    timestamp = new Date().getTime();
    Stack<String> pkgs = enumRoots(entries);
    roots = new Hashtable<>(pkgs.size());
    for (String pkg : pkgs) {
      roots.put(pkg, enumClasses(entries, pkg));
    }
  }

  private Collection<String> processDirectory(File directory) {
    Collection<String> entries = new ArrayList();
    if (directory.getName().equals("META-INF")) {
      return entries;
    }
    String packageName = directory.getPath().replaceAll("/", "\\.") + ".";
    for (File f : directory.listFiles()) {
      String path = f.getPath();
      if (f.isDirectory()) entries.addAll(processDirectory(f));
      else {
        if (path.endsWith(".class")) {
          if (!entries.contains(packageName)) entries.add(packageName);
          entries.add(path.replaceAll("/", "\\."));
        }
      }
    }
    return entries;
  }

  private Collection<String> processJar(File archive) {
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
    }
    catch (Exception e) {throw new RuntimeException("error processing jar file", e);}
    return entries;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean sourceExists() {
    return new File(source.getFile()).exists();
  }

  private String[] enumClasses(Collection<String> entries, String packageName) {
    Stack<String> packages = new Stack<>();
    for (String s : entries)
      if (s.startsWith(packageName + ".") && s.endsWith(".class")) packages.push(s.substring(0, s.length() - 6));
    String[] source = new String[packages.size()];
    return packages.toArray(source);
  }

  private Stack<String> enumRoots(Collection<String> entries) {
    Stack<String> packages = new Stack<>();
    for (String s : entries) if (s.endsWith(".")) packages.push(s.substring(0, s.length() - 1));
    return packages;
  }

  public Stack<String> getPackageList() {
    Stack s = new Stack();
    s.addAll(roots.keySet());
    return s;
  }

  public Stack<String> getClassList(String packageName) {
    Stack<String> s = new Stack();
    for (String k : roots.get(packageName)) {
      s.push(k);
    }
    return s;
  }

  public String getSource() {
    return source.getPath();
  }

}
