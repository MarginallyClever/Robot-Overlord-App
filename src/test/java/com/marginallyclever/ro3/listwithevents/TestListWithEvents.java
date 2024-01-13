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
    public void testAddItemAddedListenerAndFireItemAdded() {
        ItemAddedListener<String> listener = (source, item) -> assertEquals(testItem, item);
        listWithEvents.addItemAddedListener(listener);
        listWithEvents.add(testItem);
    }

    @Test
    public void testRemoveItemAddedListener() {
        ItemAddedListener<String> listener = (source, item) -> fail("Listener should have been removed");
        listWithEvents.addItemAddedListener(listener);
        listWithEvents.removeItemAddedListener(listener);
        listWithEvents.add(testItem);
    }

    @Test
    public void testAddItemRemovedListenerAndFireItemRemoved() {
        listWithEvents.add(testItem);
        ItemRemovedListener<String> listener = (source, item) -> assertEquals(testItem, item);
        listWithEvents.addItemRemovedListener(listener);
        listWithEvents.remove(testItem);
    }

    @Test
    public void testRemoveItemRemovedListener() {
        listWithEvents.add(testItem);
        ItemRemovedListener<String> listener = (source, item) -> fail("Listener should have been removed");
        listWithEvents.addItemRemovedListener(listener);
        listWithEvents.removeItemRemovedListener(listener);
        listWithEvents.remove(testItem);
    }

    @Test
    public void testGetList() {
        listWithEvents.add(testItem);
        assertEquals(1, listWithEvents.getList().size());
        assertEquals(testItem, listWithEvents.getList().get(0));
    }

    @Test
    public void testRemoveAll() {
        listWithEvents.add(testItem);
        listWithEvents.removeAll();
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