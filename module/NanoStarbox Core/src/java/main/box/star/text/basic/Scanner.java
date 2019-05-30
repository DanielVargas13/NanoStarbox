package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.io.Streams;
import box.star.state.MachineStorage;
import box.star.text.Char;
import box.star.state.RuntimeObjectMapping;
import box.star.text.list.PatternList;
import box.star.text.list.WordList;

import java.io.*;
import java.util.*;
import java.util.Map;
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

  private static Scanner BaseRuntimeResolver = new Scanner(null, "");

  public enum RuntimeLanguage {
    OR
  }

  protected static void globalRuntimeObject(String label, Object constVal){
    BaseRuntimeResolver.setRuntimeLabel(label, constVal);
  }

  private static final char[] SPACE_TAB_MAP =
      BaseRuntimeResolver.createRuntimeObject("space or horizontal tab", Char.toMap(SPACE, HORIZONTAL_TAB));
  private static final char[] LINE_MAP =
      BaseRuntimeResolver.createRuntimeObject("line-feed", Char.toMap('\n'));
  private static final char[] SPACE_MAP =
      BaseRuntimeResolver.createRuntimeObject("space", Char.toMap( SPACE));
  private static final char[] TAB_MAP =
      BaseRuntimeResolver.createRuntimeObject("horizontal tab", Char.toMap(HORIZONTAL_TAB));
  private static final char[] WORD_BREAK_MAP =
      BaseRuntimeResolver.createRuntimeObject("word boundary",
          new Char.Assembler(MAP_ASCII_ALL_WHITE_SPACE).merge(NULL_CHARACTER).toMap());

  public final static String SCANNER_CODE_QUALITY_BUG = " (code optimization bug)";

  // runtime object mapping
  RuntimeObjectMapping.Dictionary runtimeObjectLabels =
      new RuntimeObjectMapping.Dictionary();

  private Scanner runtimeLabelResolver = BaseRuntimeResolver;

  static {
    globalRuntimeObject("or", RuntimeLanguage.OR);
  }

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
   *   <li>The object label defined by the object's {@link box.star.state.RuntimeObjectMapping.ObjectWithLabel known mapping interface}</li>
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
      return ((RuntimeObjectMapping.ObjectWithLabel)constVal).getRuntimeLabel();
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
    if (map.length == 1) return getRuntimeLabel(map[0]);
    String[] out = new String[map.length];
    int i; for (i = 0; i < map.length - 1; i++) out[i] = getRuntimeLabel(map[i]);
    out[i++] = conjunction+" "+getRuntimeLabel(map[map.length - 1]);
    return String.join(", ", out);
  }

  public String translateObjectMap(String conjunction, Object[] map){
    if (map.length == 1) return getRuntimeLabel(map[0]);
    String[] out = new String[map.length];
    int i; for (i = 0; i < map.length - 1; i++) out[i] = getRuntimeLabel(map[i]);
    out[i++] = conjunction+" "+getRuntimeLabel(map[map.length - 1]);
    return String.join(", ", out);
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
   * @throws LegacyScanner.Exception thrown if there is an error stepping forward
   *                   or backward while checking for more data.
   */
  public boolean haveNext() throws LegacyScanner.Exception {
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
      if (Char.mapContains(c, map)) sb.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (! endOfSource());
    return sb.toString();
  }

  public @NotNull char next(char character) {
    char c = next();
    if (c != character) {
      back();
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
  public @NotNull String nextMap(int max, @NotNull char... map) {
    char c;
    StringBuilder sb = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      c = this.next();
      if (Char.mapContains(c, map)) sb.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (sb.length() != max);

    return sb.toString();
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
      if (Char.mapContains(c, map)) break;
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
      if (Char.mapContains(c, map)) {
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

  protected String nextWordPreview(){
    if (endOfSource()) return "end of source";
    long start = getIndex();
    String word = nextWord();
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
        //return sb.toString();
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
    if (autoBackStep && ! endOfSource()) back();
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
    char[] chars = new char[n];
    int pos = 0;
    while (pos < n) {
      chars[pos++] = this.next();
      if (this.endOfSource()) break;
    }
    return new String(chars);
  }

  public @NotNull String nextWord(){
    StringBuilder word = new StringBuilder();
    if (! endOfSource() ) do {
      char c = next();
      if (!mapContains(c, WORD_BREAK_MAP)) word.append(c);
      else {
        if (! endOfSource()) back();
        break;
      }
    } while (! endOfSource());
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
    String word = nextWord();
    if (word.equals(match)) return;
    walkBack(start);
    throw new SyntaxError(this, "expected "+match+" and found "+nextWordPreview());
  }

  public @NotNull String nextPattern(@NotNull Pattern pattern){
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.toString();
    } while (true);
    walkBack(start);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(pattern));
  }

  public @NotNull String nextPattern(int max, @NotNull Pattern pattern){
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.toString();
    } while (buffer.length() != max);
    walkBack(start);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(pattern));
  }

  public @NotNull String nextPattern(int max, @NotNull PatternList patterns) throws SyntaxError {
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (max == 0) --max;
    if (! endOfSource()) do {
      char c = next();
      if (endOfSource()) break;
      buffer.append(c);
      if (patterns.matches(buffer.toString())) return buffer.toString();
    } while (buffer.length() != max);
    walkBack(start);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(patterns));
  }

  public int nextPatternLength(Pattern pattern) {
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (! endOfSource()) do {
      char c = next(); if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.length();
    } while (! endOfSource());
    walkBack(start);
    return 0;
  }

  public int nextPatternLength(int max, Pattern pattern){
    long start = getIndex();
    StringBuilder buffer = new StringBuilder();
    if (! endOfSource()) do {
      char c = next(); if (endOfSource()) break;
      buffer.append(c);
      if (pattern.matcher(buffer.toString()).matches()) return buffer.length();
    } while (! endOfSource() && buffer.length() < max);
    walkBack(start);
    return 0;
  }

  /**
   * <p>Gets the next word and tests it against match</p>
   * @param caseSensitive if true: the test is case sensitive
   * @param match the string to match
   * @return if equals: return true; else: restore scanner state and return false
   * @throws SyntaxError if the word is not found
   */
  public void nextWord(boolean caseSensitive, String match) throws SyntaxError {
    long start = getIndex();
    String word = nextWord();
    if ((caseSensitive?word.equals(match):word.equalsIgnoreCase(match))) return;
    walkBack(start);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(match)+" and found `"+nextWordPreview()+"'");
  }

  /**
   * <p>Gets the next word and tests it against a list of inputs</p>
   * <br>
   * <p>The list will be sorted from longest to shortest, which prevents
   * short-circuiting.</p>
   * @param caseSensitive if true: the tests are case sensitive
   * @param wordList the set of strings to match
   * @return matched word
   * @throws SyntaxError if one of the words is not found
   */
  public @NotNull String nextWord(boolean caseSensitive, WordList wordList) throws SyntaxError {
    long start = getIndex();
    String word = nextWord();
    if ((caseSensitive?wordList.contains(word):wordList.containsIgnoreCase(word))) return word;
    walkBack(start);
    throw new SyntaxError(this, "expected "+getRuntimeLabel(wordList)+" and found `"+nextWordPreview()+"'");
  }

  public String nextDigit(int min, int max) {
    String nextNumeric = nextMap(max, MAP_ASCII_NUMBERS);
    return assertLengthFormat(min, "expected a minimum of "+min+" digits, have "+nextNumeric.length(), nextNumeric);
  }

  public String nextAlpha(int min, int max){
    String nextNumeric = nextMap(max, MAP_ASCII_LETTERS);
    return assertLengthFormat(min, "expected a minimum of "+min+" alpha characters, have "+nextNumeric.length(), nextNumeric);
  }

  public String nextHex(int min, int max){
    String nextNumeric = nextMap(max, MAP_ASCII_HEX);
    return assertLengthFormat(min, "expected a minimum of "+min+" hex characters, have "+nextNumeric.length(), nextNumeric);
  }

  public String nextOctal(int min, int max){
    String nextNumeric = nextMap(max, MAP_ASCII_OCTAL);
    return assertLengthFormat(min, "expected a minimum of "+min+" octal characters, have "+nextNumeric.length(), nextNumeric);
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
  public char current(){ return state.current(); }

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

  public static class SerialDriver extends CancellableOperation implements java.util.Iterator<Character> {
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
    interface WithMasterControlPorts extends WithExpansionControlPort, SourceDriver.WithBufferControlPort {}
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
      return previousCharacter();
    }

    public char previous(){
      if (bufferPosition < START_OF_HEADING) return NULL_CHARACTER;
      return buffer.charAt(bufferPosition - START_OF_HEADING);
    }

    @Override
    @Deprecated protected State clone() {
      try /*  throwing runtime exceptions with closure */ {
        return (State) super.clone();
      }
      catch (CloneNotSupportedException e) {throw new RuntimeException(e);}
    }

  }

  public static class EscapeDriver extends CancellableOperation implements CharacterExpander {
    protected EscapeDriver(){super();}
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
    protected CancellableOperation(Scanner scanner){
      this();
      setScanner(scanner);
    }
    protected CancellableOperation(Scanner scanner, String label){
      this(label);
      setScanner(scanner);
    }
    protected CancellableOperation(String label){ this.label = label; }
    protected void setScanner(Scanner scanner){
      this.scanner = scanner;
      this.start = scanner.getIndex();
      this.cancelled = false;
    }
    @NotNull protected Bookmark cancel() {
      Bookmark bm = scanner.createBookmark();
      if (! scanner.endOfSource()) {
        if (scanner.getHistoryLength() > 0)
          scanner.walkBack(Math.max(-1, start));
      }
      cancelled = true;
      return bm;
    }
    @Override final public String getRuntimeLabel() { return label; }
    final public boolean isCancelled() { return cancelled; }
  }

  static public class SyntaxError extends box.star.text.SyntaxError {

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

  }

}

