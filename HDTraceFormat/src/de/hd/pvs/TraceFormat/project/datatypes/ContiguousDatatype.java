package de.hd.pvs.TraceFormat.project.datatypes;

public class ContiguousDatatype extends Datatype{
	final int count;
	
	final Datatype previous;
	
	public ContiguousDatatype(Datatype previous, int count) {
		assert(count >= 1);
		
		this.count = count;
		this.previous = previous;
	}
	
	@Override
	public int getExtend() {
		return previous.getExtend() * count;
	}
	
	@Override
	public int getSize() {
		return previous.getSize() * count;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.CONTIGUOUS;
	}
	
	public int getCount() {
		return count;
	}
	
	public Datatype getPrevious() {
		return previous;
	}
}
