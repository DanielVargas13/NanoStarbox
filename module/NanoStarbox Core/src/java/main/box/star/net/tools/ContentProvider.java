package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;

import java.util.HashMap;
import java.util.Map;

public class ContentProvider {

  private static final Map<String, String> mimeTypePaths = new HashMap<>();
  private final String baseUri;
  private MimeTypeMap mimeTypeMap;

  // <user-methods>

  public ContentProvider(String baseUri) {
    this.baseUri = baseUri;
  }

  final public void setMimeTypeMap(MimeTypeMap mimeTypeMap) {
    this.mimeTypeMap = mimeTypeMap;
  }

  final public void setUriMimeType(String uri, String mimeType) {
    mimeTypePaths.put(uri, mimeType);
  }

  final protected String getUriMimeType(String uri) {
    if (mimeTypePaths.containsKey(uri)) return mimeTypePaths.get(uri);
    else if (mimeTypeMap != null) return mimeTypeMap.get(mimeTypeMap.scanFileExtension(uri));
    else return MimeTypeMap.DEFAULT_MIME_TYPE;
  }

  final public String getBaseUri() { return baseUri; }

  final protected ServerContent redirect(String location) {
    return new ServerContent(Response.redirect(location));
  }

  // </user-methods>

  final protected ServerContent notFound(String location) {
    return new ServerContent(Response.notFoundResponse());
  }

  public ServerContent getContent(IHTTPSession session) {
    return null;
  }

}
