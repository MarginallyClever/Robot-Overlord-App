package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Objects;

/**
 * SearchBar is a text field with a magnifying glass icon.
 * When the text changes, it fires a property change event for the value "match" with the new text.
 * @since 2.7.0
 * @author Dan Royer
 */
public class SearchBar extends JPanel implements DocumentListener {
    private final JTextField match = new JTextField();
    private final JToggleButton isRegex = new JToggleButton(".*");

    public SearchBar() {
        this("");
    }

    public SearchBar(String text) {
        super(new BorderLayout());
        setName("SearchBar");
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        add(isRegex, BorderLayout.LINE_START);
        var label = new JLabel(new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-search-16.png"))));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(label, BorderLayout.LINE_END);
        add(match, BorderLayout.CENTER);

        match.setText(text);
        match.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireTextChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireTextChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireTextChange();
    }

    public void fireTextChange() {
        String newValue = match.getText();
        super.firePropertyChange("match", null, newValue);
    }

    public boolean isRegex() {
        return isRegex.isSelected();
    }

    public void setRegex(boolean regex) {
        isRegex.setSelected(regex);
    }
}
