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

var server = new JavaAdapter(WebServer, {

serveFile: function(file, mimeType, httpSession) {

    var magic = server.getMimeTypeResponse(file, mimeType, httpSession);
    if (magic != null) return magic;

    if (server.isTemplateMimeType(mimeType)) {
        var obj = {
          httpSession: httpSession,
          template: file,
          path: file.getParent()
        }
        var template = server.getTemplate(file);
        var documentPath = file.getPath();
        return server.stringResponse(Status.OK, mimeType, template.fill({
            replace: function (data, record){
                return evalObject(obj, data, documentPath, record.line);
            }
        }));
    }

    return server.staticFileResponse(file, mimeType, httpSession);

}

});

server.setDocumentRoot("sample/site");
server.addStaticIndexFile("index.js");

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

server.start("localhost", 8080);

function reload() {
    server.stop();
    load("sample/debug-webserver.js");
}
