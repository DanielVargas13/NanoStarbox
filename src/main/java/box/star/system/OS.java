package box.star.system;
import java.util.Locale;
/**
 * OS.Kind ostype=OS.getOperatingSystemType();
 * switch (ostype) {
 *     case Windows: break;
 *     case MacOS: break;
 *     case Linux: break;
 *     case Other: break;
 * }
 * helper class to check the operating system this Java VM runs in
 *
 * please keep the notes below as a pseudo-license
 *
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 * compare to http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
 * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
 */
public  final class OS {
  /**
   * types of Operating Systems
   */
  public enum Kind {
    Windows, MacOS, Linux, Other
  };

  // cached result of OS detection
  protected static Kind detectedOS;

  /**
   * detect the operating system from the os.name System property and cache
   * the result
   * 
   * @returns - the operating system detected
   */
  public static Kind getOperatingSystemType() {
    if (detectedOS == null) {
      String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
      if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
        detectedOS = Kind.MacOS;
      } else if (OS.indexOf("win") >= 0) {
        detectedOS = Kind.Windows;
      } else if (OS.indexOf("nux") >= 0) {
        detectedOS = Kind.Linux;
      } else {
        detectedOS = Kind.Other;
      }
    }
    return detectedOS;
  }
}