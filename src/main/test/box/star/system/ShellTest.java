package box.star.system;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellTest {

    Shell shell = Shell.createShell(new Shell.Main("main") {
        @Override
        public int main(String[] parameters) {
            shell.call("echo", "hello world");
//            Shell shell2 = shell.createShell(new Shell.Main("test"){
//                @Override
//                public int main(String[] parameters) {
//                    Shell.Action task = shell.createAction("echo");
//                    return shell.run(task, parameters);
//                }
//            });
//            return shell2.main(parameters);
            return shell.status;
        }
    });

    {
        shell.traceLifeCycle(true);


        shell.registerActionModel(new Shell.Method("echo"){
            @Override
            public Object main(Object[] parameters) {
                String[] stringArray = Arrays.copyOf(parameters, parameters.length, String[].class);
                Stack<String> print = new Stack<>();
                print.addAll(Arrays.asList(stringArray));
                //print.remove(0);
                boolean lineMode = true;
                while (print.get(0).startsWith("-")) {
                    if (print.get(0).equals("-n")) {
                        lineMode = false;
                        print.remove(0);
                        continue;
                    }
                    break;
                }
                try {
                    String out = String.join(" ", print) +  "\n";
                    System.out.write(out.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 1;
            }

          @Override
          public boolean match(String name) {
            return (name.startsWith(":"));
          }
        });
    }

    @Test void getTaskGroup(){
        assertEquals(null, Shell.ActionGroup.getCurrentGroup());
    }

    @Test void main() throws InterruptedException {
        assertEquals(1, shell.main(":---", "hello world"));
    }

}