package box.star.shell.runtime.parts;

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
      char c = scanner.next();
      if (c == '}') {
        scanner.back();
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
