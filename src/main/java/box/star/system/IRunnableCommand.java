package box.star.system;

import java.io.Closeable;

public interface IRunnableCommand {
    String getBackgroundThreadName();
    boolean isBackgroundMode();
    String[] getParameters();
    Closeable[] getStreams();
    void onStart(Closeable[] pipe);
    void onExit(int value);
    void onException(Exception e);
}
