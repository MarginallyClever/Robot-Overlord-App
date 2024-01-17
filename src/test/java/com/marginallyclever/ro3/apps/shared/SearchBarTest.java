package com.marginallyclever.ro3.apps.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;


@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class SearchBarTest {
    @Test
    public void fireOnChange() {
        final boolean [] fired = {false};
        SearchBar searchBar = new SearchBar();
        searchBar.addPropertyChangeListener("match", e-> fired[0] = true );
        searchBar.fireMatchChange();
        assert(fired[0]);
    }

    @Test
    public void caseSensitive() {
        final int [] fired = {0};
        SearchBar searchBar = new SearchBar();
        searchBar.addPropertyChangeListener("match", e-> fired[0]++ );
        searchBar.setCaseSensitive(true);
        assert(searchBar.getCaseSensitive());
        searchBar.setCaseSensitive(false);
        assert(!searchBar.getCaseSensitive());
        assert(fired[0]==2);
    }

    @Test
    public void regex() {
        final int [] fired = {0};
        SearchBar searchBar = new SearchBar();
        searchBar.addPropertyChangeListener("match", e-> fired[0]++ );
        searchBar.setRegex(true);
        assert(searchBar.getRegex());
        searchBar.setRegex(false);
        assert(!searchBar.getRegex());
        assert(fired[0]==2);
    }
}
