package box.star.system;

import java.io.File;
import java.util.Locale;

/**
 * OS.Kind ostype=OS.getOperatingSystemKind();
 * switch (ostype) {
 * case Windows: break;
 * case MacOS: break;
 * case Linux: break;
 * case Other: break;
 * }
 */
public final class OS {

  private static OS instance;

  private static Kind thisKind;
  private static String
      lineSeparator,
      pathSeparator, fileSeparator,
      userName, userHome;

  private OS(){

    String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

    if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
      thisKind = Kind.MacOS;
    } else if (OS.indexOf("win") >= 0) {
      thisKind = Kind.Windows;
    } else if (OS.indexOf("nux") >= 0) {
      thisKind = Kind.Linux;
    } else {
      thisKind = Kind.Other;
    }

    lineSeparator = System.getProperty("line.separator");
    pathSeparator = File.pathSeparator;
    fileSeparator = File.separator;

  }

  public static boolean isWindows() {
    return getConfiguration().getOperatingSystemKind().equals(OS.Kind.Windows);
  }

  public static boolean isLinux() {
    return getConfiguration().getOperatingSystemKind().equals(OS.Kind.Linux);
  }

  public static boolean isMacOS() {
    return getConfiguration().getOperatingSystemKind().equals(OS.Kind.MacOS);
  }

  public static boolean isOtherOperatingSystem() {
    return getConfiguration().getOperatingSystemKind().equals(OS.Kind.Other);
  }

  public static OS getConfiguration(){
    if (instance == null) instance = new OS();
    return instance;
  }

  /**
   * detect the operating system from the os.name System property and cache
   * the result
   *
   * @returns - the operating system detected
   */
  public Kind getOperatingSystemKind() {
    return thisKind;
  }

  public String getLineSeparator(){
    return lineSeparator;
  }

  public String getPathSeparator(){
    return pathSeparator;
  }

  public static String getFileSeparator() {
    return fileSeparator;
  }

  public static String getUserName() {
    return System.getProperty("user.name");
  }

  public static String getUserHome() {
    return System.getProperty("user.home");
  }

  /**
   * types of Operating Systems
   */
  public enum Kind {
    Windows, MacOS, Linux, Other
  }
}