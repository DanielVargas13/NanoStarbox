package box.star.net.sock;

/*
 * #%L
 * NanoHttpd-Websocket
 * %%
 * Copyright (C) 2012 - 2016 nanohttpd
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
 * 3. Neither the name of the nanohttpd nor the names of its contributors
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Logger;

import box.star.net.http.IHTTPSession;
import box.star.net.http.HTTPServer;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;
import box.star.net.http.util.IHandler;

//public abstract class NanoWSD
public abstract class WebSocketServer extends HTTPServer {

    /**
     * logger to log to.
     */
    public static final Logger LOG = Logger.getLogger(WebSocketServer.class.getName());

    public static final String HEADER_UPGRADE = "upgrade";

    public static final String HEADER_UPGRADE_VALUE = "websocket";

    public static final String HEADER_CONNECTION = "connection";

    public static final String HEADER_CONNECTION_VALUE = "Upgrade";

    public static final String HEADER_WEBSOCKET_VERSION = "sec-websocket-version";

    public static final String HEADER_WEBSOCKET_VERSION_VALUE = "13";

    public static final String HEADER_WEBSOCKET_KEY = "sec-websocket-key";

    public static final String HEADER_WEBSOCKET_ACCEPT = "sec-websocket-accept";

    public static final String HEADER_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";

    private final static String WEBSOCKET_KEY_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    /**
     * Translates the specified byte array into Base64 string.
     * <p>
     * Android has android.util.Base64, sun has sun.misc.Base64Encoder, Java 8
     * hast java.util.Base64, I have this from stackoverflow:
     * http://stackoverflow.com/a/4265472
     * </p>
     * 
     * @param buf
     *            the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    private static String encodeBase64(byte[] buf) {
        int size = buf.length;
        char[] ar = new char[(size + 2) / 3 * 4];
        int a = 0;
        int i = 0;
        while (i < size) {
            byte b0 = buf[i++];
            byte b1 = i < size ? buf[i++] : 0;
            byte b2 = i < size ? buf[i++] : 0;

            int mask = 0x3F;
            ar[a++] = WebSocketServer.ALPHABET[b0 >> 2 & mask];
            ar[a++] = WebSocketServer.ALPHABET[(b0 << 4 | (b1 & 0xFF) >> 4) & mask];
            ar[a++] = WebSocketServer.ALPHABET[(b1 << 2 | (b2 & 0xFF) >> 6) & mask];
            ar[a++] = WebSocketServer.ALPHABET[b2 & mask];
        }
        switch (size % 3) {
            case 1:
                ar[--a] = '=';
            case 2:
                ar[--a] = '=';
        }
        return new String(ar);
    }

    public static String makeAcceptKey(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String text = key + WebSocketServer.WEBSOCKET_KEY_MAGIC;
        md.update(text.getBytes(), 0, text.length());
        byte[] sha1hash = md.digest();
        return encodeBase64(sha1hash);
    }

    protected final class Interceptor implements IHandler<IHTTPSession, Response> {

        public Interceptor() {
        }

        @Override
        public Response handle(IHTTPSession input) {
            return handleWebSocket(input);
        }
    }

    public WebSocketServer(int port) {
        super();
        configuration.set(CONFIG_PORT, port);
        addHTTPInterceptor(new Interceptor());
    }

    public WebSocketServer(String hostname, int port) {
        super();
        configuration.set(CONFIG_HOST, hostname);
        configuration.set(CONFIG_PORT, port);
        addHTTPInterceptor(new Interceptor());
    }

    private boolean isWebSocketConnectionHeader(Map<String, String> headers) {
        String connection = headers.get(WebSocketServer.HEADER_CONNECTION);
        return connection != null && connection.toLowerCase().contains(WebSocketServer.HEADER_CONNECTION_VALUE.toLowerCase());
    }

    protected boolean isWebsocketRequested(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String upgrade = headers.get(WebSocketServer.HEADER_UPGRADE);
        boolean isCorrectConnection = isWebSocketConnectionHeader(headers);
        boolean isUpgrade = WebSocketServer.HEADER_UPGRADE_VALUE.equalsIgnoreCase(upgrade);
        return isUpgrade && isCorrectConnection;
    }

    // --------------------------------Listener--------------------------------

    protected abstract WebSocket openWebSocket(IHTTPSession handshake);

    public Response handleWebSocket(final IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        if (isWebsocketRequested(session)) {
            if (!WebSocketServer.HEADER_WEBSOCKET_VERSION_VALUE.equalsIgnoreCase(headers.get(WebSocketServer.HEADER_WEBSOCKET_VERSION))) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, HTTPServer.MIME_PLAINTEXT,
                        "Invalid Websocket-Version " + headers.get(WebSocketServer.HEADER_WEBSOCKET_VERSION));
            }

            if (!headers.containsKey(WebSocketServer.HEADER_WEBSOCKET_KEY)) {
                return Response.newFixedLengthResponse(Status.BAD_REQUEST, HTTPServer.MIME_PLAINTEXT, "Missing Websocket-Key");
            }

            WebSocket webSocket = openWebSocket(session);
            Response handshakeResponse = webSocket.getHandshakeResponse();
            try {
                handshakeResponse.addHeader(WebSocketServer.HEADER_WEBSOCKET_ACCEPT, makeAcceptKey(headers.get(WebSocketServer.HEADER_WEBSOCKET_KEY)));
            } catch (NoSuchAlgorithmException e) {
                return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, HTTPServer.MIME_PLAINTEXT,
                        "The SHA-1 Algorithm required for websockets is not available on the server.");
            }

            if (headers.containsKey(WebSocketServer.HEADER_WEBSOCKET_PROTOCOL)) {
                handshakeResponse.addHeader(WebSocketServer.HEADER_WEBSOCKET_PROTOCOL, headers.get(WebSocketServer.HEADER_WEBSOCKET_PROTOCOL).split(",")[0]);
            }

            return handshakeResponse;
        } else {
            return null;
        }
    }
}