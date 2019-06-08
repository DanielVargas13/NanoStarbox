package box.star.shell.script;

import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

public class HereDocument extends Interpreter {
  public HereDocument(Scanner scanner) {
    super(scanner);
  }
  public Parameter documentTag;
  public StringBuilder document = new StringBuilder();
  @Override
  protected void start() {
    documentTag = parse(Parameter.class);
    String line;
    do {
      line = scanner.nextField(LINE_FEED);
      if (line.equals(documentTag.getPlainText())) break;
      else document.append(line + scanner.previous());
    } while (true);
    scanner.escape();
    finish();
  }
}
