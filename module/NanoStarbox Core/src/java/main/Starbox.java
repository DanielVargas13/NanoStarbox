import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class Starbox {

  public static class Runtime {

    private static Runtime ourInstance = new Runtime();

    public static Runtime getInstance() {
      return ourInstance;
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

      private static Kind thisKind;
      private static String
          lineSeparator = System.getProperty("line.separator"),
          pathSeparator = File.pathSeparator,
          fileSeparator = File.separator,
          userName,
          userHome;

      static {

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

      }

      private OS() throws OperationNotSupportedException {throw new OperationNotSupportedException();}

      public static boolean isWindows() { return thisKind.equals(Kind.Windows); }

      public static boolean isLinux() { return thisKind.equals(Kind.Linux); }

      public static boolean isMacOS() { return thisKind.equals(Kind.MacOS); }

      public static boolean isOtherOperatingSystem() { return thisKind.equals(Kind.Other); }


      /**
       * detect the operating system from the os.name System property and cache
       * the result
       *
       * @returns - the operating system detected
       */
      public static Kind getOperatingSystemKind() {
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

}
