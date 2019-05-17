package box.star.content;

import java.io.BufferedInputStream;

public interface MimeTypeScanner {
  String scanMimeType(BufferedInputStream source);
}
