package box.star.bin.sh.promise;

import box.star.bin.sh.SharedMap;
import box.star.bin.sh.Streams;
import com.sun.istack.internal.Nullable;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StreamProvider<HOST> {
  HOST writeErrorTo(@Nullable OutputStream os);
  HOST writeOutputTo(@Nullable OutputStream os);
  HOST readInputFrom(@Nullable InputStream is);
  HOST remove(Integer key);
  HOST resetStreams();
  HOST applyStreams(@Nullable Streams overlay);
  HOST set(Integer key, @Nullable Closeable stream);
  <ANY> ANY get(Integer key);
  List<Integer> streams();
  boolean haveStream(Integer key);
  SharedMap<Integer, Closeable> exportStreams();
}
