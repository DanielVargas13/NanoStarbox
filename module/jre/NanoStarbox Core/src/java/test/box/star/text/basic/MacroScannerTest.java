package box.star.text.basic;

import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroScannerTest {

  Scanner scanner = new Scanner("test", "Java Menu: %(test \"%[JDK_HOME]\" %[JAVA_HOME])");

  @Test void main(){
    MacroScanner macroContext = new MacroScanner(System.getenv());
    macroContext.addCommand("test", new MacroScanner.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    System.out.println(macroContext.start(scanner));
  }

}