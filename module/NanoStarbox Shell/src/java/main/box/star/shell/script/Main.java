package box.star.shell.script;

import box.star.lang.SyntaxError;
import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

/**
 * <p>Main Parser</p>
 * <br>
 * <p>{@link box.star.shell.script.Main} looks for a specific shell program word and processes that word,
 * using the associated {@link Interpreter} sub-class. Additionally, this
 * class is responsible for parsing white-space-characters and unidentified garbage.</p>
 * <br>
 * @see Interpreter#parse(Class, Scanner)  Parsing Text Records with the Parser class
 * @see Scanner
 * @see Scanner.SourceDriver
 */
public class Main extends Interpreter
    implements box.star.text.basic.Parser.NewFuturePromise, Scanner.SourceDriver.WithBufferControlPort {
  List records = new List();
  public Main(Scanner scanner) {
    super(scanner);
  }
  @Override
  public boolean collect(Scanner scanner, StringBuilder buffer, char character) {
    if (scanner.endOfSource()) return false;
    else if (mapContains(character, MAP_ASCII_ALL_WHITE_SPACE)) return true;
    else if (mapContains(character, MAP_ASCII_NUMBERS)){
      throw new SyntaxError(this, "expected command found digits");
    }
    else switch (character){
      case '#': {
        boolean bang = scanner.getLine() == 1 && scanner.getColumn() == 1;
        if (bang){
          Directive line = parse(Directive.class, scanner);
          if (line.successful()) records.add(line);
        } else {
          Comment comment = parse(Comment.class, scanner);
          if (comment.successful()) records.add(comment);
        }
        break;
      }
      case '(': {
        SubMain child = parse(SubMain.class, scanner);
        if (child.successful()) records.add(child);
        break;
      }
      case '{': {
        CommandGroup list = parse(CommandGroup.class, scanner);
        if (list.successful()) records.add(list);
        break;
      }
      default:
        throw new SyntaxError(this, "expected shell command");
    }
    return true;
  }
  /**
   * <p>Calls the scanner to begin main script parser assembly</p>
   * @see #collect(Scanner, StringBuilder, char)
   */
  @Override
  protected void start() {
    scanner.run(this);
    finish();
  }
}
