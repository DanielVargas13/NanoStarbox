package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.content.MimeTypeScanner;
import box.star.net.WebService;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;

import java.util.HashSet;

/**
 * <p>{@link MimeTypeDriver}s create {@link ServerResult}s from {@link ServerContent} using a server environment such as the {@link WebService}.</p>
 * <br>
 * @param <SERVICE> the server environment
 */
public interface MimeTypeDriver<SERVICE> {

  interface WithMediaMapControlPort {
    void configureMimeTypeController(MimeTypeMap controlPort);
  }

  interface WithIndexFileListControlPort {
    void configureIndexFileList(HashSet<String> indexFiles);
  }

  interface WithMimeTypeScanner extends MimeTypeScanner {}

  /**
   * <p>Creates a mime-formatted {@link ServerResult} from a {@link ServerContent} source.</p>
   * @param server the server environment
   * @param content the server content
   * @return the mime formatted server result
   */
  ServerResult createMimeTypeResult(SERVICE server, ServerContent content);
}
