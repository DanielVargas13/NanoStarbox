package box.star.text.basic;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * <p>A class that can be used as a string token, with source reference point</p>
 * <br>
 * <p>
 *   The class supports constant text data, which prevents text modifications.
 *   To access the text, use the toString method, or another string method. If
 *   the text, is changed, then the type should also be changed. Such as if an
 *   operation changes a quoted text item to a literal text item. Ideally, the
 *   final composition should be made read-only when all transformations are
 *   complete for this generic text data container.
 * </p>
 * <br>
 * <p>
 *   Additionally the class supports get/set object, which allows any object
 *   to be associated with the source text.
 * </p>
 * @param <ENUM_CLASS> the enumeration (token-identity-set) to use for the type field
 */
public class TextRecord<ENUM_CLASS extends Enum> {

  final public Bookmark origin;
  String text;
  ENUM_CLASS type;
  long creationTime, modificationTime;
  boolean readOnly;
  Object object;

  public TextRecord(Bookmark origin){
    this.origin = origin;
    modificationTime = creationTime = System.currentTimeMillis();
  }

  public void setText(ENUM_CLASS type, String text) {
    if (this.readOnly) throw new IllegalStateException("cannot set text of "+this.getClass().getName()+" the field is marked read-only for this interface");
    if (this.text != null) {
      modificationTime = System.currentTimeMillis();
    }
    this.type = type;
    this.text = text;
  }

  public void setReadOnly(){
    this.readOnly = true;
  }

  public boolean isModified(){
    return creationTime != modificationTime;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public ENUM_CLASS getType() {
    return type;
  }

  public void setObject(Object object) {
    if (this.readOnly) throw new IllegalStateException("cannot set object of "+this.getClass().getName()+" the field is marked read-only for this interface");
    this.object = object;
  }

  public <T> T getObject(Class<T>cls){
    return cls.cast(object);
  }

  @Override
  public String toString() {
    return text;
  }

  public int length() {return text.length();}

  public boolean isEmpty() {return text.isEmpty();}

  public char charAt(int index) {return text.charAt(index);}

  public int codePointAt(int index) {return text.codePointAt(index);}

  public int codePointBefore(int index) {return text.codePointBefore(index);}

  public int codePointCount(int beginIndex, int endIndex) {return text.codePointCount(beginIndex, endIndex);}

  public int offsetByCodePoints(int index, int codePointOffset) {return text.offsetByCodePoints(index, codePointOffset);}

  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {text.getChars(srcBegin, srcEnd, dst, dstBegin);}

  public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {return text.getBytes(charsetName);}

  public byte[] getBytes(Charset charset) {return text.getBytes(charset);}

  public byte[] getBytes() {return text.getBytes();}

  public boolean contentEquals(StringBuffer sb) {return text.contentEquals(sb);}

  public boolean contentEquals(CharSequence cs) {return text.contentEquals(cs);}

  public boolean equals(String anotherString) {return text.equals(anotherString);}

  public boolean equalsIgnoreCase(String anotherString) {return text.equalsIgnoreCase(anotherString);}

  public int compareTo(String anotherString) {return text.compareTo(anotherString);}

  public int compareToIgnoreCase(String str) {return text.compareToIgnoreCase(str);}

  public boolean regionMatches(int toffset, String other, int ooffset, int len) {return text.regionMatches(toffset, other, ooffset, len);}

  public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {return text.regionMatches(ignoreCase, toffset, other, ooffset, len);}

  public boolean startsWith(String prefix, int toffset) {return text.startsWith(prefix, toffset);}

  public boolean startsWith(String prefix) {return text.startsWith(prefix);}

  public boolean endsWith(String suffix) {return text.endsWith(suffix);}

  public int indexOf(int ch) {return text.indexOf(ch);}

  public int indexOf(int ch, int fromIndex) {return text.indexOf(ch, fromIndex);}

  public int lastIndexOf(int ch) {return text.lastIndexOf(ch);}

  public int lastIndexOf(int ch, int fromIndex) {return text.lastIndexOf(ch, fromIndex);}

  public int indexOf(String str) {return text.indexOf(str);}

  public int indexOf(String str, int fromIndex) {return text.indexOf(str, fromIndex);}

  public int lastIndexOf(String str) {return text.lastIndexOf(str);}

  public int lastIndexOf(String str, int fromIndex) {return text.lastIndexOf(str, fromIndex);}

  public String substring(int beginIndex) {return text.substring(beginIndex);}

  public String substring(int beginIndex, int endIndex) {return text.substring(beginIndex, endIndex);}

  public CharSequence subSequence(int beginIndex, int endIndex) {return text.subSequence(beginIndex, endIndex);}

  public String concat(String str) {return text.concat(str);}

  public String replace(char oldChar, char newChar) {return text.replace(oldChar, newChar);}

  public boolean matches(String regex) {return text.matches(regex);}

  public boolean contains(CharSequence s) {return text.contains(s);}

  public String replaceFirst(String regex, String replacement) {return text.replaceFirst(regex, replacement);}

  public String replaceAll(String regex, String replacement) {return text.replaceAll(regex, replacement);}

  public String replace(CharSequence target, CharSequence replacement) {return text.replace(target, replacement);}

  public String[] split(String regex, int limit) {return text.split(regex, limit);}

  public String[] split(String regex) {return text.split(regex);}

  public static String join(CharSequence delimiter, CharSequence... elements) {return String.join(delimiter, elements);}

  public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {return String.join(delimiter, elements);}

  public String toLowerCase(Locale locale) {return text.toLowerCase(locale);}

  public String toLowerCase() {return text.toLowerCase();}

  public String toUpperCase(Locale locale) {return text.toUpperCase(locale);}

  public String toUpperCase() {return text.toUpperCase();}

  public String trim() {return text.trim();}

  public char[] toCharArray() {return text.toCharArray();}

}
