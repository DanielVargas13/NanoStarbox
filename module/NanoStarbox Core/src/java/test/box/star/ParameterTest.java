package box.star;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Stack;

class ParameterTest {

  // in this test, the last parameter (switch) should not parse because +b is not defined in switches.
  String[] parameters = new String[]{"-a:apple", "-b: boy", "-c=car", "-d", "daisy", "-xvc:MM", "-vxa", "apple!", "+nxvb:boy"};

  Parameter.Parser parameterParser = new Parameter.Parser() {

    Stack<String> switches = new Stack<>();
    Stack<String> flags = new Stack<>();

    {
      switches.addAll(Arrays.asList("-a", "-b", "-c", "-d"));
      flags.addAll(Arrays.asList("-x", "-v", "-n"));
      flags.addAll(Arrays.asList("+x", "+v", "+n"));
    }

    @Override
    public boolean parseReference(Reference parameter) {
      String value = parameter.getValue();
      //System.err.println(value);
      if (switches.contains(value)) {
        System.err.println("got switch: " + value + "; value: " + Parameter.getNextParameterValue(parameter));
        return true;
      } else if (flags.contains(value)) {
        System.err.println("got flag: " + value);
        return true;
      } else if (parameter.isFlagList()) {
        System.err.println("got flag-list: " + value);
        Parameter.parse(parameterParser, parameter);
        return true;
      }
      throw parameter.syntaxError("unknown parameter");
    }
  };

  @Test
  void parameterParsing() {
    try {
      Parameter.parse(parameterParser, parameters);
    } catch (RuntimeException e){
      System.err.println(e.getMessage());
    }
  }

}