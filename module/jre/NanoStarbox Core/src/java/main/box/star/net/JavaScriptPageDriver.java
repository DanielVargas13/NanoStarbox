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

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static box.star.net.http.HTTPServer.MIME_HTML;

public class JavaScriptPageDriver implements MimeTypeDriver<WebService>, MimeTypeScanner {
  public final static String NANO_STARBOX_JAVASCRIPT_SERVER_PAGE = "text/html, application/x-nano-starbox-javascript-server-page";
  private Global global;
  public JavaScriptPageDriver(Global global, MimeTypeMap mimeTypeMap){
    this.global = global;
    mimeTypeMap.putIfAbsent("jsp", NANO_STARBOX_JAVASCRIPT_SERVER_PAGE);
  }
  public JavaScriptPageDriver(MimeTypeMap mimeTypeMap, @Nullable List<String> moduleDirectories){
    mimeTypeMap.putIfAbsent("jsp", NANO_STARBOX_JAVASCRIPT_SERVER_PAGE);
    global = new Global();
    Context cx = Context.enter();
    global.init(cx);
    if (moduleDirectories == null){
      String modulePath = Tools.makeNotNull(System.getenv("JSP_MODULE_URIS"), System.getProperty("box.star.net.jsp.module.uris"));
      if (modulePath != null) {
        List<String> uris = new ArrayList<>();
        uris.addAll(Arrays.asList(modulePath.split("[^\\\\];")));
        global.installRequire(cx, uris, false);
      } else {
        global.installRequire(cx, null, false);
      }
    } else {
      global.installRequire(cx, moduleDirectories, false);
    }
    Context.exit();
  }
  public JavaScriptPageDriver(MimeTypeMap mimeTypeMap){
    this(mimeTypeMap, null);
  }
  private Scriptable getScriptShell(Context cx, @Nullable Scriptable parent) {
    return ScriptRuntime.newObject(cx, Tools.makeNotNull(parent, global), "Object", null);
  }
  @Override
  public ServerResult createMimeTypeResult(WebService server, ServerContent content) {
    Context cx = Context.enter();
    try {
      String uri = content.session.getUri();
      Object location = server.getFile(uri);
      if (location != null) {
        if (((File)location).isDirectory());
        else location = ((File)location).getParentFile();
      } else {
        location = URI.create(server.getAddress() +"/"+ uri.substring(0, Math.max(0, uri.lastIndexOf("/"))).substring(1)).toURL();
      }
      Scriptable jsThis = getScriptShell(cx, global);
      ScriptRuntime.setObjectProp(jsThis, "global", global, cx);
      ScriptRuntime.setObjectProp(jsThis, "directory", Context.javaToJS(location, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "server", Context.javaToJS(server, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "session", Context.javaToJS(content.session, jsThis), cx);
      Scanner scanner = new Scanner(content.session.getUri(), content.getStream());
      MacroShell documentBuilder = new MacroShell(System.getenv());
      ScriptRuntime.setObjectProp(jsThis, "shell", Context.javaToJS(documentBuilder, jsThis), cx);
      documentBuilder.addCommand("js", new MacroShell.Command(){
        @Override
        protected String run(String command, Stack<String> parameters) {
          StringBuilder output = new StringBuilder();
          for (String p: parameters) output.append((String)
              Context.jsToJava(
                  cx.evaluateString(jsThis, p,
                      scanner.getPath(), (int) scanner.getLine(),
                      null), String.class)
          );
          return output.toString();
        }
      });

      String output = documentBuilder.start(scanner);
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
    String MAGIC = "<!MIME "+ NANO_STARBOX_JAVASCRIPT_SERVER_PAGE+">";
    try {
      source.mark(MAGIC.length());
      scan = new Scanner(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE, source).nextFieldLength(MAGIC.length(),'>') + ">";
      source.reset();
      if (scan.equals(MAGIC)) {
        //noinspection ResultOfMethodCallIgnored
        source.skip(scan.length()+1);
        return NANO_STARBOX_JAVASCRIPT_SERVER_PAGE;
      }
      return null;
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
