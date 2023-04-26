package com.marginallyclever.convenience.memento;

/**
 * 
 * @author Dan Royer
 */
@Deprecated
public class MementoDoubleArray implements Memento, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2161449213837224638L;
	public double [] values; 
	
	public MementoDoubleArray(int size) {
		values = new double[size];
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		MementoDoubleArray a = (MementoDoubleArray)super.clone();
		a.values = values.clone();
		return a;
	}
}
