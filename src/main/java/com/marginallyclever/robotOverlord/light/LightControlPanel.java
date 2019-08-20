package com.marginallyclever.robotOverlord.light;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectColorRGBA;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * Context sensitive GUI for the camera 
 * @author Dan Royer
 *
 */
public class LightControlPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8529190569816182683L;
	
	private Light light;
	private UserCommandSelectColorRGBA chooseDiffuse;
	private UserCommandSelectColorRGBA chooseAmbient;
	private UserCommandSelectColorRGBA chooseSpecular;

	/*
	 * TODO move this to a superclass?
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}*/

	public LightControlPanel(RobotOverlord gui,Light arg0) {
		super();
		
		light=arg0;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Light");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		c.gridy++;
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;

		contents.add(chooseDiffuse = new UserCommandSelectColorRGBA(gui,Translator.get("Diffuse"),light.getDiffuseColor()),c);
		c.gridy++;
		contents.add(chooseAmbient = new UserCommandSelectColorRGBA(gui,Translator.get("Ambient"),light.getAmbientColor()),c);
		c.gridy++;
		contents.add(chooseSpecular = new UserCommandSelectColorRGBA(gui,Translator.get("Specular"),light.getSpecular()),c);
		c.gridy++;

		chooseDiffuse.addChangeListener(this);
		chooseAmbient.addChangeListener(this);
		chooseSpecular.addChangeListener(this);
	}

	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {		
		chooseDiffuse.setValue(light.getDiffuseColor());
		chooseAmbient.setValue(light.getAmbientColor());
		chooseSpecular.setValue(light.getSpecular());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==chooseDiffuse) {
			float[] v = chooseDiffuse.getValue();
			light.setDiffuse(v[0],v[1],v[2],v[3]);
		}
		if(e.getSource()==chooseAmbient) {
			float[] v = chooseAmbient.getValue();
			light.setAmbient(v[0],v[1],v[2],v[3]);
		}
		if(e.getSource()==chooseSpecular) {
			float[] v = chooseSpecular.getValue();
			light.setSpecular(v[0],v[1],v[2],v[3]);
		}
	}
}
