package box.star.web.http.response;

import box.star.web.http.IHTTPSession;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class BasicAuthenticationResponse extends Response {

  BasicAuthenticationResponse(IStatus status, String mimeType) {
    super(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
  }

  public BasicAuthenticationResponse(String title, String realm) {
    this(Status.UNAUTHORIZED, "text/plain");
    addHeader("WWW-Authenticate", "Basic title=\"" + title + "\", realm=\"" + realm + "\", charset=\"UTF-8\"");
    setStatus(Status.UNAUTHORIZED);
  }

  public static String getLoginCredentials(IHTTPSession ihttpSession) {
    Map<String, String> header = ihttpSession.getHeaders();
    if (header.containsKey("authorization")) {
      String authorization = header.get("authorization");
      if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
        // Authorization: Basic base64credentials
        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        return new String(credDecoded, StandardCharsets.UTF_8);
      }
    }
    return null;
  }

}
