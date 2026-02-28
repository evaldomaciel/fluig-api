/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.oauth.OAuthConsumer
 *  net.oauth.OAuthMessage
 *  net.oauth.OAuthServiceProvider
 *  net.oauth.client.OAuthClient
 *  net.oauth.client.httpclient4.HttpClient4
 *  net.oauth.http.HttpClient
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.DesktopClient;
import com.fluig.api.client.env.IBrowserLauncher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpClient;

public class FluigClient {
    private String host;
    private String consumerKey;
    private String consumerSecret;
    private boolean requireLogin;
    private String tokenAccess;
    private String tokenSecret;
    private DesktopClient client;

    public FluigClient connect() {
        return this.connect(null);
    }

    public FluigClient connect(IBrowserLauncher browserLauncher) {
        this.client = new DesktopClient(new OAuthConsumer(null, this.consumerKey, this.consumerSecret, new OAuthServiceProvider(this.host + "/portal/api/rest/oauth/request_token", this.host + "/portal/api/rest/oauth/authorize?messaging=true&signUp=true", this.host + "/portal/api/rest/oauth/access_token")));
        this.client.setOAuthClient(new OAuthClient((HttpClient)new HttpClient4()));
        if (browserLauncher != null) {
            this.client.setBrowser(browserLauncher);
        }
        if (!this.requireLogin) {
            this.client.accessor.accessToken = this.tokenAccess;
            this.client.accessor.tokenSecret = this.tokenSecret;
        }
        return this;
    }

    public String get(String uri) {
        System.out.println("get:" + uri);
        try {
            OAuthMessage request = this.client.access("GET", this.host + uri, null);
            this.setTokenAccess(this.client.accessor.accessToken);
            this.setTokenSecret(this.client.accessor.tokenSecret);
            return request.readBodyAsString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getAsStream(String uri) {
        System.out.println("get:" + uri);
        try {
            OAuthMessage request = this.client.access("GET", this.host + uri, null);
            this.setTokenAccess(this.client.accessor.accessToken);
            this.setTokenSecret(this.client.accessor.tokenSecret);
            return request.getBodyAsStream();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String post(String uri, String parameters) {
        System.out.println("post:" + uri + " " + parameters);
        try {
            String request = this.client.makeAuthenticatedRequestWithBody(this.host + uri, new ByteArrayInputStream(parameters.getBytes()));
            this.setTokenAccess(this.client.accessor.accessToken);
            this.setTokenSecret(this.client.accessor.tokenSecret);
            return request;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String post(String uri, byte[] bytes, String contentType) {
        try {
            String request = this.client.makeAuthenticatedRequestWithBody(this.host + uri, new ByteArrayInputStream(bytes), contentType);
            this.setTokenAccess(this.client.accessor.accessToken);
            this.setTokenSecret(this.client.accessor.tokenSecret);
            return request;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getHost() {
        return this.host;
    }

    public FluigClient setHost(String host) {
        this.host = host;
        return this;
    }

    public String getConsumerKey() {
        return this.consumerKey;
    }

    public FluigClient setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
        return this;
    }

    public String getConsumerSecret() {
        return this.consumerSecret;
    }

    public FluigClient setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
        return this;
    }

    public boolean isRequireLogin() {
        return this.requireLogin;
    }

    public String getTokenAccess() {
        return this.tokenAccess;
    }

    public FluigClient setTokenAccess(String tokenAccess) {
        this.tokenAccess = tokenAccess;
        this.requireLogin = tokenAccess != null && tokenAccess.isEmpty();
        return this;
    }

    public String getTokenSecret() {
        return this.tokenSecret;
    }

    public FluigClient setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
        return this;
    }
}
