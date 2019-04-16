var WebServer = Packages.box.star.WebServer;
var Response = Packages.box.star.io.protocols.http.response.Response;
var Status = Packages.box.star.io.protocols.http.response.Status;
var Template = Packages.box.star.util.Template;

var
    NanoStarbox = Packages.box.star,
    File = java.io.File,
    FileInputStream = java.io.FileInputStream,
    FileOutputStream = java.io.FileOutputStream,
    FileReader = java.io.FileReader,
    FileWriter = java.io.FileWriter,
    BufferedReader = java.io.BufferedReader,
    BufferedWriter = java.io.BufferedWriter,
    ByteArrayInputStream = java.io.ByteArrayInputStream,
    ByteArrayOutputStream = java.io.ByteArrayOutputStream,
    System = java.lang.System
;

var templateMimes = ["text/html"];

    (function templateCache(global){

    /*
        Templates require text-pre-processing, so we cache them for latency.
        Templates older than 10 minutes, are automatically unloaded.

    */

        var templateCache = {};
        var oneMinute = 1000 * 60;
        var tenMinutes = oneMinute * 10;

        global.getTemplate = sync(function getTemplate(path) {

            var file = path;
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

        function unloadTemplate(path) {
            delete templateCache[path.getAbsolutePath()];
        }

    })(this);

var server = new JavaAdapter(WebServer, {

blackListDirectory: function(file) {
  return false;
},

serveFile: function(file, mimeType, httpSession) {

    var magic = server.getMimeTypeResponse(file, mimeType, httpSession);
    if (magic != null) return magic;

    if (templateMimes.indexOf(""+mimeType) != -1) {
        var obj = {
          httpSession: httpSession,
          template: file,
          path: file.getParent()
        }
        var template = getTemplate(file);
        var documentPath = file.getPath();
        return Response.newFixedLengthResponse(Status.OK, mimeType, template.fill({
            replace: function (data, record){
                return evalObject(obj, data, documentPath, record.line);
            }
        }));
    }

    return server.staticFileResponse(file, mimeType, httpSession);

}

}, "localhost", 8080);

server.setDocumentRoot("sample/site");
server.staticIndexFiles.add("index.js");

server.registerMimeTypeDriver("javascript/x-nano-starbox-servlet",
   new JavaAdapter(Packages.box.star.WebServer.MimeTypeDriver, {
       generateServiceResponse: function(server, file, mimeType, httpSession) {
           var servlet = {};
           loadObjectScript(servlet, file);
           return servlet.response(server, file, mimeType, httpSession);
       }
   })
);

server.makeSecure(new File("sample/Starbox.jks"), "Starbox");

server.start();

function reload() {
    server.stop();
    load("sample/debug-webserver.js");
}
