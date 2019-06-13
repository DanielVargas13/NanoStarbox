package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.io.Streams;
import box.star.lang.Array;
import box.star.lang.MachineStorage;
import box.star.lang.RuntimeObjectMapping;
import box.star.text.Char;

import java.io.*;
import java.util.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static box.star.text.Char.*;

/**
 * <h2>Basic Text Scanner</h2>
 * <p>Provides the basic facilities to (optimistically) scan text formats, with
 * precision text format error-reporting and sub-processing capabilities.</p>
 *<br>
 * <p>At a minimum, the Basic Text Scanner provides a working, language-neutral
 * interface by which any text-stream or string-processor could be constructed.
 * </p>
 * <br>
 * <tt>Basic Text Scanner (c) 2019 Hypersoft-Systems: USA (Triston-Jerard: Taylor)</tt>
 * <p></p>
 */
public class Scanner implements Closeable, Iterable<Character>, RuntimeObjectMapping.WithConfigurationPort<Scanner> {

  private static Scanner BaseRuntimeResolver;

  public int getTabSize() {
    return state.tabSize;
  }

  protected enum RuntimeLanguage {
    OR
  }

  protected static void globalRuntimeObject(String label, Object constVal){
    BaseRuntimeResolver.setRuntimeLabel(label, constVal);
  }

  static {
    BaseRuntimeResolver = new Scanner(Scanner.class.getName(), "");
    globalRuntimeObject("or", RuntimeLanguage.OR);
  }

  private static final char[] SPACE_TAB_MAP =
      BaseRuntimeResolver.createRuntimeObject("space or horizontal tab", toMap(SPACE, HORIZONTAL_TAB));
  private static final char[] LINE_MAP =
      BaseRuntimeResolver.createRuntimeObject("line-feed", toMap('\n'));
  private static final char[] SPACE_MAP =
      BaseRuntimeResolver.createRuntimeObject("space", toMap( SPACE));
  private static final char[] TAB_MAP =
      BaseRuntimeResolver.createRuntimeObject("horizontal tab", toMap(HORIZONTAL_TAB));
  private static final char[] WORD_BREAK_MAP =
      BaseRuntimeResolver.createRuntimeObject("word boundary",
          new Assembler(MAP_ASCII_ALL_WHITE_SPACE).merge(NULL_CHARACTER).toMap());

  public final static String SCANNER_CODE_QUALITY_BUG = " (code optimization bug)";

  // runtime object mapping
  Dictionary runtimeObjectLabels =
      new Dictionary();

  private Scanner runtimeLabelResolver = BaseRuntimeResolver;

  @Override
  public Scanner deleteRuntimeLabel(Object constVal) {
    runtimeObjectLabels.remove(constVal);
    return this;
  }

  @Override
  public Scanner setRuntimeLabelResolver(Scanner source) {
    runtimeLabelResolver = source;
    return this;
  }

  @Override
  public Scanner loadRuntimeLabels(Map<Object, String> map) {
    runtimeObjectLabels.putAll(map);
    return this;
  }

  /**
   * Retrieve the best available representation of an object
   * @param constVal the runtime object to get the label for
   * @return <ol>
   *   <li>The value defined on this interface through {@link RuntimeObjectMapping#setRuntimeLabel(String, Object)}</li>
   *   <li>The value of the {@link #getRuntimeLabel(Object)} call on the scanner that has been set through {@link #setRuntimeLabelResolver(Scanner)}</li>
   *   <li>The object label defined by the object's {@link ObjectWithLabel known mapping interface}</li>
   *   <li>The conjunctive-or list conversion of the map if the object is a character array</li>
   *   <li>The conjunctive-or list conversion of the map if the object is an object array</li>
   *   <li>The translation offered by the {@link Char#translate(char) global character translator}</li>
   *   <li>The translation offered by the object's toString method.</li>
   *   <li>null, because the value is null</li>
   * </ol>
   */
  @Override
  public String getRuntimeLabel(Object constVal) {
    if (runtimeObjectLabels.containsKey(constVal))
      return runtimeObjectLabels.get(constVal);
    else if (runtimeLabelResolver != null)
      return runtimeLabelResolver.getRuntimeLabel(constVal);
    else if (constVal instanceof RuntimeObjectMapping.ObjectWithLabel)
      return ((ObjectWithLabel)constVal).getRuntimeLabel();
    else if (constVal instanceof char[])
      return translateCharacterMap(getRuntimeLabel(RuntimeLanguage.OR), (char[]) constVal);
    else if (constVal instanceof Object[])
      return translateObjectMap(getRuntimeLabel(RuntimeLanguage.OR), (Object[])constVal);
    else if (constVal instanceof Character)
      return translate((char) constVal);
    else if (constVal != null)
      return constVal.toString();
    else
      return null;
  }

  public String translateCharacterMap(String conjunction, char... map){
    if (map.length == 0) return "empty character map";
    if (map.length == 1) return getRuntimeLabel(map[0]);
    String[] out = new String[map.length - 1];
    int i; for (i = 0; i < map.length - 1; i++) out[i] = getRuntimeLabel(map[i]);
    return String.join(", ", out) + " "+conjunction+" "+getRuntimeLabel(map[map.length - 1]);
  }

  public String translateObjectMap(String conjunction, Object[] map){
    if (map.length == 0) return "empty object map";
    if (map.length == 1) return getRuntimeLabel(map[0]);
    String[] out = new String[map.length - 1];
    int i; for (i = 0; i < map.length - 1; i++) out[i] = getRuntimeLabel(map[i]);
    return String.join(", ", out) + " "+conjunction+" "+getRuntimeLabel(map[map.length - 1]);
  }

  @Override
  public <T> T createRuntimeObject(String label, T constVal) {
    runtimeObjectLabels.put(constVal, label);
    return constVal;
  }

  @Override
  public Scanner setRuntimeObjects(String label, Object... constVal) {
    for (Object val:constVal) setRuntimeLabel(label, val);
    return this;
  }

