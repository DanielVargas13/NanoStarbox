package box.star.shell;

public class Variable implements Cloneable {
  boolean
      export, // aka enumerable
      constant // aka writable
  ;
  Object value;
  public Variable(Object value){
    this.value = value;
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
    return this.value != null && (!(this.isString() || this.isInteger() || this.isBoolean() || this.isDouble()));
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
