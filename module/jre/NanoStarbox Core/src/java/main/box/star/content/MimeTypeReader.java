package box.star.content;

import java.io.RandomAccessFile;

public interface MimeTypeReader {
  // return null if you can't identify the file contents
  String getMimeTypeMagic(RandomAccessFile data);
}
