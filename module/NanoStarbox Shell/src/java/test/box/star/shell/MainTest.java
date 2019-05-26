package box.star.shell;

import box.star.shell.runtime.parts.TextEnvironment;
import box.star.shell.runtime.parts.TextRedirection;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

class MainTest {

  @Test void main(){
    Main shell = new Main("hi");
    System.out.println(shell.getShellBaseDirectory());
    Scanner scanner = new Scanner("test", "1 > 'colloqial ism'");
    TextRedirection r = TextRedirection.parseRedirect(scanner);
    scanner = new Scanner("test2", "A21=44");
    TextEnvironment e = TextEnvironment.parseEnvironmentOperations(scanner);
    System.out.println(String.join(", ", TextRedirection.redirectionOperators));
  }


}