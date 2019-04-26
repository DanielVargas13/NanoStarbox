package box.star.bin.sh;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShellTest {

  Shell shell = new Shell(){

  };

  @Test void main() {
    shell.defineFunction("echo", new Function(){
      @Override
      public int main(String[] parameters) {
        List<String> out = new ArrayList<>(Arrays.asList(parameters));
        out.remove(0);
        try {
          stdout.write((String.join(" ", out) + "\n").getBytes());
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        return 0;
      }
    });
    Command starbox = shell.build( "java", "-cp", "jar/NanoStarbox.jar");
    Command cat = starbox.build("box.star.bin.cat", "sample/grep-test.txt");
    Command echo = shell.build("echo", "hello", "world,", "real", "unix-like", "parameter", "handling");
    Command grep = starbox.build("box.star.bin.grep", "hello");
    echo.pipe(grep).run();
  }
}