package de.hd.pvs.TraceFormat.project.datatypes;

public class VectorDatatype extends Datatype{
	final int count;  
	final int blocklen; 
	final int stride; 
	
	final Datatype previous;
	
	/**
	 * @param previous Datatype
	 * @param count number of blocks
	 * @param blocklen number of elements in each block
	 * @param strideElements number of elements between start of each block (might lead to holes)
	 */
	public VectorDatatype(Datatype previous, int count, int blocklen, int strideElements) {
		this.previous = previous;
		this.count = count;
		this.blocklen = blocklen;
		this.stride = strideElements;
		
		assert(count >= 1);
		assert(blocklen >= 1);
		
		//TODO allow negative values in computation.
		assert(strideElements >= 1);
	}
	
	@Override
	public int getExtend() {
		return previous.getExtend() * stride * blocklen;
	}
	
	@Override
	public int getSize() {
		return previous.getSize() * count * blocklen;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.VECTOR;
	}
	
	public int getBlocklen() {
		return blocklen;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getStride() {
		return stride;
	}
}
