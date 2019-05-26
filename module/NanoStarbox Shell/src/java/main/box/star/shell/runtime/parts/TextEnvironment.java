package box.star.shell.runtime.parts;

import box.star.text.Char;
import box.star.text.basic.Scanner;
import box.star.text.basic.driver.GenericProgramIdentifier;

import java.util.Stack;

public class TextEnvironment extends Stack<String[]> {

  public static TextEnvironment parseEnvironmentOperations(Scanner scanner) {
    TextEnvironment operations = new TextEnvironment();
    do {
      long start = scanner.getIndex();
      scanner.nextAllWhiteSpace();
      String[] op = processEnvironmentOperation(scanner);
      if (op == null) {
        //scanner.walkBack(start);
        break;
      }
      operations.push(op);
    } while (true);
    return operations;
  }

  private static String processEnvironmentLabel(Scanner scanner) {
    return scanner.nextScanOf(new GenericProgramIdentifier());
//    StringBuilder output = new StringBuilder();
//    char[] okay1 = new Char.Assembler(Char.MAP_ASCII_LETTERS).merge('-', '_').toMap();
//    do {
//      char c = scanner.next();
//      if (c == 0) return null;
//      else if (c == '=') break;
//      else if (!Char.mapContains(c, okay1)) return null;
//      else output.append(c);
//    } while (true);
//    scanner.back();
//    return output.toString();
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
