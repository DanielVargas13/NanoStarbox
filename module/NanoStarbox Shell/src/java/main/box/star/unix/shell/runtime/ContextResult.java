package box.star.unix.shell.runtime;

public class ContextResult {

  static final public ContextResult FALSE = new ContextResult(false);
  static final public ContextResult TRUE = new ContextResult(true);

  final public Object object;
  final public int status;
  final public long creationTime;
  public ContextResult(boolean status){
    this.object = status;
    this.status = (status)?0:1;
    creationTime = System.currentTimeMillis();
  }
  public ContextResult(int status) {
    object = null;
    this.status = status;
    creationTime = System.currentTimeMillis();
  }
  public ContextResult(int status, Object object) {
    this.object = object;
    this.status = status;
    creationTime = System.currentTimeMillis();
  }
  public ContextResult(boolean status, Object object) {
    this.object = object;
    this.status = (int)((status)?0:1);
    creationTime = System.currentTimeMillis();
  }
  public ContextResult(Object object) {
    this.object = object;
    this.status = (object != null)?0:1;
    creationTime = System.currentTimeMillis();
  }
}
