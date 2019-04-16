//->mime-type: javascript/x-nano-starbox-servlet

function response(server, file, mimeType, httpSession) {
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
    return Response.newFixedLengthResponse(Status.OK, WebServer.MIME_PLAINTEXT,  captureStream.toString());
};
