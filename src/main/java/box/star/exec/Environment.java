package box.star.exec;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class Environment extends ConcurrentHashMap<String, String> {

    Environment() {
        this.putAll(System.getenv());
    }

    Environment(Shell shell) {
        this.putAll(shell.environment);
    }

    public final String[] compile(){
        String[] out = new String[size()];
        int i = 0;
        for (String key : keySet()) {
            out[i++] = key + "=" + get(key);
        }
        return out;
    }

    public File changeDirectory(File currentDirectory) {
        this.put("PWD", currentDirectory.getPath());
        return currentDirectory;
    }

}
