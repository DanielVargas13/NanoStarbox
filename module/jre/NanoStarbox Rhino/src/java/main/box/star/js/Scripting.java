package box.star.js;

import org.mozilla.javascript.*;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Scripting {

  public static void addObject(Scriptable global, String name, Object value){
    Context cx = Context.enter();
    ScriptRuntime.setObjectProp(global, name,
        Context.javaToJS(value, global), cx);
    Context.exit();
  }

  public static Object createJavaScriptArray(Scriptable global, String... list){
    return createJavaScriptArray(global, new ArrayList<>(Arrays.asList(list)));
  }

  public static Object createJavaScriptArray(Scriptable global, Collection<? extends Object> array){
      Context cx = Context.getCurrentContext();
      Scriptable jsArray = cx.newArray(global, array.size());
      Object[] input = new Object[array.size()];
      array.toArray(input);

    for (int i = 0; i < input.length; i++) {
      ScriptableObject.putProperty(jsArray, i, Context.javaToJS(input[i], global));
    }
    return jsArray;
  }

  private static Object importClass(Scriptable global, String path) throws ClassNotFoundException {
    return importClass(global, Class.forName(path));
  }

  private static Object importClass(Scriptable global, Class<? extends Object> cls) {
    Context cx =  Context.getCurrentContext();
    WrapFactory wrapFactory = cx.getWrapFactory();
    Object newValue = wrapFactory.wrapJavaClass(cx, global, cls);
    return newValue;
  }

}
