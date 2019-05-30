package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.text.Char;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import static box.star.text.Char.*;

public class LegacyScanner extends Scanner {
  public LegacyScanner(@NotNull String path, @NotNull Reader reader) {
    super(path, reader);
  }

  public LegacyScanner(@NotNull String path, @NotNull InputStream inputStream) {
    super(path, inputStream);
  }

  public LegacyScanner(@NotNull String path, @NotNull String s) {
    super(path, s);
  }

  public LegacyScanner(@NotNull File file) {
    super(file);
  }

  /**
   * <p>Requires that the next character explicitly match the specified character.</p>
   * @param character character to match
   * @return the character matched
   * @throws SyntaxError if the character in the stream does not match the character specified.
   */
  @Deprecated public char nextCharacter(char character) throws SyntaxError {
    char c = next();
    if (character != c)
      throw this.syntaxError("Expected " + translateCharacter(character) + " and located " + (endOfSource()?"end of text stream":"`"+translateCharacter(c)+ "'"));
    return c;
  }

  @Deprecated public String claim() {
    return toString();
  }

  @Deprecated public int nextUnsignedInteger() {
    flagNextCharacterSyntaxError("unsigned integer", MAP_ASCII_NUMBERS);
    String numbers = nextMap(MAP_ASCII_NUMBERS);
    return Integer.parseInt(numbers);
  }

  /**
   * <p>Call this to determine if the current character should be escaped.</p>
   * <br>
   * <p>if the sequence is \ then backslash mode = true;</p>
   * <p>if the sequence is \\ then backslash mode = false (and escape mode = true).</p>
   *
   * @return true if the current state is in backslash mode.
   */
  @Deprecated public boolean backSlashMode() {
    return previous() == BACKSLASH && current() == BACKSLASH;
  }

  /**
   * <p>Gets a future claim for the next character.</p>
   * <br>
   *   <p>Some scanner tasks may need a way to record where an external procedure
   *   will be starting within the scanner stream. This method provides that
   *   functionality, with a simple next/claim/back step routine, returning
   *   a capture of the claim.</p>
   *   <br>
   * @return the future claim
   */
  @Deprecated public String nextCharacterClaim(){
    String claim;
    next();
    claim = toString();
    back();
    return claim;
  }

  /**
   * @param character
   * @param caseSensitive
   * @return
   */
  @Deprecated public char nextCharacter(char character, boolean caseSensitive) {
    char c = next();
    if (!caseSensitive) {
      c = Char.toLowerCase(c);
      character = Char.toLowerCase(character);
    }
    if (character != c)
      throw this.syntaxError("Expected " + translateCharacter(character) + " and located " + (endOfSource()?"end of text stream":"`"+translateCharacter(c)+ "'"));
    return c;
  }

  /**
   * @return the white-space-scanned
   */
  @Deprecated public String nextAllWhiteSpace(){
    return nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
  }

  /**
   * @return all white-space characters which do not escape lines
   */
  @Deprecated public String nextLineWhiteSpace(){
    return nextMap(MAP_ASCII_LINE_WHITE_SPACE);
  }

  /**
   * <p>Tries to silently fetch the requested sequence match, from the beginning. if it fails
   * it returns a zero-length-string. this allows iteration through known compounds that fit in
   * certain contexts, and pass-through-[maybe-not]-present-value. failure to resolve 1
   * of a set should throw a syntax error, citing the semantic documentation
   * language for the composition set.</p>
   * @param sequence
   * @param caseSensitive
   * @return
   */
  @Deprecated public String nextOptionalSequence(String sequence, boolean caseSensitive){
    long start = getIndex();
    String test;
    String match = test = nextOptionalLength(sequence.length());
    if (!caseSensitive) {
      test = match.toLowerCase();
      sequence = sequence.toLowerCase();
    }
    if (! sequence.equals(test)) {
      walkBack(start); return "";
    }
    return match;
  }

  /**
   * <p>Like above, but returning only a boolean for branch on keyword condition</p>
   * <br>
   * @param sequence
   * @param caseSensitive
   * @return
   */
  @Deprecated public boolean nextSequenceMatch(String sequence, boolean caseSensitive){
    return nextOptionalSequence(sequence, caseSensitive).equals(sequence);
  }