  /**
   * Set the best available representation of an object
   * @param label
   * @param constVal
   * @return the scanner
   */
  @Override
  public Scanner setRuntimeLabel(String label, Object constVal) {
    runtimeObjectLabels.put(constVal, label);
    return this;
  }

  /**
   * Reader for the input.
   */
  protected Reader reader;
  protected boolean closeable;
  protected State state;
  protected final MachineStorage userMap = new MachineStorage();

  /**
   * <p>Stores the given object in the scanner's machine storage and registers
   * the object as a known runtime object with the given label.</p>
   * @param label
   * @param key
   * @param value
   */
  public void set(String label, Enum key, Object value){
    setRuntimeLabel(label, value);
    userMap.put(key, value);
  }

  public boolean has(Enum key){
    return userMap.containsKey(key);
  }

  public void set(Enum key, Object value){
    userMap.put(key, value);
  }

  public <T> T get(Class<T> type, Enum key){
    return userMap.get(type, key);
  }

  public Class typeOf(Enum key){
    return userMap.getType(key);
  }

  public void delete(Enum key){
    userMap.remove(key);
  }

  public Scanner(@NotNull String path, @NotNull Reader reader) {
    this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
    this.state = new State(path);
  }

  public Scanner(@NotNull String path, @NotNull InputStream inputStream) {
    this(path, new InputStreamReader(inputStream));
    if (inputStream.equals(System.in)) return;
    closeable = true;
  }

  public Scanner(@NotNull String path, @NotNull String s) {
    this(path, new StringReader(s));
    this.closeable = true;
  }

  public Scanner(@NotNull File file) {
    this(file.getPath(), Streams.getUriStream(file.toURI()));
  }

  /**
   * Sometimes you need to parse some sub-text, so here is a method to set
   * the location data, after you create the scanner.
   *
   * @param line the line to use as the starting position
   * @param column the column to use as the starting position
   * @param index the index to use as the starting position
   * @return the new Scanner
   * @throws IllegalStateException if this is not a new {@link Scanner}
   */
  public Scanner At(long line, long column, long index) throws IllegalStateException {
    // since this is a profile-method, raise hell if state is
    // in post-initialization status.
    if (state.index != -1) {
      throw new IllegalStateException("Scanner has already been initialized");
    }
    state.line = line;
    state.column = column;
    state.index = index;
    return this;
  }

  /**
   * Scanner sub-section-scanning
   * @param bookmark
   * @param source
   */
  public Scanner(Bookmark bookmark, String source) {
    this(bookmark.path, source);
    state.line = bookmark.line;
    state.column = bookmark.column;
    state.index = bookmark.index;
    WithTabSizeOf(bookmark.tabSize);
  }

  public Scanner WithTabSizeOf(int tabSize){
    // since this is a profile-method, raise hell if state is
    // in post-initialization status.
    if (state.index != -1) {
      throw new IllegalStateException("Scanner has already been initialized");
    }
    state.tabSize = tabSize;
    return this;
  }

  /**
   * Determines the size of the current history buffer.
   *
   * @return the length of the history
   */
  public int getHistoryLength() {
    return state.getHistoryLength();
  }

  /**
   * Trims the size of the history buffer to the amount given.
   * <p>
   * if the amount is zero or less, the history is flushed.
   * if the amount is not reached, nothing is done.
   *
   * @param size the new target size of the history
   * @throws IllegalStateException if the current position is within the history.
   */
  public void trimHistoryLength(int size) throws IllegalStateException {
    state.trimHistoryLength(size);
  }

  /**
   * Call this after each successful main loop to clear the back-buffer.
   * <p>
   * {@link #haveNext()} loads the next position into history.
   */
  public void flushHistory() {
    state.clearHistory();
  }

  /**
   * <p>call this to close the reader.</p>
   * <br>
   * <p>If using a string source, it's okay to let this happen during finalize.</p>
   */
  @Override
  public void close() {
    try /*  ignoring exceptions with final closure */ {
      if (closeable) reader.close();
    }
    catch (IOException ignored) {}
  }

  /**
   * Closes the file if closeable when the object is garbage collected.
   */
  @Override
  protected void finalize() throws Throwable {
    close();
    flushHistory();
    super.finalize();
  }

  /**
   * Determine if the source string still contains characters that next()
   * can consume.
   *
   * @return true if not yet at the end of the source.
   */
  public boolean haveNext() {
    return ! endOfSource();
  }

  /**
   * Checks if the end of the input has been reached.
   *
   * @return true if at the end of the file and we didn't step back
   */
  public boolean endOfSource() {
    return state.eof && ! state.haveNext();
  }

  /**
   * Step backward one position.
   *
   * @throws IllegalStateException if unable to step backward
   */
  public void back() throws IllegalStateException {
    if (state.bufferPosition == -1) throw new IllegalStateException("cannot step back");
    state.stepBackward();
  }

  /**
   * <p>Step the scanner back by one character position, if the current position is not
   * the end of the scanner source</p>
   *
   * <p>This method provides touch-and-go delimiter bounds recovery, and lookahead
   * recovery. Typically called from within the domain of a parser or driver,
   * to allow the parent operation to successfully handle the end of the data stream.</p>
   */
  public void escape(){ if (! endOfSource()) back(); }

  public void back(int count) throws IllegalStateException { while (count-- > 0) back(); }

  /**
   * Get the next character.
   *
   * @return
   * @throws IllegalStateException if read fails
   */
  public char next() {
    if (state.haveNext()) return state.next();
    else if (state.eof) throw new IllegalStateException(
        Scanner.class.getSimpleName()+SCANNER_CODE_QUALITY_BUG
            +": virtual private int EOF = (-1) is final and"+
            " is not convertible to character value through this interface",
        new IOException("end of source\n   at "+getPath()
            +String.format(":%s", getLine())
        ));
    try {
        int i = this.reader.read();
        if (i == -1) { state.eof = true; return 0; }
        state.recordCharacter((char) i);
        return (char) i;
    } catch (IOException exception) { throw new RuntimeException(exception.getMessage(), exception.getCause()); }
  }

