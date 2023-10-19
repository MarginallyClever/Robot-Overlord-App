package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.BackgroundPaintedButton;
import com.marginallyclever.robotoverlord.swing.edits.ColorParameterEdit;

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
	private final ColorParameter parameter;
	
	public ViewElementColor(ColorParameter parameter) {
		super();
		this.parameter = parameter;

		parameter.addPropertyChangeListener(this);

		this.setLayout(new BorderLayout());
		JLabel label = new JLabel(parameter.getName(),JLabel.LEADING);
		this.add(label,BorderLayout.LINE_START);
		this.add(chooseButton,BorderLayout.LINE_END);

		chooseButton.setBackground(new Color(
				(int)(255* parameter.getR()),
				(int)(255* parameter.getG()),
				(int)(255* parameter.getB()),
				(int)(255* parameter.getA())));

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

		UndoSystem.addEvent(new ColorParameterEdit(parameter,newValues));
	}

	/**
	 * entity changed, change panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		chooseButton.setBackground(new Color(
				(int)(255* parameter.getR()),
				(int)(255* parameter.getG()),
				(int)(255* parameter.getB()),
				(int)(255* parameter.getA())));
	}

	@Override
	public void setReadOnly(boolean arg0) {
		chooseButton.setEnabled(!arg0);
	}
}
