package box.star.shell.runtime;

import box.star.contract.BooleanValue;
import box.star.contract.NumericValue;
import box.star.contract.StringValue;

public class ReturnValue implements NumericValue, BooleanValue, StringValue {
  final Object value;
  final int status;
  public ReturnValue(boolean value){
    this.value = value;
    status = (value)?0:1;
  }
  public ReturnValue(int status) {
    value = null;
    this.status = status;
  }
  public ReturnValue(int status, Object value) {
    this.value = value;
    this.status = status;
  }
  @Override
  public Double toNumber() {
    if (value instanceof Integer) return (double) (int) value;
    if (value instanceof Boolean) return (double)(((boolean) value)?0:1);
    if (value instanceof NumericValue) return ((NumericValue) value).toNumber();
    return 1d;
  }
  @Override
  public String toString() {
    return String.valueOf(value);
  }
  @Override
  public boolean toBoolean() {
    if (value instanceof Boolean) return (boolean) value;
    if (value instanceof Integer) return  (int) value == 0;
    if (value instanceof NumericValue) return ((NumericValue) value).toNumber() == 0;
    return false;
  }
  public Object getObject(){
    return value;
  }
}
