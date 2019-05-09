package box.star.text.basic;

import org.junit.jupiter.api.Test;

import java.util.Stack;

class MacroFilterTest {

  String macroText = "Hello World %(fire wow this is an expanded text macro)";
  Scanner scanner = new Scanner("macro text", macroText);

  @Test void macroFilter(){
    MacroFilter filter = new MacroFilter();
    filter.commandMap.put("fire", new MacroFilter.Command() {
      @Override
      public String run(MacroFilter context, Stack<String> parameters) {
        return String.join(", ", parameters);
      }
    });
    System.out.print(filter.parse(scanner));
  }
}