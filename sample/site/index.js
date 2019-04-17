//->mime-type: javascript/x-nano-starbox-servlet

NanoStarbox = Packages.box.star;
var Status = Packages.box.star.io.protocols.http.response.Status;
var ByteArrayInputStream = java.io.ByteArrayInputStream,
    ByteArrayOutputStream = java.io.ByteArrayOutputStream,
    System = java.lang.System
;

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
