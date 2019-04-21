package box.star.system;

import org.junit.jupiter.api.Test;

class CommandTest {

    Environment environment = new Environment();
    Command starbox = new Command(environment, "java", "-cp", "jar/NanoStarbox.jar");

    @Test void main(){
        Command shell = new Command(environment, "cmd", "/c");
        Command dir = shell.build("dir");
        dir.exec();
        dir.join();
    }

    @Test void cat(){
        Command cat = starbox.build("box.star.bin.cat", "sample/grep-test.txt");
        Command grep = starbox.build("box.star.bin.grep", "home");
        cat.set(1, grep);
        cat.run();
    }

}