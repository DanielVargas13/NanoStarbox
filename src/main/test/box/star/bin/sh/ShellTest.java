package box.star.bin.sh;

import box.star.bin.sh.function.unix.Echo;
import org.junit.jupiter.api.Test;

class ShellTest {

  Shell shell = new Shell();

  @Test void main() {
    shell.defineFunction(Echo.getFactory());
    Command starbox = shell.build( "java", "-cp", "jar/NanoStarbox.jar");
    Command cat = starbox.build("box.star.bin.cat", "sample/grep-test.txt");
    Command echo = shell.build("function", "echo", "-e", "hello", "world,", "real", "unix\\c-like", "parameter", "handling");
    Command grep = starbox.build("box.star.bin.grep", "hello");
    echo.pipe(grep).run();
  }
}