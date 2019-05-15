package box.star.net.tools;

import box.star.net.WebService;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;

/**
 * <p>{@link MimeTypeDriver}s create {@link ServerResult}s from {@link ServerContent} using a server environment such as the {@link WebService}.</p>
 * <br>
 * @param <SERVICE> the server environment
 */
public interface MimeTypeDriver<SERVICE> {
  /**
   * <p>Creates a mime-formatted {@link ServerResult} from a {@link ServerContent} source.</p>
   * @param server the server environment
   * @param content the server content
   * @return the mime formatted server result
   */
  ServerResult createMimeTypeResult(SERVICE server, ServerContent content);
}
