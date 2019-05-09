package box.star.text.basic;

import box.star.contract.NotNull;

import java.io.IOException;

public class ScannerStateLock {

  protected Scanner main;
  protected ScannerState backupState;

  protected ScannerStateLock(@NotNull Scanner main) {
    if (main.hasStateLock())
      throw new Scanner.Exception("cannot acquire scanner lock",
          new IllegalStateException("state lock acquired"));
    if (!main.haveNext()) {
      throw new Scanner.Exception("cannot acquire scanner lock",
          new IllegalStateException("end of source data"));
    }
    this.main = main;
    this.backupState = main.state.clone();
    try {
      main.reader.mark(1000000);
    }
    catch (IOException e) {
      throw new Scanner.Exception("failed to configure source reader", e);
    }
    main.state.snapshot = true;
  }

  public void restore() throws Scanner.Exception {
    if (main == null) return;
    try {
      try {
        main.reader.reset();
        main.state = backupState;
      }
      catch (IOException e) {
        throw new Scanner.Exception("failed to release lock", e);
      }
    }
    finally { free(); }
  }

  public void free() {
    if (main == null) return;
    try {
      try { main.reader.mark(1);}
      catch (IOException ignore) {}
    }
    finally {
      this.main.state.snapshot = false;
      this.main = null;
      this.backupState = null;
    }
  }
}
