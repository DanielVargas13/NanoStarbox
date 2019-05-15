package box.star.net.tools;

import java.io.File;

public interface NativeContentProvider {
  File getFile(String uri);
}