  @Deprecated public boolean nextWordListMatch(String[] words, char[] wordBreak, boolean caseSensitive){
    for (String word:words){
      if (nextSequenceMatch(word, caseSensitive)) {
        if (wordBreak != null && wordBreak.length > 0) nextMap(wordBreak);
        return true;
      }
    }
    return false;
  }
  /**
   * <p>Case sensitive version of {@link #nextOptionalSequence(String, boolean)}</p>
   * <br>
   * @param sequence
   * @return
   */
  @Deprecated public boolean nextSequenceMatch(String sequence){
    return  nextOptionalSequence(sequence, true).equals(sequence);
  }
  /**
   *
   * @param label the text to use for this operation if something goes wrong
   * @param words the word list to use for searching the input
   * @param wordBreaks a set of characters which if supplied and longer than zero, must follow word match to complete the match
   * @param caseSensitive
   * @return
   */
  @Deprecated public String nextWord(String label, String[] words, char[] wordBreaks, boolean caseSensitive){
    for (String test:words){
      String operation = nextOptionalSequence(test, caseSensitive);
      if (Tools.EMPTY_STRING.equals(operation)) continue;
      else {
        if (wordBreaks != null && wordBreaks.length > 0){
          if (Char.mapContains(next(), wordBreaks)) return operation;
          else back();
        } else return operation;
      }
    }
    flagNextCharacterSyntaxError(label, '\0');
    return null;
  }

