package com.marginallyclever.robotoverlord.swinginterface;

import javax.swing.*;
import java.awt.*;

@Deprecated
public class StatusBar extends JLabel {
	static final long serialVersionUID=1;

    /** Creates a new instance of StatusBar */
    public StatusBar() {
        super();
        super.setPreferredSize(new Dimension(100, 16));
        setMessage("Ready");
    }

    public void setMessage(String message) {
        setText(" "+message);        
    }        
}