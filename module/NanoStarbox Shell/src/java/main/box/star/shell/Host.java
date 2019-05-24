package box.star.shell;

import box.star.contract.NotNull;
import box.star.text.Char;
import box.star.text.SyntaxError;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerMethod;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.*;

/**
 * <p>Shell Code Reference</p>
 * <br>
 *   <h4>Text Command Shell: Text Command Model</h4>
 *   <code>
 *     RULE-SYNTAX: `?' = maybe, '?:' = if-then, '...' = repeating-or-empty-rule, '()' = anonymous-group
 *   </code><br><br>
 * <code>
 *   COMMAND: ([ENVIRONMENT_OPERATIONS]...) [PROGRAM]?: (([PARAMETERS]...) ([REDIRECTIONS]...) ([PIPE]?: [COMMAND])...)? [TERMINATOR]
 * </code><br><br>
 * <p>Technically, a command may do nothing by specifying no environment or program, using only a terminator such as comment.</p><br>
 * <p>Conventionally, a shell such as the BASH shell does not allow for
 * current process re-directions, using this form, which is why re-directions are dependent upon
 * the PROGRAM rule. Instead the shell delegates such functionality to the exec command.</p>
 * <br><p>For the list of command terminators see {@link #COMMAND_TERMINATOR}.</p>
 * <br>
 */
public class Host {

  private final static char
      ENTER_OBJECT = '{', EXIT_OBJECT = '}',
      MACRO_TRIGGER = '$';

