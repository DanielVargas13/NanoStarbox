package box.star.shell;

import box.star.text.basic.Scanner;

import java.util.Stack;

public class Command {

  final String origin;
  Stack<String[]> environmentOperations;
  Stack<String> parameters;
  /**
   * Effective status of STDIO: O | N | E
   * O = Origin
   * N = Number or Zero
   * E = End
   * if O or N: stdio[2] is user-effective
   * if E: stdio[2], stdio[1] and stdio[0] are user effective
   */
  StreamTable redirects;
  char terminator; // whatever terminated this command
  Command next; // if terminator == pipe
  Command(String origin) {this.origin = origin;}

  public static final Command parse(Scanner textScanner){
    return null;
  }

}
