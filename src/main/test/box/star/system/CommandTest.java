package box.star.system;

import org.junit.jupiter.api.Test;

class CommandTest {

    Environment environment = new Environment();

    @Test void main(){
        Command shell = new Command(environment, "cmd", "/c");
        Command dir = shell.build("dir");
        dir.exec();
        dir.join();
    }

}