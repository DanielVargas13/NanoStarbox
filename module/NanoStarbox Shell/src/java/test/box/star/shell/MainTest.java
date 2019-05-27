package box.star.shell;

import box.star.shell.runtime.parts.TextCommand;
import box.star.shell.runtime.parts.TextMain;
import box.star.shell.runtime.TextRecord;
import box.star.shell.runtime.parts.TextRedirection;
import box.star.shell.runtime.text.MainControl;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.io.File;

class MainTest {

  void main(){
    Main shell = new Main("hi");
    System.out.println(shell.getShellBaseDirectory());
    Scanner scanner = new Scanner("test", "(shit hit the fan < 'colloqial ism') | (grep shell)");
    //TextRedirection r = TextRedirection.parseRedirect(scanner);
    //scanner = new Scanner("test2", "A21=44 b72=33 cout shit | grep fu");
    TextCommand e = TextMain.parseTextCommands(scanner);
    System.out.println(String.join(", ", TextRedirection.redirectionOperators));
  }

  void scanControl(){
    MainControl mc = new MainControl();
    Scanner scanner = new Scanner(getClass().getSimpleName(), "Hello to you");
    scanner.assemble(mc);
  }

  @Test void text_record_main(){
    File shebang_line_file = new File("src/java/test/box/star/shell/shebang.txt");
    Scanner scanner = new Scanner(shebang_line_file);
    TextRecord.Main result = TextRecord.parse(TextRecord.Main.class, scanner);
    System.err.println(result.getOrigin()); // DEBUG-BREAK-HERE, and inspect Results
  }
}