package de.hd.pvs.TraceFormat.project.datatypes;

/**
 * Datatype, useful for MPI memory structures and file access patterns.
 * @author julian
 */
public abstract class Datatype implements IDatatype{
	/* id of the datatype, used for reference */
	private long tid;
	
	abstract public DatatypeEnum getType();
	
	abstract public int getExtend();
	
	abstract public int getSize();
	
	
	final public void setTid(long tid) {
		this.tid = tid;
	}
	
	/**
	 * Return the datatype ID.
	 */
	final public long getTid() {
		return tid;
	}
	
	@Override
	public String toString() {	
		return getType() + "(" + tid + ") " + getSize() + ", " + getExtend();
	}
}
