package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        match.setToolTipText("Search for this pattern");
        isCaseSensitive.setToolTipText("Case sensitive search");
        isRegex.setToolTipText("Regular expression search");
        
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

        isCaseSensitive.addActionListener(e -> fireMatchChange());
        isRegex.addActionListener(e -> fireMatchChange());

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                match.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireMatchChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireMatchChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireMatchChange();
    }

    protected void fireMatchChange() {
        String newValue = match.getText();
        super.firePropertyChange("match", null, newValue);
    }

    public boolean getRegex() {
        return isRegex.isSelected();
    }

    public void setRegex(boolean regex) {
        isRegex.setSelected(regex);
        fireMatchChange();
    }

    public boolean getCaseSensitive() {
        return isCaseSensitive.isSelected();
    }

    public void setCaseSensitive(boolean caseSensitive) {
        isCaseSensitive.setSelected(caseSensitive);
        fireMatchChange();
    }

    /**
     * Does the given text match the search criteria?
     * @param text the text to match
     * @return true if the text matches the search criteria
     */
    public boolean matches(String text) {
        String searchCriteria = match.getText();
        if(searchCriteria==null || searchCriteria.isBlank()) return true;
        if(text==null) return false;
        if(this.getRegex()) {
            Pattern pattern = this.getCaseSensitive() ?
                    Pattern.compile(searchCriteria) :
                    Pattern.compile(searchCriteria,Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        } else {
            if(!this.getCaseSensitive()) {
                text = text.toLowerCase();
                searchCriteria = searchCriteria.toLowerCase();
            }
            return text.contains(searchCriteria);
        }
    }

    /**
     * Set the text in the search bar.
     * @param text the text to search for.  Can be a regular expression.
     */
    public void setSearchText(String text) {
        match.setText(text);
    }

    /**
     * Get the text in the search bar.
     * @return the text to search for.  Can be a regular expression.
     */
    public String getSearchText() {
        return match.getText();
    }
}
