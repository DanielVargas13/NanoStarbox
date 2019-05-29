package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.LegacyScanner;

import java.util.Stack;

import static box.star.shell.runtime.parts.TextCommand.COMMAND_TERMINATOR_MAP;
import static box.star.text.Char.MAP_ASCII_ALL_WHITE_SPACE;
import static box.star.text.Char.PIPE;

@Deprecated public class TextParameters extends Stack<String> {

  public static final char[] PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>'))
          .merge(COMMAND_TERMINATOR_MAP).merge(MAP_ASCII_ALL_WHITE_SPACE.toMap()).toMap();

  public static TextParameters parseParameters(LegacyScanner scanner){
    return processParameters(scanner);
  }

  public static String parseParameter(LegacyScanner scanner) {
    TextParameters p = new TextParameters();
    p.bookmark = scanner.nextBookmark();
    if (parseParameter(scanner, p)) return String.join(" ", p);
    return null;
  }

  public Bookmark bookmark;
  public static boolean parseParameter(LegacyScanner scanner, TextParameters parameters) {

    if (TextRedirection.findRedirection(scanner)) return false;

    StringBuilder builder = new StringBuilder();
    scanner.nextLineWhiteSpace();

    char c;
    do {
      c = scanner.next();
      if (Char.mapContains(c, MAP_ASCII_ALL_WHITE_SPACE.toMap())) break;
      switch (c) {
        case '\'': {
          builder.append(c).append(processQuotedLiteralText(scanner));
          builder.append(scanner.nextCharacter(c));
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
    } while (!Char.mapContains(c, PARAMETER_TERMINATOR_MAP) && ! scanner.endOfSource());
    parameters.push(builder.toString());
    return true;
  }

  static TextParameters processParameters(LegacyScanner scanner) {
    if (scanner.endOfSource()) return null;
    TextParameters parameters = new TextParameters();
    parameters.bookmark = scanner.nextBookmark();
    do {
      long start = scanner.getIndex();
      if (!parseParameter(scanner, parameters)) {
        scanner.walkBack(start);
        if (parameters.isEmpty()) { return null; }
        break;
      }
    } while (! scanner.endOfSource());
    return parameters;
  }

  static String processLiteralText(LegacyScanner scanner) {
    return scanner.nextField(PARAMETER_TERMINATOR_MAP);
  }

  static String processQuotedLiteralText(LegacyScanner scanner) {
    return scanner.nextField('\'');
  }

  static String processQuotedMacroText(LegacyScanner scanner) {
    return scanner.nextBoundField('"');
  }


}
