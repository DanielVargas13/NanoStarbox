package box.star;

public class Parameter {

  // Not instantiable.
  private Parameter() {}

  static String trim(String input) {
    return input.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
  }

  public static String getNextParameterValue(Parser.Reference parameter) {
    String thisParameter = parameter.source[parameter.index];
    if (parameter.split) return trim(parameter.value[1]);
    if (thisParameter.matches("^.+: *.*") || thisParameter.matches("^.+= *.*")) {
      return trim(splitValue(thisParameter)[1]);
    }
    if (!parameter.dataAvailable) throw new RuntimeException("subscript out of range for request");
    return parameter.source[++parameter.index];
  }

  static String[] splitValue(String input) {
    String[] split = new String[2];
    if (input.matches("^[^=]+: *.*")) {
      split[0] = input.substring(0, input.indexOf(":"));
      split[1] = input.substring(split[0].length() + 1);
      return split;
    }
    if (input.matches("^.+= *.*")) {
      split[0] = input.substring(0, input.indexOf("="));
      split[1] = input.substring(split[0].length() + 1);
      return split;
    }
    throw new RuntimeException("failed to split parameters");
  }

  public static void parse(Parser parser, String... parameters) {
    Parser.Reference currentParameter = new Parser.Reference();
    currentParameter.source = parameters;
    for (currentParameter.index = 0; currentParameter.index < parameters.length; currentParameter.index++) {
      int i = currentParameter.index;
      currentParameter.select(i, 0, parameters[i], i + 1 < parameters.length);
      if (currentParameter.value[0].matches("^.+: *.*") || currentParameter.value[0].matches("^.+= *.*")) {
        currentParameter.value = splitValue(currentParameter.value[0]);
        currentParameter.split = true;
        if (parser.parseReference(currentParameter)) continue;
        else break;
      }
      if (parser.parseReference(currentParameter)) continue;
      else break;
    }
  }

  public static void parse(Parser parser, Parser.Reference parameter) {
    Parser.Reference currentParameter = new Parser.Reference();
    currentParameter.source = parameter.source;
    char[] switches = parameter.value[0].substring(1).toCharArray();
    currentParameter.type = parameter.value[0].charAt(0);
    for (currentParameter.subIndex = 0; currentParameter.subIndex < switches.length; currentParameter.subIndex++) {
      int i = currentParameter.subIndex;
      currentParameter.select(parameter.index, i, String.valueOf(currentParameter.type) + switches[i], parameter.dataAvailable);
      if (parser.parseReference(currentParameter)) continue;
      else return;
    }
    parameter.index = currentParameter.index;
  }

  public interface Parser {

    boolean parseReference(Reference parameter);

    class Reference {
      private int index, subIndex;
      private String[] value;
      private char type;
      private String[] source;
      private boolean dataAvailable, split;
      private void select(int index, int subIndex, String value, boolean dataAvailable) {
        this.index = index;
        this.subIndex = subIndex;
        this.value = new String[]{value, null};
        this.dataAvailable = dataAvailable;
        this.split = false;
      }
      /**
       * Returns the char that started this sub-index/switch-parameter
       * @return 0, + or -
       */
      public char getType(){
        return type;
      }
      /**
       * Position in parameter stream
       * @return
       */
      public int getIndex() {
        return index;
      }
      /**
       * Character position in parameter
       * @return
       */
      public int getSubIndex(){
        return subIndex;
      }
      /**
       * Parameter value (the actual parameter, not its arguments)
       * @return
       */
      public String getValue() {
        return value[0];
      }
      /**
       * Compares the parameter value to obj
       * @param obj
       * @return
       */
      @Override
      public boolean equals(Object obj) {
        return value[0].equals(obj);
      }
      /**
       * Performs a test to see if this parameter follow the form of a swet
       * of switches.
       * @return true if the parameter starts with a plus or minus followed by two or more letters.
       */
      public boolean isFlagList(){
        return value[0].matches("^[+|-][a-zA-Z0-9][a-zA-Z0-9].*");
      }
      /**
       * Constructs a simple syntax error describing this parameter
       * @param message
       * @return
       */
      public RuntimeException syntaxError(String message){
        if (subIndex > 0){
          return new RuntimeException(message+" at parameter " + (index + 1) + " switch #"+(subIndex+1)+ " = "+getValue());
        }
        return new RuntimeException(message+" at parameter " + (index + 1) + " = "+getValue());
      }
    }

  }

}
