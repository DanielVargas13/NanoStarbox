package box.star.bin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.*;
import java.util.Scanner;
import java.io.*;

public class grep {

  public static void main2(String[] args) {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line;
    try {
      while ((line = br.readLine()) != null) {
        for (String expr: args) {
          if (line.matches(expr)) {
            System.out.println(line);
          }
        }
      }
    } catch (Exception e){}
  }

  public static void main(String[] args) {
    String line;
    int lines = 0;
    int matches = 0;

    Scanner scan = null;
    scan = new Scanner( System.in );

    // Get a regular expression from the command line
    Pattern pat = Pattern.compile( args[0] );

    // Match lines against the Regular Expression
    while ( scan.hasNextLine() )
    {
      line = scan.nextLine();
      lines++;

      // Check if the current line contains the pattern
      Matcher match = pat.matcher( line );
      if (match.find())
      {
        System.out.println (line);
        matches++;
      }
    }

    System.err.println( "grep-status: " + lines + " lines, " + matches + " matches" );

    System.out.flush();
  }

}