  /**
   * <p>Returns the scanner to the specified position. This position must be within
   * the buffer history. No validation is performed.</p>
   * @param to
   */
  public void walkBack(long to){
    if (getIndex() < to)
      throw new IllegalArgumentException("destination is in front of the current position, cannot walk back to the future");
    while (to != getIndex()) back();
  }

  /**
   * Scan and assemble characters while scan is in map.
   *
   * @param map
   * @return
   */
  public @NotNull String nextMap(@NotNull char... map) {
    char c;
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) do {
      c = this.next();
      if (mapContains(c, map)) sb.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (! endOfSource());
    return sb.toString();
  }

  public @NotNull String nextMap(@NotNull RangeList map){
    char c;
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) do {
      c = this.next();
      if (map.match(c)) sb.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (! endOfSource());
    return sb.toString();
  }

  public @NotNull char next(char character) {
    if (endOfSource()){
      throw new SyntaxError(this,
          "expected "+getRuntimeLabel(character)
              +" and located end of source");
    }
    char c = next();
    if (c != character) {
      if (endOfSource()){
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(character)
                +" and located end of source");
      }
      throw new SyntaxError(this,
          "expected "+getRuntimeLabel(character)
          +" and located " +getRuntimeLabel(c));
    }
    return c;
  }

  /**
   * Scan and assemble characters while scan is in map and scan-length < max.
   *
   * @param max
   * @param map
   * @return
   */
  public @NotNull String nextMap(int min, int max, @NotNull char... map) {
    char c;
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      c = this.next();
      if (mapContains(c, map)) buffer.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (buffer.length() != max);
    if (buffer.length() < min)
      throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(map)+" and have only "+buffer.length()+" characters");
    return buffer.toString();
  }

  /**
   * Scan and assemble characters while scan is in map and scan-length < max.
   *
   * @param max
   * @param map
   * @return
   */
  public @NotNull String nextMap(int min, int max, @NotNull RangeList map) {
    char c;
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      c = this.next();
      if (map.match(c)) buffer.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (buffer.length() != max);
    if (buffer.length() < min) {
      walkBack(start);
      throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(map)+" and have only "+buffer.length()+" characters");
    }
    return buffer.toString();
  }

  /**
   * <p>Scan and assemble characters while scan is not in map</p>
   * <br>
   * <p>Automatically eats the delimiter. The delimiter can be read through
   * {@link #previous()}.</p>
   * <br>
   * @param map the collection of delimiters to break scanning with
   * @return the delimited text; could be truncated
   */
  @NotNull
  public String nextField(@NotNull char... map) {
    char c;
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) do {
      c = this.next();
      if (mapContains(c, map)) break;
      if (endOfSource()){
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(map)
                +" and found end of source");
      }
      else sb.append(c);
    } while (! endOfSource());
    return sb.toString();
  }

  /**
   * <p>Scan and assemble characters while scan is not delimiter</p>
   * <br>
   * <p>Automatically eats the delimiter. The delimiter can be read through
   * {@link #previous()}.</p>
   * <br>
   * @param delimiter the delimiter to break scanning with
   * @return the delimited text; could be truncated
   */
  public @NotNull String nextField(char delimiter) {
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) do {
      char c = this.next();
      if (c == delimiter) break;
      if (endOfSource())
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(delimiter)
                +" and found end of source");
      sb.append(c);
    } while (true);
    return sb.toString();
  }

  /**
   * <p>Scan and assemble characters while scan is not delimiter</p>
   * <br>
   * <p>Automatically eats the delimiter. The delimiter can be read through
   * {@link #previous()}.</p>
   * <br>
   * @param max the maximum amount of characters to read
   * @param delimiter the delimiter to break scanning with
   * @return the delimited text; could be truncated
   */
  public @NotNull String nextField(int max, char delimiter) {
    StringBuilder sb = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      char c = this.next();
      if (c == delimiter) break;
      if (endOfSource())
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(delimiter)
                +" and found end of source");
      sb.append(c);
    } while (sb.length() != max);
    return sb.toString();
  }

  /**
   * <p>Scan and assemble characters while field driver signals continuation</p>
   *
   * @param fieldDriver the driver to use for breaking the field and expanding escapes
   * @return the delimited text; could be truncated
   */
  public @NotNull String nextField(@NotNull Scanner.FieldDriver fieldDriver) {
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) {
      char c;
      fieldDriver.setScanner(this);
      do {
        c = this.next();
        if (c == BACKSLASH && ! escapeMode()){
          c = this.next();
          if (endOfSource())
            throw new SyntaxError(fieldDriver,
                "escape detected at end of source while scanning for "
                    +getRuntimeLabel(fieldDriver));
          sb.append(fieldDriver.expand(this));
          continue; }
        else if (fieldDriver.breakField(sb.length(), c)) break;
        else if (endOfSource())
          throw new SyntaxError(fieldDriver,
              "expected "+getRuntimeLabel(fieldDriver)
                  +" and found end of source");
        else sb.append(c);
      } while (true);
    }
    return sb.toString();
  }

  /**
   * <p>Scan and assemble characters while scan is not in map</p>
   * <br>
   * <p>Automatically eats the delimiter. The delimiter can be read through
   * {@link #previous()}.</p>
   * <br>
   * @param delimiterMap the collection of delimiters to break scanning with
   * @return the delimited text; could be truncated
   */
  @NotNull
  public String nextField(Char.Map delimiterMap) {
    char c;
    StringBuilder sb = new StringBuilder();
    if (! endOfSource()) do {
      c = this.next();
      if (delimiterMap.contains(c)) break;
      if (endOfSource()){
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(delimiterMap)
                +" and found end of source");
      }
      else sb.append(c);
    } while (! endOfSource());
    return sb.toString();
  }

  /**
   * <p>Scan and assemble characters while scan is not in map, and length < max</p>
   *
   * @param max
   * @param map the collection of delimiters to break scanning with
   * @return the delimited text; could be truncated
   */
  @NotNull
  public String nextField(int max, @NotNull char... map) {
    char c;
    StringBuilder sb = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      c = this.next();
      if (mapContains(c, map)) {
        if (! endOfSource()) this.back();
        break;
      }
      if (endOfSource()) {
        throw new SyntaxError(this,
            "expected "+getRuntimeLabel(map)
                +" and found end of source");
      }
      sb.append(c);
    } while (sb.length() != max);
    return sb.toString();
  }

  /**
   * <p>Checks the required string to see if it's length member meets requirements</p>
   * <br>
   * <p>You should not eat a delimiter if using this as a field-pass-through.</p>
   * @param min the minimum amount of characters to accept from the source input
   * @param backStep if true: failure will rewind the scanner before throwing any FormatException
   * @param format the string format that will be used during the call to new SyntaxError(this, {@link String#format(String, Object...) String.format(format, required)});
   * @param source null or test value assembled from scanner
   * @return the source parameter given (operates as a pass-through filter)
   * @throws SyntaxError if the required string does not meet requirements
   */
  public String assertLengthFormat(int min, boolean backStep, @NotNull String format, @Nullable String source) throws SyntaxError {
    if (min <= 0) return source;
    else if (source == null || source.length() < min) {
      if (backStep && source != null) back(source.length());
      throw new SyntaxError(this, String.format(format, source));
    }
    return source;
  }

  /**
   * <p>Checks the required string to see if it's length member meets requirements</p>
   * <br>
   * <p>If this operation fails, the scanner will walk-back to the beginning
   * of this string, and flag the format exception from that point.</p>
   * <br>
   * <p>If you want to assert the length of delimited output, use {@link #assertDelimitedLengthFormat(int, String, String)}</p>
   * @param min the minimum amount of characters to accept from the source input
   * @param format the string format that will be used during the call to new SyntaxError(this, {@link String#format(String, Object...) String.format(format, required)});
   * @param source null or test value assembled from scanner
   * @return the source parameter given (operates as a pass-through filter)
   * @throws SyntaxError if the required string does not meet requirements
   */
  public String assertLengthFormat(int min, @NotNull String format, @Nullable String source) throws SyntaxError {
    if (min <= 0) return source;
    else if (source == null || source.length() < min) {
      if (source != null) back(source.length());
      throw new SyntaxError(this, String.format(format, source));
    }
    return source;
  }

  /**
   * <p>Checks the required string to see if it's length member meets requirements</p>
   * <br>
   * <p>If this operation fails, the scanner will walk-back to the beginning
   * of this string, and flag the format exception from that point.</p>
   * <br>
   * @param min the minimum amount of characters to accept from the source input
   * @param format the string format that will be used during the call to new SyntaxError(this, {@link String#format(String, Object...) String.format(format, required)});
   * @param source null or test value assembled from scanner
   * @return the source parameter given (operates as a pass-through filter)
   * @throws SyntaxError if the required string does not meet requirements
   */
  public String assertDelimitedLengthFormat(int min, @NotNull String format, @Nullable String source) throws SyntaxError {
    if (min <= 0) return source;
    else if (source == null || source.length() < min) {
      if (source != null) back(source.length()+1);
      throw new SyntaxError(this, String.format(format, source));
    }
    return source;
  }

  public String nextWhiteSpace(){return nextField(MAP_ASCII_ALL_WHITE_SPACE);}
  public String nextLineSpace(){ return nextMap(SPACE_TAB_MAP);}
  public String nextLine(){ return nextField(LINE_MAP);}
  public String nextSpace(){return nextMap(SPACE_MAP);}
  public String nextTab(){ return nextMap(TAB_MAP); }

  public String nextWordPreview(){
    if (endOfSource()) return "end of source";
    long start = getIndex();
    String word = nextWord();
    walkBack(start);
    return word;
  }

  public String nextWordPreview(int max){
    if (endOfSource()) return "end of source";
    long start = getIndex();
    String word = nextWord(max);
    walkBack(start);
    return word;
  }

 /**
  * <p>Runs the given source driver</p>
   * @param driver the source driver to use
   * @return the compiled string output of the driver
   */
  public String run(@NotNull SourceDriver driver) throws IllegalStateException, SyntaxError {
    char c;
    boolean autoBackStep = driver instanceof SourceDriver.WithAutoBackStep;
    StringBuilder sb = new StringBuilder();
    /* driver-loading */
    SourceDriver.WithSimpleControlPort simpleControlPort = null;
    SourceDriver.WithExpansionControlPort expansionControlPort = null;
    SourceDriver.WithBufferControlPort bufferControlPort = null;
    if (driver instanceof SourceDriver.WithExpansionControlPort)
      expansionControlPort = ((SourceDriver.WithExpansionControlPort)driver);
    if (driver instanceof SourceDriver.WithBufferControlPort)
      bufferControlPort = ((SourceDriver.WithBufferControlPort)driver);
    else {
      if (driver instanceof SourceDriver.WithSimpleControlPort)
        simpleControlPort = ((SourceDriver.WithSimpleControlPort)driver);
      else
        throw new IllegalStateException
            ("SourceDriver does not host any valid control ports");
    }
    /* end-driver-loading */
    boolean ending, escaping;
    do {

      if (ending = endOfSource()) c = 0;
      else {
        c = this.next();
        ending = endOfSource();
      }

      escaping = escapeMode();

      if (expansionControlPort != null && c == BACKSLASH && ! escaping) continue;

      if (ending) {
        if (expansionControlPort == null && escaping)
          throw new SyntaxError(this,"expected character escape sequence, found end of stream");
      }

      if (expansionControlPort != null && escaping) {
        String swap = expansionControlPort.expand(this);
        sb.append(swap);
        continue;
      }

      if (bufferControlPort != null){
        if (!bufferControlPort.collect(this, sb, c)) break;
        if (ending) break;
        continue;
      } else if (! simpleControlPort.collect(this, c)) { break; }

      if (ending) break;
     sb.append(c);

    } while (! ending);
    if (autoBackStep) escape();
    return sb.toString();
  }

  /**
   * Get the next n characters.
   *
   * @param n The number of characters to take.
   * @return A string of n characters, could be zero length or truncated.
   */
  public @NotNull String nextLength(int n) {
    if (endOfSource() || n == 0) return Tools.EMPTY_STRING;
    StringBuilder buffer = new StringBuilder();
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
    } while(buffer.length() != n);
    return buffer.toString();
  }

  public @NotNull String nextWord(){
    StringBuilder word = new StringBuilder();
    if (! endOfSource() ) do {
      char c = next();
      if (!mapContains(c, WORD_BREAK_MAP)) word.append(c);
      else { escape(); break; }
    } while (! endOfSource());
    return word.toString();
  }

  public @NotNull String nextWord(int max){
    StringBuilder word = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource() ) do {
      char c = next();
      if (!mapContains(c, WORD_BREAK_MAP)) word.append(c);
      else { escape(); break; }
    } while (word.length() != max);
    return word.toString();
  }

  /**
   * <p>Gets the next word and tests it against match</p>
   * <br>
   * <p>The test is case sensitive.</p>
   * <br>
   * @param match
   * @return if equals: return true; else: restore scanner, return false
   * @throws SyntaxError if the word is not found
   */
  public void nextWord(String match) throws SyntaxError {
    long start = getIndex();
    String word = nextLength(match.length());
    if (word.equals(match)) return;
    walkBack(start);
    throw new SyntaxError(this, "expected "+match+" and found "+nextWordPreview());
  }

  public @NotNull String nextPattern(int min, @NotNull Pattern pattern){
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (min > 0) {
      buffer.append(endOfSource()?"":nextLength(min));
      if (buffer.length() < min) {
        walkBack(start);
        throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(pattern)+" and have only "+buffer.length()+" characters");
      }
    }
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.toString();
    } while (true);
    walkBack(start);
    String preview;
    if (min == 0) preview = nextWordPreview();
    else preview = nextWordPreview(min);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(pattern)+" and found "+preview);
  }

  public @NotNull String nextPattern(int min, int max, @NotNull Pattern pattern){
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (min > 0) {
      buffer.append(endOfSource()?"":nextLength(min));
      if (buffer.length() < min) {
        walkBack(start);
        throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(pattern)+" and have only "+buffer.length()+" characters");
      }
    }
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.toString();
    } while (buffer.length() != max);
    walkBack(start);
    String preview;
    if (min == 0) preview = nextWordPreview();
    else preview = nextWordPreview(min);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(pattern)+" and found "+preview);
  }

  public @NotNull String nextPattern(int min, int max, @NotNull PatternList patterns) throws SyntaxError {
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (min > 0) {
      buffer.append(endOfSource()?"":nextLength(min));
      if (buffer.length() < min) {
        walkBack(start);
        throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(patterns)+" and have only "+buffer.length()+" characters");
      }
    }
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (patterns.matches(buffer.toString())) return buffer.toString();
    } while (buffer.length() != max);
    walkBack(start);
    String preview;
    if (min == 0) preview = nextWordPreview();
    else preview = nextWordPreview(min);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(patterns)+" and found "+preview);
  }

  /**
   * <p>Tries a list of pattern matches within the given range</p>
   * <br>
   * <p>method: accumulating 1 character at a time (starting with min), and trying
   * each pattern in the pattern list until a match succeeds or branch options
   * are exhausted.</p>
   *
   * @param optional if true: no errors will be thrown if matching fails
   * @param min the minimum amount of characters to accept<br><i>this item is not affected by the optional parameter</i>
   * @param max the maximum amount of characters to scan<br><i>this item is not affected by the optional parameter</i>
   * @param pattern the list of patterns to match
   * @return the match or null
   * @throws SyntaxError if all branches are exhausted and optional = false OR if min is not available
   */
  public Matcher nextMatch(boolean optional, int min, int max, PatternList pattern) throws SyntaxError {
    long start = getIndex();
    Matcher matcher;
    if (max == 0) --max;
    StringBuilder buffer = new StringBuilder();
    if (min > 0) {
      buffer.append(endOfSource()?"":nextLength(min));
      if (buffer.length() < min) {
        walkBack(start);
        throw new SyntaxError(this, "expected a minimum of "+min+" characters while searching for "+getRuntimeLabel(pattern)+" and have only "+buffer.length()+" characters");
      }
    }
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      matcher = pattern.match(buffer.toString());
      if (matcher != null) return matcher;
    } while (buffer.length() != max);
    walkBack(start);
    if (! optional ){
      String preview;
      if (min == 0) preview = nextWordPreview();
      else preview = nextWordPreview(min);
      throw new SyntaxError(this, "expected "+getRuntimeLabel(pattern)+" and found "+preview);
    }
    return null;
  }

  /**
   * <p>Back step on match</p>
   * <br>
   *   <p>method: {@link #back(int) back(<code>matcher.end() - matcher.start()</code>)}</p>
   * @param matcher the match to back step by
   */
  public void back(Matcher matcher){
    back(matcher.end() - matcher.start());
  }

  /**
   * Look behind
   * @param length
   * @return the current string
   * @throws ArrayIndexOutOfBoundsException if current position - length < 0
   * @see #getHistoryLength()
   */
  public String currentString(int length) throws ArrayIndexOutOfBoundsException {
    int start = state.bufferPosition - length;
    if (start < 0)
      throw new ArrayIndexOutOfBoundsException("cannot read "+start);
    return state.buffer.substring(start, start + length);
  }

  /**
   * <p>Gets the next word and tests it against match</p>
   * @param caseSensitive if true: the test is case sensitive
   * @param match the string to match
   * @return true if the next word matches the input
   */
  public boolean nextWord(boolean caseSensitive, String match) {
    long start = getIndex();
    String word = nextWord(match.length());
    walkBack(start);
    if ((caseSensitive?word.equals(match):word.equalsIgnoreCase(match))) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * <p>Gets the next word and tests it against a list of inputs</p>
   * <br>
   * @param caseSensitive if true: the tests are case sensitive
   * @param wordList the set of strings to match
   * @return matched word
   * @throws SyntaxError if one of the words is not found
   */
  public @NotNull String nextWord(boolean caseSensitive, WordList wordList) throws SyntaxError {
    for (String word: wordList) {
      if (nextWord(caseSensitive, word)) return nextWord(word.length());
    }
    throw new SyntaxError(this, "expected "+getRuntimeLabel(wordList)+" and found `"+nextWordPreview()+"'");
  }

  public String nextDigit(int min, int max) {
    return nextMap(min, max, MAP_ASCII_NUMBERS);
  }

  public String nextAlpha(int min, int max){
    return nextMap(min, max, MAP_ASCII_LETTERS);
  }

  public String nextHex(int min, int max){
    return nextMap(min, max, MAP_ASCII_HEX);
  }

  public String nextOctal(int min, int max){
    return nextMap(min, max, MAP_ASCII_OCTAL);
  }

  /**
   * @return true if the current state is in escape mode for the current character.
   */
  public boolean escapeMode() { return state.escaped; }

  public Bookmark createBookmark(){ return new Bookmark(this); }

  public Bookmark nextBookmark(){
    if (endOfSource()) return createBookmark();
    next(); Bookmark x = createBookmark(); back();
    return x;
  }

  public String toString() { return String.valueOf(createBookmark()); }

  /**
   * @return the current scanner character or {@link Char#NULL_CHARACTER}
   */
  public char current(){
    return state.current();
  }

  /**
   * @return the previous scanner character or {@link Char#NULL_CHARACTER}
   */
  public char previous(){ return state.previous(); }

  /**
   * @return the scanner's connotation of where this stream-data originates
   */
  public String getPath() { return state.path; }

  /**
   * @return the position within the text-stream
   */
  public long getIndex() { return state.index; }

  /**
   * @return the line number within the text-stream
   */
  public long getLine() { return state.line; }

  /**
   * @return the column number at the current line within the text-stream
   */
  public long getColumn() { return state.column; }

  @Override
  public SerialDriver iterator() { return new SerialDriver(this); }

  public static class SerialDriver extends CancellableOperation implements Iterator<Character> {
    protected SerialDriver(@NotNull Scanner scanner, @NotNull String label){
      super(scanner, label);
    }
    protected SerialDriver(@NotNull Scanner scanner) {
      super(scanner, SerialDriver.class.getName());
    }
    @Override public boolean hasNext() {
      return scanner.haveNext();
    }
    @Override public Character next() {
      return scanner.next();
    }
  }

  public static interface SourceDriver {
    interface WithAutoBackStep {}
    interface WithSimpleControlPort extends SourceDriver {
      boolean collect(@NotNull Scanner scanner, char character);
    }
    interface WithExpansionControlPort extends SourceDriver, CharacterExpander {}
    interface WithBufferControlPort extends SourceDriver {
      boolean collect(@NotNull Scanner scanner, @NotNull StringBuilder buffer, char character);
    }
    interface WithMasterControlPorts extends WithExpansionControlPort, WithBufferControlPort {}
  }

  private static class State implements Cloneable, Serializable {

    protected static final int historySize = 256;
    protected int tabSize;

    protected String path;
    protected long column, index, line;

    protected Stack<Long> columnHistory;
    protected StringBuilder buffer;
    protected int bufferPosition;
    @Deprecated protected boolean locked, escapeLines, escapeUnderscoreLine;
    protected boolean eof, slashing, escaped;

    public State(@NotNull String path) {
      State state = this;
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
      buffer = new StringBuilder(historySize);
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

      if (escaped && escapeLines && lineMode) c = NULL_CHARACTER;

      return c;

    }

    public boolean haveNext() {
      return bufferPosition != (buffer.length() - 1);
    }

    protected char previousCharacter() {
      if (bufferPosition < NULL_CHARACTER) return NULL_CHARACTER;
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
        case HORIZONTAL_TAB: {
          this.column += tabSize;
          break;
        }
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
        case HORIZONTAL_TAB:{
          this.column -= tabSize;
          break;
        }
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
      if (bufferPosition < NULL_CHARACTER)
        throw new IllegalStateException("read has not been called on the scanner");
      return previousCharacter();
    }

    public char previous(){
      if (bufferPosition < START_OF_HEADING) return NULL_CHARACTER;
      return buffer.charAt(bufferPosition - START_OF_HEADING);
    }

    @Override @Deprecated protected State clone() {
      try { return (State) super.clone(); }
      catch (CloneNotSupportedException e) {throw new RuntimeException(e);}
    }

  }

  public static class EscapeDriver extends CancellableOperation implements CharacterExpander {
    public EscapeDriver(){super();}
    protected EscapeDriver(String label) { super(label); }
  }

  public static class FieldDriver extends EscapeDriver {
    public FieldDriver(){}
    public FieldDriver(String label){ super(label); }
    public boolean breakField(int size, char current){ return scanner.endOfSource(); }
  }

  public static class CancellableOperation implements ObjectWithLabel {
    protected Scanner scanner;
    protected long start;
    protected String label;
    protected boolean cancelled;
    protected CancellableOperation() { this.label = this.getClass().getName(); }
    protected CancellableOperation(Scanner scanner){ this(); setScanner(scanner); }
    protected CancellableOperation(Scanner scanner, String label){ this(label); setScanner(scanner); }
    protected CancellableOperation(String label){ this.label = label; }
    protected void setScanner(Scanner scanner){
      this.scanner = scanner;
      this.start = scanner.getIndex();
      this.cancelled = false;
    }
    @NotNull protected Bookmark cancel() {
      Bookmark bm = scanner.createBookmark();
      if (! scanner.endOfSource())
        if (scanner.getHistoryLength() > 0)
          scanner.walkBack(Math.max(-1, start));
      cancelled = true;
      return bm;
    }
    @Override final public String getRuntimeLabel() { return label; }
    final public boolean isCancelled() { return cancelled; }
  }

  static public class SyntaxError extends box.star.lang.SyntaxError {

    SyntaxError(@NotNull String sourceTag, @NotNull String message) {
      super("\n\n"+message+":\n\n   "+sourceTag+"\n");
    }

    SyntaxError(@NotNull String sourceTag, @NotNull String message, @NotNull Throwable cause) {
      super("\n\n"+message+":\n\n   "+sourceTag+"\n", cause);
    }

    SyntaxError(Bookmark location, String message) {
      this(location.toString(), message);
    }
    SyntaxError(Bookmark location, String message, Throwable cause) {
      this(location.toString(), message, cause);
    }

    public SyntaxError(@NotNull CancellableOperation action, @NotNull String message){
      this(action.cancel(), message);
      this.host = action;
    }

    public SyntaxError(@NotNull CancellableOperation action, @NotNull String message, Throwable cause){
      this(action.cancel(), message, cause);
      this.host = action;
    }

    public SyntaxError(@NotNull Scanner source, @NotNull String message){
      this(source.createBookmark(), message);
      this.host = source;
    }

    public SyntaxError(@NotNull Scanner source, @NotNull String message, Throwable cause){
      this(source.createBookmark(), message, cause);
      this.host = source;
    }

    public SyntaxError(Object host, @NotNull Scanner source, @NotNull String message){
      this(source.createBookmark(), message);
      this.host = host;
    }

    public SyntaxError(Object host, @NotNull Scanner source, @NotNull String message, Throwable cause){
      this(source.createBookmark(), message, cause);
      this.host = host;
    }

  }

  /**
   * <h2>ScannerMethod</h2>
   *
   * <p>This class extends the operational capabilities of the basic text
   * {@link Scanner}.</p>
   * <br>
   * <p>Use this class to perform inline stream extrapolations.</p>
   * <br>
   *
   * <h3>Working Theory</h3>
   *
   * <p>A ScannerMethod is instantiated by an implementation to obtain structured
   * data. The method may be started with optional parameters, and may be cloned
   * for concurrent operations or sub-calls.</p>
   * <br>
   * <p>For example, you want to collect some meta-document-language attributes into a hash-map.
   * What you will do is send a hash map to the method through the text-scanner's
   * {@link Scanner#run(ScannerMethod, Object...)} method, and when the run method
   * calls your {@link #compile(Scanner)} method, you simply parse the data you
   * collected in your buffer, and store it in your attribute-map. To obtain your
   * method parameters you must record them during the Scanner's call to your
   * {@link #start(Scanner, Object[])} method.</p>
   * <br>
   * <p>A method may call other methods, and may also call upon the methods
   * of the scanner during any execution phase of its lifecycle.</p>
   * <br>
   *<p>If and When MacroShell gets rewritten, this interface and its constiutent
   * parts will be dissolved.</p>
   * <br>
   * <h3>lifecycle</h3>
   * <ul>
   * {@link #reset()}, {@link #start(Scanner, Object[])}, {@link #collect(Scanner, char)}, {@link #terminate(Scanner, char)} and {@link #scan(Scanner)}</li>
   * </ul>
   */
  @Deprecated public static class ScannerMethod implements Cloneable {

    protected String claim;
    protected StringBuilder buffer;
    protected int bufferOffset;

    protected ScannerMethod() {this("TextScannerMethod");}

    protected ScannerMethod(@NotNull String claim) {this.claim = claim;}

    /**
     * Create the character buffer
     *
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     */
    protected void reset() {
      buffer = new StringBuilder((int) SPACE);
      bufferOffset = -1;
    }

    /**
     * Called by the scanner to signal that a new method call is beginning.
     *
     * @param scanner    the host scanner
     * @param parameters the parameters given by the caller.
     */
    protected void start(@NotNull Scanner scanner, Object[] parameters) {}

    /**
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     *
     * @return String representation
     */
    @NotNull
    public String toString() { return claim; }

    /**
     * Places the given character on the character buffer at the current position,
     * overwriting the current position.
     * <p>
     * This feature enables incorporation of escape expansions into the current
     * buffer.
     *
     * @param forLastBufferCharacter
     */
    protected void swap(@Nullable char forLastBufferCharacter) {
      if (bufferOffset > -1) buffer.setLength(bufferOffset--);
      buffer.append(forLastBufferCharacter);
      bufferOffset++;
    }

    /**
     * Places the given string on the character buffer at the current position,
     * overwriting the current position.
     * <p>
     * If the string is empty or null, the operation is silently aborted.
     * <p>
     * This feature enables incorporation of escape expansions into the current
     * buffer.
     *
     * @param forLastBufferCharacter
     */
    protected void swap(@Nullable String forLastBufferCharacter) {
      if (bufferOffset > -1) buffer.setLength(bufferOffset--);
      if (forLastBufferCharacter == null || forLastBufferCharacter.equals(Tools.EMPTY_STRING)) return;
      buffer.append(forLastBufferCharacter);
      bufferOffset += forLastBufferCharacter.length();
    }

    /**
     * Add a character to the method buffer.
     *
     * @param scanner
     * @param character
     */
    protected void collect(@NotNull Scanner scanner, char character) {
      buffer.append(character);
      bufferOffset++;
    }

    /**
     * Returns true if the character is zero.
     *
     * @param scanner
     * @param character
     * @return
     */
    protected final boolean zeroTerminator(@NotNull Scanner scanner, char character) {
      //pop();
      //backStep(scanner);
      return character == 0;
    }

    /**
     * Return true to break processing at this character position.
     * <p>
     * The default method handles the zero terminator.
     *
     * @param scanner
     * @param character
     * @return false to continue processing.
     */
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      return zeroTerminator(scanner, character);
    }

    /**
     * Return the compiled buffer contents.
     * <p>
     * This method is called after the scanner completes a method call.
     *
     * @param scanner
     * @return the buffer.
     */
    @NotNull
    protected String compile(@NotNull Scanner scanner) {
      return buffer.toString();
    }

    /**
     * <p>Signals whether or not the process should continue reading input.</p>
     *
     * <p>The default method returns true.</p>
     *
     * @param scanner
     * @return true if the TextScanner should read more input.
     */
    protected boolean scan(@NotNull Scanner scanner) { return true; }

    /**
     * Step back the scanner and the buffer by 1 character.
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     *
     * @param scanner
     */
    protected void backStep(@NotNull Scanner scanner) {
      scanner.back();
      buffer.setLength(bufferOffset--);
    }

    /**
     * Step back the scanner and the buffer by 1 character.
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     *
     * @param scanner
     */
    protected void backStep(@NotNull Scanner scanner, long to) {
      while (scanner.getIndex() != to) {
        scanner.back();
        bufferOffset--;
      }
      buffer.setLength(bufferOffset+1);
    }

    /**
     * Examine the character on the top of the buffer.
     * <p>
     * Works like {@link #pop()}, but doesn't modify the buffer.
     *
     * @return
     */
    protected char current() {
      return buffer.charAt(bufferOffset);
    }

    /**
     * Examine characters on the top of the buffer.
     * <p>
     * Works like {@link #pop(int)}, but doesn't modify the buffer.
     *
     * @param count
     * @return
     */
    protected char[] peek(int count) {
      int offset = Math.max(0, buffer.length() - count);
      return buffer.substring(offset).toCharArray();
    }

    /**
     * Cuts the top character from the top of the buffer.
     *
     * @return the top character on the buffer
     */
    protected char pop() {
      int newLength = buffer.length() - 1;
      if (bufferOffset != newLength) throw new IllegalStateException("buffer offset is not at the top of the stack");
      bufferOffset--;
      char c = buffer.charAt(newLength);
      buffer.setLength(newLength);
      return c;
    }

    /**
     * Cut characters from the top of the buffer.
     *
     * @param count the amount of characters to cut
     * @return the characters selected
     */
    protected char[] pop(int count) {
      int offset = Math.max(0, buffer.length() - count);
      char[] c = buffer.substring(offset).toCharArray();
      buffer.setLength(Math.max(0, offset));
      bufferOffset = offset - 1;
      return c;
    }

    /**
     * <h2>Clone</h2>
     * <p>Creates a re-entrant-safe-single-state-method, from a given method.</p>
     * <br>
     * <p>The method implementation must honor the reset contract, by configuring
     * itself as a new instance.</p>
     *
     * <p>A default method should not store any runtime values. Runtime values
     * should be applied during {@link #reset()} and
     * {@link #start(Scanner, Object[])}.</p>
     *
     * @return the cloned method instance
     */
    @NotNull
    @Override
    protected ScannerMethod clone() {
      try {
        ScannerMethod clone = (ScannerMethod) super.clone();
        clone.reset();
        return clone;
      }
      catch (CloneNotSupportedException failure) {
        throw new RuntimeException("unable to create method object", failure);
      }
    }

  }
  /**
   * <p>Starts a {@link ScannerMethod}</p>
   * <br>
   * <p>Creates a copy of the method, and calls its
   * {@link ScannerMethod#start(Scanner, Object[])} method with the given
   * parameters.</p>
   *
   * @param method the method to use
   * @param parameters the parameters to forward to the method
   * @return hopefully, the result of the scanner method's {@link ScannerMethod#compile(Scanner)} routine, possibly an Exception or SyntaxError
   */
  @NotNull
  @Deprecated final public String run(ScannerMethod method, Object... parameters) {
    method = method.clone();
    method.start(this, parameters);
    do {
      char c = next();
      method.collect(this, c);
      if (method.terminate(this, c)) break;
    } while (method.scan(this) && ! endOfSource());
    return method.compile(this);
  }

  /**
   * <p>Calls upon a scanner method, as a branch from within a scanner method.</p>
   *
   * @param method the method to use
   * @param parameters the parameters for the method
   * @return hopefully, the result of the scanner method's {@link ScannerMethod#compile(Scanner)} routine, possibly an Exception or SyntaxError
   */
  @NotNull
  @Deprecated final public String branch(ScannerMethod method, Object... parameters) {
    method = method.clone();
    method.start(this, parameters);
    method.collect(this, current());
    if (! method.terminate(this, current()) && method.scan(this))
      do {
        char c = next();
        method.collect(this, c);
        if (method.terminate(this, c)) break;
      } while (method.scan(this));
    return method.compile(this);
  }

  public static class WordList extends Array<String> {
    private static final long serialVersionUID = 7943841258072204166L;
    /**
     * <p>A word-list short-circuit is a condition, where a word list fails to correctly
     * match an item because a shorter item matches the longer item first. This method
     * sorts the array from longest to shortest, to ensure that a short-circuit
     * is not possible.</p>
     * <br>
     * @param words
     */
    static private void preventWordListShortCircuit(String[] words){
      boolean longestFirst = true;
      Arrays.sort(words, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          return (longestFirst)?
              Integer.compare(o2.length(), o1.length()):
              Integer.compare(o1.length(), o2.length());
        }
      });
    }
    final int minLength, maxLength;
    public WordList(String label, String... words){
      super(label, words);
      preventWordListShortCircuit(data);
      maxLength = data[0].length();
      minLength = data[Math.max(0, data.length - 1)].length();
    }
    public boolean containsIgnoreCase(String string){
      for(String word:data) if (word.equalsIgnoreCase(string))return true;
      return false;
    }
    public int getMaxLength() {
      return maxLength;
    }
    public int getMinLength() {
      return minLength;
    }

  }

  public static class RangeList extends Array<RangeMap> {
    private static final long serialVersionUID = 9017972538783689725L;
    public RangeList(String label, RangeMap... ranges){
      super(label, ranges);
    }
    public boolean match(char c){
      for (RangeMap range:data) if (range.match(c)) return true;
      return false;
    }
  }

  public static class PatternList extends Array<Pattern> {
    private static final long serialVersionUID = -8772340584149844412L;
    public PatternList(String label, Pattern... patterns){
      super(label, patterns);
    }

    public boolean matches(String input){
      for (Pattern pattern:data) if (pattern.matcher(input).matches())return true;
      return false;
    }
    public Matcher match(String input){
      Matcher matcher;
      for (Pattern pattern:data) {
        matcher = pattern.matcher(input);
        if (matcher.matches())return matcher;
      }
      return null;
    }
  }
}

