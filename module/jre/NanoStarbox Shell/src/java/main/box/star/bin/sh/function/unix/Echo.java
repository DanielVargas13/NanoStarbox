package box.star.bin.sh.function.unix;

import box.star.bin.sh.Function;
import box.star.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Echo extends Function implements Parameter.Parser {

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

  boolean interpretEscapes = false, printLine = true;
  String line = "";
  List<String> parameterList;

  @Override
  public boolean acceptParameter(Parameter.State parameter) {
    if (parameter.value.equals(getName())) {
      parameterList.remove(0);
      return true;
    }
    if (! parameter.value.startsWith("-")) return false;
    if (parameter.value.equals("-n")){
      parameterList.remove(0);
      printLine = false;
      return true;
    }
    if (parameter.value.equals("-e")){
      parameterList.remove(0);
      interpretEscapes = true;
      return true;
    }
    if (parameter.value.equals("-E")){
      interpretEscapes = false;
      return true;
    }
    return false;
  }

  @Override
  public int main(String[] parameters) {

    parameterList = new ArrayList<>(Arrays.asList(parameters));

    Parameter.parse(this, parameters);

    if (interpretEscapes) parameterList = interpretEscapes(parameterList);
    if (printLine) line = shell.getLineSeparator();

    try {
      stdout.write((String.join(" ", parameterList) + line).getBytes());
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

