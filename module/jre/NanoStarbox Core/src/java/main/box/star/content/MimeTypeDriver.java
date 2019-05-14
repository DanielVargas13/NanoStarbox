package box.star.content;

import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;

public interface MimeTypeDriver {
  ServerResult createMimeTypeResult(ServerContent content);
}
