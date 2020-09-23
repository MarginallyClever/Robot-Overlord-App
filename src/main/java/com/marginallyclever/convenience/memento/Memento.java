package com.marginallyclever.convenience.memento;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Memento is a snapshot of a robot at a moment in time.  
 * See Memento design pattern: https://sourcemaking.com/design_patterns/memento
 * @author Dan Royer
 *
 */
public abstract interface Memento {
	/**
	 * Write the Memento to a stream
	 * @param arg0 the stream
	 * @throws IOException
	 */
	abstract public void save(OutputStream arg0) throws IOException;

	/**
	 * Read the Memento to a stream
	 * @param arg0 the stream
	 * @throws IOException
	 */
	abstract public void load(InputStream arg0) throws IOException;
}
