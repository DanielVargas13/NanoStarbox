package box.star.shell;

import box.star.text.basic.Scanner;

import java.util.Stack;

public class Command {

  final String origin;
  Stack<String[]> environmentOperations;
  Stack<String> parameters;
  /**
   * <pre>
   * Effective status of STDIO: O | N | E
   * O = Origin
   * N = Number or Zero
   * E = End
   * if O or N: stdio[2] is user-effective
   * if E: stdio[2], stdio[1] and stdio[0] are user effective
   * </pre>
   */
  StreamTable redirects;
  char terminator; // whatever terminated this command
  Command next; // if terminator == pipe
  Command(String origin) {this.origin = origin;}

  public static final Command parse(Scanner textScanner){
    return null;
  }

  /**
   * <p>Execute the command within the current context, returning the status of
   * each command executed.</p>
   *
   * @param context the shell context
   * @return array of each command exit status, where element 0 is always the status of the last command executed. this feature enables "PIPE-STATUS"
   */
  public int[] exec(Main context){
    return new int[0];
  }

}
