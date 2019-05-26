package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;
import box.star.text.basic.driver.GenericProgramIdentifier;

import java.util.Stack;

public class TextEnvironment extends Stack<String[]> {

  public Bookmark bookmark;

  public static TextEnvironment parseEnvironmentOperations(Scanner scanner) {
    TextEnvironment operations = new TextEnvironment();
    operations.bookmark = scanner.nextBookmark();
    do {
      long start = scanner.getIndex();
      scanner.nextAllWhiteSpace();
      String[] op = processEnvironmentOperation(scanner);
      if (op == null) {
        scanner.walkBack(start);
        break;
      }
      operations.push(op);
    } while (true);
    return operations;
  }

  private static String processEnvironmentLabel(Scanner scanner) {
    return scanner.nextScanOf(new GenericProgramIdentifier());
  }

  static String[] processEnvironmentOperation(Scanner scanner) {
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
