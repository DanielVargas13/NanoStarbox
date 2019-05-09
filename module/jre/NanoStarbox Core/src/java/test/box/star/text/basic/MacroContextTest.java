package box.star.text.basic;

import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroContextTest {

  @Test void main(){
    MacroContext macroContext = new MacroContext(System.getenv());
    macroContext.addCommand("test", new MacroContext.Command(){
      @Override
      protected String run(String command, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    Scanner scanner = new Scanner("test", "Java Menu: %(test \"%[JDK_HOME]\" %[JAVA_HOME])");
    System.out.println(scanner.run(new MacroRunner(macroContext)));
  }
}