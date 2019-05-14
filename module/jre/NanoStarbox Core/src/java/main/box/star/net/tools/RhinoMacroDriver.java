package box.star.net.tools;

import box.star.Tools;
import box.star.content.MimeTypeDriver;
import box.star.contract.Nullable;
import box.star.net.WebService;
import box.star.net.http.response.Status;
import box.star.text.MacroShell;
import box.star.text.basic.Scanner;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.util.Stack;

public class RhinoMacroDriver implements MimeTypeDriver {
  public final static String RHINO_MACRO_DRIVER_MIME_TYPE = "javascript/x-nano-starbox-rhino-macro-document";
  Global global;
  public RhinoMacroDriver(WebService ws){
    ws.mimeTypeMap.addMagicReader(new RhinoMimeTypeReader());
    global = new Global();
    Context cx = Context.enter();
    global.init(cx);
    addGlobalObject("global", global);
    addGlobalObject("server", ws);
  }
  public void addGlobalObject(String name, Object javaObject) {
    Context cx = Context.enter();
    ScriptRuntime.setObjectProp(global, name, Context.javaToJS(javaObject, global), cx);
    Context.exit();
  }
  private Scriptable getScriptShell(Context cx, @Nullable Scriptable parent) {
    return ScriptRuntime.newObject(cx, Tools.makeNotNull(parent, global), "Object", null);
  }
  @Override
  public ServerResult createMimeTypeResult(ServerContent content) {
    Context cx = Context.enter();
    try {
      Scriptable shell = getScriptShell(cx, global);
      ScriptRuntime.setObjectProp(shell, "session", Context.javaToJS(content.session, shell), cx);
      Scanner scanner = new Scanner(content.session.getUri(), content.getStream());
      MacroShell macroShell = new MacroShell(System.getenv());
      ScriptRuntime.setObjectProp(shell, "shell", Context.javaToJS(macroShell, shell), cx);
      macroShell.addCommand("js", new MacroShell.Command(){
        @Override
        protected String run(String command, Stack<String> parameters) {
          StringBuilder output = new StringBuilder();
          for (String p: parameters) output.append((String)
              Context.jsToJava(
                  cx.evaluateString(shell, p,
                      scanner.getPath(), (int) scanner.getLine(),
                      null), String.class)
          );
          return output.toString();
        }
      });
      return new ServerResult(content.session, Status.OK, content.mimeType, macroShell.start(scanner));
    } catch (Exception e){throw new RuntimeException(e);}
    finally {
      cx.exit();
    }
  }
}
