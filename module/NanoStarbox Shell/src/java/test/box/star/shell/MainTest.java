package box.star.shell;

import box.star.shell.runtime.parts.TextCommand;
import box.star.shell.runtime.parts.TextMain;
import box.star.shell.script.Interpreter;
import box.star.shell.runtime.parts.TextRedirection;
import box.star.shell.script.ShellMain;
import box.star.text.basic.LegacyScanner;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.shell.script.Parameter.QuoteType.*;
import static box.star.text.Char.SINGLE_QUOTE;
import static org.junit.jupiter.api.Assertions.*;

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
    ShellMain result = Interpreter.parse(ShellMain.class, scanner);
    return; // DEBUG-BREAK-HERE, and inspect Results
  }

  @Test void parse_literal_parameter(){
    String source = "echo";
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(NOT_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

  @Test void parse_single_quoted_parameter(){
    String source = "'echo'";
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(SINGLE_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

  @Test void parse_double_quoted_parameter(){
    String source = "\"echo\"";
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(DOUBLE_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

  @Test void parse_compound_parameter(){
    String source = "echo'hello'\"world\"";
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(COMPOUND_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

}