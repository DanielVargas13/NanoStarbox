package box.star.shell.script;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class Command extends Interpreter {

  public static final char[] COMMAND_TERMINATOR_MAP = new Char.Assembler(
      Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')
  ).toMap();

  public EnvironmentOperationList environmentOperations;
  public ParameterList parameters;
  public RedirectList redirects;
  public box.star.shell.script.Command pipe;
  String terminator;
  public Command(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    environmentOperations = parseEnvironmentOperationList(scanner);
    parameters = parseParameterList(scanner);
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
          pipe = parse(Command.class);
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
      default: scanner.escape();
    }
    finish();
  }
}
