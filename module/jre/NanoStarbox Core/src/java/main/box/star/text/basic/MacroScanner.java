package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.text.Char;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class MacroScanner {

  private final static char
      ENTER_PROCEDURE = '(', EXIT_PROCEDURE = ')',
      ENTER_OBJECT = '{', EXIT_OBJECT = '}',
      ENTER_VARIABLE = '[', EXIT_VARIABLE = ']';

  private static final char[] BREAK_PROCEDURE_MAP =
      new Char.Assembler(Char.MAP_ASCII_ALL_WHITE_SPACE)
          .merge(EXIT_PROCEDURE)
            .toArray();

  public char macroTrigger = '%';

  public static class Command implements Cloneable {
    protected Scanner scanner;
    protected MacroScanner main;

    protected String call(String name, Stack<String> parameters){
      return main.doCommand(scanner, name, parameters);
    }

    protected String eval(String source){
      return main.start("eval", source);
    }

    protected Stack<String> split(String source){
      ParameterBuilder pb = new ParameterBuilder();
      Scanner scanner = new Scanner("split", source+EXIT_PROCEDURE);
      Stack<String> parameters = new Stack<>();
      scanner.run(pb, main, parameters);
      return parameters;
    }

    protected void enterContext(Scanner scanner, MacroScanner main){
      this.scanner = scanner;
      this.main = main;
    }
    /**
     * Your subclass implementation here
     * @param command
     * @param parameters
     * @return
     */
    protected String run(String command, Stack<String> parameters){ return Tools.EMPTY_STRING; }
    
    @Override
    protected Object clone() {
      try { return super.clone(); }
      catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }
  }

  public Map<String, String> environment;
  public Map<String, Object> objects;
  public Map<String, MacroScanner.Command> commands;

  private CommandBuilder commandBuilder = new CommandBuilder();
  private Main macroRunner = new Main(this);

  MacroScanner(Map<String, String> environment){
    this.environment = new Hashtable<>(environment);
    objects = new Hashtable<>();
    commands = new Hashtable<>();
  }

  public MacroScanner addCommand(String name, Command command){
    commands.put(name, command); return this;
  }

  public MacroScanner addCommands(Map<String, Command>map){
    commands.putAll(map); return this;
  }

  public MacroScanner addObject(String name, Object object){
    objects.put(name, object); return this;
  }

  public MacroScanner loadObjects(Map<String, Object>map){
    objects.putAll(map); return this;
  }

  private String nextMacroBody(Scanner scanner, char closure){
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  public String start(Scanner scanner){
    return scanner.run(macroRunner);
  }

  public String start(String file, String text){
    Scanner scanner = new Scanner(file, text);
    return scanner.run(macroRunner);
  }

  private String doMacro(Scanner scanner){
    MacroScanner context = this;
    char next = scanner.next();
    switch (next){
      case ENTER_OBJECT: {
        return(Tools.makeNotNull(
            context.objects.get(nextMacroBody(scanner, EXIT_OBJECT)
            ), "undefined").toString());
      }
      case ENTER_PROCEDURE: {
        String output = scanner.run(commandBuilder, context);
        scanner.nextCharacter(EXIT_PROCEDURE);
        return(output);
      }
      case ENTER_VARIABLE: {
        return(context.environment.get(nextMacroBody(scanner, EXIT_VARIABLE)));
      }
      default: scanner.back();
    }
    return Char.toString(macroTrigger); // failed;
  }

  private String doCommand(Scanner scanner, String commandName, Stack<String> parameters) {
    if (commands.containsKey(commandName)){
      Command command = commands.get(commandName);
      command.enterContext(scanner, this);
      return command.run(commandName, parameters);
    }
    throw scanner.syntaxError("unknown command: '"+commandName+"'");
  }

  public static class Main extends ScannerMethod {

    MacroScanner context;

    public Main(MacroScanner context){ this.context = context; }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == context.macroTrigger) { swap(context.doMacro(scanner)); return false; }
      return super.terminate(scanner, character);
    }

  }

  private class CommandBuilder extends ScannerMethod {

    MacroScanner context;
    private ParameterBuilder parameterBuilder = new ParameterBuilder();

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      this.context = (MacroScanner) parameters[0];
    }

    @Override
    protected boolean scan(@NotNull Scanner scanner) {
      if (current() == EXIT_PROCEDURE){ backStep(scanner); }
      else {
        String name = current()+scanner.nextField(BREAK_PROCEDURE_MAP);
        Stack<String> parameters = new Stack<>();
        scanner.run(parameterBuilder, context, parameters);
        swap(context.doCommand(scanner, name, parameters));
      }
      return false;
    }
    
  }

  private static class ParameterBuilder extends  ScannerMethod {

    MacroScanner context;
    Stack<String> parameters;

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      this.context = (MacroScanner) parameters[0];
      this.parameters = (Stack<String>)parameters[1];
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
    }

    @Override
    protected boolean scan(@NotNull Scanner scanner) {
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      return true;
    }

    protected Stack<String> split(String source){
      Scanner scanner = new Scanner("split", source+EXIT_PROCEDURE);
      Stack<String> parameters = new Stack<>();
      scanner.run(this, context, parameters);
      return parameters;
    }

    private String getParameter(Scanner scanner, char character){
      if (character == Char.DOUBLE_QUOTE) {
        String file = scanner.claim();
        String data = scanner.nextBoundField(character);
        scanner.nextCharacter(character);
        while (!Char.mapContains(character = scanner.next(), BREAK_PROCEDURE_MAP)){
          data += getParameter(scanner, character);
        }
        // expand double quoted text
        return (context.start(file, data));
      } else if (character == Char.SINGLE_QUOTE) {
        String data = scanner.nextField(character);
        scanner.nextCharacter(character);
        while (!Char.mapContains(character = scanner.next(), BREAK_PROCEDURE_MAP)){
          data += getParameter(scanner, character);
        }
        return (data);
      }
      return (character + scanner.nextField(BREAK_PROCEDURE_MAP));
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == context.macroTrigger) {
        // expand unquoted macros, into positional parameters.
        // does not support concatenations.
        parameters.addAll(split(context.doMacro(scanner)));
        //parameters.push();
        return false;
      } else if (character == EXIT_PROCEDURE){
        backStep(scanner);
        return true;
      }
      parameters.push(getParameter(scanner, character));
      return false;
    }

    @Override
    protected @NotNull String compile(@NotNull Scanner scanner) { return Tools.EMPTY_STRING; }

  }
}

