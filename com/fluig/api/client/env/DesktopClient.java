/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.oauth.OAuth
 *  net.oauth.OAuth$Parameter
 *  net.oauth.OAuthAccessor
 *  net.oauth.OAuthConsumer
 *  net.oauth.OAuthException
 *  net.oauth.OAuthMessage
 *  net.oauth.OAuthProblemException
 *  net.oauth.ParameterStyle
 *  net.oauth.client.OAuthClient
 *  net.oauth.client.URLConnectionClient
 *  net.oauth.client.httpclient4.HttpClient4
 *  net.oauth.http.HttpClient
 *  org.mortbay.jetty.Connector
 *  org.mortbay.jetty.Handler
 *  org.mortbay.jetty.Request
 *  org.mortbay.jetty.Server
 *  org.mortbay.jetty.handler.AbstractHandler
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.BareBonesBrowserLaunch;
import com.fluig.api.client.env.CloseListener;
import com.fluig.api.client.env.IBrowserLauncher;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpClient;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class DesktopClient {
    public final OAuthAccessor accessor;
    private IBrowserLauncher browser;
    private int timeoutInSeconds;
    private String verifier = null;
    private OAuthClient oauthClient = DEFAULT_CLIENT;
    private static final OAuthClient DEFAULT_CLIENT = new OAuthClient((HttpClient)new URLConnectionClient());
    private static final String CALLBACK_PATH = "/oauth/callback";

    public DesktopClient(OAuthConsumer consumer) {
        this.accessor = new OAuthAccessor(consumer);
        this.browser = new BareBonesBrowserLaunch();
        this.timeoutInSeconds = 180;
    }

    public OAuthClient getOAuthClient() {
        return this.oauthClient;
    }

    public void setOAuthClient(OAuthClient client) {
        this.oauthClient = client;
    }

    public void setBrowser(IBrowserLauncher browser) {
        this.browser = browser;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OAuthMessage access(String httpMethod, String resourceURL, Collection<? extends Map.Entry> parameters) throws Exception {
        try {
            Server server = null;
            try {
                OAuthAccessor oAuthAccessor = this.accessor;
                synchronized (oAuthAccessor) {
                    List callback = null;
                    while (this.accessor.accessToken == null) {
                        if (server == null) {
                            int callbackPort = DesktopClient.getEphemeralPort();
                            server = new Server(callbackPort);
                            for (Connector c : server.getConnectors()) {
                                c.setHost("localhost");
                            }
                            server.setHandler(this.newCallback());
                            server.start();
                            callback = OAuth.newList((String[])new String[]{"oauth_callback", "http://localhost:" + callbackPort + CALLBACK_PATH});
                        }
                        OAuthMessage response = this.getOAuthClient().getRequestTokenResponse(this.accessor, null, callback);
                        String authorizationURL = OAuth.addParameters((String)this.accessor.consumer.serviceProvider.userAuthorizationURL, (String[])new String[]{"oauth_token", this.accessor.requestToken});
                        if (response.getParameter("oauth_callback_confirmed") == null) {
                            authorizationURL = OAuth.addParameters((String)authorizationURL, callback);
                        }
                        CloseListener listener = new CloseListener(){

                            /*
                             * WARNING - Removed try catching itself - possible behaviour change.
                             */
                            @Override
                            public void browserClosed() {
                                OAuthAccessor oAuthAccessor = DesktopClient.this.accessor;
                                synchronized (oAuthAccessor) {
                                    DesktopClient.this.accessor.notifyAll();
                                }
                            }
                        };
                        this.browser.setCloseListener(listener);
                        this.browser.browse(authorizationURL);
                        if (this.timeoutInSeconds > 0) {
                            this.accessor.wait(TimeUnit.SECONDS.toMillis(this.timeoutInSeconds));
                        } else {
                            this.accessor.wait();
                        }
                        if (this.accessor.accessToken == null) {
                            this.oauthClient.getAccessToken(this.accessor, null, this.verifier == null ? null : OAuth.newList((String[])new String[]{"oauth_verifier", this.verifier.toString()}));
                            if (this.browser.isOpen()) {
                                this.browser.forceClose();
                            }
                        }
                        this.accessor.notifyAll();
                    }
                }
            }
            finally {
                if (server != null) {
                    try {
                        server.stop();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return this.getOAuthClient().invoke(this.accessor, httpMethod, resourceURL, parameters);
        }
        catch (OAuthProblemException p) {
            Object response;
            StringBuilder msg = new StringBuilder();
            String problem = p.getProblem();
            if (problem != null) {
                msg.append(problem);
            }
            if ((response = p.getParameters().get("HTTP response")) != null) {
                String eol = System.getProperty("line.separator", "\n");
                msg.append(eol).append(response);
            }
            throw new OAuthException(msg.toString(), (Throwable)p);
        }
    }

    private static int getEphemeralPort() throws IOException {
        s.bind(null);
        try (Socket s = new Socket();){
            int n = s.getLocalPort();
            return n;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void proceed(String requestToken, String verifier) {
        OAuthAccessor oAuthAccessor = this.accessor;
        synchronized (oAuthAccessor) {
            if (requestToken == null || requestToken.equals(this.accessor.requestToken)) {
                this.verifier = verifier;
                this.accessor.notifyAll();
                return;
            }
        }
        System.err.println("ignored authorization of request token " + requestToken);
    }

    protected Handler newCallback() {
        return new Callback(this);
    }

    public String makeAuthenticatedRequestWithContentType(String contentType, String httpMethod, String resourceURL, Collection<? extends Map.Entry> parameters) throws IOException, OAuthException, URISyntaxException {
        OAuthClient client = new OAuthClient((HttpClient)new HttpClient4());
        OAuthMessage response = this.invoke(contentType, this.accessor, httpMethod, resourceURL, parameters, client);
        return response.readBodyAsString();
    }

    public OAuthMessage invoke(String contentType, OAuthAccessor accessor, String httpMethod, String url, Collection<? extends Map.Entry> parameters, OAuthClient client) throws IOException, OAuthException, URISyntaxException {
        OAuthMessage request = this.newRequestMessage(httpMethod, url, parameters, accessor);
        Object accepted = accessor.consumer.getProperty("HTTP.header.Accept-Encoding");
        if (accepted != null) {
            request.getHeaders().add(new OAuth.Parameter("Accept-Encoding", accepted.toString()));
        }
        request.getHeaders().add(new OAuth.Parameter("Content-Type", contentType));
        return client.invoke(request, ParameterStyle.AUTHORIZATION_HEADER);
    }

    private OAuthMessage newRequestMessage(String method, String url, Collection<? extends Map.Entry> parameters, OAuthAccessor accessor) throws OAuthException, IOException, URISyntaxException {
        OAuthMessage message = new OAuthMessage(method, url, parameters);
        message.addRequiredParameters(accessor);
        return message;
    }

    public String makeAuthenticatedRequestWithBody(String url, Collection<? extends Map.Entry> parameters, InputStream body, String contentType) {
        try {
            OAuthClient client = new OAuthClient((HttpClient)new HttpClient4());
            OAuthMessage response = this.invoke(this.accessor, "POST", url, parameters, body, client, contentType);
            return response.readBodyAsString();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to make an authenticated request.", e);
        }
    }

    public String makeAuthenticatedRequestWithBody(String url, InputStream body) {
        return this.makeAuthenticatedRequestWithBody(url, Collections.emptySet(), body, null);
    }

    public String makeAuthenticatedRequestWithBody(String url, InputStream body, String contentType) {
        return this.makeAuthenticatedRequestWithBody(url, Collections.emptySet(), body, contentType);
    }

    public OAuthMessage invoke(OAuthAccessor accessor, String httpMethod, String url, Collection<? extends Map.Entry> parameters, InputStream body, OAuthClient client, String contentType) throws IOException, OAuthException, URISyntaxException {
        InputStreamMessage request = this.newRequestMessage(httpMethod, url, parameters, body, accessor);
        Object accepted = accessor.consumer.getProperty("HTTP.header.Accept-Encoding");
        if (accepted != null) {
            request.getHeaders().add(new OAuth.Parameter("Accept-Encoding", accepted.toString()));
        }
        request.getHeaders().add(new OAuth.Parameter("Content-Type", contentType == null ? "application/json" : contentType));
        return client.invoke((OAuthMessage)request, ParameterStyle.AUTHORIZATION_HEADER);
    }

    private InputStreamMessage newRequestMessage(String method, String url, Collection<? extends Map.Entry> parameters, InputStream body, OAuthAccessor accessor) throws OAuthException, IOException, URISyntaxException {
        InputStreamMessage message = new InputStreamMessage(method, url, parameters, body);
        message.addRequiredParameters(accessor);
        return message;
    }

    static {
        try {
            Logger.getLogger("org.mortbay.log").setLevel(Level.WARNING);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            System.setProperty("org.apache.commons.logging.simplelog.log.org.mortbay.log", "warn");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static class InputStreamMessage
    extends OAuthMessage {
        private final InputStream body;

        InputStreamMessage(String method, String url, Collection<? extends Map.Entry> parameters, InputStream body) {
            super(method, url, parameters, null);
            this.body = body;
        }

        public InputStream getBodyAsStream() throws IOException {
            return this.body;
        }
    }

    protected static class Callback
    extends AbstractHandler {
        protected final DesktopClient client;

        protected Callback(DesktopClient client) {
            this.client = client;
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
            if (!DesktopClient.CALLBACK_PATH.equals(target)) {
                response.setStatus(404);
            } else {
                this.conclude(response);
                this.client.proceed(request.getParameter("oauth_token"), request.getParameter("oauth_verifier"));
                ((Request)request).setHandled(true);
            }
        }

        protected void conclude(HttpServletResponse response) throws IOException {
            response.setStatus(200);
            response.setContentType("text/html");
            PrintWriter doc = response.getWriter();
            doc.println("<HTML>");
            doc.println("<body onLoad=\"window.close();\">");
            doc.println("Thank you.  You can close this window now.");
            doc.println("</body>");
            doc.println("</HTML>");
        }
    }
}
