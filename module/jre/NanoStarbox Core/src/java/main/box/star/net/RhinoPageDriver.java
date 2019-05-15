package box.star.net;

import box.star.Tools;
import box.star.content.MimeTypeMap;
import box.star.content.MimeTypeScanner;
import box.star.contract.Nullable;
import box.star.io.Streams;
import box.star.js.ClassPathLoader;
import box.star.net.http.response.Status;
import box.star.net.tools.MimeTypeDriver;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;
import box.star.text.Char;
import box.star.text.MacroShell;
import box.star.text.basic.Scanner;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static box.star.net.http.HTTPServer.MIME_HTML;

public class RhinoPageDriver implements MimeTypeDriver<WebService>, MimeTypeDriver.WithMediaMapControlPort, MimeTypeScanner {
  public final static String NANO_STARBOX_JAVASCRIPT_SERVER_PAGE = "text/html, application/x-nano-starbox-javascript-server-page";
  public static final String URI_LIST_SPLITTER = Char.toString(Char.BACKSLASH, Char.PIPE);
  private Global global;
  public RhinoPageDriver(Global global){
    this.global = global;
  }
  @Override
  public void configureMimeTypeController(MimeTypeMap controlPort) {
    controlPort.putIfAbsent("jsp", NANO_STARBOX_JAVASCRIPT_SERVER_PAGE);
  }

  public Global getGlobal() {
    return global;
  }

  public RhinoPageDriver(){this((List)null);}
  public RhinoPageDriver(@Nullable List<String> moduleDirectories){
    Context cx = Context.enter();
    global = Main.global;
    global.init(cx);
    if (moduleDirectories == null){
      String modulePath = Tools.switchNull(
          System.getProperty("box.star.net.jsp.require.module.uris"),
          System.getenv("JSP_REQUIRE_MODULE_URIS"));
      if (modulePath != null)
        global.installRequire(cx, Arrays.asList(modulePath.split(URI_LIST_SPLITTER)), false);
      else
        global.installRequire(cx, null, false);
    } else global.installRequire(cx, moduleDirectories, false);
    Context.exit();
  }
  private Scriptable getScriptShell(Context cx, @Nullable Scriptable parent) {
    return ScriptRuntime.newObject(cx, Tools.switchNull(parent, global), "Object", null);
  }
  @Override
  public ServerResult createMimeTypeResult(WebService server, ServerContent content) {
    Context cx = Context.enter();
    // TODO: FIX THIS SUPER UGLY HACK
    cx.setApplicationClassLoader(ScriptRuntime.classPathLoader);
    cx.setOptimizationLevel(-1);
    try {
      String uri = content.session.getUri();
      Object location = server.getFile(uri);
      if (location != null) {
        if (((File)location).isDirectory());
        else location = ((File)location).getParentFile();
      } else {
        location = URI.create(server.getAddress() + server.getParentUri(uri)).toURL();
      }
      Scriptable jsThis = getScriptShell(cx, global);
      ScriptRuntime.setObjectProp(jsThis, "global", global, cx);
      ScriptRuntime.setObjectProp(jsThis, "directory", Context.javaToJS(location, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "server", Context.javaToJS(server, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "session", Context.javaToJS(content.session, jsThis), cx);
      InputStream sourceStream = content.getStream();
      Scanner scanner = new Scanner(content.session.getUri(), Streams.readWholeString(sourceStream));
      sourceStream.close();
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
      if (content.mimeType.equals(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE)) content.mimeType = MIME_HTML;
      return new ServerResult(content.session, Status.OK, content.mimeType, documentBuilder.start(scanner));
    } catch (Exception e){throw new RuntimeException(e);}
    finally {
      Context.exit();
    }
  }

  private final static String HTML_MAGIC = "<!MIME "+ NANO_STARBOX_JAVASCRIPT_SERVER_PAGE+">";

  private boolean detectHtmlDocument(BufferedInputStream source) throws IOException {
    String scan;
    int SEEK = HTML_MAGIC.length();
    source.mark(SEEK);
    scan = new Scanner(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE, source).nextFieldLength(SEEK,'>');
    source.reset();
    if ((scan + '>').equals(HTML_MAGIC)){
      //noinspection ResultOfMethodCallIgnored
      source.skip(SEEK);
      return true;
    }
    return false;
  }

  @Override
  public String scanMimeType(BufferedInputStream source) {
    try {
      if (detectHtmlDocument(source))
        return NANO_STARBOX_JAVASCRIPT_SERVER_PAGE; else return null;
    } catch (Exception e) { throw new RuntimeException(e); }
  }

}
