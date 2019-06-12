package box.star.unix.shell.runtime;

import box.star.unix.shell.io.ReadableStream;
import box.star.unix.shell.io.Stream;
import box.star.unix.shell.io.WritableStream;

import java.util.Hashtable;

public class StreamMap extends Hashtable<Integer, Stream> {
  StreamMap loadFactoryStreams() {
    put(0, new ReadableStream(System.in));
    put(1, new WritableStream(System.out));
    put(2, new WritableStream(System.out));
    return this;
  }
}
