package box.star.shell;

import box.star.shell.script.Command;
import box.star.text.basic.Scanner;

public class ReadEvalPrintLoopPrototype {
  public static void main(String[] p){
    Scanner x = new Scanner("/dev/stdin", System.in);
    Command cmd;
    System.err.println("type exit followed by enter on a single line to quit");
    do {
      cmd = ScriptParser.parse(Command.class, x);
    } while (! cmd.parameters.get(0).text.equals("exit"));

  }
}
