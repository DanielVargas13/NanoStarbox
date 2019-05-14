//->mime-type: javascript/x-nano-starbox-rhino-servlet

var Status = Net.http.response.Status;
var ByteArrayOutputStream = java.io.ByteArrayOutputStream;

function generateServiceResponse(uri, stream, mimeType, httpSession) {
    var captureStream = new ByteArrayOutputStream();
    var scriptDirectory = new java.io.File(httpSession.getUri().substring(1))
    if (scriptDirectory == "") scriptDirectory = ".";
    if (System.getProperty("os.name").startsWith("Windows")) new Starbox.Command("cmd", "/c")
        .setDirectory(scriptDirectory)
            .writeOutputTo(captureStream)
                .writeErrorTo(captureStream)
                    .start("dir");
    else { // assume: unix-shell
        new NanoStarbox.Command("sh", "-c")
            .setDirectory(scriptDirectory)
                .writeOutputTo(captureStream)
                    .writeErrorTo(captureStream)
                        .start("ls");
    }
    return server.plainTextResponse(Status.OK, captureStream.toString());
};
