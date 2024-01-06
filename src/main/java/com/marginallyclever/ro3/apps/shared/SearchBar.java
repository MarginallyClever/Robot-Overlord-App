package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Objects;

/**
 * <p>{@link SearchBar} is a text field, a toggle for case-sensitive, and a toggle for regular expressions.</p>
 * <p>When the text changes, it fires a property change event for the value "match" with the new text.</p>
 */
public class SearchBar extends JPanel implements DocumentListener {
    private final JTextField match = new JTextField();
    private final JToggleButton isCaseSensitive = new JToggleButton("Aa");
    private final JToggleButton isRegex = new JToggleButton(".*");

    public SearchBar() {
        this("");
    }

    public SearchBar(String text) {
        super(new BorderLayout());
        setName("SearchBar");
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        var label = new JLabel(new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-search-16.png"))));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        JPanel inner = new JPanel(new BorderLayout());
        inner.add(label, BorderLayout.LINE_START);
        inner.add(match, BorderLayout.CENTER);
        inner.add(isCaseSensitive, BorderLayout.LINE_END);
        add(inner, BorderLayout.CENTER);
        add(isRegex, BorderLayout.LINE_END);

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

    public boolean getRegex() {
        return isRegex.isSelected();
    }

    public void setRegex(boolean regex) {
        isRegex.setSelected(regex);
    }

    public boolean getCaseSensitive() {
        return isCaseSensitive.isSelected();
    }

    public void setCaseSensitive(boolean caseSensitive) {
        isCaseSensitive.setSelected(caseSensitive);
    }
}
