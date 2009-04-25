package de.hd.pvs.TraceFormat.project.datatypes;

public interface IDatatype {
	/**
	 * Return the combiner i.e. type of this datatype
	 */
	public DatatypeEnum getType();
	
	/**
	 * Return the size of the datatype (including holes)
	 * @return
	 */
	public int getExtend();
	
	/**
	 * Return the used size of the datatype (i.e. do not count holes)
	 * @return
	 */
	public int getSize();
}
