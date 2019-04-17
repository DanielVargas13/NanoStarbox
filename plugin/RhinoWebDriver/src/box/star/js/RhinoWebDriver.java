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

public class RhinoWebDriver extends WebServer.MimeTypeDriver implements Template.TemplateFiller {

    private static final String RHINO_DRIVER_KEY = "javascript/x-nano-starbox-servlet";

    Global global;

    public RhinoWebDriver(WebServer server) {
        global = new Global();
        Context cx = Context.enter();
        global.init(cx);
        Context.exit();
        addGlobalObject("server", server);
        server.addStaticIndexFile("index.js");
        server.registerMimeTypeDriver(RHINO_DRIVER_KEY, this);
    }

    public void addGlobalObject(String name, Object javaObject) {
        Context cx = Context.enter();
        ScriptRuntime.setObjectProp(global, name, Context.javaToJS(javaObject, global), cx);
        Context.exit();
    }

    public String replace(String script, Template.SourceData record) {
        Context cx = Context.enter();
        try {
            Script eval = cx.compileString(script, record.file, record.line, null);
            Scriptable shell = getScriptShell(cx);
            return (String) Context.jsToJava(eval.exec(cx, shell), String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            Context.exit();
        }
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

    private Scriptable getScriptShell(Context cx) {
        return ScriptRuntime.newObject(cx, global, "Object", null);
    }

}
