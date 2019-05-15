package box.star.net.tools;

import box.star.net.WebService;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;

public interface MimeTypeDriver<SERVICE> {
  ServerResult createMimeTypeResult(SERVICE server, ServerContent content);
}
