package box.star.text.basic;

import box.star.contract.NotNull;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.*;

/**
 * Uses Basic Text Scanner to create an inline text macro processor.
 *
 */
public class MacroFilter {

  Scanner scanner;

  public MacroFilter(Scanner source){this.scanner = source;}

  public interface Command {
    String run(MacroFilter context, String... parameters);
  }

  public Map<String, Command> commandMap = new Hashtable<>();

  String exec(String command, String... parameters){
    if (commandMap.containsKey(command)){
      Command action = commandMap.get(command);
      return action.run(this, parameters);
    }
    throw scanner.syntaxError("unknown command", new IllegalArgumentException(command));
  }

  public String parse(){
    return scanner.run(macroFilterMain);
  }

  ScannerMethod macroFilterMain = new ScannerMethod("MacroFilter"){
    /**
     * <p>Signals whether or not the process should continue reading input.</p>
     *
     * <p>The default method returns true.</p>
     *
     * @param scanner
     * @return true if the TextScanner should read more input.
     */
    @Override
    protected boolean scanning(@NotNull Scanner scanner) {
      if (peek() == '%') {
        char next = scanner.next();
        if (next == '(') swap(scanner.run(macroFilterFunction));
        else scanner.back();
      }
      return true;
    }

  };

  static char[] breakMap = new Assembler(MAP_ASCII_ALL_WHITE_SPACE).merge(')').toArray();

  ScannerMethod macroFilterFunction = new ScannerMethod("MacroFilterFunction"){
    Stack<String> params;
    MacroFilter context = MacroFilter.this;

    /**
     * Create the character buffer
     *
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     */
    @Override
    protected void reset() {
      super.reset();
      params = new Stack<>();
    }

    @Override
    protected void collect(@NotNull Scanner scanner, char character) {
      super.collect(scanner, character);
    }

    @Override
    protected boolean terminator(@NotNull Scanner scanner, char character) {
      if (zeroTerminator(scanner, character)){
        scanner.nextCharacter(')', false);
      } else if (character == '%') {
        char next = scanner.next();
        if (next == '(') swap(scanner.run(macroFilterFunction));
        else scanner.back();
      } else if (character == ')') {
        return true;
      } else {
        String param = character + scanner.nextField(breakMap);
        if (param.length() > 0) params.add(param);
      }
      return false;
    }

    @Override
    protected @NotNull String compile(@NotNull Scanner scanner) {
      String command = params.firstElement();
      params.remove(0);
      String[] parameters = new String[params.size()];
      params.toArray(parameters);
      return context.exec(command, parameters);
    }

  };

}
