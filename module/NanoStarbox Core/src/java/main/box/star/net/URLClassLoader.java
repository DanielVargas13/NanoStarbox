package box.star.net;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

public class URLClassLoader extends java.net.URLClassLoader {
  public URLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }
  public URLClassLoader(URL[] urls) {
    super(urls);
  }
  public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
  }
  public void addURL(URL url){
    super.addURL(url);
  }
}
