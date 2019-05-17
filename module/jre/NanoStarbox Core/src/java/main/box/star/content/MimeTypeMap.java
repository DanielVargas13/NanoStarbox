package box.star.content;

import box.star.io.Streams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MimeTypeMap extends HashMap<String, String> {

  public static final String MIME_SEPARATOR = " ";
  public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private static final Map<String, String> mimeTypePaths = new HashMap<>();
  private static final List<MimeTypeScanner> mimeTypeScanners = new ArrayList<>();

  public MimeTypeMap() {
    super(64);
    this.loadMimeTypesPropertyStream(
        Streams.getResourceAsStream(
            "META-INF/mime-type/default-mimetypes.properties"
        )
    );
  }

  public void loadMimeTypesPropertyStream(InputStream stream) {
    Properties properties = new Properties();
    try {
      properties.load(stream);
    }
    catch (IOException e) {
      throw new RuntimeException("can't load mime types from stream", e);
    }
    finally { try { stream.close(); } catch (Exception e) {} }
    this.putAll((Map) properties);
  }

  @Override
  public String put(String key, String value) {
    if (this.containsKey(key)) value = super.get(key) + MIME_SEPARATOR + value;
    return super.put(key, value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> map) {
    for (String key : map.keySet()) this.put(key, map.get(key));
  }

  public void setPathMimeType(String path, String mimeType) {
    mimeTypePaths.put(path, mimeType);
  }

  @Override
  public String get(Object key) {
    if (this.containsKey(key)) return super.get(key);
    else return mimeTypePaths.getOrDefault(key, DEFAULT_MIME_TYPE);
  }

  public String scanFileExtension(String fileName) {
    String name = new java.io.File(fileName).getName();
    List<String> extensions = new ArrayList<>();
    if (name.contains(".")) {
      for (String extension : super.keySet()) {
        if (name.endsWith(extension)) extensions.add(extension);
      }
      if (!extensions.isEmpty()) {
        String longest = "";
        for (String e : extensions) {
          if (e.length() > longest.length()) longest = e;
        }
        return longest;
      }
      return name.substring(name.lastIndexOf('.') + 1);
    }
    return "";
  }

  public String getFileExtensionForMimeType(String mimeType) {
    if (mimeType != null || mimeType.length() != 0) {
      for (String extension : super.keySet())
        if (super.get(extension).contains(mimeType)) return extension;
    }
    return DEFAULT_MIME_TYPE;
  }

//  public String scanMimeType(File file){
//    try {
//      FileInputStream fr = new FileInputStream(file);
//      BufferedInputStream br = new BufferedInputStream(fr);
//      String mimeType = scanMimeType(br);
//      br.close();
//      return mimeType;
//    } catch (Exception e){throw new RuntimeException(e);}
//  }

  public String scanMimeType(BufferedInputStream stream) {
    String mimeType;
    for (MimeTypeScanner scanner : mimeTypeScanners) {
      mimeType = scanner.scanMimeType(stream);
      if (mimeType != null) return mimeType;
    }
    return DEFAULT_MIME_TYPE;
  }

  public String[] parseMultiPartMimeType(String mimeType) {
    return mimeType.split(MIME_SEPARATOR);
  }

  public MimeTypeMap addMimeTypeScanner(MimeTypeScanner magicReader) {
    mimeTypeScanners.add(magicReader);
    return this;
  }

}

