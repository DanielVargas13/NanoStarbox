package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class Comment extends SourceElement {

  public static final char COMMENT_CHAR = '#';
  public static final char[] COMMENT_DELIMITER = new char[]{'\0', '\n'};

  public String text;

  public Comment(@NotNull Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    if (scanner.current() != COMMENT_CHAR) {
      throw new Scanner.SyntaxError(this, "expected comment character, found "+scanner.nextWordPreview());
    }
    text = scanner.nextField(0, COMMENT_DELIMITER);
    scanner.back();
    finish();
  }

}
