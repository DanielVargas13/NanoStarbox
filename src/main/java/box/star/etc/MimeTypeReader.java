package box.star.etc;

import java.io.File;
import java.io.RandomAccessFile;

public interface MimeTypeReader {
    // return null if you can't identify the file contents
    String getMimeTypeMagic(RandomAccessFile data);
}
