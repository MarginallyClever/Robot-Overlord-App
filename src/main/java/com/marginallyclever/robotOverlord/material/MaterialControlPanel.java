package com.marginallyclever.robotOverlord.material;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectColorRGBA;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.model.ModelLoadAndSave;

/**
 * Context sensitive GUI for the camera 
 * @author Dan Royer
 *
 */
public class MaterialControlPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2339033692396700503L;
	
	private Material mat;
	private UserCommandSelectColorRGBA chooseDiffuse;
	private UserCommandSelectColorRGBA chooseAmbient;
	private UserCommandSelectColorRGBA chooseSpecular;
	private UserCommandSelectNumber chooseShininess;
	private UserCommandSelectFile chooseTexture;
	private UserCommandSelectBoolean chooseIsLit;

	public MaterialControlPanel(RobotOverlord gui, Material arg0) {
		super();
		
		mat=arg0;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Material");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		c.gridy++;
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		
		contents.add(chooseDiffuse = new UserCommandSelectColorRGBA(gui,Translator.get("Diffuse"),mat.getDiffuseColor()),con1);
		con1.gridy++;
		contents.add(chooseAmbient = new UserCommandSelectColorRGBA(gui,Translator.get("Ambient"),mat.getAmbientColor()),con1);
		con1.gridy++;
		contents.add(chooseSpecular = new UserCommandSelectColorRGBA(gui,Translator.get("Specular"),mat.getSpecular()),con1);
		con1.gridy++;
		contents.add(chooseShininess = new UserCommandSelectNumber(gui,Translator.get("Shininess"),mat.getShininess()),con1);
		con1.gridy++;
		contents.add(chooseTexture = new UserCommandSelectFile(gui,Translator.get("Texture"),mat.getTextureFilename()),con1);
		con1.gridy++;
		contents.add(chooseIsLit = new UserCommandSelectBoolean(gui,Translator.get("Lit"),mat.isLit()),c);

		chooseDiffuse.addChangeListener(this);
		chooseAmbient.addChangeListener(this);
		chooseSpecular.addChangeListener(this);
		chooseShininess.addChangeListener(this);
		chooseTexture.addChangeListener(this);
		chooseIsLit.addChangeListener(this);

		// supported file formats
		chooseTexture.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png"));
		chooseTexture.addChoosableFileFilter(new FileNameExtensionFilter("BMP", "bmp"));
		chooseTexture.addChoosableFileFilter(new FileNameExtensionFilter("JPEG", "jpeg"));
		chooseTexture.addChoosableFileFilter(new FileNameExtensionFilter("TGA", "tga"));
	}

	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {		
		chooseDiffuse.setValue(mat.getDiffuseColor());
		chooseAmbient.setValue(mat.getAmbientColor());
		chooseSpecular.setValue(mat.getSpecular());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==chooseDiffuse) {
			float[] v = chooseDiffuse.getValue();
			mat.setDiffuseColor(v[0],v[1],v[2],v[3]);
		}
		if(e.getSource()==chooseAmbient) {
			float[] v = chooseAmbient.getValue();
			mat.setAmbientColor(v[0],v[1],v[2],v[3]);
		}
		if(e.getSource()==chooseSpecular) {
			float[] v = chooseSpecular.getValue();
			mat.setSpecularColor(v[0],v[1],v[2],v[3]);
		}
		if(e.getSource()==chooseShininess) {
			float arg0 = chooseShininess.getValue();
			mat.setShininess(arg0);
		}
		if(e.getSource()==chooseTexture) {
			String arg0 = chooseTexture.getFilename();
			mat.setTextureFilename(arg0);
		}
		if(e.getSource()==chooseIsLit) {
			boolean arg0 = chooseIsLit.getValue();
			mat.setLit(arg0);
		}
	}
}
