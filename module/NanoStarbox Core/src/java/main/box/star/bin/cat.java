package box.star.bin;

import box.star.io.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class cat {
  private static String INPUT_STREAM_SIGNAL = "-";
  public static void main(String[] args) {
    if (args.length == 0) args = new String[]{INPUT_STREAM_SIGNAL};
    for (String fileName : args) doFile(fileName);
  }
  static private void doFile(String file){
    try {
      if (file.equals(INPUT_STREAM_SIGNAL)) Streams.transfer(System.in, System.out);
      else Streams.transfer(new FileInputStream(new File(file)), System.out);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}