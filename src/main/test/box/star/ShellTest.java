package box.star;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell sh = new Shell(null, new Shell.IShellExecutive() {

        @Override
        public void main(String[] parameters) {
            try {
                Thread.sleep(Integer.valueOf(parameters[0]));
            } catch (InterruptedException e) {}
        }

        @Override
        public int exitStatus() {
            return 12;
        }

    });

    @Test void run(){
        sh.exec("15000");
        assertEquals(12, sh.getExitCode());
    }

}