package box.star.bin.sh.builtin.unix;

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

  private final static String backslash = "\\\\";
  private final static String vt = String.valueOf((char)11);
  private final static String bell = String.valueOf((char)7);
  private final static String esc = String.valueOf((char)27);


  List<String> interpretEscapes(List<String> source){
    List<String>out = new ArrayList<>(source.size());
    for (String param: source){
      param = param.replaceAll(backslash+"a", bell);
      param = param.replaceAll(backslash+"b", "\b");
      param = param.replaceAll(backslash+"c.*$", "");
      param = param.replaceAll(backslash+"e", esc);
      param = param.replaceAll(backslash+"E", esc);
      param = param.replaceAll(backslash+"f", "\f");
      param = param.replaceAll(backslash+"n", "\n");
      param = param.replaceAll(backslash+"r", "\r");
      param = param.replaceAll(backslash+"t", "\t");
      param = param.replaceAll(backslash+"v", vt);
      param = param.replaceAll(backslash+backslash, backslash);
      out.add(param);
    }
    return out;
  }

  @Override
  public int main(String[] parameters) {
    List<String> out = new ArrayList<>(Arrays.asList(parameters));
    out.remove(0);
    boolean interpretEscapes = true;
    String swtch = out.get(0), line = shell.getLineSeparator();
    while (swtch.startsWith("-")){
      if (swtch.equals("-n")) {
        line=""; out.remove(0);
        if (out.size() == 0) break;
        swtch = out.get(0);
        if (out.size() == 0) break;
        continue;
      }
      if (swtch.equals("-e")){
        interpretEscapes = true;
        out.remove(0);
        if (out.size() == 0) break;
        swtch = out.get(0);
        continue;
      }
      if (swtch.equals("-E")){
        interpretEscapes = false;
        out.remove(0);
        if (out.size() == 0) break;
        swtch = out.get(0);
        continue;
      }
      break;
    }
    if (interpretEscapes) out = interpretEscapes(out);
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

