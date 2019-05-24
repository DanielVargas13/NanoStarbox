package box.star.text.basic;

import java.io.Serializable;
import java.util.Stack;

import static box.star.text.Char.*;

public class ScannerState implements Cloneable, Serializable {

  protected static final int historySize = 1024;

  protected String path;
  protected long column, index, line;

  protected Stack<Long> columnHistory;
  protected StringBuilder buffer;
  protected int bufferPosition;
  @Deprecated protected boolean locked;
  protected boolean eof, slashing, escaped, escapeLines, escapeUnderscoreLine;

  public ScannerState(String path) {
    ScannerState state = this;
    state.path = path;
    state.index = -1;
    state.line = 1;
    state.clearHistory();
  }

  public void trimHistoryLength(int length) {
    if (haveNext()) {
      // in any case, we don't want to move the user's cursor (bufferPosition).
      throw new IllegalStateException("trying to trim history while browsing history");
    }
    int max = getHistoryLength();
    // don't trim if we have not met the length
    if (length >= max) {return;}
    // zero or less means clear-all
    if (length <= 0) {
      clearHistory();
      return;
    }
    Stack<Long> cHist = new Stack<>();
    // collect the column positions for back-stepping through lines
    for (int i = 0, y = max, z = columnHistory.size(); i < length; i++) {
      switch (buffer.charAt(--y)) {
        case LINE_FEED:
        case CARRIAGE_RETURN:
          cHist.add(0, columnHistory.get(--z));
          break;
      }
    }
    StringBuilder cbuffer = new StringBuilder(historySize);
    // trim the buffer
    cbuffer.append(buffer.substring(max - length));
    // apply the buffer
    buffer = cbuffer;
    // restore the user's cursor
    bufferPosition = length - 1;
    // apply the column history
    columnHistory = cHist;
  }

  public int getHistoryLength() {
    return buffer.length();
  }

  public void clearHistory() {
    buffer = new StringBuilder(128);
    bufferPosition = -1;
    columnHistory = new Stack<>();
  }

  protected char escape(char c) {

    char previous = previousCharacter();

    boolean lineMode = (c == CARRIAGE_RETURN || c == LINE_FEED);
    boolean slashMode = (c == BACKSLASH);

    if (previous == BACKSLASH && slashing == true) escaped = true;
    else escaped = escapeUnderscoreLine && previous == '_' && lineMode;

    if (slashMode) this.slashing = !this.slashing;
    else this.slashing = false;

    if (escaped && escapeLines && lineMode) c = 0;

    return c;

  }

  public boolean haveNext() {
    return bufferPosition != (buffer.length() - 1);
  }

  protected char previousCharacter() {
    if (bufferPosition < 0) return NULL_CHARACTER;
    return buffer.charAt(bufferPosition);
  }

  protected long nextColumn() {
    columnHistory.push(column);
    return 0;
  }

  protected long previousColumn() {
    return columnHistory.pop();
  }

  protected char nextCharacter(char c) {
    switch (escape(c)) {
      case CARRIAGE_RETURN: {
        this.column = nextColumn();
        break;
      }
      case LINE_FEED: {
        this.column = nextColumn();
        this.line++;
        break;
      }
      default:
        this.column++;
    }
    return c;
  }

  protected void recordCharacter(char c) {
    if (this.buffer.length() == this.buffer.capacity()) {
      this.buffer.ensureCapacity(this.buffer.length() + historySize);
    }
    this.buffer.append(nextCharacter(c));
    this.bufferPosition++;
    this.index++;
  }

  protected void stepBackward() {
    char c = previousCharacter();
    bufferPosition--;
    this.index--;
    this.eof = false;
    switch (escape(c)) {
      case CARRIAGE_RETURN:
        this.column = previousColumn();
        break;
      case LINE_FEED:
        this.column = previousColumn();
        this.line--;
        break;
      default:
        this.column--;
    }
  }

  protected char next() {
    this.index++;
    this.bufferPosition++;
    return nextCharacter(previousCharacter());
  }

  /**
   * An alias for previous character (disambiguation)
   * @return
   */
  public char current(){
    return previousCharacter();
  }

  @Override
  @Deprecated protected ScannerState clone() {
    try /*  throwing runtime exceptions with closure */ {
      return (ScannerState) super.clone();
    }
    catch (CloneNotSupportedException e) {throw new RuntimeException(e);}
  }

}
