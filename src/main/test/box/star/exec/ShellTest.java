package box.star.exec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    @Test void main() {
        Shell shell = new Shell();
        Streams out = new Streams();
        shell.run(shell.createSubshell(out, new Shell.ISubshell() {
            @Override
            public void run(Shell shell) {
                shell.start(shell.createProcess(null), "cmd", "/c", "dir");
                shell.setExitCode(0);
            }
        }));

        assertEquals(0, shell.getExitCode());

    }
}