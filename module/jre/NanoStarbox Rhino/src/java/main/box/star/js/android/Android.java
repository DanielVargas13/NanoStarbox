package box.star.js.android;

import java.io.File;
import java.lang.reflect.Constructor;

public class Android {

  private static boolean tryDalvik = true;
  private static Class dalvikClassLoader;
  private static Constructor<ClassLoader> dalvikConstructor;

  public static boolean isPlatform(){
    if (tryDalvik) {
      try {
        dalvikClassLoader = Class.forName("dalvik.system.PathClassLoader");
        dalvikConstructor = dalvikClassLoader.getConstructor(String.class, ClassLoader.class);
      }
      catch (Exception e) {
        e.printStackTrace();
      } finally {
        tryDalvik = false;
      }
    }
    return  dalvikConstructor != null;
  }

  public static ClassLoader getDalvikClassLoader(String path, ClassLoader parent) throws ClassNotFoundException {
    if (isPlatform()){
      try { return dalvikConstructor.newInstance(path, parent); }
      catch (Exception e) { throw new RuntimeException(e); }
    }
    throw new ClassNotFoundException("dalvik.system.PathClassLoader");
  }

  public static File getApplicationCacheDirectory(){
    return new File(System.getProperty("java.io.tmpdir"));
  }

}