  /**
   * <p>Calls next word with a word-space requirement</p>
   * @param label
   * @param words
   * @return
   */
  @Deprecated public String nextWord(String label, String[] words, boolean caseSensitive){
    return nextWord(label, words, null, caseSensitive);
  }
  @Deprecated public String nextWord(String label, String[] words){
    return nextWord(label, words, null, true);
  }
  /**
   * <p>Call this method on a word list to make sure it doesn't short-circuit</p>
   * <br>
   * <p>A word-list short-circuit is a condition, where a word list fails to correctly
   * match an item because a shorter item matches the longer item first. This method
   * sorts the array from longest to shortest, to ensure that a short-circuit
   * is not possible.</p>
   * <br>
   * @param words
   */
  @Deprecated final static public void preventWordListShortCircuit(String[] words){
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

  /**
   * @param character
   * @param caseSensitive
   * @return
   */
  @Deprecated public char nextCharacter(String label, char character, boolean caseSensitive) {
    char c = endOfSource()?0:next();
    if (!caseSensitive) {
      c = Char.toLowerCase(c);
      character = Char.toLowerCase(character);
    }
    if (character != c)
      throw this.syntaxError("Expected " + label + " and located " + (endOfSource()?"end of text stream":"`"+translateCharacter(c)+ "'"));
    return c;
  }

  @Deprecated public char nextCharacter(String label, char character) {
    char c = endOfSource()?0:next();
    if (character != c)
      throw this.syntaxError("Expected " + label + " and located " + (endOfSource()?"end of text stream":"`"+translateCharacter(c)+ "'"));
    return c;
  }

  @Deprecated public String nextCharacterMap(String label, int max, char[] map, boolean caseSensitive){
    StringBuilder mapped = (max > 0)?new StringBuilder(max):new StringBuilder();
    char[] mini = null;
    if (!caseSensitive) mini = Char.toString(map).toLowerCase().toCharArray();
    if (max == 0) --max;
    boolean found;
    char c, v;
    do {
      found = false; v = next();
      if (!caseSensitive) c = Char.toLowerCase(v); else c = v;
      for (char t:caseSensitive?map:mini) if (c == t) { found = true; break;}
      if (!found)
        throw this.syntaxError("Expected " + label +
            " and located " + (endOfSource()?"end of text stream":
            "`"+translateCharacter(c)+ "'"));
      mapped.append(v);
    } while (mapped.length() != max && ! endOfSource());
    return mapped.toString();
  }
  /**
   * Match the next string input with a source string.
   *
   * @param seek
   * @param caseSensitive
   * @return
   * @throws SyntaxError if match fails
   */
  @NotNull @Deprecated
  public String nextString(@NotNull String seek, boolean caseSensitive) throws SyntaxError {
    StringBuilder out = new StringBuilder();
    char[] sequence = seek.toCharArray();
    for (char c : sequence) out.append(nextCharacter(seek, c, caseSensitive));
    return out.toString();
  }
  /**
   * <p>A rendition of {@link #nextField(char...)} that searches for a character sequence
   * with case sensitivity and optional backslash detection.</p>
   * <br>
   * <p>The found part is discarded from the buffer, and the stream is not rewound;
   * unlike the character variants of *Field which put the character back into the buffer.
   * These behaviors follow the logic that:</p>
   * <ol>
   *   <li>keywords are consumed by the caller</li>
   *   <li>symbols are terminals rather than input</li>
   * </ol>
   *
   * @param sequence a string to find at the end of the input buffer.
   * @param caseSensitive true if the search should be case sensitive (exact)
   * @param detectEscape handle backslash escapes on the head of this sequence (any found escape within sequence, will break the match regardless of this setting)
   * @return all the text up to but not including sequence
   * @throws SyntaxError if not found
   */
  @NotNull
  @Deprecated public String nextSequence(String sequence, boolean caseSensitive, boolean detectEscape) throws SyntaxError {
    int sourceLength = sequence.length(), bl = 0, matchIndex = 0;
    if (sourceLength == 0) return "";
    char[] search = ((caseSensitive)?sequence:sequence.toLowerCase()).toCharArray();
    StringBuilder sb = new StringBuilder();
    do {
      char c = this.next(); // step
      ++bl; // count buffer length
      if (endOfSource()) // die
        throw syntaxError("Expected `" + sequence + "' and found end of text stream");
      sb.append(c); // add to collection
      char find = (caseSensitive?c:Char.toLowerCase(c)); // transliterate if needed
      if (find == search[matchIndex]) { // got a match at stream, and search-index
        if (detectEscape && matchIndex == 0 && escapeMode()){ // got a escapable sequence-head-match
          /* doing this is: escaping */
        } else matchIndex++; // go to next char
      } else matchIndex = 0; // no match, go back to start
    } while (matchIndex != sourceLength);
    // if we got here, then a match occurred because no error was thrown during seek.
    return sb.substring(0, bl - sourceLength); // chop off the ending, returning what we scanned.
  }
  /**
   * <p>Performs all right-hand-side-backslash operations</p>
   * <br>
   * <code>
   * for this: right-hand-side = "everything following": `\' in the left-to-right-order
   * </code>
   *
   * @param character the first character of the text to expand. technically this is not correct usage. any character-sequence that requires further scanning, may invoke the scanner for its input. in some cases it may be possible to expand a character without further scanning, therefore this method provides the route
   * @return the string expansion of the escaped interpretation provided by the implementation.
   */
  @Deprecated @NotNull public String expand(char character) {
    if (characterExpander != null) {
      String expansion = characterExpander.expand(this, character);
      if (expansion != null) return expansion;
    }
    switch (character) {
      case 'd':
        return DELETE + Tools.EMPTY_STRING;
      case 'e':
        return ESCAPE + Tools.EMPTY_STRING;
      case 't':
        return "\t";
      case 'b':
        return "\b";
      case 'v':
        return VERTICAL_TAB + Tools.EMPTY_STRING;
      case 'r':
        return "\r";
      case 'n':
        return "\n";
      case 'f':
        return "\f";
      /*unicode*/
      case 'u': {
        try { return String.valueOf((char) Integer.parseInt(this.nextMap(0, 4, MAP_ASCII_HEX), 16)); }
        catch (NumberFormatException e) { throw this.syntaxError("Illegal escape", e); }
      }
      /*hex or octal*/
      case '0': {
        char c = this.next();
        if (c == 'x') {
          try { return String.valueOf((char) Integer.parseInt(this.nextMap(0, 4, MAP_ASCII_HEX), 16)); }
          catch (NumberFormatException e) { throw this.syntaxError("Illegal escape", e); }
        } else {
          this.back();
        }
        String chars = '0' + this.nextMap(0, 3, MAP_ASCII_OCTAL);
        int value = Integer.parseInt(chars, 8);
        if (value > 255) {
          throw this.syntaxError("octal escape subscript out of range; expected 00-0377; have: " + value);
        }
        char out = (char) value;
        return out + Tools.EMPTY_STRING;
      }
      /*integer or pass-through */
      default: {
        if (Char.mapContains(character, MAP_ASCII_NUMBERS)) {
          String chars = character + this.nextMap(0, 2, MAP_ASCII_NUMBERS);
          int value = Integer.parseInt(chars);
          if (value > 255) {
            throw this.syntaxError("integer escape subscript out of range; expected 0-255; have: " + value);
          } else {
            char out = (char) value;
            return out + Tools.EMPTY_STRING;
          }
        } else return fallBackCharacterExpander.expand(this, character);
      }
    }
  }

  /**
   * <p>Scan and assemble characters while scan is not in map, expanding escape
   * sequences, and ignoring escaped characters in map.</p>
   * <br>
   * <p>If eof is encountered, it is considered as the field boundary.</p>
   *
   * @param map the field boundaries
   * @return the assembled characters which exclude the field boundary characters
   * @throws SyntaxError if trying to escape end of stream.
   */
  @NotNull
  @Deprecated public String nextBoundField(@NotNull char... map) throws SyntaxError {

    StringBuilder sb = new StringBuilder();

    while (haveNext()) {

      char c = next();

      if (c == BACKSLASH && !escapeMode()) continue;

      if (c == 0) {
        if (escapeMode() && !haveNext())
          throw syntaxError("expected character escape sequence, found end of stream");
        if (endOfSource()) return sb.toString();
      }

      if (escapeMode()) {
        String swap = expand(c);
        sb.append(swap);
        continue;
      }

      if (Char.mapContains(c, map)) {
        this.back();
        break;
      }

      sb.append(c);

    }
    return sb.toString();
  }

  /**
   * Scan and assemble characters while scan is not in map and scan-length < max.
   *
   * @param max
   * @param map
   * @return
   * @throws Exception if read fails.
   */
  @NotNull
  @Deprecated public String nextFieldLength(int max, @NotNull char... map) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    do {
      if (sb.length() == max) break;
      c = this.next();
      if (!Char.mapContains(c, map)) sb.append(c);
      else {
        if (! endOfSource()) this.back();
        break;
      }
    } while (haveNext());

    return sb.toString();
  }

