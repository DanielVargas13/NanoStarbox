package box.star;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell sh = new Shell(null) {
        int myExitCode = 12;
        @Override
        public void main(String[] parameters) {
            try {
                Shell subShell = new Shell(this, null){
                    PrintWriter getPrintWriter(int stream) {
                        return new PrintWriter(getOutputStream(stream));
                    }
                    @Override
                    public void main(String[] parameters) {
                        PrintWriter error = getPrintWriter(STDERR);
                        error.println(parameters[0]);
                        error.flush();
                    }
                };
                subShell.exec("hello world");
                Thread.sleep(Integer.valueOf(parameters[0]));
            } catch (Exception e) {e.printStackTrace();}
        }
        @Override
        public int exitStatus() {
            return myExitCode;
        }
    };

    @Test void run(){
        sh.exec("650");
        assertEquals(12, sh.getExitStatus());
    }

}