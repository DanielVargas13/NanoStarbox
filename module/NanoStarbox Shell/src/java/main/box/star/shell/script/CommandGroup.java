package box.star.shell.script;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class CommandGroup extends Interpreter {

  CommandList commands;
  String terminator;
  RedirectList redirects;
  Interpreter pipe;

  public CommandGroup(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commands = Interpreter.parseCommandList(scanner);
    redirects = parseParameterRedirectList(scanner);
    scanner.nextLineSpace();
    char c = scanner.next();
    switch (c){
      case Char.PIPE: {
        if (scanner.next() == Char.PIPE) {
          terminator = "||";
        } else {
          scanner.escape();
          scanner.nextWhiteSpace();
          char t = scanner.next();
          switch (t){
            case '(': {
              pipe = parse(ShellSubMain.class);
              break;
            }
            case '{': {
              pipe = parse(CommandGroup.class);
              break;
            }
            default:
              scanner.escape();
              pipe = parse(Command.class);
          }
        }
        break;
      }
      case '&': {
        if (scanner.next() == '&') {
          terminator = "&&";
        } else {
          scanner.escape();
          terminator = "&";
        }
        break;
      }
      case '\r':
        c = scanner.next('\n');
      case '\n':
      case '#':
      case ';': {
        terminator = Char.toString(c);
        break;
      }
      default:
        scanner.back();
        while (Char.mapContains(scanner.current(), Char.MAP_ASCII_ALL_WHITE_SPACE)) scanner.back();
    }
    finish();
  }
}
