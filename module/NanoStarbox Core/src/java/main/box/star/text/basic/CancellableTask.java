package box.star.text.basic;

import box.star.contract.NotNull;

interface CancellableTask {
  @NotNull Bookmark cancel();
}
