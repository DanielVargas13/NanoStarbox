package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.*;

public class TextCommand {

  public static final String
      KEYWORD_IF = "if",
      KEYWORD_THEN = "then",
      KEYWORD_ELSE = "else",
      KEYWORD_ELSE_IF = "elif",
      KEYWORD_END_IF = "fi",
      KEYWORD_FOR = "for",
      KEYWORD_DO = "do",
      KEYWORD_DONE = "done",
      KEYWORD_UNTIL = "until",
      KEYWORD_CASE = "case",
      KEYWORD_END_CASE = "esac",
      KEYWORD_LEFT_BRACE = "{",
      KEYWORD_RIGHT_BRACE = "}"
  ;

  public static final String SYMBOL_NULL = "\0";

  public static final String
  COMMAND_TERMINATOR_NULL = SYMBOL_NULL,
  COMMAND_TERMINATOR_LINE = "\n",
  COMMAND_TERMINATOR_CARRIAGE_RETURN = "\r", // if detected we will demand a line-feed
  COMMAND_TERMINATOR_COMMENT = "#",
  COMMAND_TERMINATOR_SEMI_COLON = ";",
  COMMAND_TERMINATOR_AMPERSAND = "&",
  COMMAND_TERMINATOR_LEFT_PAREN = "(",
  COMMAND_TERMINATOR_RIGHT_PAREN = ")",
  COMMAND_TERMINATOR_LEFT_BRACE = KEYWORD_LEFT_BRACE,
  COMMAND_TERMINATOR_RIGHT_BRACE = KEYWORD_RIGHT_BRACE,
  COMMAND_TERMINATOR_DOUBLE_SEMI_COLON = ";;",
  COMMAND_TERMINATOR_DOUBLE_AMPERSAND = "&&",
  COMMAND_TERMINATOR_DOUBLE_PIPE = "||";

  public static final String[] keyWords = new String[]{
      KEYWORD_IF, KEYWORD_THEN, KEYWORD_ELSE, KEYWORD_ELSE_IF, KEYWORD_END_IF,
      KEYWORD_FOR, KEYWORD_DO, KEYWORD_DO, KEYWORD_DONE, KEYWORD_UNTIL,
      KEYWORD_CASE, KEYWORD_END_CASE,
      KEYWORD_LEFT_BRACE, KEYWORD_RIGHT_BRACE
  };

  public static final String[] commandTerminators = new String[]{
      COMMAND_TERMINATOR_NULL, COMMAND_TERMINATOR_LINE, COMMAND_TERMINATOR_CARRIAGE_RETURN,
      COMMAND_TERMINATOR_COMMENT, COMMAND_TERMINATOR_SEMI_COLON,
      COMMAND_TERMINATOR_AMPERSAND, COMMAND_TERMINATOR_LEFT_PAREN, COMMAND_TERMINATOR_RIGHT_PAREN,
      COMMAND_TERMINATOR_LEFT_BRACE, COMMAND_TERMINATOR_RIGHT_BRACE,
      COMMAND_TERMINATOR_DOUBLE_SEMI_COLON, COMMAND_TERMINATOR_DOUBLE_AMPERSAND,
      COMMAND_TERMINATOR_DOUBLE_PIPE
  };

  static {
    Scanner.preventWordListShortCircuit(keyWords);
    Scanner.preventWordListShortCircuit(commandTerminators);
  }

  public static final char[] COMMAND_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')).toMap();

  public static final char[] PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>')).merge(COMMAND_TERMINATOR_MAP).merge(MAP_ASCII_ALL_WHITE_SPACE).toMap();

  public String source;
  public Stack<String[]> environmentOperations;
  public Stack<String> parameters;
  public Map<Integer, String> redirects = new Hashtable<>();
  public char terminator; // whatever terminated this command
  public TextCommand next; // if terminator == pipe
  public TextCommand(String source) {this.source = source;}

  public static TextCommand parseCommandLine(Scanner scanner) {
    scanner.nextAllWhiteSpace();
    TextCommand textCommand = new TextCommand(scanner.nextBookmark().origin.substring(1));
    textCommand.environmentOperations = processEnvironmentOperations(scanner);
    textCommand.parameters = processParameters(scanner);
    return processRedirects(scanner, textCommand);
  }

  static Stack<String[]> processEnvironmentOperations(Scanner scanner) {
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

  static String processEnvironmentLabel(Scanner scanner) {
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

  static String[] processEnvironmentOperation(Scanner scanner) {
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

  static String processLiteralText(Scanner scanner) {
    return scanner.nextField(PARAMETER_TERMINATOR_MAP);
  }

  static String processQuotedLiteralText(Scanner scanner) {
    return scanner.nextField('\'');
  }

  static String processQuotedMacroText(Scanner scanner) {
    return scanner.nextBoundField('"');
  }

  static String processParameter(Scanner scanner) {
    Stack<String> p = new Stack<>();
    if (processParameter(scanner, p)) return String.join(" ", p);
    return null;
  }

  static boolean processParameter(Scanner scanner, Stack<String> parameters) {
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
          if (Char.mapContains(c, PARAMETER_TERMINATOR_MAP)) {
            scanner.back();
            if (builder.length() == 0) return false;
            parameters.push(builder.toString());
            return true;
          }
          builder.append(c).append(processLiteralText(scanner));
        }
      }
    } while (!Char.mapContains(c, PARAMETER_TERMINATOR_MAP));
    parameters.push(builder.toString());
    return true;
  }

  static Stack<String> processParameters(Scanner scanner) {
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

  static TextCommand processRedirects(Scanner scanner, TextCommand commandEntry) {
    char c = scanner.next();
    commandEntry.redirects = new Hashtable<>();
    while (Char.mapContains(c, '<', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
      switch (c) {
        case '<': {
          scanner.nextAllWhiteSpace();
          commandEntry.redirects.put(0, c + scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
          break;
        }
        case '>': {
          scanner.nextAllWhiteSpace();
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
          scanner.nextAllWhiteSpace();
          commandEntry.redirects.put(v, c + scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
        }
      }
      c = scanner.next();
    }
    if (c == PIPE) {
      commandEntry.terminator = c;
      commandEntry.next = parseCommandLine(scanner);
      return commandEntry;
    }
    commandEntry.terminator = processCommandEnding(scanner);
    return commandEntry;
  }

  static char processCommandEnding(Scanner scanner) {
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

}
