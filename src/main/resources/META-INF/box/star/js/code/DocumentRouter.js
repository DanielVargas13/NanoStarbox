
var Server,
    Status = Packages.box.star.io.protocols.http.response.Status,
    Response = Packages.box.star.io.protocols.http.response.Response,
    DocumentRouter = Packages.box.star.io.web.DocumentRouter,
    Template = Packages.box.star.io.web.Template
;

var JS_FILE_TYPE = "application/javascript",
    JS_DOCUMENT_RESPONSE_MIME_TYPE = "javascript/x-nano-starbox-document-router-response";

function getDirectoryIndex(file) {
  var fx, type = Settings["site-index-type"];
  for (var i = 0; i < type.length; i++) {
    fx = new File(file, "index" + type[i]);
    if (fx.exists()) return fx;
  }
  return file;
}

function createTimerTask(run) {
    return new JavaAdapter(Packages.box.star.js.TimerTask, {
        run: run
    });
}

const templateMimeTypeKey = "template-mime-type";

var Settings = {
    site: ".", host: "localhost", port: 8080,
    "site-index-type": [".htm", ".html", ".js"],
    "template-mime-type": [JS_FILE_TYPE]
};

function addTemplateMimeType(mimeType) {
    if (Settings[templateMimeTypeKey].includes(mimeType)) return;
    Settings[templateMimeTypeKey].push(mimeType);
}

Object.defineProperty(Settings, "os", {value: System.getProperty("os.name")});

function main(parameters) {
    
  loadParameterObject(Settings, parameters);

  Settings.site = new File(Settings.site);

  Server = new DocumentRouter(Settings.site, Settings.host, Settings.port);

  Settings.ssl = Server.makeSecure(new File(Settings.keystore),
    Settings["keystore-password"]
  );

  Server.setControl({// SessionController

    loadUserSession: function(session) {},
    deleteUserSession: function(session) {},
    createUserSession: function(query, session) {},

    }, {// HostController

    stop: function onStopServer(){},

    blackListClient: function(host, ip, session){return false;},

    blackListUri: function (uri) {return false;},

    locateFile: function (uri) {
      return new File(Settings.site, uri.substring(1));
    },

    handleFile: function handleFile(document, mimeType, method, query, session) {
      if (document.exists()) {
        if (document.isDirectory()) {
          // Resolve index document
          var index = getDirectoryIndex(document);
          // if that returns a directory forbid the request
          if (index.isDirectory()) return Server.ForbiddenResponse();
          // reconfigure and continue processing
          document = index, mimeType = Server.uriMimeType(index);
        }

        var magic = Server.mimeTypeMagic(document);
        // check: active-document-extensions
        if (magic.equals(JS_DOCUMENT_RESPONSE_MIME_TYPE)) {
              // load: document-generator
              var script = loadObject({
                Session: session,
                scriptFile: document,
                scriptDirectory: new File(document).getParent()
              }, document);
              // generate: document-request-content
              return script.response(method, query);
        }

        if (magic.startsWith("text") || Settings[templateMimeTypeKey].includes(magic)) {
            var obj = {
              Session: session,
              scriptFile: document,
              scriptDirectory: new File(document).getParent()
            }
            var template = getTemplate(document);
            var documentPath = document.getPath();
            return Response.newFixedLengthResponse(Status.OK, mimeType, template.parse({
                replace: function (data, record){
                    return evalObject(obj, data, documentPath, record.line).result;
                }
            }));
        }
        // serve: static-document-content
        var stream = new FileInputStream(document);
        return Response.newFixedLengthResponse(Status.OK, mimeType, stream, document.length());
      }
      // fail: 404 (document does not exist)
      return Server.NotFoundResponse();
    }

  });

    (function templateCache(global){

    /*
        Templates require text-pre-processing, so we cache them for latency.
        Templates older than 10 minutes, are automatically unloaded.

    */

        var templateCache = {};
        var oneMinute = 1000 * 60;
        var tenMinutes = oneMinute * 10;

        global.getTemplate = sync(function getTemplate(path) {

            var file = path.getAbsoluteFile();
            var data = templateCache[file.getPath()];
            var stamp = Date.now();
            if (data != undefined) {
                if (file.exists() && file.lastModified() > data.lastDiskAccess) {
                    data.template = new Template(file);
                    data.lastDiskAccess = stamp;
                }
            } else {
                data = (templateCache[file.getPath()] = {
                    lastDiskAccess: stamp, template: new Template(file)
                })
            }
            data.lastReadAccess = stamp;
            return data.template;

        }, templateCache);

        trimTemplateCache = sync(function trimTemplateCache(age){
            var path, data, now = Date.now();
            for (path in templateCache) {
                data = templateCache[path];
                if (now - data.lastReadAccess > age) unloadTemplate(path);
            }
        }, templateCache);

        function unloadTemplate(path) {
            delete templateCache[path.getAbsolutePath()];
        }

        Server.startTimerTask(
            createTimerTask(function(){trimTemplateCache(tenMinutes)}),
            oneMinute
         );

    })(this);

  Server.start(DocumentRouter.SOCKET_READ_TIMEOUT, false);

  help();
  System.out.println(((Settings.ssl)?"Browse: https://":"Browse: http://") + Server.getHost() + ":" + Server.getPort() + "\n");

  enterConsole();
  Server.stop();

}