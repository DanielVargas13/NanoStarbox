package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.net.http.IHTTPSession;

import java.util.HashMap;
import java.util.Map;

public class ContentProvider {

  private static final Map<String, String> mimeTypePaths = new HashMap<>();
  private final MimeTypeMap mimeTypeMap;
  private final String baseUri;

  final public void setPathMimeType(String path, String mimeType){
    mimeTypePaths.put(path, mimeType);
  }

  final public String getMimeType(String uri){
    if (mimeTypePaths.containsKey(uri)) return mimeTypePaths.get(uri);
    return mimeTypeMap.get(mimeTypeMap.scanFileExtension(uri));
  }

  final public String getBaseUri() { return baseUri; }

  public ContentProvider(MimeTypeMap mimeTypeMap, String baseUri){
    this.baseUri = baseUri;
    this.mimeTypeMap = mimeTypeMap;
  }

  public ServerContent getContent(IHTTPSession session) {
    return null;
  }

}
