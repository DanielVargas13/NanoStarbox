package box.star.web.http.threading;

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

import box.star.web.http.HTTPClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default threading strategy for HTTPServer.
 * <p/>
 * <p>
 * By default, the server spawns a new Thread for every incoming request. These
 * are set to <i>daemon</i> status, and named according to the request number.
 * The name is useful when profiling the application.
 * </p>
 */
public class DefaultAsyncRunner implements IAsyncRunner {

  private final List<HTTPClient> running = Collections.synchronizedList(new ArrayList<HTTPClient>());
  protected long requestCount;

  /**
   * @return a list with currently running clients.
   */
  public List<HTTPClient> getRunning() {
    return running;
  }

  @Override
  public void closeAll() {
    // copy of the list for concurrency
    for (box.star.web.http.HTTPClient HTTPClient : new ArrayList<HTTPClient>(this.running)) {
      HTTPClient.close();
    }
  }

  @Override
  public void closed(HTTPClient HTTPClient) {
    this.running.remove(HTTPClient);
  }

  @Override
  public void exec(HTTPClient HTTPClient) {
    ++this.requestCount;
    this.running.add(HTTPClient);
    createThread(HTTPClient).start();
  }

  protected Thread createThread(HTTPClient HTTPClient) {
    Thread t = new Thread(HTTPClient);
    t.setDaemon(true);
    t.setName("NanoHttpd Request Processor (#" + this.requestCount + ")");
    return t;
  }
}
