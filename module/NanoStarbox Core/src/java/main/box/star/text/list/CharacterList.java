package box.star.text.list;

import box.star.state.RuntimeObjectMapping;
import box.star.text.Char;

import java.io.Serializable;

public class CharacterList extends StandardList<Character> {
  private static final long serialVersionUID = -6565154605439853891L;
  public CharacterList(String label, Character... chars){
    super(label, chars);
  }
}
