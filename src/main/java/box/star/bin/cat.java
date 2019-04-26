package box.star.bin;

import box.star.io.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class cat {
  public static void main(String[] args) {
    for (String fileName : args) {
      try {
        System.out.print(Streams.readWholeString(new FileInputStream(new File(fileName))));
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
