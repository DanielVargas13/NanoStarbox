package box.star.text;

import box.star.text.basic.LegacyScanner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Stack;

class MacroShellTest {

  @Test
  void main() {
    LegacyScanner scanner = new LegacyScanner(new File("src/java/resource/macro-document-test"));
    MacroShell context = new MacroShell(System.getenv());
    context.environment.put("what", "Menu");
    context.addCommand("list", new MacroShell.Command() {
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    context.addCommand("alert", new MacroShell.Command() {
      @Override
      protected String run(String command, Stack<String> parameters) {
        return "Java";
      }
    });
    context.addCommand("menu", new MacroShell.Command() {
      @Override
      protected String run(String command, Stack<String> parameters) {
        StringBuilder out = new StringBuilder();
        out.append(call("alert"));
        out.append(eval(" %[what]: \n\n"));
        // spec: first element is variable list; platform: Windows; tweak variables for local system effects.
        for (String p : parameters.firstElement().split(", ")) {
          //out.append(p);
          out.append("\t" + eval(p) + "\n");
        }
        return out.toString();
      }
    });
    long startTime = System.nanoTime();
    String data = context.start(scanner);
    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    System.out.println(data);
    System.out.println("Execution time in milliseconds: " + ((double) timeElapsed) / ((double) 1000000));
  }

}