package box.star.system;

import box.star.system.action.echo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    /** Auto register builtins ( lazy-testing )
     *
     */
    static {
        Environment.registerAction(new echo());
    }

    Environment environment = new Environment();
    Command starbox = new Command(environment, "java", "-cp", "jar/NanoStarbox.jar");
    Command echo = new Command(environment, "echo");

    @Test void main(){
        Command shell = new Command(environment, "cmd", "/c");
        Command dir = shell.create("dir");
        dir.exec();
        dir.join();
    }

    @Test void grep(){
        Command cat = starbox.create("box.star.bin.cat", "sample/grep-test.txt");
        Command grep = starbox.create("box.star.bin.grep", "hello");
        cat.pipe(grep).run();
    }

    @Test void action(){
        echo.create("-n", "hello world").run();
    }

}