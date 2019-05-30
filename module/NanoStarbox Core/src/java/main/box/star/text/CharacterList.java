package box.star.text;

import box.star.state.RuntimeObjectMapping;

public class CharacterList implements RuntimeObjectMapping.ObjectWithLabel {
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
