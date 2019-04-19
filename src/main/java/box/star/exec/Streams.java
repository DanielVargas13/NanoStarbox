package box.star.exec;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Streams {

    private static final Map<Integer, Closeable> defaultStreams = new Hashtable<>();

    static {
        defaultStreams.put(0, System.in);
        defaultStreams.put(1, System.out);
        defaultStreams.put(2, System.err);
    }

    private ConcurrentHashMap<Integer, Closeable> streams;

    Streams() {
        streams = new ConcurrentHashMap<>(defaultStreams);
    }

    public Streams(Shell shell) {
        streams = new ConcurrentHashMap<>(shell.io.streams);
    }

    void input(InputStream is) {
        streams.put(0, is);
    }

    InputStream input(){
        return (InputStream) streams.get(0);
    }

    void output(OutputStream os) {
        streams.put(1, os);
    }

    OutputStream output(){
        return (OutputStream) streams.get(1);
    }

    void error(OutputStream os) {
        streams.put(2, os);
    }

    OutputStream error(){
        return (OutputStream) streams.get(2);
    }

    Closeable getStream(int stream) {
        return streams.get(stream);
    }

    OutputStream getOutputStream(int stream) {
        Closeable value = getStream(stream);
        if (value instanceof OutputStream) return (OutputStream) value;
        throw new RuntimeException("stream #"+stream+" is not an OutputStream");
    }

    InputStream getInputStream(int stream) {
        Closeable value = getStream(stream);
        if (value instanceof InputStream) return (InputStream) value;
        throw new RuntimeException("stream #"+stream+" is not an InputStream");
    }

    void setStream(int stream, Closeable value) {
        streams.put(stream, value);
    }

    public void map(Streams streams) {
        this.streams.putAll(streams.streams);
    }

}