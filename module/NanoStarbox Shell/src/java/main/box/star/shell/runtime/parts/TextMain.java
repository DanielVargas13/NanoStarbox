package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.LegacyScanner;

import java.util.Stack;

@Deprecated public class TextMain extends TextCommand {

  Stack<TextCommand> stack;

  TextMain(String source){super(source);}

  public static TextCommand parseTextCommands(LegacyScanner scanner){
    if (scanner.endOfSource()) return null;
    scanner.nextLineWhiteSpace();
    char c = scanner.next(); scanner.back();
    if (c == '(') return parseTextCommandShell(scanner);
    else if (c == '{') return parseTextCommandGroup(scanner);
    else return TextCommand.parseTextCommandStream(scanner);
  }

  private static TextMain parseTextCommandShell(LegacyScanner scanner){
    return parseTextCommandGroup(scanner, '(', ')');
  }

  private static TextMain parseTextCommandGroup(LegacyScanner scanner){
    return parseTextCommandGroup(scanner, '{', '}');
  }

  private static TextMain parseTextCommandGroup(LegacyScanner scanner, char open, char close) {
    TextMain textCommands = new TextMain(scanner.toString());
    scanner.nextCharacter(open);
    TextCommand command = null;
    do {
      scanner.nextAllWhiteSpace();
      char c = scanner.next();
      scanner.back();
      if (c == 0){
        if (scanner.endOfSource()) break;
      }
      else if (c == ')') {
        break;
      }
      else if (c == '}') {
        scanner.back(1);
        scanner.nextCharacterMap("white-space", 1, Char.MAP_ASCII_ALL_WHITE_SPACE, true);
        break;
      }
      command = parseTextCommands(scanner);
      if (command == null) {
        break;
      }
      if (textCommands.stack == null) textCommands.stack = new Stack<>();
      textCommands.stack.push(command);
    } while (! scanner.endOfSource());

    scanner.nextCharacter(close);

    TextRedirection r;
    while ((r = TextRedirection.parseRedirect(scanner))!= null){
      if (textCommands.redirects == null) textCommands.redirects = new Stack<>();
      textCommands.redirects.push(r);
    }

    TextCommand.processPipes(scanner, textCommands);

    return textCommands;

  }
}
