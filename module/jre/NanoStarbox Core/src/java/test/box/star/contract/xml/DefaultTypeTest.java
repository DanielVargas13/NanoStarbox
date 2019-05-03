package box.star.contract.xml;

import box.star.contract.xml.core.Persister;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 *
 */
class DefaultTypeTest {

  @Root
  public static class Example {

    @Element
    private String text;

    @Attribute
    private int index;

    public Example() {
      super();
    }

    public Example(String text, int index) {
      this.text = text;
      this.index = index;
    }

    public String getMessage() {
      return text;
    }

    public int getId() {
      return index;
    }
  }

  @Test void main() throws Exception {
    Serializer serializer = new Persister();
    Example example = new Example("Example message", 123);

    serializer.write(example, System.err);
  }
}