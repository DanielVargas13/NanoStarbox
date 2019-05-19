package box.star.net;

import box.star.Tools;
import box.star.content.MimeTypeMap;
import box.star.contract.Nullable;
import box.star.io.Streams;
import box.star.net.http.response.Status;
import box.star.net.tools.MimeTypeDriver;
import box.star.net.tools.ServerContent;
import box.star.net.tools.ServerResult;
import box.star.text.Char;
import box.star.text.MacroShell;
import box.star.text.basic.Scanner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static box.star.net.http.HTTPServer.MIME_HTML;

public class RhinoPageDriver implements
    MimeTypeDriver,
    MimeTypeDriver.WithMediaMapControlPort,
    MimeTypeDriver.WithMimeTypeScanner,
    MimeTypeDriver.WithIndexFileListControlPort {
  /**
   * <p>{@link MimeTypeDriver} mime-type</p>
   * <br>
   */
  public final static String NANO_STARBOX_JAVASCRIPT_SERVER_PAGE =
      "text/html, application/x-nano-starbox-javascript-server-page";
  /**
   * <p>System property name pointing to a string-list of URIs separated by {@link #URI_LIST_SPLITTER}</p>
   * <br>
   */
  public final static String REQUIRE_MODULE_URIS_PROPERTY = "box.star.net.jsp.require.module.uris";
  /**
   * <p>System property name pointing to a Java Regular expression String splitter</p>
   * <br>
   */
  public final static String URI_LIST_SPLITTER_PROPERTY = "box.star.net.jsp.uri.list.splitter";
  /**
   * <p>System environment name pointing to a string-list of URIs separated by {@link #URI_LIST_SPLITTER}</p>
   * <br>
   */
  public final static String REQUIRE_MODULE_URIS_VAR = "JSP_REQUIRE_MODULE_URIS";
  /**
   * <p>System environment name pointing to a Java Regular expression String splitter to use as {@link #URI_LIST_SPLITTER}</p>
   * <br>
   */
  public final static String URI_LIST_SPLITTER_VAR = "JSP_URI_LIST_SPLITTER";
  /**
   * <p>Java Regular Expression {@link String} for splitting URIs using {@link String#split(String)}.</p>
   * <br>
   * <b>Required by:</b>
   * <ul>
   * <li>{@link RhinoPageDriver#RhinoPageDriver(List)} to split uris</li>
   * </ul>
   * <br>
   * <b>Inherits From:</b>
   * <ol>
   * <li>System override through: {@link #URI_LIST_SPLITTER_PROPERTY}</li>
   * <li>Environment override through: {@link #URI_LIST_SPLITTER_VAR}</li>
   * <li><b>Default</b>: "\|"</li>
   * </ol>
   */
  public final static String URI_LIST_SPLITTER =
      Tools.switchNull(
          // System property or environment var
          Tools.switchNull(System.getProperty(URI_LIST_SPLITTER_PROPERTY), System.getenv(URI_LIST_SPLITTER_VAR)),
          Char.toString(Char.BACKSLASH, Char.PIPE)); // default
  private final static String HTML_MAGIC = "<!MIME " + NANO_STARBOX_JAVASCRIPT_SERVER_PAGE + ">";
  private final static MacroShell.Command srcCommand = new MacroShell.Command() {
    @Override
    protected String run(String command, Stack<String> parameters) {
      Context cx = Context.enter();
      cx.setOptimizationLevel(9);
      try {
        // TODO: optimize this loop's cache request
        for (String file : parameters)
          Main.processFileNoThrow(cx, (Scriptable) main.objects.get("this"), main.objects.get("directory") + "/" + file);
      }
      finally {
        Context.exit();
      }
      return "";
    }
  };
  private final static MacroShell.Command starCommand = new MacroShell.Command() {
    @Override
    protected String run(String command, Stack<String> parameters) {
      if (command.equalsIgnoreCase("<script>")) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        String result;
        try {
          result = call("val", parameters);
        }
        finally {
          Context.exit();
        }
        return result;
      }
      throw new IllegalArgumentException("unknown command: " + command);
    }
  };
  private final static MacroShell.Command doCommand = new MacroShell.Command() {
    @Override
    protected String run(String command, Stack<String> parameters) {
      Context cx = Context.getCurrentContext();
      Scriptable jsThis = (Scriptable) main.objects.get("this");
      for (String p : parameters)
        cx.evaluateString(jsThis, p,
            scanner.getPath(), (int) scanner.getLine(),
            null);
      return "";
    }
  };
  private final static MacroShell.Command valCommand = new MacroShell.Command() {
    @Override
    protected String run(String command, Stack<String> parameters) {
      Context cx = Context.getCurrentContext();
      Scriptable jsThis = (Scriptable) main.objects.get("this");
      StringBuilder output = new StringBuilder();
      for (String p : parameters)
        output.append((String)
            Context.jsToJava(
                cx.evaluateString(jsThis, p,
                    scanner.getPath(), (int) scanner.getLine(),
                    null), String.class)
        );
      return output.toString();
    }
  };
  private static RhinoPageDriver impl;
  /**
   * <p>The global JavaScript object/environment</p>
   * <br>
   */
  private Global global;

  /**
   * <p>Creates a new page-driver with the specified script environment</p>
   * <br>
   *
   * @param global the global JavaScript environment to use for this page-driver
   */
  public RhinoPageDriver(Global global) {
    this.global = global;
  }

  /**
   * <p>Creates a new rhino page driver with any system or environment specified
   * REQUIRE.JS modules.</p>
   *
   * @see RhinoPageDriver#RhinoPageDriver(List)
   */
  public RhinoPageDriver() {this((List) null);}

  /**
   * <p>Creates a new rhino page driver with the specified REQUIRE.JS module paths</p>
   * <br>
   * <p>The currrent design and testing model of this class is for a single instance to support
   * multiple requests. Other usages are of a foreign nature with respect to this
   * documentation.
   * </p>
   *
   * @param moduleURIs a list of modules and directories to initialize the REQUIRE.JS module with. <br><br><b>Default Values:</b> the value of the system property identified by: {@link #REQUIRE_MODULE_URIS_PROPERTY} or the value of the system environment variable identified by: {@link #REQUIRE_MODULE_URIS_VAR} if this parameter is null. If this parameter is not provided or resolved, it is ignored.
   * @see #URI_LIST_SPLITTER
   * @see #URI_LIST_SPLITTER_PROPERTY
   * @see #URI_LIST_SPLITTER_VAR
   */
  public RhinoPageDriver(@Nullable List<String> moduleURIs) {
    Context cx = Context.enter();
    global = Main.global;
    global.init(cx);
    if (moduleURIs == null) {
      String modulePath = Tools.switchNull(
          System.getProperty(REQUIRE_MODULE_URIS_PROPERTY),
          System.getenv(REQUIRE_MODULE_URIS_VAR));
      if (modulePath != null)
        global.installRequire(cx, Arrays.asList(modulePath.split(URI_LIST_SPLITTER)), false);
      else
        global.installRequire(cx, null, false);
    } else global.installRequire(cx, moduleURIs, false);
    Context.exit();
  }

  // support a bare-bones-global-system-runtime-configuration
  public final static RhinoPageDriver getInstance() {
    if (impl != null) return impl;
    impl = new RhinoPageDriver();
    return impl;
  }

  /**
   * <p>Called by media map controllers to integrate media map control configuration</p>
   * <br>
   *
   * @param controlPort
   */
  @Override
  public void configureMimeTypeController(MimeTypeMap controlPort) {
    controlPort.putIfAbsent("jsp", NANO_STARBOX_JAVASCRIPT_SERVER_PAGE);
  }

  /**
   * <p>Provides access to the global scripting environment</p>
   * <br>
   *
   * @return
   */
  public Global getGlobal() { return global; }

  private Scriptable getScriptShell(Context cx, @Nullable Scriptable parent) {
    Scriptable o = ScriptRuntime.newObject(cx, Tools.switchNull(parent, global), "Object", null);
    ScriptRuntime.setObjectProp(o, "global", global, cx);
    return o;
  }

  @Override
  public ServerResult createMimeTypeResult(ServerContent content) {
    Context cx = Context.enter();
    cx.setOptimizationLevel(-1);
    try {
      Scriptable jsThis = getScriptShell(cx, global);
      InputStream sourceStream = content.getStream();
      Scanner scanner = new Scanner(content.session.getUri(), Streams.readWholeString(sourceStream));
      sourceStream.close();
      MacroShell documentBuilder = new MacroShell(System.getenv());
      ScriptRuntime.setObjectProp(jsThis, "directory", Context.javaToJS(content.getDirectory(), jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "content", Context.javaToJS(content, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "server", Context.javaToJS(content.session.getServer(), jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "session", Context.javaToJS(content.session, jsThis), cx);
      ScriptRuntime.setObjectProp(jsThis, "shell", Context.javaToJS(documentBuilder, jsThis), cx);
      documentBuilder.objects.put("this", jsThis);
      documentBuilder.objects.put("directory", content.getDirectory());
      documentBuilder.addCommand("*", starCommand);
      documentBuilder.addCommand("src", srcCommand);
      documentBuilder.addCommand("do", doCommand);
      documentBuilder.addCommand("val", valCommand);
      if (content.mimeType.equals(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE)) content.mimeType = MIME_HTML;
      return new ServerResult(content.session, Status.OK, content.mimeType, documentBuilder.start(scanner));
    }
    catch (Exception e) {throw new RuntimeException(e);}
    finally {
      Context.exit();
    }
  }

  private boolean detectHtmlDocument(BufferedInputStream source) throws IOException {
    String scan;
    int SEEK = HTML_MAGIC.length();
    source.mark(SEEK);
    scan = new Scanner(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE, source).nextFieldLength(SEEK, '>');
    source.reset();
    if ((scan + '>').equals(HTML_MAGIC)) {
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
        return NANO_STARBOX_JAVASCRIPT_SERVER_PAGE;
      else return null;
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }

  @Override
  public void configureIndexFileList(HashSet<String> indexFiles) {
    indexFiles.add("index.jsp");
  }

}
