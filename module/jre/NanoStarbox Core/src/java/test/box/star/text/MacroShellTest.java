package box.star.text;

import box.star.text.MacroShell;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroShellTest {

  Scanner scanner = new Scanner("test", "%(menu \"%(list '%[JDK_HOME]' '%[TEMP]' \\64test-bound-field-expansion)\")");

  @Test void main(){
    MacroShell context = new MacroShell(System.getenv());
    context.environment.put("what", "Menu");
    context.addCommand("list", new MacroShell.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    context.addCommand("alert", new MacroShell.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return "Java";
      }
    });
    context.addCommand("menu", new MacroShell.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        StringBuilder out = new StringBuilder();
        out.append(call("alert"));
        out.append(eval(" %[what]: \n\n"));
        // spec: first element is variable list; platform: Windows; tweak variables for local system effects.
        for (String p: parameters.firstElement().split(", ")){
          out.append("\t"+eval(p)+"\n");
        }
        return out.toString();
      }
    });
    System.out.println(context.start(scanner));
  }

}