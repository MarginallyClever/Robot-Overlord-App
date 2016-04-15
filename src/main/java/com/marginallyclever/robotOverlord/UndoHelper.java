package com.marginallyclever.robotOverlord;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

public class UndoHelper implements UndoableEditListener {
	private RobotOverlord client;
	
	public UndoHelper(RobotOverlord gui) {
		client = gui;
	}
	
	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
		client.getUndoManager().addEdit(arg0.getEdit());
		client.updateMenu();
	}
}
