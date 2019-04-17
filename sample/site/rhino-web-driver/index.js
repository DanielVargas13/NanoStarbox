//->mime-type: javascript/x-nano-starbox-rhino-servlet

var Status = Packages.box.star.io.protocols.http.response.Status;
var ByteArrayOutputStream = java.io.ByteArrayOutputStream;

function generateServiceResponse(file, mimeType, httpSession) {
    var captureStream = new ByteArrayOutputStream();
    var scriptDirectory = file.getParent();
    if (System.getProperty("os.name").startsWith("Windows")) new NanoStarbox.Command("cmd", "/c")
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
