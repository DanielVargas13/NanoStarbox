package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.net.http.IHTTPSession;

public class ContentProvider {

  private final MimeTypeMap mimeTypeMap;
  private final String baseUri;

  final public String getMimeType(String uri){
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