  private static final char[] COMMAND_TERMINATOR =
      new Char.Assembler(Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')).toMap();

  private static final char[] BREAK_PARAMETER_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>')).merge(COMMAND_TERMINATOR).merge(MAP_ASCII_ALL_WHITE_SPACE).toMap();

  Environment environment;
  StreamTable streams;
  Main macroRunner;
  Stack<String> parameters;

  public Host(){
    environment = new Environment();
    streams = new StreamTable();
    macroRunner = new Main(this);
  }

  public String start(File file, String... parameters) {
    this.parameters = new Stack<>();
    this.parameters.push(file.getPath());
    for (int i = 0; i < parameters.length; i++) this.parameters.push(parameters[i]);
    Scanner scanner = new Scanner(file);
    return scanner.run(macroRunner);
  }

  private String nextMacroBody(Scanner scanner, char closure) {
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  private String getMacroText(Scanner scanner){
    char next = scanner.next();
    if (Char.mapContains(next, Char.MAP_ASCII_NUMBERS)){
      return next+scanner.nextMap(Char.MAP_ASCII_NUMBERS); }
    switch (next) {
      case '_': case '#': return Char.toString(next);
      case ENTER_OBJECT: return (nextMacroBody(scanner, EXIT_OBJECT)); }
    scanner.flagThisCharacterSyntaxError("macro");
    return null; // not reached
  }

  private String doMacro(Scanner scanner) {
    char next = scanner.next();
    if (Char.mapContains(next, Char.MAP_ASCII_NUMBERS)){
      int index = Integer.parseInt(next+scanner.nextMap(Char.MAP_ASCII_NUMBERS));
      return parameters.get(index);
    }
    switch (next) {
      case '#': { return parameters.size()+""; }
      case '_': { return parameters.peek(); }
      case ENTER_OBJECT: {
        return environment.get(nextMacroBody(scanner, EXIT_OBJECT)).toString(); }
      default: scanner.back();scanner.nextCharacter(ENTER_OBJECT); }
    return Char.toString(MACRO_TRIGGER);
  }

  private String doCommand(Scanner scanner, Stack<String> parameters) {
    StringBuilder out = new StringBuilder();
    for(String p: parameters) out.append(p).append(SPACE);
    return out.substring(0, Math.max(0, out.length() - 1));
  }

  public static class Main extends ScannerMethod {

    static class TextCommandEntry {
      String source;
      Stack<String[]> environmentOperations;
      Stack<String> parameters;
      Map<Integer, String> redirects = new Hashtable<>();
      char terminator; // whatever terminated this command
      TextCommandEntry next; // if terminator == pipe
      TextCommandEntry(String source) {this.source = source;}
    }

    Host context;

    public Main(Host context) { this.context = context; }

    TextCommandEntry processCommandLine(Scanner scanner) {
      scanner.scanAllWhiteSpace();
      TextCommandEntry textCommand = new TextCommandEntry(scanner.nextCharacterClaim().substring(1));
      textCommand.environmentOperations = processEnvironmentOperations(scanner);
      textCommand.parameters = processParameters(scanner);
      return processRedirects(scanner, textCommand);
    }

    Stack<String[]> processEnvironmentOperations(Scanner scanner) {
      Stack<String[]> operations = new Stack<>();
      do {
        long start = scanner.getIndex();
        scanner.scanAllWhiteSpace();
        String[] op = processEnvironmentOperation(scanner);
        if (op == null) {
          scanner.walkBack(start);
          break;
        }
        operations.push(op);
      } while (true);
      return operations;
    }

    String processEnvironmentLabel(Scanner scanner) {
      StringBuilder output = new StringBuilder();
      char[] okay1 = new Char.Assembler(Char.MAP_ASCII_LETTERS).merge('-', '_').toMap();
      do {
        char c = scanner.next();
        if (c == 0) return null;
        else if (c == '=') break;
        else if (!Char.mapContains(c, okay1)) return null;
        else output.append(c);
      } while (true);
      scanner.back();
      return output.toString();
    }

    String[] processEnvironmentOperation(Scanner scanner) {
      String[] operation = new String[3];
      operation[0] = processEnvironmentLabel(scanner);
      if (operation[0] == null) return null;
      try {
        operation[1] = Char.toString(scanner.nextCharacter('='));
      }
      catch (Exception e) { return null; }
      operation[2] = processParameter(scanner);
      return operation;
    }

    String processLiteralText(Scanner scanner) {
      return scanner.nextField(BREAK_PARAMETER_MAP);
    }

    String processQuotedLiteralText(Scanner scanner) {
      return scanner.nextField('\'');
    }

    String processQuotedMacroText(Scanner scanner) {
      return scanner.nextBoundField('"');
    }

    String processParameter(Scanner scanner) {
      Stack<String> p = new Stack<>();
      if (processParameter(scanner, p)) return String.join(" ", p);
      return null;
    }

    boolean processParameter(Scanner scanner, Stack<String> parameters) {
      scanner.scanLineWhiteSpace();
      StringBuilder builder = new StringBuilder();
      long start = scanner.getIndex();
      char c;
      do {
        c = scanner.next();
        if (Char.mapContains(c, MAP_ASCII_ALL_WHITE_SPACE)) break;
        switch (c) {
          case '<':
          case '>': {
            boolean notAnumber = false;
            try {
              int v = Integer.parseInt(builder.toString());
            }
            catch (NumberFormatException nfe) { notAnumber = true; }
            if (notAnumber == false) {
              scanner.walkBack(start);
              return false;
            }
            scanner.back();
            return false;
          }
          case '\'': {
            builder.append(c).append(processQuotedLiteralText(scanner));
            scanner.nextCharacter(c);
            builder.append(c);
            break;
          }
          case '"': {
            builder.append(c).append(processQuotedMacroText(scanner));
            scanner.nextCharacter(c);
            builder.append(c);
            break;
          }
          default: {
            if (Char.mapContains(c, BREAK_PARAMETER_MAP)) {
              scanner.back();
              if (builder.length() == 0) return false;
              parameters.push(builder.toString());
              return true;
            }
            builder.append(c).append(processLiteralText(scanner));
          }
        }
      } while (!Char.mapContains(c, BREAK_PARAMETER_MAP));
      parameters.push(builder.toString());
      return true;
    }

    Stack<String> processParameters(Scanner scanner) {
      Stack<String> parameters = new Stack<>();
      do {
        long start = scanner.getIndex();
        if (!processParameter(scanner, parameters)) {
          scanner.walkBack(start);
          if (parameters.isEmpty()) { return null; }
          break;
        }
      } while (true);
      return parameters;
    }

    TextCommandEntry processRedirects(Scanner scanner, TextCommandEntry commandEntry) {
      char c = scanner.next();
      commandEntry.redirects = new Hashtable<>();
      while (Char.mapContains(c, '<', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
        switch (c) {
          case '<': {
            scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(0, c + scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
            break;
          }
          case '>': {
            scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(1, c + scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
            break;
          }
          default: {
            String scan = c + scanner.nextMap(MAP_ASCII_NUMBERS);
            int v = Integer.parseInt(scan);
            c = scanner.next();
            if (!Char.mapContains(c, '<', '>')) {
              scanner.flagThisCharacterSyntaxError("< or >");
              return null; // not reached
            }
            scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(v, c + scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
          }
        }
        c = scanner.next();
      }
      if (c == PIPE) {
        commandEntry.terminator = c;
        commandEntry.next = processCommandLine(scanner);
        return commandEntry;
      }
      commandEntry.terminator = processCommandEnding(scanner);
      return commandEntry;
    }

    char processCommandEnding(Scanner scanner) {
      char c;
      switch (c = scanner.next()) {
        case ';':
        case '#':
        case '\0':
        case '\n':
          return c;
        case '\r':
          return processCommandEnding(scanner);
      }
      scanner.flagThisCharacterSyntaxError
          ("semi-colon, hash-mark, carriage-return, line-feed or end of source");
      return 0; // not reached
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (Char.mapContains(character, Char.MAP_ASCII_ALL_WHITE_SPACE)) {return false;}
      switch (character) {
        case 0: {
          if (scanner.escapeMode()) throw new SyntaxError("escaped end of stream");
          return true;
        }
        case '#': {
          swap(character + scanner.nextField('\n'));
          return false;
        }
      }
      backStep(scanner);
      TextCommandEntry tce = processCommandLine(scanner);
      //swap(processCommandLine(scanner));
      return false;
    }
  }
}
