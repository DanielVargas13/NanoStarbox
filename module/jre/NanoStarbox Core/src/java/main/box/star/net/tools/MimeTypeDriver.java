package box.star.net.tools;

import box.star.content.MimeTypeMap;
import box.star.content.MimeTypeScanner;
import box.star.net.WebService;

import java.util.HashSet;

/**
 * <p>{@link MimeTypeDriver}s create {@link ServerResult}s from {@link ServerContent} using a server environment such as the {@link WebService}.</p>
 * <br>
 *
 */
public interface MimeTypeDriver {

  /**
   * <p>Creates a mime-formatted {@link ServerResult} from a {@link ServerContent} source.</p>
   *
   * @param content the server content
   * @return the mime formatted server result
   */
  ServerResult createMimeTypeResult(ServerContent content);

  interface WithMediaMapControlPort {
    void configureMimeTypeController(MimeTypeMap controlPort);
  }

  interface WithIndexFileListControlPort {
    void configureIndexFileList(HashSet<String> indexFiles);
  }

  interface WithMimeTypeScanner extends MimeTypeScanner {}
}