  /**
   * Tries to get up to n characters from the stream.
   * @param n the size of the string request
   * @return an empty string (n<=0), all the characters requested, or a truncated buffer (eof = true), whichever comes first
   */
  @Deprecated public String nextOptionalLength(int n){
    if (n <= 0) return Tools.EMPTY_STRING;
    char[] chars = new char[n];
    int pos = 0;
    while (pos < n) {
      chars[pos] = this.next();
      if (this.endOfSource()) break;
      pos += 1;
    }
    return new String(chars);
  }


  /**
   * @return true if this scanner already has a state lock.
   */
  @Deprecated  public boolean hasStateRecordLock() {
    return false;
  }

  /**
   * <p>Like {@link #flagThisCharacterSyntaxError}, but does the error checking beforehand.</p>
   * <br>
   *   <p>The current stream position is maintained if an error does not occur.</p>
   * @param message the content type expected by the driver (caller)
   * @param map the list of characters to match the current character with
   * @throws SyntaxError if the current character is not found within the given map
   */
  @Deprecated public void flagNextCharacterSyntaxError(String message, char... map) throws SyntaxError {
    char c = next();
    if (! Char.mapContains(c, map))
      throw this.syntaxError("Expected " + message + " and located `" + translateCharacter(c) + "'");
    back();
  }

  /**
   * <p>Raises a syntax error with the specified message on the current character position.</p>
   * @param message the content type expected by the driver (caller)
   * @throws SyntaxError representing this character at this position with this expected content message
   */
  @Deprecated public void flagThisCharacterSyntaxError(String message) throws SyntaxError {
    throw this.syntaxError("Expected " + message + " and located `" + translateCharacter(current()) + "'");
  }

  @Deprecated public @NotNull SyntaxError getThisCharacterSyntaxError(String message) throws SyntaxError {
    return this.syntaxError("Expected " + message + " and located `" + translateCharacter(current()) + "'");
  }

  @Override
  public LegacyScanner At(long line, long column, long index) throws IllegalStateException {
    return (LegacyScanner)super.At(line, column, index);
  }

