package box.star.system;

import box.star.system.builtins.*;
import org.junit.jupiter.api.Test;
import static box.star.system.Command.*;

class CommandTest {

    Environment environment = new Environment();
    Command starbox = new Command(environment, "java", "-cp", "jar/NanoStarbox.jar");

    @Test void main(){
        Command shell = new Command(environment, "cmd", "/c");
        Command dir = shell.build("dir");
        dir.exec();
        dir.join();
    }

    @Test void grep(){
        Command cat = starbox.build("box.star.bin.cat", "sample/grep-test.txt");
        Command grep = starbox.build("box.star.bin.grep", "hello");
        cat.pipe(grep).run();
    }

    @Test void builtin(){
        Environment.registerBuiltin("echo", echo.class);
        Command echo = new Command(environment,"echo", "hello world");
        echo.run();
    }

}