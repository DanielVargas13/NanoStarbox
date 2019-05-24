package box.star.shell;

import java.util.Arrays;

public class Variable implements Cloneable {
  boolean
      export, // aka enumerable
      constant // aka writable
  ;
  Object value;
  public Variable(Object value){
    this.value = value;
  }
  public Variable(Object value, boolean export){
    this.value = value;
    this.export = export;
  }
  @Override
  public String toString() {
    if (this.isString()) return (String) value;
    return this.value.toString();
  }
  public void setValue(Object value) {
    if (this.constant == false) this.value = value;
  }
  public <ANY> ANY getValue(){
    return (ANY) this.value;
  }

  public boolean isByte(){
    return Byte.class.isInstance(value);
  }
  public boolean isCharacter(){
    return Character.class.isInstance(value);
  }
  public boolean isString(){
    return String.class.isInstance(this.value);
  }
  public boolean isBoolean(){
    return Boolean.class.isInstance(this.value);
  }
  public boolean isInteger(){
    return Integer.class.isInstance(this.value);
  }
  public boolean isDouble(){
    return Double.class.isInstance(this.value);
  }
  public boolean isObject(){
    return isObjectOfClass(Object.class);
  }
  public Class<?> getObjectClass(){
    return getValue().getClass();
  }
  public boolean isObjectOfClass(Class type){
    return type.isInstance(this.value);
  }
  public <T> T getObject(Class<? extends T> type){
    return type.cast(getValue());
  }
  @Override
  protected Variable clone() {
    try /*  throwing runtime exceptions with closure */ {
      return (Variable) super.clone();
    } catch (Exception e){throw new RuntimeException(e);}
    finally /*  complete */ {
      ;
    }
  }
}