  @Override
  public LegacyScanner WithTabSizeOf(int tabSize) {
    return (LegacyScanner)super.WithTabSizeOf(tabSize);
  }

  @Deprecated private Map<Character, String> TRANSLATION = new Hashtable<>();

  /**
   * <p>Call this method to configure the translation for a particular character</p>
   * @param c the character to map
   * @param translation the string to use when displaying this character to a user
   * @return the translation given
   */
  @Deprecated public String mapCharacterTranslation(char c, String translation) {
    TRANSLATION.put(c, translation);
    return translation;
  }

  /**
   * <p>Call this method to get a user display capable version of any character,
   * according to this scanner's internal configuration, or the static global configuration
   * of the box.star.Char class</p>
   * @param c the character to translate
   * @return the translation of the character
   */
  @Deprecated public String translateCharacter(char c) {
    if (c == 0) return "null";
    if (TRANSLATION.containsKey(c)) return TRANSLATION.get(c);
    else return Tools.switchNull(Char.translate(c), String.valueOf(c));
  }

  @Deprecated private static final CharacterExpander defaultCharacterExpander = new CharacterExpander() {
    @Override
    public String expand(Scanner scanner, char c) {
      return Char.toString(c);
    }
  };

  /**
   * <p>A custom character expander.</p>
   * <br>
   * <p>If this character expander returns null or is null, the default
   * implementation will be used.</p>
   * <p></p>
   */
  @Deprecated public CharacterExpander characterExpander = null;

  /**
   * The character expander that will resolve backslash escapes, if the current
   * configuration does not handle the escape.
   */
  @Deprecated public CharacterExpander fallBackCharacterExpander = defaultCharacterExpander;

  /**
   * Obtains a state lock, which can reset the reader and state if needed.
   *
   * @return a new state lock if the state is not already locked.
   */
  @NotNull
  @Deprecated public ScannerStateRecord getStateLock() {
    return new ScannerStateRecord(this);
  }


  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @return A Exception object, suitable for throwing
   */
  @NotNull
  @Deprecated public SyntaxError syntaxError(String message) {
    return new SyntaxError(message +":\n\n   "+this.claim());
  }

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message  The error message.
   * @param causedBy The throwable that caused the error.
   * @return A Exception object, suitable for throwing
   */
  @NotNull
  @Deprecated public SyntaxError syntaxError(@NotNull String message, @NotNull Throwable causedBy) {
    return new SyntaxError(message + ":\n\n   " +this.claim(), causedBy);
  }

  /**
   * <p>Starts a {@link ScannerMethod}</p>
   * <br>
   * <p>Creates a copy of the method, and calls its
   * {@link ScannerMethod#start(LegacyScanner, Object[])} method with the given
   * parameters.</p>
   *
   * @param method the method to use
   * @param parameters the parameters to forward to the method
   * @return hopefully, the result of the scanner method's {@link ScannerMethod#compile(LegacyScanner)} routine, possibly an Exception or SyntaxError
   */
  @NotNull
  @Deprecated final public String run(ScannerMethod method, Object... parameters) {
    method = method.clone();
    method.start(this, parameters);
    do {
      char c = next();
      method.collect(this, c);
      if (method.terminate(this, c)) break;
    } while (method.scan(this));
    return method.compile(this);
  }

  /**
   * <p>Calls upon a scanner method, as a branch from within a scanner method.</p>
   *
   * @param method the method to use
   * @param parameters the parameters for the method
   * @return hopefully, the result of the scanner method's {@link ScannerMethod#compile(LegacyScanner)} routine, possibly an Exception or SyntaxError
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

  public static interface CharacterExpander {
    @NotNull String expand(@NotNull Scanner scanner, char character);
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
   * {@link LegacyScanner#run(ScannerMethod, Object...)} method, and when the run method
   * calls your {@link #compile(LegacyScanner)} method, you simply parse the data you
   * collected in your buffer, and store it in your attribute-map. To obtain your
   * method parameters you must record them during the Scanner's call to your
   * {@link #start(LegacyScanner, Object[])} method.</p>
   * <br>
   * <p>A method may call other methods, and may also call upon the methods
   * of the scanner during any execution phase of its lifecycle.</p>
   * <br>
   *
   * <h3>lifecycle</h3>
   * <ul>
   * {@link #reset()}, {@link #start(LegacyScanner, Object[])}, {@link #collect(LegacyScanner, char)}, {@link #terminate(LegacyScanner, char)} and {@link #scan(LegacyScanner)}</li>
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
    protected void start(@NotNull LegacyScanner scanner, Object[] parameters) {}

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
    protected void collect(@NotNull LegacyScanner scanner, char character) {
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
    protected final boolean zeroTerminator(@NotNull LegacyScanner scanner, char character) {
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
    protected boolean terminate(@NotNull LegacyScanner scanner, char character) {
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
    protected String compile(@NotNull LegacyScanner scanner) {
      return buffer.toString();
    }

    /**
     * <p>Signals whether or not the process should continue reading input.</p>
     *
     * <p>The default method returns true.</p>
     *
     * @param scanner
     * @return true if the TextLegacyScanner should read more input.
     */
    protected boolean scan(@NotNull LegacyScanner scanner) { return true; }

