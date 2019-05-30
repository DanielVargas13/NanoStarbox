package box.star.text.list;

import box.star.state.RuntimeObjectMapping;
import box.star.text.Char;

import java.io.Serializable;

public class CharacterList implements Serializable, RuntimeObjectMapping.ObjectWithLabel {
  private static final long serialVersionUID = -6565154605439853891L;
  final String label;
  final char[] chars;
  public CharacterList(String label, char... chars){
    this.label = label;
    this.chars = chars;
  }
  @Override
  public String getRuntimeLabel() {
    return label;
  }
  @Override
  public String toString() {
    return getRuntimeLabel();
  }
  public boolean contains(char c){
    return Char.mapContains(c, chars);
  }
}
