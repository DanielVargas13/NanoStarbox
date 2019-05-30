package box.star.text;

import box.star.state.RuntimeObjectMapping;

public class RangeList implements RuntimeObjectMapping.ObjectWithLabel {
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
