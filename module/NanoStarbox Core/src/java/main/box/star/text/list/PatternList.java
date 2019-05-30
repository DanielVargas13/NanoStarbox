package box.star.text.list;

import box.star.state.RuntimeObjectMapping;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternList extends StandardList<Pattern> {
  private static final long serialVersionUID = -8772340584149844412L;
  public PatternList(String label, Pattern... patterns){
    super(label, patterns);
  }

  public boolean matches(String input){
    for (Pattern pattern:data) if (pattern.matcher(input).matches())return true;
    return false;
  }
  public Matcher match(String input){
    Matcher matcher;
    for (Pattern pattern:data) {
      matcher = pattern.matcher(input);
      if (matcher.matches())return matcher;
    }
    return null;
  }
}
