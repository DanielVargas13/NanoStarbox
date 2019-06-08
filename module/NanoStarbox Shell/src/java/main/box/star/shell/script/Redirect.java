package box.star.shell.script;

import box.star.Tools;
import box.star.shell.script.content.HereDocument;
import box.star.text.Char;
import box.star.text.basic.Scanner;

public class Redirect extends Interpreter {

  HereDocument hereDocument;

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

  public static final Scanner.WordList redirectionOperators = new Scanner.WordList("redirection operator",
      OP_OPEN_RANDOM, OP_CLOSE_READABLE, OP_CLOSE_WRITABLE,
      OP_COPY_READABLE, OP_COPY_WRITABLE, OP_OPEN_WRITABLE_APPEND,
      OP_OPEN_WRITABLE_CLOBBER, OP_HERE_DOC_JUSTIFIED, OP_HERE_DOC,
      OP_OPEN_READABLE, OP_OPEN_WRITABLE
  );

  public static final char[] ARROWS = new char[]{'<', '>'};

  public int stream;
  public String operation;
  public Parameter file;
  public Redirect(Scanner scanner) { super(scanner); }
  @Override
  protected void start() {
    String stream;
    scanner.nextLineSpace();
    stream = scanner.nextMap(0,3, Char.MAP_ASCII_NUMBERS);
    scanner.nextLineSpace();
    try { operation = scanner.nextWord(true, redirectionOperators);
    } catch (Exception e){cancel(); return;}
    if (stream.equals(Tools.EMPTY_STRING)){
      if (operation.contains(OP_OPEN_WRITABLE)) stream = "1";
      else stream = "0";
    }
    this.stream = Integer.parseInt(stream);
    boolean
        here_doc_mode = operation.equals(OP_HERE_DOC),
        justified_here_doc_mode = operation.equals(OP_HERE_DOC_JUSTIFIED);
    if (here_doc_mode || justified_here_doc_mode) {
      hereDocument = parse(HereDocument.class);
    } else {
      file = parse(Parameter.class);
    }
    finish();
  }
}
