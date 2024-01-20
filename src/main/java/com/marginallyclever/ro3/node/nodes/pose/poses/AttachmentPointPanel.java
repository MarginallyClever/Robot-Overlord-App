package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AttachmentPointPanel extends JPanel {
    private final AttachmentPoint attachmentPoint;

    public AttachmentPointPanel() {
        this(new AttachmentPoint());
    }

    public AttachmentPointPanel(AttachmentPoint attachmentPoint) {
        super(new GridLayout(0,2));
        this.attachmentPoint = attachmentPoint;
        this.setName(AttachmentPoint.class.getSimpleName());

        // radius
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.01);  // radius > 0
        var radiusField = new JFormattedTextField(formatter);
        radiusField.setToolTipText("Radius of the attachment point");
        radiusField.setValue(attachmentPoint.getRadius());
        radiusField.addPropertyChangeListener("value", e -> {
            attachmentPoint.setRadius( ((Number)radiusField.getValue()).doubleValue() );
        });
        PanelHelper.addLabelAndComponent(this,"Radius",radiusField);

        var attached = buildAttachToggle();
        PanelHelper.addLabelAndComponent(this,"Action",attached);
    }

    /**
     * Build the "attach/detach" toggle button.  Public so that it can be included in the control panel of other
     * nodes like MarlinRobotArm.
     * @return a new JToggleButton
     */
    public JComponent buildAttachToggle() {
        // attach/detach toggle
        var attached = new JToggleButton();
        attached.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/apps/actions/icons8-disconnect-16.png"))));
        attached.setSelected(attachmentPoint.getIsAttached());  // must come before action listener.
        setAttachedText(attached);

        attached.addActionListener(e -> {
            attachmentPoint.setIsAttached( attached.isSelected() );
            var isAttached = attachmentPoint.getIsAttached();
            if(isAttached) attachmentPoint.attemptAttach();
            else attachmentPoint.release();
            setAttachedText(attached);
        });
        return attached;
    }

    private void setAttachedText(JToggleButton attached) {
        attached.setText(attachmentPoint.getIsAttached() ? "Release" : "Attach");
    }
}
