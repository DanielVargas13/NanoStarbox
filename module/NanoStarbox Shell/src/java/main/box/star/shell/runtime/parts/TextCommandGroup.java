package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import java.util.Stack;

public class TextCommandGroup extends TextCommand {

  Stack<TextCommand> stack;

  public TextCommandGroup(String source) {
    super(source);
  }

  public static TextCommandGroup parseTextCommandGroup(Scanner scanner) {
    TextCommandGroup textCommands = new TextCommandGroup("");
    scanner.nextCharacter('{');
    do {
      scanner.nextAllWhiteSpace();
      char c = scanner.next();
      if (c == 0){
        if (scanner.endOfSource()) break;
      }
      if (c == '}') {
        scanner.back(2);
        scanner.nextCharacterMap("white-space", 1, Char.MAP_ASCII_ALL_WHITE_SPACE, true);
        break;
      }
      TextCommand command = TextCommand.parseCommandLine(scanner);
      if (command == null) {
        break;
      }
      if (textCommands.stack == null) textCommands.stack = new Stack<>();
      textCommands.stack.push(command);
    } while (! scanner.endOfSource());

    scanner.nextCharacter('}');

    TextRedirection r;
    while ((r = TextRedirection.parseRedirect(scanner))!= null){
      if (textCommands.redirects == null) textCommands.redirects = new Stack<>();
      textCommands.redirects.push(r);
    }

    TextCommand.processPipes(scanner, textCommands);

    return textCommands;

  }
}
