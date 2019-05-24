package box.star.shell;

import box.star.text.basic.Scanner;

import java.util.Stack;

public class Command {

  final String origin;
  Stack<String[]> environmentOperations;
  Stack<String> parameters;
  StreamTable redirects;
  char terminator; // whatever terminated this command
  Command next; // if terminator == pipe
  Command(String origin) {this.origin = origin;}

  public static final Command parse(Scanner textScanner){
    return null;
  }

}
