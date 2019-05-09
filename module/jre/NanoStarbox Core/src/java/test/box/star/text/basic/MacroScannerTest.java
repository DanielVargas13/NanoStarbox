package box.star.text.basic;

import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroScannerTest {

  Scanner scanner = new Scanner("test", "%(menu %(list \"%[JDK_HOME]\" %[JAVA_HOME]))");

  @Test void main(){
    MacroScanner macroContext = new MacroScanner(System.getenv());
    macroContext.environment.put("THIS", "Menu");
    macroContext.addCommand("list", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    macroContext.addCommand("say-java", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return "Java";
      }
    });
    macroContext.addCommand("menu", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return
            call("say-java", null)
              +eval(" %[THIS]: ")
                +String.join(" ", parameters);
      }
    });
    System.out.println(macroContext.start(scanner));
  }

}