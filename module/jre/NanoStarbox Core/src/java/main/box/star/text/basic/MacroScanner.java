package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.text.Char;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class MacroScanner {

  static final   char[] scanBreak = new Char.Assembler(Char.MAP_ASCII_ALL_WHITE_SPACE).merge(')').toArray();

  public static class Command implements Cloneable {
    protected Scanner scanner;
    protected MacroScanner main;
    protected void enterContext(Scanner scanner, MacroScanner main){
      this.scanner = scanner;
      this.main = main;
    }
    protected String run(String command, Stack<String> parameters){
      return "";
    }
    @Override
    protected Object clone() {
      try {
        return super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Map<String, String> environment;
  public Map<String, Object> objects;
  public Map<String, MacroScanner.Command> commands;

  private CommandBuilder commandBuilder = new CommandBuilder();
  private Runner macroRunner = new Runner(this);

  MacroScanner(Map<String, String> environment){
    this.environment = environment;
    objects = new Hashtable<>();
    commands = new Hashtable<>();
  }

  public MacroScanner addCommand(String name, Command command){
    commands.put(name, command);
    return this;
  }

  public MacroScanner addObject(String name, Object object){
    objects.put(name, object);
    return this;
  }

  String nextMacroBody(Scanner scanner, char closure){
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
      case '{': {
        return(Tools.makeNotNull(
            context.objects.get(nextMacroBody(scanner, '}')
            ), "undefined").toString());
      }
      case '(': {
        String output = scanner.run(commandBuilder, context);
        scanner.nextCharacter(')');
        return(output);
      }
      case '[': {
        return(context.environment.get(nextMacroBody(scanner, ']')));
      }
      default: scanner.back();
    }
    return "%"; // failed;
  }

  private String doCommand(Scanner scanner, String commandName, Stack<String> parameters) {
    if (commands.containsKey(commandName)){
      Command command = commands.get(commandName);
      command.enterContext(scanner, this);
      return command.run(commandName, parameters);
    }
    throw scanner.syntaxError("unknown command: '"+commandName+"'");
  }

  public static class Runner extends ScannerMethod {

    MacroScanner context;

    public Runner(){
      this.context = new MacroScanner(System.getenv());
    }

    public Runner(MacroScanner context){
      this.context = context;
    }

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      if (context == null) this.context = (MacroScanner) parameters[0];
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == '%') {
        swap(context.doMacro(scanner));
        return false;
      }
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
      String name = current()+scanner.nextField(scanBreak);
      Stack<String> parameters = new Stack<>();
      scanner.run(parameterBuilder, context, parameters);
      swap(context.doCommand(scanner, name, parameters));
      return false;
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == ')'){
        back(scanner);
        return true;
      }
      return super.terminate(scanner, character);
    }

  }

  private class ParameterBuilder extends  ScannerMethod {

    MacroScanner context;
    Stack<String> parameters;

    @Override
    protected void start(@NotNull Scanner scanner, Object[] parameters) {
      this.context = (MacroScanner) parameters[0];
      this.parameters = (Stack<String>)parameters[1];
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (character == '%') {
        parameters.push(context.doMacro(scanner));
        scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
        return false;
      } else if (character == ')'){
        back(scanner);
        return true;
      } else if (character == Char.DOUBLE_QUOTE) {
        String file = scanner.claim();
        String data = scanner.nextUnescapedField(character);
        parameters.push(context.start(file, data));
        scanner.nextCharacter(character);
        scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
        return false;
      } else if (character == Char.SINGLE_QUOTE) {
        parameters.push(scanner.nextUnescapedField(character));
        scanner.nextCharacter(character);
        scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
        return false;
      }
      parameters.push(character + scanner.nextField(scanBreak));
      scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      return false;
    }

    @Override
    protected @NotNull String compile(@NotNull Scanner scanner) {
      return "";
    }

  }
}

