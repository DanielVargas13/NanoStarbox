package box.star;

public class Parameter {

  public interface Parser { boolean acceptParameter(State parameter); }

  public static class State {
    String[] source;
    public int id, subid; public String value;
    public String type;
    public boolean plus;
    private boolean dataAvailable, split;
    private void select(int id, int subid, String value, boolean dataAvailable){
      this.id = id; this.subid = subid; this.value = value;
      this.dataAvailable = dataAvailable;
      this.split = false;
    }
  }

  static String trim(String input){
    return input.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
  }

  public static String getParameterValue(State parameter){
    String thisParameter = parameter.source[parameter.id];
    if (parameter.split) return trim(thisParameter.substring(parameter.value.length()+1));
    if (thisParameter.matches("^.+: *.*") || thisParameter.matches("^.+= *.*")){
      return trim(splitValue(thisParameter)[1]);
    }
    if (!parameter.dataAvailable) throw new RuntimeException("subscript out of range for request");
    return parameter.source[++parameter.id];
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
    State currentParameter = new State();
    currentParameter.source = parameters;
    for (currentParameter.id = 0; currentParameter.id < parameters.length; currentParameter.id++) {
      int i = currentParameter.id;
      currentParameter.select(i, 0, parameters[i], i+1 < parameters.length);
      if (currentParameter.value.matches("^.+: *.*") || currentParameter.value.matches("^.+= *.*")){
        currentParameter.value = splitValue(currentParameter.value)[0];
        currentParameter.split = true;
        if (parser.acceptParameter(currentParameter)) continue; else break;
      }
      if (parser.acceptParameter(currentParameter)) continue; else break;
    }
  }

  public static boolean parse(Parser parser, State parameter){
    State currentParameter = new State();
    currentParameter.source = parameter.source;
    char[] switches = parameter.value.substring(1).toCharArray();
    char type = parameter.value.charAt(0);
    if (type == '+') currentParameter.plus = true;
    for (currentParameter.subid = 0; currentParameter.subid < switches.length; currentParameter.subid++) {
      int i = currentParameter.subid;
      currentParameter.select(parameter.id, i, String.valueOf(type)+switches[i], parameter.dataAvailable);
      if (parser.acceptParameter(currentParameter)) continue;
      else return false;
    }
    parameter.id = currentParameter.id;
    return true;
  }

}
