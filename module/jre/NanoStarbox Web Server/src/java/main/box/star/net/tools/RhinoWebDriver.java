package box.star.net.tools;

import box.star.net.WebServer;
import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class RhinoWebDriver extends WebServer.MimeTypeDriver {

  private static final String RHINO_DRIVER_KEY = "javascript/x-nano-starbox-rhino-servlet";
  private static final String initScript = "var Starbox = Packages.box.star, " +
    "Net = Packages.box.star.net, " +
      "System = java.lang.System, " +
        "File = java.io.File;";
  Global global;

  public RhinoWebDriver(WebServer server) {
    global = new Global();
    Context cx = Context.enter();
    global.init(cx);
    cx.evaluateString(global, initScript, "<stdin>", 1, null);
    Context.exit();
    addGlobalObject("server", server);
    server.addStaticIndexFile("index.js");
    server.registerMimeTypeDriver(RHINO_DRIVER_KEY, this);
    server.registerTemplateFiller(HTTPServer.MIME_HTML, new RhinoTemplateFiller(this));
  }

  public static void createInstance(WebServer ws) {
    new RhinoWebDriver(ws);
  }

  public void addGlobalObject(String name, Object javaObject) {
    Context cx = Context.enter();
    ScriptRuntime.setObjectProp(global, name, Context.javaToJS(javaObject, global), cx);
    Context.exit();
  }

  @Override
  public Response generateServiceResponse(WebServer webServer, File file, String mimeType, IHTTPSession ihttpSession) {
    Context cx = Context.enter();
    try {
      try (Reader reader = new FileReader(file)) {
        Script script = cx.compileReader(reader, file.getPath(), 1, null);
        Scriptable shell = getScriptShell(cx);
        script.exec(cx, shell);
        Function f = (Function) ScriptableObject.getProperty(shell, "generateServiceResponse");
        Object[] parameters = new Object[]{
            Context.javaToJS(file, shell),
            Context.javaToJS(mimeType, shell),
            Context.javaToJS(ihttpSession, shell)};
        return (Response) Context.jsToJava(f.call(cx, global, shell, parameters), Response.class);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    finally {
      Context.exit();
    }
  }

  Scriptable getScriptShell(Context cx) {
    return ScriptRuntime.newObject(cx, global, "Object", null);
  }

}
