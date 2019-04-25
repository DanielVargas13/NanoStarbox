package box.star.fn.sh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Streams {

  public final static Integer STDIN = 0, STDOUT = 1, STDERR = 2;

  private SharedMap<Integer, Closeable> streams;

  public Streams(Closeable[] streams){
    this.streams = new SharedMap<Integer, Closeable>();
    for (int i = 0; i < streams.length; i++) {
      set(i, streams[i]);
    }
  }

  public Streams() {
    this(null, System.out, System.err);
  }

  public Streams(InputStream stdin, OutputStream stdout, OutputStream stderr) {
    streams = new SharedMap<>();
    set(STDIN, stdin);
    set(STDOUT, stdout);
    set(STDERR, stderr);
  }

  Streams(Map<Integer, Closeable> streams) {
   this.streams = new SharedMap<>(streams.size());
   this.streams.putAll(streams);
  }

  public <ANY> ANY get(Integer key) {
    return (ANY) streams.get(key);
  }

  public void set(Integer key, Closeable value) {
    if (value == null) { streams.remove(key);
      return;
    }
    streams.put(key, value);
  }

  public void remove(Integer key) {
    streams.remove(key);
    return;
  }

  public List<Closeable> values(){
    return new ArrayList(streams.values());
  }

  public List<Integer> keyList() {
    return new ArrayList<>(streams.keySet());
  }

  public Streams copy(){
    return new Streams(streams);
  }

  void layer(Streams overlay){
    streams.putAll(overlay.streams);
  }

  Streams createLayer(Streams overlay){
    Streams copy = copy();
    if (overlay != null) copy.streams.putAll(overlay.streams);
    return copy;
  }

  void close(int stream){
    Closeable x = streams.get(stream);
    if (x != null) {
      try {x.close();}
      catch (IOException e) {}
    }
  }

  public boolean hasStream(Integer key) {
    return streams.containsKey(key);
  }

}
