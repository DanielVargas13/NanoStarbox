package box.star.text;

import box.star.state.RuntimeObjectMapping;

import java.util.regex.Pattern;

public class PatternList implements RuntimeObjectMapping.ObjectWithLabel {
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
}
