package box.star;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Network {

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
