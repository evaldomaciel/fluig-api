/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.mortbay.jetty.Request
 *  org.mortbay.jetty.handler.AbstractHandler
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.DesktopClient;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

protected static class DesktopClient.Callback
extends AbstractHandler {
    protected final DesktopClient client;

    protected DesktopClient.Callback(DesktopClient client) {
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
