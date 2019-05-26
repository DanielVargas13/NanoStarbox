package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import java.util.Stack;

public class TextCommandGroup extends TextCommand {

  Stack<TextCommand> stack;

  public TextCommandGroup(String source) {
    super(source);
  }

  public static TextCommand parseTextCommands(Scanner scanner){
    if (scanner.endOfSource()) return null;
    scanner.nextLineWhiteSpace();
    char c = scanner.next(); scanner.back();
    if (c == '(') return parseTextCommandShell(scanner);
    if (c == '{') return parseTextCommandGroup(scanner);
    return TextCommand.parseCommandLine(scanner);
  }

  public static TextCommandGroup parseTextCommandShell(Scanner scanner){
    return parseTextCommandGroup(scanner, '(', ')');
  }

  public static TextCommandGroup parseTextCommandGroup(Scanner scanner){
    return parseTextCommandGroup(scanner, '{', '}');
  }

  private static TextCommandGroup parseTextCommandGroup(Scanner scanner, char open, char close) {
    TextCommandGroup textCommands = new TextCommandGroup("");
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
      else if (c == '('){
        command = TextCommandGroup.parseTextCommandShell(scanner);
      } else if (c == '{'){
        command = TextCommandGroup.parseTextCommandGroup(scanner);
      } else {
        command = TextCommand.parseCommandLine(scanner);
      }
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
