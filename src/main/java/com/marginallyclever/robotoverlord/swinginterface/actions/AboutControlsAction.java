package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Display an About dialog box. This action is not an undoable action.
 * @author Admin
 *
 */
public class AboutControlsAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AboutControlsAction() {
		super(Translator.get("Controls"));
        putValue(SHORT_DESCRIPTION, Translator.get("About controls"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String body = "<h1>Controls</h1>";
		body+="<h2>Flying</h2>";
		body+="<p>Pan + Tilt Camera  --  M-Mouse  --  RS</p>";
		body+="<p>Fly Left, Right, Up + Down  --  Shift + M-Mouse  --  LS</p>";
		body+="<p>Fly Forward + Back  --  Ctrl + M-Mouse  --  L2/R2</p>";
		body+="<h2>Selecting [entity?? TODO*]</h2>";
		body+="<p>Double click anything in the world to see its menu.</p>";
		body+="<p>Double click again or press escape to unselect.</p>";
		body+="<h2>Entity/Object</h2>";
		body+="<p>**Any Entity/Object in the simulation can be moved when selected according to current <i>frame of reference</i>.**</p>";
		body+="<p>**The <i>End-Effector<i> must be selected in the Robot Entity Tree in order to move and perform IK.**</p>";
		body+="<p>LMB: Select the axis you wish to translate along and drag.</p>";
		body+="<p>Shift + LMB: Select the circular plane you wish to rotate and drag.</p>";
		body+="<p>'X' + LS: Translate along X and Y axis.</p>";
		body+="<p>'X' + L2/R2: Translate along Z axis.</p>";
		body+="<p>'O' + LS: Rotate around X and Y axis.</p>";
		body+="<p>'O' + L2/R2: Rotate around Z axis.</p>";

		body = "<html><body>"+body+"</body></html>";
		
		JOptionPane.showMessageDialog(null,body);
	}
}
