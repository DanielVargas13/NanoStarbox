package box.star;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    Shell.IShellControllerFactory shellFactory = new Shell.IShellControllerFactory() {

        @Override
        public Shell.IShellController createMainController() {
            return new Shell.IShellController() {
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
            };
        }

        @Override
        public Shell.IShellController createSubController(Shell.IShellController context) {
            return createMainController();
        }

    };

    Shell sh = new Shell(shellFactory);

    @Test void run(){
        sh.exec("650");
        assertEquals(12, sh.getExitStatus());
    }

}