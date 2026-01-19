package com.marginallyclever.convenience.helpers;

import java.awt.*;
import java.awt.datatransfer.Clipboard;

public class ClipboardHelper {
    private static Clipboard clipboard = null;

    public static Clipboard getClipboard() {
        if( clipboard == null ) {
            if (GraphicsEnvironment.isHeadless()) {
                // in headless mode, use a dummy clipboard.
                clipboard = new Clipboard("headless");
            } else {
                // normal clipboard
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
        }
        return clipboard;
    }
}
