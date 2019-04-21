package box.star.system;

import java.io.Closeable;

public interface IRunnableCommand {
    ThreadGroup getBackgroundThreadGroup();
    String getBackgroundThreadName();
    boolean isBackgroundMode();
    String[] getParameters();
    Closeable[] getStreams();
    void onStart();
    void onExit(int value);
    void onException(Exception e);
}
