package com.marginallyclever.convenience.swing;

import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

public class NumberFormatHelper {
    static public NumberFormatter getNumberFormatter() {
        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
    }
}
