package box.star.shell.script;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class Redirect extends Interpreter {

  public final static String
      OP_OPEN_RANDOM = "<>",
      OP_OPEN_READABLE = "<",
      OP_OPEN_WRITABLE = ">",
      OP_OPEN_WRITABLE_CLOBBER = ">|",
      OP_OPEN_WRITABLE_APPEND = ">>",
      OP_COPY_READABLE = "<&",
      OP_COPY_WRITABLE = ">&",
      OP_CLOSE_READABLE = "<&-",
      OP_CLOSE_WRITABLE = ">&-",
      OP_HERE_DOC_JUSTIFIED = "<<-",
      OP_HERE_DOC = "<<";

  public static final Scanner.WordList redirectionOperators = new Scanner.WordList("",
      OP_OPEN_RANDOM, OP_CLOSE_READABLE, OP_CLOSE_WRITABLE,
      OP_COPY_READABLE, OP_COPY_WRITABLE, OP_OPEN_WRITABLE_APPEND,
      OP_OPEN_WRITABLE_CLOBBER, OP_HERE_DOC_JUSTIFIED, OP_HERE_DOC,
      OP_OPEN_READABLE, OP_OPEN_WRITABLE
  );

  String stream;
  String operation;
  Parameter file;
  public Redirect(Scanner scanner) { super(scanner); }
  @Override
  protected void start() {
    scanner.nextLineSpace();
    Redirect redirect = this;
    stream = scanner.nextMap(0,3, Char.MAP_ASCII_NUMBERS);
    scanner.nextLineSpace();
    try {
      redirect.operation = scanner.nextWord(true, redirectionOperators);
    } catch (Exception e){cancel(); return;}
    if (redirect.stream.equals("")){
      if (redirect.operation.contains(">")) redirect.stream = "1";
      else redirect.stream = "0";
    }
    file = parse(Parameter.class);
    finish();
  }
}
