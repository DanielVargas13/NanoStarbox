package box.star.net.tools;

import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import static box.star.net.http.HTTPServer.MIME_PLAINTEXT;

public class ServerResult extends ServerContent {

  public ServerResult(ServerContent content) {
    this.session = content.session;
    this.data = content.data;
    this.mimeType = content.mimeType;
    this.length = content.length;
    this.lastModified = content.lastModified;
    if (content.status == null) {
      if (content.data != null) status = Status.OK;
      else status = Status.NO_CONTENT;
    } else {
      status = content.status;
    }
  }

  public ServerResult(IHTTPSession session, Status status, String mimeType, String string) {
    this.status = status;
    this.mimeType = mimeType;
    this.data = string;
    this.session = session;
  }

  public ServerResult(IHTTPSession session, Status status, String string) {
    this(session, status, MIME_PLAINTEXT, string);
  }

  public Response getResponse() {

    if (status == Status.NOT_FOUND) { return Response.notFoundResponse(); } else if (data instanceof Response) {
      return (Response) data;
    } else if (data instanceof String) {
      String output = (String) data;
      return Response.newFixedLengthResponse(status, mimeType, output);
    } else if (data instanceof BufferedInputStream) {
      if (length > 0) {
        return Response.newFixedLengthResponse(status, mimeType, (BufferedInputStream) data, length);
      } else {
        return Response.newChunkedResponse(status, mimeType, (BufferedInputStream) data);
      }
    } else if (data instanceof byte[]) {
      return Response.newFixedLengthResponse(status, mimeType, (byte[]) data);
    } else if (data instanceof File) {
      try {
        return Response.newFixedFileResponse(mimeType, (File) data);
      }
      catch (FileNotFoundException e) {
        // this should not be happening.
        throw new RuntimeException(e);
      }
    }

    throw new RuntimeException("unknown mime type request object: " + data.getClass());

  }

}
