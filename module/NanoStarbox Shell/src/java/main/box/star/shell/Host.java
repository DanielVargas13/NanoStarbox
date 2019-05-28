package box.star.shell;

import box.star.contract.NotNull;
import box.star.shell.runtime.Environment;
import box.star.shell.runtime.io.StreamTable;
import box.star.shell.runtime.parts.TextCommand;
import box.star.text.Char;
import box.star.text.basic.LegacyScanner;

import java.io.File;
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
    LegacyScanner scanner = new LegacyScanner(file);
    return scanner.run(macroRunner);
  }

  private String nextMacroBody(LegacyScanner scanner, char closure) {
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  private String getMacroText(LegacyScanner scanner){
    char next = scanner.next();
    if (Char.mapContains(next, Char.MAP_ASCII_NUMBERS)){
      return next+scanner.nextMap(Char.MAP_ASCII_NUMBERS); }
    switch (next) {
      case '_': case '#': return Char.toString(next);
      case ENTER_OBJECT: return (nextMacroBody(scanner, EXIT_OBJECT)); }
    scanner.flagThisCharacterSyntaxError("macro");
    return null; // not reached
  }

  private String doMacro(LegacyScanner scanner) {
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

  private String doCommand(LegacyScanner scanner, Stack<String> parameters) {
    StringBuilder out = new StringBuilder();
    for(String p: parameters) out.append(p).append(SPACE);
    return out.substring(0, Math.max(0, out.length() - 1));
  }

  public static class Main extends LegacyScanner.ScannerMethod {

    Host context;

    public Main(Host context) { this.context = context; }

    TextCommand processCommandLine(LegacyScanner scanner) {
     // scanner.nextAllWhiteSpace();
      //TextCommand textCommand = new TextCommand(scanner.nextCharacterClaim().substring(1));
//      textCommand.environmentOperations = processEnvironmentOperations(scanner);
//      textCommand.parameters = processParameters(scanner);
      return null;//processRedirects(scanner, textCommand);
    }

    Stack<String[]> processEnvironmentOperations(LegacyScanner scanner) {
      Stack<String[]> operations = new Stack<>();
      do {
        long start = scanner.getIndex();
        scanner.nextAllWhiteSpace();
        String[] op = processEnvironmentOperation(scanner);
        if (op == null) {
          scanner.walkBack(start);
          break;
        }
        operations.push(op);
      } while (true);
      return operations;
    }

    String processEnvironmentLabel(LegacyScanner scanner) {
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

    String[] processEnvironmentOperation(LegacyScanner scanner) {
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

    String processLiteralText(LegacyScanner scanner) {
      return scanner.nextField(BREAK_PARAMETER_MAP);
    }

    String processQuotedLiteralText(LegacyScanner scanner) {
      return scanner.nextField('\'');
    }

    String processQuotedMacroText(LegacyScanner scanner) {
      return scanner.nextBoundField('"');
    }

    String processParameter(LegacyScanner scanner) {
      Stack<String> p = new Stack<>();
      if (processParameter(scanner, p)) return String.join(" ", p);
      return null;
    }

    boolean processParameter(LegacyScanner scanner, Stack<String> parameters) {
      scanner.nextLineWhiteSpace();
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

    Stack<String> processParameters(LegacyScanner scanner) {
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

    char processCommandEnding(LegacyScanner scanner) {
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
    protected boolean terminate(@NotNull LegacyScanner scanner, char character) {
      if (Char.mapContains(character, Char.MAP_ASCII_ALL_WHITE_SPACE)) {return false;}
      switch (character) {
        case 0: {
          if (scanner.escapeMode()) throw new LegacyScanner.SyntaxError("escaped end of stream");
          return true;
        }
        case '#': {
          swap(character + scanner.nextField('\n'));
          return false;
        }
      }
      backStep(scanner);
      TextCommand tce = processCommandLine(scanner);
      //swap(processCommandLine(scanner));
      return false;
    }
  }

}
