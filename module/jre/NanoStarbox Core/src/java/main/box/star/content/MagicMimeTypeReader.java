package box.star.content;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

public interface MagicMimeTypeReader {
  String getMimeType(BufferedInputStream source);
}
