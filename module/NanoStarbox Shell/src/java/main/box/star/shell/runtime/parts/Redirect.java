package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;

public class Redirect {

  public final static int OP_OPEN_RANDOM = 0,
  OP_CLOSE_READER = 1,
  OP_CLOSE_WRITER = 2,
  OP_COPY_READER = 3,
  OP_COPY_WRITER = 4,
  OP_APPEND_FILE = 5,
  OP_CLOBBER_FILE = 6,
  OP_HERE_DOC_JUSTIFIED = 7,
  OP_HERE_DOC = 8,
  OP_OPEN_READER = 9,
  OP_OPEN_WRITER = 10;

  public Bookmark bookmark;
  public int channel;
  public String operation;
  public String file;

  boolean isCopyOperation(){
    return redirectOperators[3].equals(operation) || redirectOperators[4].equals(operation);
  }

  boolean isJustifiedHereDoc(){
    return redirectOperators[8].equals(operation);
  }

  boolean isQuotedHereDoc(){
    return isHereDoc() && file.startsWith(Char.toString(Char.DOUBLE_QUOTE));
  }

  boolean isHereDoc(){
    return redirectOperators[8].equals(operation) || redirectOperators[7].equals(operation);
  }

  boolean isCloseOperation(){
    return redirectOperators[1].equals(operation) || redirectOperators[2].equals(operation);
  }

  boolean isAppendOperation(){
    return redirectOperators[5].equals(operation);
  }

  static final String[] redirectOperators = new String[]{
      "<>", "<&-", ">&-", "<&", ">&", ">>", ">|", "<<-", "<<", "<", ">"
  };

  public static Redirect parseRedirect(Scanner scanner){
    Redirect redirect = new Redirect();
    redirect.bookmark = scanner.nextBookmark();
    redirect.channel = scanner.nextUnsignedInteger();
    scanner.nextLineWhiteSpace();
    redirect.operation = scanner.nextWord("redirection operator", redirectOperators, true);
    scanner.nextLineWhiteSpace();
    if (redirect.operation.endsWith("&")){
      redirect.file = "/dev/fd/"+scanner.nextUnsignedInteger();
    } else //noinspection StatementWithEmptyBody
      if (redirect.isCloseOperation());
    else {
      redirect.file = scanner.nextField(Char.MAP_ASCII_ALL_WHITE_SPACE);
    }
    return redirect;
  }

}
