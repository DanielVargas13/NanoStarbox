package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.text.Char;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.basic.MacroContext.scanBreak;

public class MacroContext {

  static final   char[] scanBreak = new Char.Assembler(Char.MAP_ASCII_ALL_WHITE_SPACE).merge(')').toArray();

  public static class Command implements Cloneable {
    protected Scanner scanner;
    protected MacroContext main;
    protected void enterContext(Scanner scanner, MacroContext main){
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
  public Map<String, MacroContext.Command> commands;

  MacroContext(Map<String, String> environment){
    this.environment = environment;
    objects = new Hashtable<>();
    commands = new Hashtable<>();
  }

  public MacroContext addCommand(String name, Command command){
    commands.put(name, command);
    return this;
  }

  public MacroContext addObject(String name, Object object){
    objects.put(name, object);
    return this;
  }

  String nextMacroBody(Scanner scanner, char closure){
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  public String runMacro(String file, String text){
    Scanner scanner = new Scanner(file, text);
    return scanner.run(new MacroRunner(this));
  }

  public String doMacro(Scanner scanner){
    MacroContext context = this;
    char next = scanner.next();
    switch (next){
      case '{': {
        return(Tools.makeNotNull(
            context.objects.get(nextMacroBody(scanner, '}')
            ), "undefined").toString());
      }
      case '(': {
        String output = scanner.run(new CommandComposer(), context);
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

  public String doCommand(Scanner scanner, String commandName, Stack<String> parameters) {
    if (commands.containsKey(commandName)){
      Command command = commands.get(commandName);
      command.enterContext(scanner, this);
      return command.run(commandName, parameters);
    }
    throw scanner.syntaxError("unknown command: '"+commandName+"'");
  }

}

class MacroRunner extends ScannerMethod {

  MacroContext context;

  public MacroRunner(){
    this.context = new MacroContext(System.getenv());
  }

  public MacroRunner(MacroContext context){
    this.context = context;
  }

  @Override
  protected void start(@NotNull Scanner scanner, Object[] parameters) {
    if (context == null) this.context = (MacroContext) parameters[0];
  }

  @Override
  protected boolean terminate(@NotNull Scanner scanner, char character) {
    if (character == '%') {
      swap(context.doMacro(scanner));
      return false;
    }
    return super.terminate(scanner, character);
  }

  /**
   * Return the compiled buffer contents.
   * <p>
   * This method is called after the scanner completes a method call.
   *
   * @param scanner
   * @return the buffer.
   */
  @Override
  protected @NotNull String compile(@NotNull Scanner scanner) {
    return super.compile(scanner);
  }

}

class CommandComposer extends ScannerMethod {

  MacroContext context;

  @Override
  protected void start(@NotNull Scanner scanner, Object[] parameters) {
    this.context = (MacroContext) parameters[0];
  }

  @Override
  protected boolean scan(@NotNull Scanner scanner) {
    String name = current()+scanner.nextField(scanBreak);
    Stack<String> parameters = new Stack<>();
    scanner.run(new ParameterBuilder(), context, parameters);
    swap(context.doCommand(scanner, name, parameters));
    return false;
  }

  /**
   * Return true to break processing at this character position.
   * <p>
   * The default method handles the zero terminator.
   *
   * @param scanner
   * @param character
   * @return false to continue processing.
   */
  @Override
  protected boolean terminate(@NotNull Scanner scanner, char character) {
    if (character == ')'){
      back(scanner);
      return true;
    }
    return super.terminate(scanner, character);
  }

}

class ParameterBuilder extends  ScannerMethod {

  MacroContext context;
  Stack<String> parameters;

  @Override
  protected void start(@NotNull Scanner scanner, Object[] parameters) {
    this.context = (MacroContext) parameters[0];
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
      String data;
      data = scanner.nextUnescapedField(character);
      Scanner scan = new Scanner("parameter", data);
      data = scan.run(new MacroRunner(), context);
      parameters.push(data);
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