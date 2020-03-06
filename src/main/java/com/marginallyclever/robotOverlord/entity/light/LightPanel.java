package com.marginallyclever.robotOverlord.entity.light;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectColorRGBA;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectComboBox;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * Context sensitive GUI for the camera 
 * @author Dan Royer
 *
 */
public class LightPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8529190569816182683L;
	
	private Light light;
	private UserCommandSelectBoolean chooseEnabled;
	private UserCommandSelectComboBox choosePreset;
	private UserCommandSelectColorRGBA chooseDiffuse;
	private UserCommandSelectColorRGBA chooseAmbient;
	private UserCommandSelectColorRGBA chooseSpecular;

	private ColorRGB [] presetBlack = {
		new ColorRGB(0,0,0),  // ambient
		new ColorRGB(0,0,0),  // specular
		new ColorRGB(0,0,0),  // diffuse
	};
	
	private ColorRGB [] presetNoon = {
			new ColorRGB(   0,   0,   0),
			new ColorRGB( 255, 255, 251),
			new ColorRGB(   1,   1,   1),
		};
		
	private ColorRGB [] presetMetalHalide = {
			new ColorRGB(   0,   0,   0),
			new ColorRGB( 242, 252, 255),
			new ColorRGB(   0,   0,   0),
		};
		
	private String [] presetNames = {
			"custom/unknown",
			"Noon",
			"Metal halide",
			"Black",
	};
	
	/*
	 * TODO move this to a superclass?
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}*/

	public LightPanel(RobotOverlord gui,Light arg0) {
		super();
		
		light=arg0;

		this.setName("Light");
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		this.add(chooseEnabled = new UserCommandSelectBoolean(gui,Translator.get("On"),light.getEnabled()),con1);

		con1.gridy++;
		this.add(choosePreset = new UserCommandSelectComboBox(gui,Translator.get("Preset"),presetNames,detectPreset(light)),con1);
		
		con1.gridy++;
		this.add(chooseAmbient = new UserCommandSelectColorRGBA(gui,Translator.get("Ambient"),light.getAmbient()),con1);
		
		con1.gridy++;
		this.add(chooseSpecular = new UserCommandSelectColorRGBA(gui,Translator.get("Specular"),light.getSpecular()),con1);
		
		con1.gridy++;
		this.add(chooseDiffuse = new UserCommandSelectColorRGBA(gui,Translator.get("Diffuse"),light.getDiffuse()),con1);

		PanelHelper.ExpandLastChild(this, con1);
		chooseEnabled.addChangeListener(this);
		chooseDiffuse.addChangeListener(this);
		chooseAmbient.addChangeListener(this);
		chooseSpecular.addChangeListener(this);
	}

	public int detectPreset(Light light) {
		//TODO finish me
		return 0;
	}
	
	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
		choosePreset.setIndex(detectPreset(light));
		chooseEnabled.setValue(light.getEnabled());
		chooseDiffuse.setValue(light.getDiffuse());
		chooseAmbient.setValue(light.getAmbient());
		chooseSpecular.setValue(light.getSpecular());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source ==choosePreset) {
			int i = choosePreset.getIndex();
			if(i!=0) {
				ColorRGB [] choice;
				switch(i) {
				case 1: choice = presetNoon;		break;
				case 2:	choice = presetMetalHalide; break;
				case 3: choice = presetBlack;		break;
				default: choice=null;
				}
				if(choice!=null) {
					ColorRGB c;
					c= choice[0];	light.setAmbient (c.red/255, c.green/255, c.blue/255, 1);
					c= choice[1];	light.setSpecular(c.red/255, c.green/255, c.blue/255, 1);
					c= choice[2];	light.setDiffuse (c.red/255, c.green/255, c.blue/255, 1);
				}
			}
		}
		if(source==chooseEnabled) {
			light.setEnable(chooseEnabled.getValue());
		}
		if(source==chooseDiffuse) {
			float[] v = chooseDiffuse.getValue();
			light.setDiffuse(v[0],v[1],v[2],v[3]);
		}
		if(source==chooseAmbient) {
			float[] v = chooseAmbient.getValue();
			light.setAmbient(v[0],v[1],v[2],v[3]);
		}
		if(source==chooseSpecular) {
			float[] v = chooseSpecular.getValue();
			light.setSpecular(v[0],v[1],v[2],v[3]);
		}
	}
}
