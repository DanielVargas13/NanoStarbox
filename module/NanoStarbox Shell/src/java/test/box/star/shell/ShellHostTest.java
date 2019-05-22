package box.star.shell;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ShellHostTest {

  private final File shebang_line_file = new File("src/java/test/box/star/shell/shebang-line.txt");

  @Test void shebang_line_sh(){
    Host sh = new Host();
    System.out.print(sh.start(shebang_line_file, "hello world"));
  }

}