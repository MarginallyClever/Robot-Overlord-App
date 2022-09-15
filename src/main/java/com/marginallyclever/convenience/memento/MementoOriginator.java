package com.marginallyclever.convenience.memento;

/**
 * The Originator can create or use a Memento
 * See Memento design pattern: https://sourcemaking.com/design_patterns/memento
 * @author Dan Royer
 *
 */
@Deprecated
public abstract interface MementoOriginator {
	/**
	 * Return an opaque snapshot of a moment in time.  This snapshot can be used to reproduce the Originator's state.
	 * @return a Memento of the the Originator's state
	 */
	abstract public Memento getState();
	
	/**
	 * Attempt to set Originator's current state to that described in the Memento.  Fails if the Memento is of the wrong type.
	 * @param arg0
	 */
	abstract public void setState(final Memento arg0);
}
