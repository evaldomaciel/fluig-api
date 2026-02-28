/*
 * Decompiled with CFR 0.152.
 */
package com.fluig.api.client.env;

import com.fluig.api.client.env.BrowserException;
import com.fluig.api.client.env.CloseListener;
import com.fluig.api.client.env.IBrowserLauncher;
import java.lang.reflect.Method;
import javax.swing.JOptionPane;

public class BareBonesBrowserLaunch
implements IBrowserLauncher {
    @Override
    public void openURL(String url) {
        try {
            this.browse(url);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n" + e.getLocalizedMessage());
        }
    }

    @Override
    public void browse(String url) throws BrowserException {
        try {
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                String[] browsers = new String[]{"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; ++count) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() != 0) continue;
                    browser = browsers[count];
                }
                if (browser == null) {
                    throw new NoSuchMethodException("Could not find web browser");
                }
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }
        catch (Exception ex) {
            throw new BrowserException(ex);
        }
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void forceClose() {
    }

    @Override
    public void setCloseListener(CloseListener listener) {
    }
}
