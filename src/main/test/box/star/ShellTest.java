package box.star;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell sh = new Shell(new Shell.IShellExecutive() {

        @Override
        public void main(String[] parameters) {
            try {
                PrintWriter error = sh.getPrintWriter(2);
                error.println("hello world");
                error.flush();
                Thread.sleep(Integer.valueOf(parameters[0]));
            } catch (Exception e) {e.printStackTrace();}
        }

        @Override
        public int exitStatus() {
            return 12;
        }

    });

    @Test void run(){
        sh.exec("15000");
        assertEquals(12, sh.getExitStatus());
    }

}