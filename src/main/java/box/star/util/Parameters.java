package box.star.util;

public class Parameters {

  public static class CurrentParameter {
    public int id, subid; public String value;
    public boolean dataAvailable;
    private void select(int id, int subid, String value, boolean dataAvailable){
      this.id = id; this.subid = subid; this.value = value;
      this.dataAvailable = dataAvailable;
    }
  }

  public interface ParameterHandler {
    boolean nextParameter(CurrentParameter parameter);
    boolean wantParameterValue(CurrentParameter parameter);
    void postParameterValue(CurrentParameter parameter, String value);
  }

  public static void parse(ParameterHandler parameterHandler, CurrentParameter parameter){
    CurrentParameter currentParameter = new CurrentParameter();
    char[] switches = parameter.value.toCharArray();
    for (int i = 0; i < switches.length; i++) {
      currentParameter.select(i, 0, switches[i]+"", parameter.dataAvailable);
      if (parameterHandler.nextParameter(currentParameter)) {
        continue;
      }
    }
  }

  public static void parse(ParameterHandler parameterHandler, String... parameters){
    CurrentParameter currentParameter = new CurrentParameter();
    for (int i = 0; i < parameters.length; i++) {
      currentParameter.select(i, 0, parameters[i], i+1 < parameters.length);
      if (parameterHandler.nextParameter(currentParameter)) {
       if (parameterHandler.wantParameterValue(currentParameter)){
         parameterHandler.postParameterValue(currentParameter, parameters[++i]);
       }
     } else break;
    }
  }

}
