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
public abstract class TextRecord<ENUM_CLASS extends Enum> {

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

  /**
   * The protected method a subclass uses to configure the text
   * @param type
   * @param text
   */
  public void setText(ENUM_CLASS type, String text) {
    if (this.readOnly) throw new IllegalStateException("cannot set text of "+this.getClass().getName()+" the field is marked read-only for this interface");
    if (this.text != null) {
      modificationTime = System.currentTimeMillis();
    }
    this.type = type;
    this.text = text;
  }

  final public void setReadOnly(){
    this.readOnly = true;
  }

  final public boolean isModified(){
    return creationTime != modificationTime;
  }

  final public long getCreationTime() {
    return creationTime;
  }

  final public long getModificationTime() {
    return modificationTime;
  }

  final public boolean isReadOnly() {
    return readOnly;
  }

  final public ENUM_CLASS getType() {
    return type;
  }

  final public void setObject(Object object) {
    if (this.readOnly) throw new IllegalStateException("cannot set object of "+this.getClass().getName()+" the field is marked read-only for this interface");
    this.object = object;
  }

  final public <T> T getObject(Class<T>cls){
    return cls.cast(object);
  }

  final public Object getObject() {
    return object;
  }

  final public String getText() {
    return text;
  }

  @Override
  final public String toString() {
    return text;
  }

}
