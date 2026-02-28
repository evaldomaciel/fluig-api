/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.oauth.OAuthMessage
 */
package com.fluig.api.client.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import net.oauth.OAuthMessage;

private static class DesktopClient.InputStreamMessage
extends OAuthMessage {
    private final InputStream body;

    DesktopClient.InputStreamMessage(String method, String url, Collection<? extends Map.Entry> parameters, InputStream body) {
        super(method, url, parameters, null);
        this.body = body;
    }

    public InputStream getBodyAsStream() throws IOException {
        return this.body;
    }
}
