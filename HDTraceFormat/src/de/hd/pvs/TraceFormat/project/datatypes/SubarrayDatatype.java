package de.hd.pvs.TraceFormat.project.datatypes;

import java.util.ArrayList;

public class SubarrayDatatype extends Datatype{
	public static enum Order{
		MPI_ORDER_FORTRAN,
		MPI_ORDER_C
	}
	
	public static class DimensionSpec{
		final int size;
		final int subsize;
		final int start;
		
		public DimensionSpec(int size, int subsize, int start) {
			this.size = size;
			this.subsize = subsize;
			this.start = start;
		}
		
		public int getSize() {
			return size;
		}
		
		public int getStart() {
			return start;
		}
		
		public int getSubsize() {
			return subsize;
		}
	}
	
	final Order order;
	final Datatype previous;
	final DimensionSpec[] dimensionSpec;

	   
	public SubarrayDatatype(DimensionSpec[] dimSpec, Order order, Datatype previous) {
		this.order = order;
		this.previous = previous;
		this.dimensionSpec = dimSpec;
	}
	
	public Datatype getPrevious() {
		return previous;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public DimensionSpec[] getDimensionSpec() {
		return dimensionSpec;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.SUBARRAY;
	}

	@Override
	public ArrayList<Datatype> getChildDataTypes() {
		ArrayList<Datatype> type = new ArrayList<Datatype>();
		type.add(previous);
		return type;
	}

	@Override
	public long getExtend() {
		long extend = 0;
		
		extend = dimensionSpec[0].size; 
			
		for(int i=1; i < dimensionSpec.length; i++){
			extend = extend * dimensionSpec[i].size;
		}
		
		return extend * previous.getExtend();
	}

	@Override
	public long getSize() { 
		if(dimensionSpec.length == 1){
			return dimensionSpec[0].subsize * previous.getSize();
		}
		
		long size;		
		size = dimensionSpec[0].subsize; 
			
		for(int i=1; i < dimensionSpec.length; i++){
			size= size * dimensionSpec[i].subsize;
		}
		
		return size * previous.getSize();		
	}
}
