package box.star.text.list;

import box.star.state.RuntimeObjectMapping;
import box.star.text.Char;

import java.io.Serializable;

public class RangeList implements Serializable, RuntimeObjectMapping.ObjectWithLabel {
  private static final long serialVersionUID = 9017972538783689725L;
  final String label;
  final Char.RangeMap[] ranges;
  public RangeList(String label, Char.RangeMap... ranges){
    this.label = label;
    this.ranges = ranges;
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
    for (Char.RangeMap range:ranges) if (range.match(c)) return true;
    return false;
  }
}
