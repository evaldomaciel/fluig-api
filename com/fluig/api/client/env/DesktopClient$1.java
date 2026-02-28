/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.oauth.OAuthAccessor
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.CloseListener;
import net.oauth.OAuthAccessor;

class DesktopClient.1
extends CloseListener {
    DesktopClient.1() {
    }

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
}
