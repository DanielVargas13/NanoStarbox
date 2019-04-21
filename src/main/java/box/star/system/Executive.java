package box.star.system;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class Executive extends ThreadGroup {

    private static final void transfer(InputStream source, OutputStream dest) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
        dest.flush();
        if (!System.out.equals(dest) && !System.err.equals(dest)) dest.close();
        if (!System.in.equals(source)) source.close();
    }

    private Process host;
    private Thread readable, writable, error;
    private long[] waitTimers;

    Executive(Process host, long[] waitTimers) {
        super("Starbox Environment Executive");
        this.host = host;
        if (waitTimers != null) this.waitTimers = waitTimers;
    }

    public Executive readInputFrom(InputStream input) {
        if (input == null) return this;
        (readable = new Thread(this, new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(input, get(Environment.IO_WRITABLE));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Reader")).start();
        return this;
    }

    public Executive writeOutputTo(OutputStream output) {
        if (output == null) return this;
        (writable = new Thread(this, new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(get(Environment.IO_READABLE), output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Writer")).start();
        return this;
    }

    public Executive writeErrorTo(OutputStream output) {
        if (output == null) return this;
        (error = new Thread(this, new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(get(Environment.IO_ERROR), output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Error")).start();
        return this;
    }

    public int getExitValue() {
        if (writable != null) try {
            if (waitTimers[Environment.IO_WRITABLE] > 0) writable.join(waitTimers[Environment.IO_WRITABLE]);
            else writable.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (readable != null) try {
            if (waitTimers[Environment.IO_READABLE] > 0) readable.join(waitTimers[Environment.IO_READABLE]);
            else readable.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (error != null) try {
            if (waitTimers[Environment.IO_ERROR] > 0) error.join(waitTimers[Environment.IO_ERROR]);
            else error.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (waitTimers[Environment.WT_PROCESS] > 0) host.waitFor(waitTimers[Environment.WT_PROCESS], TimeUnit.MILLISECONDS);
            else host.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return host.exitValue();
    }

    public Closeable[] getStreams(){
        return new Closeable[]{get(0), get(1), get(2)};
    }

    @SuppressWarnings("unchecked")
    public <ANY> ANY get(int stream) {
        switch (stream) {
            case Environment.IO_WRITABLE: // = 1
                return (ANY) host.getOutputStream();
            case Environment.IO_READABLE: // = 0
                return (ANY) host.getInputStream();
            case Environment.IO_ERROR: // = 2
                return (ANY) host.getErrorStream();
        }
        return null;
    }

}
