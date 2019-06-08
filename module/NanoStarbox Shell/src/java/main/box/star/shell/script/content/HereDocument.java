package box.star.shell.script.content;

import box.star.shell.ScriptParser;
import box.star.shell.script.Parameter;
import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

public class HereDocument extends ScriptParser {
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
