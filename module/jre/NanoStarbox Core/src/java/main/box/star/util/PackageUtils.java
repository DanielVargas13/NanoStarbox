package box.star.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PackageUtils {

  private static boolean debug = true;

  private PackageUtils() {}

  public static List getClassNamesInPackage
      (String jarName, String packageName) {
    ArrayList classes = new ArrayList();

    packageName = packageName.replaceAll("\\.", "/");
    if (debug) System.out.println
        ("Jar " + jarName + " looking for " + packageName);
    try {
      JarInputStream jarFile = new JarInputStream
          (new FileInputStream(jarName));
      JarEntry jarEntry;

      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        if ((jarEntry.getName().startsWith(packageName)) &&
            (jarEntry.getName().endsWith(".class"))) {
          if (debug) System.out.println
              ("Found " + jarEntry.getName().replaceAll("/", "\\."));
          classes.add(jarEntry.getName().replaceAll("/", "\\."));
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return classes;
  }
}