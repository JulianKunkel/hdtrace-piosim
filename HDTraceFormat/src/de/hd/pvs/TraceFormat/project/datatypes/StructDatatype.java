package de.hd.pvs.TraceFormat.project.datatypes;

import java.util.ArrayList;

public class StructDatatype extends Datatype{
	/**
	 * Real size of the struct
	 */
	private int size = 0;
	
	public class StructType{
		final Datatype type;
		final int displacement;
		final int blocklen;
		
		public StructType(Datatype type, int displacement, int blocklen) {
			this.type = type;
			this.displacement = displacement;
			this.blocklen = blocklen;
		}
		
		public Datatype getType() {
			return type;
		}
		
		public int getDisplacement() {
			return displacement;
		}
		
		public int getBlocklen() {
			return blocklen;
		}
		
		public int getEndPos() {
			return displacement + blocklen * type.getExtend();
		}
	}
	
	final ArrayList<StructType> types = new ArrayList<StructType>();
	
	public int getCount(){
		return types.size();
	}
	
	public StructType getType(int which){
		return types.get(which);
	}
	
	public void appendType(Datatype type, int displacement, int blockLen){			
		types.add(new StructType(type, displacement, blockLen));
		
		size += type.getSize() * blockLen;
	}
	
	@Override
	public int getExtend() {
		if(types.size() == 0)
			return 0;
		return types.get(types.size() - 1).getEndPos();
	}
	
	@Override
	public int getSize() {		
		return size;
	}
	
	@Override
	public DatatypeEnum getType() {	
		return DatatypeEnum.STRUCT;
	}
}
