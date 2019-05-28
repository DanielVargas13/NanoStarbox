package box.star.text.basic;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

class ParserTest {

  public static class TestErrorParser extends Parser {
    public TestErrorParser(@NotNull Scanner scanner) {
      super(scanner);
    }
    @Override
    protected void start() {
     throw new SyntaxError(this, "sorry, i expected this to fail");
    }
  }

  @Test void syntax_error(){
    Scanner input = new Scanner(ParserTest.class.getName(), "test input");
    try {
      Parser.parse(TestErrorParser.class, input);
    } catch (Parser.SyntaxError pse){
      pse.printStackTrace();
    }
  }

  @Test void syntax_error2(){
    Scanner input = new Scanner(ParserTest.class.getName(), "test input");
    Parser.parse(TestErrorParser.class, input);
  }

}