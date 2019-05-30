package box.star.lang;

import box.star.contract.NotNull;

public class SyntaxError extends RuntimeException {

  protected Object host;

  private @NotNull String tag(){
    return host.getClass().getName()+".SyntaxError: ";
  }
  @Override
  public @NotNull String toString() {
    return tag() + super.getMessage();
  }

  public SyntaxError(@NotNull Object source, @NotNull String message){
    super(message);
    this.host = source;
  }

  public SyntaxError(@NotNull Object source, @NotNull String message, Throwable cause){
    super(message, cause);
    this.host = source;
  }

  protected SyntaxError(@NotNull String messge){
    super(messge);
  }
  protected SyntaxError(@NotNull String message, @NotNull Throwable cause){
    super(message, cause);
  }

}
