package box.star.shell.script;

import box.star.text.basic.Scanner;

public class HereDocument extends Parameter implements box.star.text.basic.Parser.NewFuturePromise {
  HereDocument(Scanner scanner) {
    super(scanner);
  }
}
