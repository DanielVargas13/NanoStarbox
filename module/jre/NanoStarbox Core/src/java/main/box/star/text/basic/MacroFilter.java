package box.star.text.basic;

import box.star.contract.NotNull;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.*;

/**
 * Uses Basic Text Scanner to create an inline text macro processor,
 * with recursive parse call capability using shared environment and
 * object map.
 *
 */
public class MacroFilter implements Cloneable {

  protected MacroFilter clone(Scanner source) {
    try {
      MacroFilter clone = (MacroFilter) super.clone();
      clone.scanner = source;
      return clone;
    }
    catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
  }

  public Scanner scanner;
  public Map<String, Command> commandMap = new Hashtable<>();
  public Map<String, Object> objectMap = new Hashtable<>();
  public Map<String, String> environment = new Hashtable<>();

  public String parse(Scanner source){
    MacroFilter macro = clone(source);
    return macro.scanner.run(main);
  }

  public interface Command {
    String run(MacroFilter context, Stack<String> parameters);
  }

  String exec(String command, Stack<String> parameters){
    if (commandMap.containsKey(command)){
      Command action = commandMap.get(command);
      return action.run(this, parameters);
    }
    throw scanner.syntaxError("unknown command", new IllegalArgumentException(command));
  }

  ScannerMethod main = new ScannerMethod("MacroFilter"){
    @Override
    protected boolean scan(@NotNull Scanner scanner) {
      if (current() == '%') {
        char next = scanner.next();
        if (next == '(') swap(scanner.run(submacro));
        else scanner.back();
      }
      return true;
    }
  };

  static char[] breakMap = new Assembler(MAP_ASCII_ALL_WHITE_SPACE).merge(')').toArray();

  ScannerMethod submacro = new ScannerMethod("MacroFilterFunction"){

    /**
     * Disable buffer collection
     */
    @Override
    protected void collect(@NotNull Scanner scanner, char character) {}

    Stack<String> params;
    MacroFilter context = MacroFilter.this;

    /**
     * Called by the scanner to signal that a new method call is beginning.
     * <p>
     * if you override this, call the super method to initialize the input buffer.
     * <code>super(scanner, parameters); ... return sourceBuffer</code>
     *
     * @param scanner    the host scanner
     * @param parameters the parameters given by the caller.
     */
    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      params = new Stack<>();
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (zeroTerminator(scanner, character)){
        scanner.nextCharacter(')', false);
      } else if (character == '%') {
        char next = scanner.next();
        if (next == '(') swap(scanner.run(submacro));
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
      return context.exec(command, params);
    }

  };

}
