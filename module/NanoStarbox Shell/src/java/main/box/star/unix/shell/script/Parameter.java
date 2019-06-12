package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

import java.util.ArrayList;

public class Parameter extends SourceElement {
  /**
   * <p>A single parameter can be composed of multiple text elements.</p>
   */
  public ArrayList<String> text;
  public ParameterType type;
  public Parameter(@NotNull Scanner scanner) {
    super(scanner);
  }

}
