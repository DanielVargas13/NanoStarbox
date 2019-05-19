package box.star.net.http;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2016 mime-type
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the mime-type nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

/**
 * The runnable that will be used for the main listening thread.
 */
public class HTTPService implements Runnable {

  private final int timeout;
  private HTTPServer httpd;
  private IOException socketBindingException;

  private boolean boundSocketConnection = false;

  public HTTPService(HTTPServer httpd, int timeout) {
    this.httpd = httpd;
    this.timeout = timeout;
  }

  @Override
  public void run() {
    try {
      String host = httpd.configuration.get(HTTPServer.CONFIG_HOST);
      int port = httpd.configuration.get(HTTPServer.CONFIG_PORT);
      httpd.getMyServerSocket().bind(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(port));
      boundSocketConnection = true;
    }
    catch (IOException e) {
      this.socketBindingException = e;
      return;
    }
    do {
      try {
        final Socket finalAccept = httpd.getMyServerSocket().accept();
        if (this.timeout > 0) {
          finalAccept.setSoTimeout(this.timeout);
        }
        final InputStream inputStream = finalAccept.getInputStream();
        httpd.asyncRunner.exec(httpd.createClientHandler(finalAccept, inputStream));
      }
      catch (IOException e) {
        HTTPServer.LOG.log(Level.FINE, "Communication with the client broken", e);
      }
    } while (!httpd.getMyServerSocket().isClosed());
  }

  public IOException getSocketBindingException() {
    return socketBindingException;
  }

  public boolean hasBoundSocketConnection() {
    return boundSocketConnection;
  }

}
