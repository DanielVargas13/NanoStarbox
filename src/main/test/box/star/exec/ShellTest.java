package box.star.exec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    @Test void main() {
        Shell shell = new Shell();
        shell.run(shell.createSubshell(new Shell.ISubshell() {
            @Override
            public void run(Shell shell) {
                Process process = shell.createProcess();
                process.io.set(1, 2); // redirect stdout to stderr
                shell.start(process,"cmd", "/c", "dir");
                shell.setExitCode(process.getExitCode());
            }
        }));

        assertEquals(0, shell.getExitCode());

    }
}