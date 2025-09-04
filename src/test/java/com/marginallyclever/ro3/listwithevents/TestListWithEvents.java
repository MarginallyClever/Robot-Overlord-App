package com.marginallyclever.ro3.listwithevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestListWithEvents {
    private ListWithEvents<String> listWithEvents;
    private String testItem = "Test";

    @BeforeEach
    public void setup() {
        listWithEvents = new ListWithEvents<>();
    }

    @Test
    public void testAdd() {
        listWithEvents.add(testItem);
        assertTrue(listWithEvents.getList().contains(testItem));
    }

    @Test
    public void testRemove() {
        listWithEvents.add(testItem);
        listWithEvents.remove(testItem);
        assertFalse(listWithEvents.getList().contains(testItem));
    }

    @Test
    public void testListeners() {
        final boolean[] add = {false};
        final boolean[] remove = {false};

        var listener = new ListListener<String>() {
            @Override
            public void itemAdded(Object source, String item) {
                add[0] = true;
            }

            @Override
            public void itemRemoved(Object source, String item) {
                remove[0] = true;
            }
        };
        listWithEvents.addItemListener(listener);
        listWithEvents.add(testItem);
        listWithEvents.remove(testItem);
        assertTrue(add[0]);
        assertTrue(remove[0]);
    }

    @Test
    public void testGetList() {
        listWithEvents.add(testItem);
        assertEquals(1, listWithEvents.getList().size());
        assertEquals(testItem, listWithEvents.getList().get(0));
    }

    @Test
    public void testClear() {
        listWithEvents.add(testItem);
        listWithEvents.clear();
        assertEquals(0, listWithEvents.getList().size());
    }

    @Test
    public void testSize() {
        listWithEvents.add(testItem);
        assertEquals(1, listWithEvents.size());
    }

    @Test
    public void testAddAll() {
        ArrayList<String> items = new ArrayList<>(Arrays.asList("Test1", "Test2"));
        listWithEvents.addAll(items);
        assertEquals(2, listWithEvents.size());
        assertTrue(listWithEvents.getList().containsAll(items));
    }

    @Test
    public void testSet() {
        ArrayList<String> items = new ArrayList<>(Arrays.asList("Test1", "Test2"));
        listWithEvents.set(items);
        assertEquals(2, listWithEvents.size());
        assertTrue(listWithEvents.getList().containsAll(items));
    }
}