package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import java.util.Stack;

import static box.star.shell.runtime.parts.TextCommand.COMMAND_TERMINATOR_MAP;
import static box.star.text.Char.MAP_ASCII_ALL_WHITE_SPACE;
import static box.star.text.Char.PIPE;

public class TextParameters extends Stack<String> {

  public static final char[] PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>'))
          .merge(COMMAND_TERMINATOR_MAP).merge(MAP_ASCII_ALL_WHITE_SPACE).toMap();

  public static TextParameters parseParameters(Scanner scanner){
    return processParameters(scanner);
  }

  public static String parseParameter(Scanner scanner) {
    TextParameters p = new TextParameters();
    if (parseParameter(scanner, p)) return String.join(" ", p);
    return null;
  }

  public static boolean parseParameter(Scanner scanner, TextParameters parameters) {
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
            if (c != 0) scanner.back();
            if (builder.length() == 0) return false;
            parameters.push(builder.toString());
            return true;
          }
          builder.append(c).append(processLiteralText(scanner));
        }
      }
    } while (c != 0 && !Char.mapContains(c, PARAMETER_TERMINATOR_MAP));
    parameters.push(builder.toString());
    return true;
  }

  static TextParameters processParameters(Scanner scanner) {
    TextParameters parameters = new TextParameters();
    do {
      long start = scanner.getIndex();
      if (!parseParameter(scanner, parameters)) {
        scanner.walkBack(start);
        if (parameters.isEmpty()) { return null; }
        break;
      }
    } while (true);
    return parameters;
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


}
