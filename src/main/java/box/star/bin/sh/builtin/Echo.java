package box.star.bin.sh.builtin;

import box.star.bin.sh.Function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Echo extends Function {

  private static Echo factory;

  public static Function getFactory(){
    if (factory == null) factory = new Echo();
    return factory;
  }

  private Echo(){};

  @Override
  public int main(String[] parameters) {
    List<String> out = new ArrayList<>(Arrays.asList(parameters));
    out.remove(0);
    String swtch = out.get(0), line = shell.getLineSeparator();
    while (swtch.startsWith("-")){
      if (swtch.equals("-n")) {
        line=""; out.remove(0);
        continue;
      }
      break;
    }
    try {
      stdout.write((String.join(" ", out) + shell.getLineSeparator()).getBytes());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public String getName() {
    return "echo";
  }

}

