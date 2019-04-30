package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import box.star.bin.sh.Streams;
import box.star.contract.Nullable;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StreamCatalog<Host> {
  Host writeErrorTo(@Nullable OutputStream os);
  Host writeOutputTo(@Nullable OutputStream os);
  Host readInputFrom(@Nullable InputStream is);
  Host remove(Integer key);
  Host resetStreams();
  Host applyStreams(@Nullable Streams overlay);
  Host set(Integer key, @Nullable Closeable stream);
  <ANY> ANY get(Integer key);
  List<Integer> streams();
  boolean haveStream(Integer key);
  SharedMap<Integer, Closeable> exportStreams();
}
