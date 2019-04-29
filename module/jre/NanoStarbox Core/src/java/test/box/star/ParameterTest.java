package box.star;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Stack;

class ParameterTest {

  // in this test, the last parameter (switch) should not parse because +b is not defined in switches.
  String[] parameters = new String[]{"-a:apple", "-b: boy", "-c=car", "-d", "daisy", "-xvc:MM", "-vxa", "apple!", "+nxvb:boy"};

  Parameter.Parser parser = new Parameter.Parser() {

    Stack<String>switches = new Stack<>();
    Stack<String>flags = new Stack<>();

    {
      switches.addAll(Arrays.asList("-a", "-b", "-c", "-d"));
      flags.addAll(Arrays.asList("-x", "-v", "-n"));
      flags.addAll(Arrays.asList("+x", "+v", "+n"));
    }

    @Override
    public boolean acceptParameter(Parameter.State parameter) {
      System.err.println(parameter.value);
      if (switches.contains(parameter.value)) {
        System.err.println("got switch: "+parameter.value+"; value: "+Parameter.getParameterValue(parameter));
        return true;
      } else if (flags.contains(parameter.value)){
        System.err.println("got flag: "+ parameter.value);
        return true;
      } else if (parameter.value.matches("^[+|-][a-zA-Z0-9][a-zA-Z0-9].*")) {
        System.err.println("got parameter-set: "+ parameter.value);
        Parameter.parse(parser, parameter);
        return true;
      }
      return false;
    }
  };

  @Test
  void parameterParsing(){
    Parameter.parse(parser, parameters);
  }

}