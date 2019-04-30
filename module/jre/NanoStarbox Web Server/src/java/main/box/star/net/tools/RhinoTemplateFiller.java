package box.star.net.tools;

import box.star.util.Template;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class RhinoTemplateFiller implements Template.Filler {

  RhinoWebDriver rwd;

  RhinoTemplateFiller(RhinoWebDriver webDriver) {
    rwd = webDriver;
  }

  @Override
  public String replace(String script, Template.SourceData record) {
    Context cx = Context.enter();
    try {
      Script eval = cx.compileString(script,
          record.file, record.line, null);
      Scriptable shell = rwd.getScriptShell(cx);
      return (String) Context.jsToJava(eval.exec(cx, shell), String.class);
    }
    catch (Exception e) {
      e.printStackTrace();
      return e.getMessage();
    }
    finally {
      Context.exit();
    }
  }

}