    /**
     * Step back the scanner and the buffer by 1 character.
     * <p><i>
     * Overriding is not recommended.
     * </i></p>
     *
     * @param scanner
     */
    protected void backStep(@NotNull LegacyScanner scanner) {
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
    protected void backStep(@NotNull LegacyScanner scanner, long to) {
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
     * {@link #start(LegacyScanner, Object[])}.</p>
     *
     * @return the cloned method instance
     */
    @NotNull
    @Override
    protected LegacyScanner.ScannerMethod clone() {
      try {
        ScannerMethod clone = (ScannerMethod) super.clone();
        clone.reset();
        return clone;
      }
      catch (CloneNotSupportedException failure) {
        throw new Exception("unable to create method object", failure);
      }
    }

  }

  /**
   * This class is deprecated because it's usage adds a layer of complexity to
   * the scanner that is quite frankly, no longer needed due to the inbuilt
   * StringBuilder buffering capabilities of the ScannerState which was not
   * available in the project until NSR7 (estimation).
   *
   * This class and its counterparts are marked for deletion SPEC: NSR10.
   */
  @Deprecated public static class ScannerStateRecord {

    protected Scanner main;
    //protected Scanner.State backupState;

    protected ScannerStateRecord(@NotNull Scanner main) {
      if (true)
        throw new Exception("cannot acquire scanner lock",
            new IllegalStateException("state lock acquired"));
      if (!main.haveNext()) {
        throw new Exception("cannot acquire scanner lock",
            new IllegalStateException("end of source data"));
      }
      this.main = main;
     // this.backupState = main.state.clone();
      try {
        main.reader.mark(1000000);
      }
      catch (IOException e) {
        throw new Exception("failed to configure source reader", e);
      }
     // main.state.locked = true;
    }

    public void restore() throws Exception {
      if (main == null) return;
      try {
        try {
          main.reader.reset();
        //  main.state = backupState;
        }
        catch (IOException e) {
          throw new Exception("failed to restore backup state", e);
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
        //this.main.state.locked = false;
        this.main = null;
      //  this.backupState = null;
      }
    }
  }

  /**
   * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
   *
   * @author Hypersoft-Systems: USA
   * @version 2015-12-09
   */
  @Deprecated public static class Exception extends RuntimeException {
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 0;

    /**
     * Constructs a TextScanner.Exception with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public Exception(final String message) {
      super(message);
    }

    /**
     * Constructs a TextScanner.Exception with an explanatory message and cause.
     *
     * @param message Detail about the reason for the exception.
     * @param cause   The cause.
     */
    public Exception(final String message, final Throwable cause) {
      super(message, cause);
    }

    /**
     * Constructs a new TextScanner.Exception with the specified cause.
     *
     * @param cause The cause.
     */
    public Exception(final Throwable cause) {
      super(cause.getMessage(), cause);
    }

  }

  /**
   * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
   */
  @Deprecated public static class SyntaxError extends RuntimeException {
    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 0;

    /**
     * Constructs a TextScanner.Exception with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public SyntaxError(final String message) {
      super(message);
    }

    /**
     * Constructs a TextScanner.Exception with an explanatory message and cause.
     *
     * @param message Detail about the reason for the exception.
     * @param cause   The cause.
     */
    public SyntaxError(final String message, final Throwable cause) {
      super(message, cause);
    }

  }
}
