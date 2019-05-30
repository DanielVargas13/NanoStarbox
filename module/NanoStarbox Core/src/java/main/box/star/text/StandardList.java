package box.star.text;

import box.star.state.RuntimeObjectMapping;

import java.io.Serializable;

public abstract class StandardList<T> implements Serializable, RuntimeObjectMapping.ObjectWithLabel  {
  protected String label;
  protected T[] data;
  protected StandardList(String label, T[] data){
    this.label = label;
    this.data = data;
  }
  public int size(){
    return data.length;
  }
  @Override
  public String getRuntimeLabel() {
    return label;
  }
  @Override
  public String toString() {
    return getRuntimeLabel();
  }
  public boolean contains(T value){
    for (T item :
        data) {
      if (item.equals(value)) return true;
    }
    return false;
  }
}
