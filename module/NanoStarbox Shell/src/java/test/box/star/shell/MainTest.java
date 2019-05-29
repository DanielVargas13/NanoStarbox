package box.star.shell;

import box.star.shell.runtime.parts.TextCommand;
import box.star.shell.runtime.parts.TextMain;
import box.star.shell.script.Parser;
import box.star.shell.runtime.parts.TextRedirection;
import box.star.text.basic.LegacyScanner;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

  void main(){
    Main shell = new Main("hi");
    System.out.println(shell.getShellBaseDirectory());
    LegacyScanner scanner = new LegacyScanner("test", "(shit hit the fan < 'colloqial ism') | (grep shell)");
    //TextRedirection r = TextRedirection.parseRedirect(scanner);
    //scanner = new Scanner("test2", "A21=44 b72=33 cout shit | grep fu");
    TextCommand e = TextMain.parseTextCommands(scanner);
    System.out.println(String.join(", ", TextRedirection.redirectionOperators));
  }

  @Test void text_record_main(){
    File shebang_line_file = new File("src/java/test/box/star/shell/shebang.txt");
    Scanner scanner = new Scanner(shebang_line_file);
    Parser.Main result = Parser.parse(Parser.Main.class, scanner);
    System.err.println(result.getOrigin()); // DEBUG-BREAK-HERE, and inspect Results
  }
  @Test void parser_parameter_list(){
    Scanner scanner = new Scanner("test", "'echo'");
    Parser.ParameterList pl = Parser.ParameterList.parse(scanner);
    System.err.println(pl);
  }
}