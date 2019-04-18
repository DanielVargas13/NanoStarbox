package box.star;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell sh = new Shell(null) {
        @Override
        public void main(String[] parameters) {
            try {
                Shell subshell = new Shell(this, null){
                    @Override
                    public void main(String[] parameters) {
                        PrintWriter error = sh.getPrintWriter(2);
                        error.println(parameters[0]);
                        error.flush();
                        super.main(parameters);
                    }
                    @Override
                    public int exitStatus() {
                        return super.exitStatus();
                    }
                };
                subshell.exec("hello world");
                Thread.sleep(Integer.valueOf(parameters[0]));
            } catch (Exception e) {e.printStackTrace();}
        }
        @Override
        public int exitStatus() {
            return 12;
        }
    };

    @Test void run(){
        sh.exec("650");
        assertEquals(12, sh.getExitStatus());
    }

}