package box.star.js;

import box.star.WebServer;
import box.star.io.protocols.http.IHTTPSession;
import box.star.io.protocols.http.response.Response;
import box.star.util.Template;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class RhinoWebDriver extends WebServer.MimeTypeDriver {

    private static final String RHINO_DRIVER_KEY = "javascript/x-nano-starbox-rhino-servlet";

    private static final String initScript =
            "var NanoStarbox = Packages.box.star, "+
                "System = java.lang.System, "+
                "File = java.io.File;";

    Global global;
    RhinoTemplateFiller rhinoTemplateFiller;

    public RhinoWebDriver(WebServer server) {
        global = new Global();
        Context cx = Context.enter();
        global.init(cx);
        cx.evaluateString(global, initScript,
                "<stdin>", 1, null);
        Context.exit();
        addGlobalObject("server", server);
        server.addStaticIndexFile("index.js");
        server.registerMimeTypeDriver(RHINO_DRIVER_KEY, this);
        rhinoTemplateFiller = new RhinoTemplateFiller(this);
    }

    public RhinoTemplateFiller getTemplateFiller(){
        return rhinoTemplateFiller;
    }

    public void addGlobalObject(String name, Object javaObject) {
        Context cx = Context.enter();
        ScriptRuntime.setObjectProp(global, name,
                Context.javaToJS(javaObject, global), cx);
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            Context.exit();
        }
    }

    Scriptable getScriptShell(Context cx) {
        return ScriptRuntime.newObject(cx, global, "Object", null);
    }

}
