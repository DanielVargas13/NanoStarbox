package box.star.bin.sh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class Executive {

  public static final int
      IO_READABLE = 0,
      IO_WRITABLE = 1,
      IO_ERROR = 2;

  private final Process host;
  private Thread readable, writable, error;

  public Executive(Process host) {
    this.host = host;
  }

  private static final void transfer(InputStream source, OutputStream dest) throws IOException {
    byte[] buf = new byte[8192];
    int n;
    while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
    dest.flush();
    if (!System.out.equals(dest) && !System.err.equals(dest)) dest.close();
    if (!System.in.equals(source)) source.close();
  }

  public Executive readInputFrom(InputStream input) {
    if (input == null) return this;
    (readable = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          transfer(input, get(IO_WRITABLE));
        }
        catch (IOException e) {
          //e.printStackTrace();
        }
      }
    }, "Reader")).start();
    return this;
  }

  public Executive writeOutputToCommand(Command command) {
    command.exec();
    writeOutputTo(command.executive.get(1));
    return command.executive;
  }

  public Executive writeOutputTo(Closeable output) {
    if (output == null) return this;
    if (output instanceof Command) {
      return writeOutputToCommand((Command) output);
    }
    (writable = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          transfer(get(IO_READABLE), (OutputStream) output);
        }
        catch (IOException e) {
          //e.printStackTrace();
        }
      }
    }, "Writer")).start();
    return this;
  }

  public Executive writeErrorTo(OutputStream output) {
    if (output == null) return this;
    (error = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          transfer(get(IO_ERROR), output);
        }
        catch (IOException e) {
          //e.printStackTrace();
        }
      }
    }, "Error")).start();
    return this;
  }

  public Streams getStreams() {
    return new Streams(get(0), get(1), get(2));
  }

  @SuppressWarnings("unchecked")
  public <ANY> ANY get(int stream) {
    switch (stream) {
      case IO_WRITABLE: // = 1
        return (ANY) host.getOutputStream();
      case IO_READABLE: // = 0
        return (ANY) host.getInputStream();
      case IO_ERROR: // = 2
        return (ANY) host.getErrorStream();
    }
    return null;
  }

  public int waitFor() throws InterruptedException {return host.waitFor();}

  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {return host.waitFor(timeout, unit);}

  public int exitValue() {return host.exitValue();}

  public void destroy() {host.destroy();}

  public Process destroyForcibly() {return host.destroyForcibly();}

  public boolean isAlive() {return host.isAlive();}

}
