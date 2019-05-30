package box.star.text.basic;

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

  SyntaxError(@NotNull String sourceTag, @NotNull String message) {
    super("\n\n"+message+":\n\n   "+sourceTag+"\n");
  }

  SyntaxError(@NotNull String sourceTag, @NotNull String message, @NotNull Throwable cause) {
    super("\n\n"+message+":\n\n   "+sourceTag+"\n", cause);
  }

  SyntaxError(Bookmark cancel, String message) {
    this(cancel.toString(), message);
  }
  SyntaxError(Bookmark cancel, String message, Throwable cause) {
    this(cancel.toString(), message, cause);
  }

  public SyntaxError(@NotNull Cancellable action, @NotNull String message){
    this(action.cancel(), message);
    this.host = action;
  }

  public SyntaxError(@NotNull Cancellable action, @NotNull String message, Throwable cause){
    this(action.cancel(), message, cause);
    this.host = action;
  }
}
