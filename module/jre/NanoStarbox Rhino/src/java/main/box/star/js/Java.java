package box.star.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Java {

  public static void initObjects(Context cx, Scriptable global, ArchiveLoader loader){
    new Java(cx, global, loader);
  }

  private Scriptable globalObject;

  public Object getKnownPackages() {return toArray(archiveLoader.getKnownPackages());}

  public boolean havePackage(String name) {return archiveLoader.havePackage(name);}

  public Object loadClass(String name) throws ClassNotFoundException {return archiveLoader.get(globalObject, name);}

  public Object getKnownSources() {return toArray(archiveLoader.getKnownSources());}

  public Object getKnownClasses() {return toArray(archiveLoader.getKnownClasses());}

  public boolean haveClass(String name) {return archiveLoader.haveClass(name);}

  private final ArchiveLoader archiveLoader;

  private Java(Context cx, Scriptable global, ArchiveLoader loader){
    this.globalObject = global;
    this.archiveLoader = loader;
    Scripting.addObject(global, "Java", this);
  }

  public Object toArray(String... strings){ return Scripting.createJavaScriptArray(globalObject, strings); }
  public Object toArray(Collection arr){
    return Scripting.createJavaScriptArray(globalObject, arr);
  }
  public Object cast(Class<?> cls, Object value){
    return Context.jsToJava(value, cls);
  }

  public void loadClassPath(String path) {
    if (new File(path).exists()) archiveLoader.addURL(path);
    else throw new RuntimeException(new FileNotFoundException(path));
  }

//  public Object getRuntime(){
//    return toArray(archiveLoader.getRuntime());
//  }

  public Object getClassPath(){
    return toArray(archiveLoader.getURIs());
  }

}
