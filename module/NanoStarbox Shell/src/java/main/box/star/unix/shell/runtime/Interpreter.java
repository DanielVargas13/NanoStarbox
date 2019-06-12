package box.star.unix.shell.runtime;

import box.star.text.basic.Bookmark;
import box.star.text.basic.TextRecord;

import java.util.ArrayList;
import java.util.Stack;

public class Interpreter {

  /**
   * <p>When an unquoted tilde is detected as a parameter, this method is called
   * to expand the tilde character.</p>
   * <br>
   * <p>
   *   Tilde expansion can occur in many places. When a command is evaluated,
   *   this method is the first method to be called on all text expansions.
   * </p>
   * @param context
   * @param origin
   * @return the text representing the tilde character (user's home path)
   */
  static public String expandTilde(Context context, Bookmark origin) {
    return null;
  }

  /**
   * <p>Expands the environment variable script</p>
   * <br>
   * <p>
   *   An environment variable script, is whatever text occurs in between '${'
   *   and '}'. Historically this "method" is referred to as expand-parameter,
   *   within the open group shell command language manual. The syntax bug
   *   which expresses ambiguity between command parameters and environment
   *   variables is not duplicated by this class. This method is the second
   *   to be called in all text expansions.
   * </p>
   * <br>
   * <p>
   *   This implementation returns an object, because environment variables
   *   may be objects or strings. This implementation supports java based functions
   *   that can receive objects as parameters, and return objects as exit status.
   *   Those objects can also be passed to text based commands or concatenated
   *   with other text as a string, using the object's toString method, in addition
   *   to being able to be used via direct method call (native function to function)
   *   within the JVM.
   * </p>
   * <br>
   * (https://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_06_02)
   * @param context
   * @param source
   * @return a java object or string object representing the environment variable script
   */
  static public Object expandVariable(Context context, TextRecord source) {
    return null;
  }

  /**
   * <p>Evaluate command script, and return the string conversion of stdout.</p>
   * <br>
   * <p>
   *   command script is the text found between '$(' and ')'
   * </p>
   * @param context
   * @param source
   * @return
   */
  static public String expandCommand(Context context, TextRecord source) {
    return null;
  }

  /**
   * <p>Evaluate math script, and return the string conversion.</p>
   * <br>
   * <p>
   *   Math script is the text found between '$[' and ']'
   * </p>
   * @param context
   * @param origin
   * @param text
   * @return
   */
  static public String expandArithmetic(Context context, Bookmark origin, String text) {
    return null;
  }

  /**
   * Perform field splitting using IFS
   * @param context
   * @param origin
   * @param text
   * @return
   */
  static public ArrayList<String> splitField(Context context, Bookmark origin, String text) {
    return null;
  }

  /**
   * Expand shell path query language
   * @param context
   * @param origin
   * @param text
   * @return
   */
  static public String expandPath(Context context, Bookmark origin, String text) {
    return null;
  }

}
