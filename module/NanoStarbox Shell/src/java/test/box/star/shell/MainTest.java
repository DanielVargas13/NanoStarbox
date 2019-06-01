package box.star.shell;

import box.star.shell.script.Interpreter;
import box.star.shell.script.ShellMain;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.shell.script.Parameter.QuoteType.*;
import static box.star.text.Char.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

  @Test void parse_shell_script_main(){
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
    String source = DOUBLE_QUOTE+"echo"+BACKSLASH+DOUBLE_QUOTE+DOUBLE_QUOTE;
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(DOUBLE_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

  @Test void parse_compound_parameter(){
    String source = "echo'hello'\"world\"echo\\ ";
    Scanner scanner = new Scanner("test", source);
    Interpreter.ParameterList pl = Interpreter.parseParameterList(scanner);
    assertEquals(source, pl.get(0).getText());
    assertEquals(COMPOUND_QUOTING, pl.get(0).getQuoteType());
    assertTrue(scanner.endOfSource());
  }

}