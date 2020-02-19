package com.marginallyclever.robotOverlord.engine.undoRedo;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import com.marginallyclever.robotOverlord.RobotOverlord;

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
