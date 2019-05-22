package box.star.shell;

import java.util.concurrent.ConcurrentHashMap;

public class StreamTable extends ConcurrentHashMap<Integer, Stream> {
  {
    put(0, new ReadableStream(System.in));
    put(1, new WritableStream(System.out));
    put(2, new WritableStream(System.out));
  }
}
