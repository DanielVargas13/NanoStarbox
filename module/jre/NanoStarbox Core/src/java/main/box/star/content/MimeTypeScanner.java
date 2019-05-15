package box.star.content;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

public interface MimeTypeScanner {
  String scanMimeType(BufferedInputStream source);
}
