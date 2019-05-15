package box.star.net;

import box.star.Tools;
import box.star.content.MimeTypeMap;
import box.star.content.MimeTypeScanner;
import box.star.contract.Nullable;
import box.star.net.http.response.Status;
import box.star.net.tools.MimeTypeDriver;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;
import box.star.text.MacroShell;
import box.star.text.basic.Scanner;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.io.BufferedInputStream;
import java.util.Stack;

import static box.star.net.http.HTTPServer.MIME_HTML;

public class JavaScriptPageBuilder implements MimeTypeDriver<WebService>, MimeTypeScanner {
  public final static String NANO_STARBOX_JAVASCRIPT_SERVER_PAGE = "text/html, application/x-nano-starbox-javascript-server-page";
  private Global global;
  public JavaScriptPageBuilder(MimeTypeMap mimeTypeMap){
    mimeTypeMap.putIfAbsent("jsp", NANO_STARBOX_JAVASCRIPT_SERVER_PAGE);
    global = new Global();
    Context cx = Context.enter();
    global.init(cx);
    addGlobalObject("global", global);
    Context.exit();
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
  public ServerResult createMimeTypeResult(WebService server, ServerContent content) {
    Context cx = Context.enter();
    try {
      Scriptable shell = getScriptShell(cx, global);
      ScriptRuntime.setObjectProp(shell, "server", Context.javaToJS(server, shell), cx);
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
      String output = macroShell.start(scanner);
      scanner.close();
      if (content.mimeType.equals(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE)) content.mimeType = MIME_HTML;
      return new ServerResult(content.session, Status.OK, content.mimeType, output);
    } catch (Exception e){throw new RuntimeException(e);}
    finally {
      Context.exit();
    }
  }
  @Override
  public String scanMimeType(BufferedInputStream source) {
    String scan;
    try {
      source.mark(59);
      scan = new Scanner(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE, source).nextField('>');
      source.reset();
      if (scan.equals("<!MIME "+ NANO_STARBOX_JAVASCRIPT_SERVER_PAGE)) {
        //noinspection ResultOfMethodCallIgnored
        source.skip(scan.length()+1);
        return NANO_STARBOX_JAVASCRIPT_SERVER_PAGE;
      }
      return null;
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
