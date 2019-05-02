package box.star;

public class Parameter {

  // Not instantiable.
  private Parameter(){}

  public interface Parser {

    boolean parseReference(Reference parameter);

    class Reference {
      private String[] source;
      public int index, subIndex; public String value;
      public boolean plus;
      private boolean dataAvailable, split;
      private void select(int index, int subIndex, String value, boolean dataAvailable){
        this.index = index; this.subIndex = subIndex; this.value = value;
        this.dataAvailable = dataAvailable;
        this.split = false;
      }
    }

  }

  static String trim(String input){
    return input.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
  }

  public static String getNextParameterValue(Parser.Reference parameter){
    String thisParameter = parameter.source[parameter.index];
    if (parameter.split) return trim(thisParameter.substring(parameter.value.length()+1));
    if (thisParameter.matches("^.+: *.*") || thisParameter.matches("^.+= *.*")){
      return trim(splitValue(thisParameter)[1]);
    }
    if (!parameter.dataAvailable) throw new RuntimeException("subscript out of range for request");
    return parameter.source[++parameter.index];
  }

  static String[] splitValue(String input){
    String[] split = new String[2];
    if (input.matches("^[^=]+: *.*")) {
      split[0] = input.substring(0, input.indexOf(":"));
      split[1] = input.substring(split[0].length()+1);
      return split;
    }
    if (input.matches("^.+= *.*")) {
      split[0] = input.substring(0, input.indexOf("="));
      split[1] = input.substring(split[0].length()+1);
      return split;
    }
    throw new RuntimeException("failed to split parameters");
  }

  public static void parse(Parser parser, String... parameters){
    Parser.Reference currentParameter = new Parser.Reference();
    currentParameter.source = parameters;
    for (currentParameter.index = 0; currentParameter.index < parameters.length; currentParameter.index++) {
      int i = currentParameter.index;
      currentParameter.select(i, 0, parameters[i], i+1 < parameters.length);
      if (currentParameter.value.matches("^.+: *.*") || currentParameter.value.matches("^.+= *.*")){
        currentParameter.value = splitValue(currentParameter.value)[0];
        currentParameter.split = true;
        if (parser.parseReference(currentParameter)) continue; else break;
      }
      if (parser.parseReference(currentParameter)) continue; else break;
    }
  }

  public static void parse(Parser parser, Parser.Reference parameter){
    Parser.Reference currentParameter = new Parser.Reference();
    currentParameter.source = parameter.source;
    char[] switches = parameter.value.substring(1).toCharArray();
    char type = parameter.value.charAt(0);
    if (type == '+') currentParameter.plus = true;
    for (currentParameter.subIndex = 0; currentParameter.subIndex < switches.length; currentParameter.subIndex++) {
      int i = currentParameter.subIndex;
      currentParameter.select(parameter.index, i, String.valueOf(type)+switches[i], parameter.dataAvailable);
      if (parser.parseReference(currentParameter)) continue;
      else return;
    }
    parameter.index = currentParameter.index;
  }

}
