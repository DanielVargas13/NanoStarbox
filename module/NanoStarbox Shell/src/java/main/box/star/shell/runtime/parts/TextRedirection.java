package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;

@Deprecated public class TextRedirection {

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

  public static boolean findRedirection(Scanner scanner){
    long start = scanner.getIndex();
    int stream;
    scanner.nextLineWhiteSpace();
    try { stream = scanner.nextUnsignedInteger(); }
    catch (Exception e) {stream = -1; scanner.walkBack(start);}
    try {
      if (scanner.nextWordListMatch(redirectionOperators, Scanner.WORD_BREAK, true)) {
        scanner.walkBack(start);
        return true;
      }
    } catch (Exception e){}
    scanner.walkBack(start);
    return false;
  }

  public static TextRedirection parseRedirect(Scanner scanner){
    TextRedirection redirect = new TextRedirection();
    scanner.nextLineWhiteSpace();
    if (scanner.endOfSource()) return null;
    redirect.bookmark = scanner.nextBookmark();
    try { redirect.stream = scanner.nextUnsignedInteger(); }
    catch (Exception e){redirect.stream = -1; scanner.walkBack(redirect.bookmark.index - 1);}
    try {
      redirect.operation = scanner.nextWord("redirection operator", redirectionOperators);
    } catch (Exception e){scanner.walkBack(redirect.bookmark.index - 1); return null;}
    if (redirect.stream == -1){
      if (redirect.operation.contains(">")) redirect.stream = 1;
      else redirect.stream = 0;
    }
    scanner.nextLineWhiteSpace();
    if (! redirect.isCloseOperation()){
      if (redirect.isCopyOperation()){
        redirect.file = "/dev/fd/"+scanner.nextUnsignedInteger();
      } else {
        redirect.file = TextParameters.parseParameter(scanner);
      }
    }
    return redirect;
  }

  public Bookmark bookmark;
  public int stream;
  public String operation;
  public String file;

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
