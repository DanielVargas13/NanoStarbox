package box.star.shell.script;

import static box.star.text.Char.*;

import box.star.lang.Char;
import box.star.text.basic.Scanner;

public class Comment extends Interpreter  {
  public final static char[] BREAK_COMMENT_MAP=Char.toMap(NULL_CHARACTER, LINE_FEED);
  protected String text;
  public Comment(Scanner scanner) {
    super(scanner);
  }
  public String getText() {
    return text;
  }
  @Override
  public void start() {
    char c = scanner.current();
    if (c != '#') { cancel(); return; }
    this.text = c+scanner.nextField(BREAK_COMMENT_MAP);
    finish();
    return;
  }
}
