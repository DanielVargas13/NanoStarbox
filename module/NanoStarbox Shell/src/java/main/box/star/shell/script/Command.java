package box.star.shell.script;

import box.star.text.Char;
import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

public class Command extends Interpreter {

  public static final char[] COMMAND_TERMINATOR_MAP = new Char.Assembler(
      Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')
  ).toMap();

  public EnvironmentOperationList environmentOperations;
  public ParameterList parameters;
  public RedirectList redirects;
  public Command pipe;
  public String terminator;

  public Command(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    environmentOperations = parseEnvironmentOperationList(scanner);
    parameters = parseParameterList(scanner);
    parseEnding();
    finish();
  }
  protected void parseEnding(){
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
              pipe = parse(CommandContext.class);
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
        terminator = Char.toString(c, scanner.next(LINE_FEED));
        break;
      case '#': {
        terminator = c + scanner.nextMap('\0', LINE_FEED);
        break;
      }
      case LINE_FEED:
      case ';': {
        terminator = Char.toString(c);
        break;
      }
      default:
        scanner.back();
        while (Char.mapContains(scanner.current(), MAP_ASCII_ALL_WHITE_SPACE)) scanner.back();
    }
  }
}
