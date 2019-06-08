package box.star.shell.script;

import static box.star.text.basic.Scanner.*;

import box.star.shell.ScriptParser;
import box.star.shell.script.content.Comment;
import box.star.shell.script.content.Directive;
import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

/**
 * <p>Main Parser</p>
 * <br>
 * <p>{@link Main} looks for a specific shell program word and processes that word,
 * using the associated {@link ScriptParser} sub-class. Additionally, this
 * class is responsible for parsing white-space-characters and unidentified garbage.</p>
 * <br>
 * @see ScriptParser#parse(Class, Scanner)  Parsing Text Records with the Parser class
 * @see Scanner
 * @see Scanner.SourceDriver
 */
public class Main extends ScriptParser
    implements box.star.text.basic.Parser.NewFuturePromise, Scanner.SourceDriver.WithBufferControlPort {
  public List records = new List();
  public Main(Scanner scanner) {
    super(scanner);
  }
  @Override
  public boolean collect(Scanner scanner, StringBuilder buffer, char character) {
    if (scanner.endOfSource()) return false;
    else if (mapContains(character, MAP_ASCII_ALL_WHITE_SPACE)) return true;
    else switch (character){
      case '#': {
        boolean bang = scanner.getLine() == 1 && scanner.getColumn() == 1;
        if (bang){
          Directive line = parse(Directive.class);
          if (line.successful()) records.add(line);
        } else {
          Comment comment = parse(Comment.class);
          if (comment.successful()) records.add(comment);
        }
        return true;
      }
    }
    scanner.back();
    Command command = parse(Command.class);
    if (command.successful()) records.add(command);
    else throw new SyntaxError(this, "expected command and found "+scanner.nextWordPreview());
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
