package box.star.system;

import box.star.system.action.*;
import org.junit.jupiter.api.Test;

class CommandTest {

    /** Auto register builtins ( lazy-testing )
     *
     */
    static {
        Environment.registerAction(new echo());
        Environment.registerAction(new exit());
    }

    Environment environment = new Environment();
    Command starbox = new Command(environment, "java", "-cp", "jar/NanoStarbox.jar");
    Command echo = new Command(environment, "echo");
    Command exit = new Command(environment, "exit");

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
        //exit.create("4").run();
        echo.create("-n", "hello world").run();
    }

}