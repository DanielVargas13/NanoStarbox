package box.star.text.basic;

import org.junit.jupiter.api.Test;

class MacroFilterTest {

  String macroText = "%(fire wow this is a macro function";
  Scanner scanner = new Scanner("macro text", macroText);

  @Test void macroFilter(){
    MacroFilter filter = new MacroFilter(scanner);
    filter.commandMap.put("fire", new MacroFilter.Command() {
      @Override
      public String run(MacroFilter context, String... parameters) {
        return String.join(", ", parameters);
      }
    });
    System.out.print(filter.parse());
  }
}