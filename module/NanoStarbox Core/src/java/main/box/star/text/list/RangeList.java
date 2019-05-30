package box.star.text.list;

import box.star.state.RuntimeObjectMapping;
import static box.star.text.Char.*;

import java.io.Serializable;

public class RangeList extends StandardList<RangeMap> {
  private static final long serialVersionUID = 9017972538783689725L;
  public RangeList(String label, RangeMap... ranges){
    super(label, ranges);
  }
  public boolean match(char c){
    for (RangeMap range:data) if (range.match(c)) return true;
    return false;
  }
}
