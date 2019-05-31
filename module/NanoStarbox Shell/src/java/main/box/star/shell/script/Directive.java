package box.star.shell.script;

import box.star.text.basic.Scanner;

public class Directive extends Comment {
  public Directive(Scanner scanner) {
    super(scanner);
  }
  String getParameterString(){
    String[] data = text.split("\\s", 2);
    return data[data.length - 1];
  }
  String getInterpreterPath(){
    String[] data = text.split("\\s", 2);
    return data[0];
  }
}
