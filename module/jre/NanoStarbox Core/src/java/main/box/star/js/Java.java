package box.star.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class Java {

  private final ClassPathLoader classPathLoader;
  private Scriptable globalObject;

  private Java(Context cx, Scriptable global, ClassPathLoader loader) {
    this.globalObject = global;
    this.classPathLoader = loader;
    Scripting.addObject(global, "Java", this);
  }

  public static void initObjects(Context cx, Scriptable global, ClassPathLoader loader) {
    new Java(cx, global, loader);
  }

  public Object getKnownPackages() {
    List<String> packages = (classPathLoader.getOwnPackages());
    packages.addAll(classPathLoader.getRuntimePackages());
    return toArray(packages);
  }

  public boolean havePackage(String name) {return classPathLoader.havePackage(name);}

  public Object getKnownSources() {return toArray(classPathLoader.getOwnSources());}

  public Object getKnownClasses() {
    List<String> classes = (classPathLoader.getOwnClasses());
    classes.addAll(classPathLoader.getRuntimeClasses());
    return toArray(classes);
  }

  /**
   * Returns true if the class is known.
   *
   * @param name
   * @return
   */
  public boolean haveClass(String name) {return classPathLoader.haveClass(name);}

  public Object toArray(String... strings) { return Scripting.createJavaScriptArray(globalObject, strings); }

  public Object toArray(Collection arr) {
    return Scripting.createJavaScriptArray(globalObject, arr);
  }

  public Object cast(Class<?> cls, Object value) {
    return Context.jsToJava(value, cls);
  }

  public void loadClassPath(String path) { classPathLoader.addURL(path);}

//  public Object getRuntime(){
//    return toArray(archiveLoader.getRuntime());
//  }

  public Object getClassPath() {
    return toArray(classPathLoader.getURIs());
  }

}
