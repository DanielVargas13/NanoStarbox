package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.LegacyScanner;
import box.star.text.basic.driver.GenericProgramIdentifier;

import java.util.Stack;

@Deprecated public class TextEnvironment extends Stack<String[]> {

  public Bookmark bookmark;

  public static TextEnvironment parseEnvironmentOperations(LegacyScanner scanner) {
    TextEnvironment operations = new TextEnvironment();
    operations.bookmark = scanner.nextBookmark();
    long start = scanner.getIndex();
    do {
      scanner.nextAllWhiteSpace();
      String[] op = processEnvironmentOperation(scanner);
      if (op == null) {
        scanner.walkBack(start);
        if (scanner.endOfSource()) {
          return null;
        }
        break;
      }
      operations.push(op);
      start = scanner.getIndex();
    } while (true);
    if (operations.isEmpty()) {
      scanner.walkBack(start);
      return null;
    }
    return operations;
  }

  private static String processEnvironmentLabel(LegacyScanner scanner) {
    return scanner.run(new GenericProgramIdentifier());
  }

  static String[] processEnvironmentOperation(LegacyScanner scanner) {
    String[] operation = new String[3];
    operation[0] = processEnvironmentLabel(scanner);
    if (operation[0] == null) return null;
    try {
      operation[1] = Char.toString(scanner.nextCharacter('='));
    }
    catch (Exception e) { return null; }
    operation[2] = TextParameters.parseParameter(scanner);
    return operation;
  }

}
