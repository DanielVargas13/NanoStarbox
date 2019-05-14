package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.net.WebService;
import box.star.net.http.IHTTPSession;

public class ContentProvider {

  private final MimeTypeMap mimeTypeMap;
  private final String baseUri;

  final public String getMimeType(String uri){
    return mimeTypeMap.get(mimeTypeMap.getFileExtension(uri));
  }
  final public String getBaseUri() { return baseUri; }

  public ContentProvider(WebService webService, String baseUri){
    this.baseUri = baseUri;
    this.mimeTypeMap = webService.getMimeTypeMap();
    webService.mountContentProvider(this);
  }

  public ServerContent getContent(IHTTPSession session) {
    return null;
  }

}
