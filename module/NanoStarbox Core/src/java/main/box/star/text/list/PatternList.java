package box.star.text.list;

import box.star.state.RuntimeObjectMapping;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternList implements Serializable, RuntimeObjectMapping.ObjectWithLabel {
  private static final long serialVersionUID = -8772340584149844412L;
  final String label;
  final Pattern[] patterns;
  public PatternList(String label, Pattern... patterns){
    this.label = label;
    this.patterns = patterns;
  }
  @Override
  public String getRuntimeLabel() {
    return label;
  }
  @Override
  public String toString() {
    return getRuntimeLabel();
  }
  public boolean matches(String input){
    for (Pattern pattern:patterns) if (pattern.matcher(input).matches())return true;
    return false;
  }
  public Matcher match(String input){
    Matcher matcher;
    for (Pattern pattern:patterns) {
      matcher = pattern.matcher(input);
      if (matcher.matches())return matcher;
    }
    return null;
  }
}
