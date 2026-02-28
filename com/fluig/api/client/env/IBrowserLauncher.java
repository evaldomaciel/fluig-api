/*
 * Decompiled with CFR 0.152.
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.BrowserException;
import com.fluig.api.client.env.CloseListener;

public interface IBrowserLauncher {
    public void openURL(String var1);

    public void browse(String var1) throws BrowserException;

    public boolean isOpen();

    public void forceClose();

    public void setCloseListener(CloseListener var1);
}
