package box.star;

import org.junit.jupiter.api.Test;

class CommandTest {
    @Test
    void test_command_chain() throws Exception {

        Command NanoStarbox = new Command("java", "-cp", "jar/NanoStarbox.jar");

        Command cat = NanoStarbox.use("box.star.bin.cat");
        Command grep = NanoStarbox.use("box.star.bin.grep");

        cat.use("sample/grep-test.txt").connect(grep.use("hello|^grep|match")).start();

    }
}