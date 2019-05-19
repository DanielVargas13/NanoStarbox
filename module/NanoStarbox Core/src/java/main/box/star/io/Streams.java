package box.star.io;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.URI;

public class Streams {

  private static final int BUFFER_SIZE = 8192;

  public static final void transfer(@NotNull InputStream source, @NotNull OutputStream dest) throws IOException {
    byte[] buf = new byte[BUFFER_SIZE];
    int n;
    while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
    dest.close();
    source.close();
  }

  public static String readWholeString(@NotNull InputStream is) throws IOException {
    return new String(readAllBytes(is));
  }

  public static String[] readAllLines(@NotNull InputStream is) throws IOException {
    return readWholeString(is).split("\n");
  }

  public static void writeWholeString(OutputStream os, String data) throws IOException {
    os.write(data.getBytes());
  }

  public static byte[] readAllBytes(@NotNull InputStream is) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    transfer(is, out);
    return out.toByteArray();
  }

  public static void writeAllBytes(OutputStream os, byte[] data) throws IOException {
    os.write(data);
  }

  public static Serializable readSerializable(@NotNull InputStream is) throws IOException, ClassNotFoundException {
    return (Serializable) new ObjectInputStream(is).readObject();
  }

  public static void writeSerializable(OutputStream os, Serializable object) throws IOException {
    new ObjectOutputStream(os).writeObject(object);
  }

  public static InputStream getUriStream(URI link) {
    try {
      return link.toURL().openConnection().getInputStream();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getUriText(URI link) throws IOException {
    return readWholeString(getUriStream(link));
  }

  public static InputStream getResourceAsStream(String path) {
    //name = resolveName(name);
    ClassLoader cl = Streams.class.getClassLoader();
    if (cl == null) {
      // A system class.
      return ClassLoader.getSystemResourceAsStream(path);
    }
    return cl.getResourceAsStream(path);
  }

  public static String getFileText(String path) {
    try {
      return readWholeString(new FileInputStream(path));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getResourceText(String path) {
    try {
      return readWholeString(getResourceAsStream(path));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static InputStream getInputStream(File source) {
    try {
      return new FileInputStream(source);
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
