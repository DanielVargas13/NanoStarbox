package box.star.unix.shell.runtime;

public class Viron {

  boolean readOnly, export;
  String name;
  Object value;
  
  public Viron(String name, Object value){
    this.value = value;
  }

  public String getType() {
    return "string";
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setReadOnly(){
    this.readOnly = true;
  }
  
  public void export(boolean toChildren){
    this.export = toChildren;
  }
  
  @Override
  public String toString() {
    return super.toString();
  }
  
}
