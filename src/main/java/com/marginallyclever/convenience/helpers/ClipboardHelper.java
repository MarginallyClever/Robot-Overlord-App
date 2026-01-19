package com.marginallyclever.convenience.helpers;

import java.awt.*;
import java.awt.datatransfer.Clipboard;

public class ClipboardHelper {
    public static Clipboard getClipboard() {
        // if headless,
        if (GraphicsEnvironment.isHeadless()) {
            return new Clipboard("headless");
        } else {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }
    }
}
