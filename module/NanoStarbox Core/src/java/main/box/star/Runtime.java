package box.star;

import box.star.contract.NotNull;
import box.star.contract.Nullable;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;

public class Runtime {

  private static java.lang.Runtime jrt = java.lang.Runtime.getRuntime();
  private static Runtime ourInstance = new Runtime();

  private static final URI baseDirectory = baseDirectoryOf(Runtime.class);

  final private static Console console = System.console();
  final private static InputStream standardInput = System.in;
  final private static OutputStream standardOutput = System.out;
  final private static OutputStream standardError = System.err;

  public static Runtime getRuntime() {
    return ourInstance;
  }

  public static URI getBaseDirectory() { return baseDirectory; }

  /**
   * Retrieves the based directory of the given class
   * @param source
   * @return the class's base-directory
   */
  public static URI baseDirectoryOf(Class source){
    URI baseDirectory;
    try {
      baseDirectory = source.getProtectionDomain().getCodeSource().getLocation().toURI();
    } catch (Exception e){throw new RuntimeException("failed to get runtime base directory URI", e);}
    return baseDirectory;
  }

  @Nullable
  public static <T> T switchNull(@Nullable T test, @Nullable T notNull) {
    return ((test == null) ? notNull : test);
  }

  @NotNull
  public static List<String> toString(Object... items) {
    List<String> out = new ArrayList<>();
    for (Object o : items)
      if (o instanceof String) out.add((String) o);
      else out.add(o.toString());
    return out;
  }

  @NotNull
  public static Object[] toArray(List<Object> list) {
    Object[] out = new Object[list.size()];
    return list.toArray(out);
  }

  @NotNull
  public static String[] toStringArray(List<String> list) {
    String[] out = new String[list.size()];
    return list.toArray(out);
  }

  @NotNull
  public static <T> List<T> toList(@NotNull T[] items) {
    return toList(Arrays.asList(items));
  }

  @NotNull
  public static <T> List<T> toList(@NotNull Collection<T> collection) {
    return new ArrayList<>(collection);
  }

  public static <ANY> ANY arrestIsNull(ANY value) {
    assert value != null;
    return value;
  }

  public static <ANY> ANY arrestIsNull(ANY value, String message) {
    if (value == null) throw new IllegalStateException(message);
    return value;
  }

  public static void arrestNotNull(Object value, String message) {
    if (value != null) throw new IllegalStateException(message);
  }

  public Process exec(String[] parameters, String[] environment, File directory){
    try { return jrt.exec(parameters, environment, directory); }
    catch (IOException e) { throw new RuntimeException(e); }
  }

  private Runtime() {}

  /**
   * OS.Kind ostype=OS.getOperatingSystemKind();
   * switch (ostype) {
   * case Windows: break;
   * case MacOS: break;
   * case Linux: break;
   * case Other: break;
   * }
   */
  public static final class OS {

    private static OS.Kind thisKind;
    private static String
        lineSeparator = System.getProperty("line.separator"),
        pathSeparator = File.pathSeparator,
        fileSeparator = File.separator,
        userName,
        userHome;

    static {

      String thisOs = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

      if ((thisOs.indexOf("mac") >= 0) || (thisOs.indexOf("darwin") >= 0)) {
        thisKind = OS.Kind.MacOS;
      } else if (thisOs.indexOf("win") >= 0) {
        thisKind = OS.Kind.Windows;
      } else if (thisOs.indexOf("nux") >= 0) {
        thisKind = OS.Kind.Linux;
      } else {
        thisKind = OS.Kind.Other;
      }

    }

    private OS() throws OperationNotSupportedException {throw new OperationNotSupportedException();}

    public static boolean isWindows() { return thisKind.equals(OS.Kind.Windows); }

    public static boolean isLinux() { return thisKind.equals(OS.Kind.Linux); }

    public static boolean isMacOS() { return thisKind.equals(OS.Kind.MacOS); }

    public static boolean isOtherOperatingSystem() { return thisKind.equals(OS.Kind.Other); }


    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    public static OS.Kind getOperatingSystemKind() {
      return thisKind;
    }

    public static String getLineSeparator() {
      return lineSeparator;
    }

    public static String getPathSeparator() {
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

  public static class Network {

    private static InetAddress ip;

    public static String getLocalNetworkAddress() {
      try {
        ip = InetAddress.getLocalHost();
        return ip.toString();
      }
      catch (UnknownHostException e) {
        e.printStackTrace();
      }
      return null;
    }

    public static String getLocalHostName() {
      if (ip == null) getLocalNetworkAddress();
      return ip.getHostName();
    }

  }
}
