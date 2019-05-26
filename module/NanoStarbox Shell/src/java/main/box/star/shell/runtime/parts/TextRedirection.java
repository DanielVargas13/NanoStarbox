package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;

public class TextRedirection {

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

  public static final String[] redirectionOperators = new String[]{
      OP_OPEN_RANDOM, OP_CLOSE_READABLE, OP_CLOSE_WRITABLE,
      OP_COPY_READABLE, OP_COPY_WRITABLE, OP_OPEN_WRITABLE_APPEND,
      OP_OPEN_WRITABLE_CLOBBER, OP_HERE_DOC_JUSTIFIED, OP_HERE_DOC,
      OP_OPEN_READABLE, OP_OPEN_WRITABLE
  };

  static {
    Scanner.preventWordListShortCircuit(redirectionOperators);
  }

  public static TextRedirection parseRedirect(Scanner scanner){
    TextRedirection redirect = new TextRedirection();
    redirect.bookmark = scanner.nextBookmark();
    redirect.channel = scanner.nextUnsignedInteger();
    scanner.nextLineWhiteSpace();
    redirect.operation = scanner.nextWord("redirection operator", redirectionOperators);
    scanner.nextLineWhiteSpace();
    if (! redirect.isCloseOperation()){
      if (redirect.isCopyOperation()){
        redirect.file = "/dev/fd/"+scanner.nextUnsignedInteger();
      } else if (redirect.isHereDoc()){
        char c = scanner.next();
        scanner.back();
        if (Char.mapContains(c, Char.DOUBLE_QUOTE, Char.SINGLE_QUOTE)){
          redirect.expandText = (c == Char.DOUBLE_QUOTE);
        }
        redirect.file = TextParameters.parseParameter(scanner);
      } else {
        redirect.file = TextParameters.parseParameter(scanner);
      }
    }
    return redirect;
  }

  public Bookmark bookmark;
  public int channel;
  public String operation;
  public String file;
  public boolean expandText;

  boolean isCopyOperation(){
    return OP_COPY_READABLE.equals(operation) || OP_COPY_WRITABLE.equals(operation);
  }

  boolean isJustifiedHereDoc(){
    return OP_HERE_DOC_JUSTIFIED.equals(operation);
  }

  boolean isQuotedHereDoc(){
    return isHereDoc() && file.startsWith(Char.toString(Char.DOUBLE_QUOTE));
  }

  boolean isHereDoc(){
    return isJustifiedHereDoc() || OP_HERE_DOC.equals(operation);
  }

  boolean isCloseOperation(){
    return OP_CLOSE_READABLE.equals(operation) || OP_CLOSE_WRITABLE.equals(operation);
  }

  boolean isAppendOperation(){
    return OP_OPEN_WRITABLE_APPEND.equals(operation);
  }

}
