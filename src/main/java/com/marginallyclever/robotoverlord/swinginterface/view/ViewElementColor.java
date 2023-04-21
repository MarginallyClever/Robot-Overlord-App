package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ColorRGBAEdit;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementColor extends ViewElement implements PropertyChangeListener {
	private final BackgroundPaintedButton chooseButton = new BackgroundPaintedButton("");
	private final ColorParameter colorEntity;
	
	public ViewElementColor(ColorParameter colorEntity) {
		super();
		this.colorEntity = colorEntity;

		colorEntity.addPropertyChangeListener(this);

		this.setLayout(new BorderLayout());
		JLabel label = new JLabel(colorEntity.getName(),JLabel.LEADING);
		this.add(label,BorderLayout.LINE_START);
		this.add(chooseButton,BorderLayout.LINE_END);

		chooseButton.setBackground(new Color(
				(int)(255*colorEntity.getR()),
				(int)(255*colorEntity.getG()),
				(int)(255*colorEntity.getB()),
				(int)(255*colorEntity.getA())));

		chooseButton.addActionListener(e -> {
			Color old = chooseButton.getBackground();
			JColorChooser chooser = new JColorChooser(chooseButton.getBackground());
			chooser.getSelectionModel().addChangeListener(evt -> {
				Color newColor = chooser.getColor();
				chooseButton.setBackground(newColor);
				fireColorChangeEvent(newColor);
			});

			JDialog dialog = JColorChooser.createDialog(SwingUtilities.getWindowAncestor(this),label.getText(),true, chooser,
					e1 -> {
						Color newColor = chooser.getColor();
						if(!old.equals(newColor)) {
							chooseButton.setBackground(newColor);
							fireColorChangeEvent(newColor);
						}
					},
					e2 -> {
						chooseButton.setBackground(old);
						fireColorChangeEvent(old);
					}
			);
			dialog.setVisible(true);
		});
	}

	/**
	 * selected color changed, update entity.
	 * @param c
	 */
	private void fireColorChangeEvent(Color c) {
		double [] newValues = new double[]{
				c.getRed()/255.0,
				c.getGreen()/255.0,
				c.getBlue()/255.0,
				c.getAlpha()/255.0
		};

		UndoSystem.addEvent(this,new ColorRGBAEdit(colorEntity,newValues));
	}

	/**
	 * entity changed, change panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		chooseButton.setBackground(new Color(
				(int)(255*colorEntity.getR()),
				(int)(255*colorEntity.getG()),
				(int)(255*colorEntity.getB()),
				(int)(255*colorEntity.getA())));
	}

	@Override
	public void setReadOnly(boolean arg0) {
		chooseButton.setEnabled(!arg0);
	}
}
