package box.star.text.basic;

import box.star.contract.NotNull;

public interface CancellableTask {
  @NotNull Bookmark cancel();
}
