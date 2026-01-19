package com.marginallyclever.convenience.helpers;

import java.awt.*;
import java.awt.datatransfer.Clipboard;

public class ClipboardHelper {
    private static Clipboard clipboard = null;
    public static Clipboard getClipboard() {
        if( clipboard == null ) {
            if (GraphicsEnvironment.isHeadless()) {
                // in headless mode, use a dummy clipboard.
                return new Clipboard("headless");
            } else {
                // normal clipboard
                return Toolkit.getDefaultToolkit().getSystemClipboard();
            }
        }
        return clipboard;
    }
}
