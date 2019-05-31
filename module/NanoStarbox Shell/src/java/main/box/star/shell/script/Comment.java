package box.star.shell.script;

import box.star.text.basic.Scanner;

public class Comment extends Interpreter implements box.star.text.basic.Parser.NewFuturePromise {
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
    this.text = c+scanner.nextLine();
    finish();
    return;
  }
}
