package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import java.util.Stack;

import static box.star.text.Char.*;

public class TextCommand implements TextElement {

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

  public String source;
  public TextEnvironment environmentOperations;
  public TextParameters parameters;
  public Stack<TextRedirection> redirects = new Stack<>();
  public String terminator; // whatever terminated this command
  public TextCommand next; // if terminator == pipe
  public TextCommand(String source) {this.source = source;}

  public static TextCommand parseCommandLine(Scanner scanner) {
    scanner.nextAllWhiteSpace();
    TextCommand textCommand = new TextCommand(scanner.nextBookmark().origin.substring(1));
    textCommand.environmentOperations = TextEnvironment.parseEnvironmentOperations(scanner);
    textCommand.parameters = TextParameters.parseParameters(scanner);
    TextRedirection r;
    while ((r = TextRedirection.parseRedirect(scanner))!= null){
      textCommand.redirects.push(r);
    }
    processPipes(scanner, textCommand);
    return textCommand;
  }

  static char processCommandEnding(Scanner scanner) {
    char c;
    switch (c = scanner.next()) {
      case ';':
      case '\0': return ';';
      case '#': {

      }
      case '\n':
        return c;
      case '\r':
        return processCommandEnding(scanner);
    }
    scanner.flagThisCharacterSyntaxError
        ("semi-colon, hash-mark, carriage-return, line-feed or end of source");
    return 0; // not reached
  }

  static TextCommand processPipes(Scanner scanner, TextCommand commandEntry) {
    if (scanner.endOfSource()) return null;
    if (scanner.nextSequenceMatch(COMMAND_TERMINATOR_DOUBLE_PIPE)){
      commandEntry.terminator = COMMAND_TERMINATOR_DOUBLE_PIPE;
      return commandEntry;
    } else if (scanner.nextSequenceMatch(COMMAND_TERMINATOR_DOUBLE_AMPERSAND)) {
      commandEntry.terminator = COMMAND_TERMINATOR_DOUBLE_AMPERSAND;
      return commandEntry;
    } else if (scanner.nextSequenceMatch(COMMAND_TERMINATOR_DOUBLE_SEMI_COLON)) {
      commandEntry.terminator = COMMAND_TERMINATOR_DOUBLE_SEMI_COLON;
      return commandEntry;
    }
    char c = scanner.next();
    if (c == ')') {
      scanner.back();
      return commandEntry;
    }
    if (c == PIPE) {
      commandEntry.terminator = "|";
      commandEntry.next = TextCommandGroup.parseTextCommands(scanner);
      return commandEntry;
    } else {
      scanner.back();
    }
    commandEntry.terminator = Char.toString(processCommandEnding(scanner));
    return commandEntry;
  }

}

/*
      c = scanner.next();
      scanner.back();
      if (c == '('){
        commandEntry.next = TextCommandGroup.parseTextCommandShell(scanner);
      } else if (c == '{'){
        commandEntry.next = TextCommandGroup.parseTextCommandGroup(scanner);
      } else commandEntry.next = TextCommand.parseCommandLine(scanner);
      //commandEntry.next = TextCommand.parseCommandLine(scanner);

 */