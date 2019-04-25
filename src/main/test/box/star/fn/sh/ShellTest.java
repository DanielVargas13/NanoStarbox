package box.star.fn.sh;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ShellTest {

  Shell shell = new Shell();

  @Test void main() {
    shell.defineFunction("echo", new Function(){
      @Override
      public int main(String[] parameters) {
        try {
          stdout.write((parameters[1] + "\n").getBytes());
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        return 0;
      }
    });
    Command starbox = shell.build( "java", "-cp", "jar/NanoStarbox.jar");
    Command cat = shell.build("echo", "hello world");
    //Command cat = starbox.build("box.star.bin.cat", "sample/grep-test.txt");
    Command grep = starbox.build("box.star.bin.grep", "hello");
    cat.pipe(grep).run();
  }
}