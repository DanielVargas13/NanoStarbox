package box.star.text.basic;

import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroScannerTest {

  Scanner scanner = new Scanner("test", "%(menu \"%(list '%[JDK_HOME]' '%[TEMP]')\")");

  @Test void main(){
    MacroScanner macroContext = new MacroScanner(System.getenv());
    macroContext.environment.put("what", "Menu");
    macroContext.addCommand("list", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    macroContext.addCommand("alert", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return "Java";
      }
    });
    macroContext.addCommand("menu", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        StringBuilder out = new StringBuilder();
        out.append(call("alert", null));
        out.append(eval(" %[what]: \n\n"));
        // spec: first element is variable list; platform: Windows; tweak variables for local system effects.
        for (String p: parameters.firstElement().split(", ")){
          out.append("\t"+eval(p)+"\n");
        }
        return out.toString();
      }
    });
    System.out.println(macroContext.start(scanner));
  }

}