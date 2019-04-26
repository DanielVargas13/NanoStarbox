package box.star.bin;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class grep {

  public static void main(String[] args) {
    String line;
    int lines = 0;
    int matches = 0;

    Scanner scan = null;
    scan = new Scanner(System.in);

    // Get a regular expression from the command line
    Pattern pat = Pattern.compile(args[0]);

    // Match lines against the Regular Expression
    while (scan.hasNextLine()) {
      line = scan.nextLine();
      lines++;

      // Check if the current line contains the pattern
      Matcher match = pat.matcher(line);
      if (match.find()) {
        System.out.println(line);
        matches++;
      }
    }

    System.err.println("grep-status: " + lines + " lines, " + matches + " matches");

    System.out.flush();
  }

}