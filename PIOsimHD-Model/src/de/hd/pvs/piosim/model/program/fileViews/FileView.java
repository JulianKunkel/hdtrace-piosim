package de.hd.pvs.piosim.model.program.fileView;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;
import de.hd.pvs.piosim.model.inputOutput.ListIO;

/**
 * Encapsulates logical to physical file byte mapping.
 *
 * @author julian
 */
public class FileView {
	final private Datatype datatype;
	final private long displacement;

	public FileView(Datatype datatype, long displacement) {
		this.datatype = datatype;
		this.displacement = displacement;

		if(datatype.getExtend() <= 0){
			throw new IllegalArgumentException("Datatype extend <= 0!");
		}

		if(datatype.getSize() <= 0){
			throw new IllegalArgumentException("Datatype size <= 0!");
		}
	}

	public Datatype getDatatype() {
		return datatype;
	}

	public long getDisplacement() {
		return displacement;
	}

	/**
	 * Create and add an I/O operation to the given ListIO by applying the view.
	 *
	 * @param listIO
	 * @param offset
	 * @param sizes
	 */
	public void createIOOperation(ListIO listIO, long offset, long accessSize){
		if(accessSize == 0){
			return;
		}

		assert(offset >= 0);

		final long typeSize = datatype.getSize();

		// determine start position in datatype
		final long offsetInDatatype = offset % typeSize;

		unrollContiguous(datatype, new CurrentPosition(offset - offsetInDatatype, listIO), offsetInDatatype, accessSize);
	}

	private class CurrentPosition{
		private long currentPhysicalPosition;
		private ListIO listIO;

		public CurrentPosition(long offset, ListIO listIO) {
			this.currentPhysicalPosition = offset;
			this.listIO = listIO;
		}

		public void createIOJob(long size){
			assert(size > 0);
			listIO.addIOOperation(currentPhysicalPosition + displacement, size);
			currentPhysicalPosition += size;
		}

		public void skipHole(long size){
			assert(size >= 0);
			currentPhysicalPosition += size;
		}

		public long getCurrentPhysicalPosition() {
			return currentPhysicalPosition;
		}
	}

	private void unrollContiguous(Datatype prevdatatype, CurrentPosition cur , long offsetInDatatype, long accessSize){
		final long typeSize = prevdatatype.getSize();

		if(typeSize == 0){
			return;
		}

		assert(accessSize > 0);

		if(typeSize == prevdatatype.getExtend()){
			// no holes! Just go ahead.
			cur.skipHole(offsetInDatatype);
			cur.createIOJob(accessSize);
			return;
		}

		// first one might be partial:
		if(offsetInDatatype != 0){
			final long remainder = typeSize - offsetInDatatype;
			final long preReadSize = (accessSize > remainder) ? remainder : accessSize;

			addDatatypeIOOperation(cur, prevdatatype, offsetInDatatype, preReadSize);
			accessSize -= preReadSize;
		}

		// treat view as a contiguous datatype with holes:
		final long repeats = accessSize / typeSize;
		for(int i= 0 ; i < repeats; i++){
			addDatatypeIOOperation(cur, prevdatatype, 0, typeSize);
		}

		accessSize -= repeats * typeSize;

		// last one is partial:
		if(accessSize > 0){
			addDatatypeIOOperation(cur, prevdatatype, 0, accessSize);
		}

	}

	/**
	 * Helper function to write a structure partially.
	 * @param cur
	 * @param type
	 * @param typeCount
	 * @param offsetInDatatype
	 * @param accessSize
	 */
	private void unrollPartialStruct(CurrentPosition cur, StructDatatype type, int typeCount, long offsetInDatatype, long accessSize){
		long lastPos = 0;

		for(int t= 0 ; t < typeCount; t++){
			final StructType childType = type.getType(t);
			long childAccessSize = childType.getBlocklen()* childType.getType().getSize();
			long blockChildExtend = childType.getBlocklen()* childType.getType().getExtend();

			if(offsetInDatatype > childAccessSize){
				// skip complete data type:
				offsetInDatatype -= childAccessSize;
				continue;
			}

			childAccessSize = (accessSize < childAccessSize - offsetInDatatype) ? accessSize : (childAccessSize - offsetInDatatype);

			cur.skipHole(childType.getDisplacement() - lastPos);

			if(offsetInDatatype > 0){
				unrollContiguous(childType.getType(), cur, offsetInDatatype, childAccessSize);
				offsetInDatatype = 0;
			}else{
				unrollContiguous(childType.getType(), cur, 0, childAccessSize);
			}

			accessSize -= childAccessSize;

			if(accessSize == 0){
				break;
			}

			lastPos = childType.getDisplacement() + blockChildExtend;
		}
	}

	/**
	 * Internal function, recursively unrolls the datatype.
	 *
	 * @param listIO
	 * @param physicalOffset The offset at which the datatype starts (does not include displacement)
	 * @param offsetInDatatype
	 * @param accessSize
	 */
	private void addDatatypeIOOperation(CurrentPosition cur, Datatype datatype, long offsetInDatatype, long accessSize){
		assert(offsetInDatatype >= 0);
		assert(accessSize > 0);
		final long typeSize = datatype.getSize();
		assert(typeSize >= accessSize + offsetInDatatype);

		if(typeSize== datatype.getExtend()){
			// no holes! Just go ahead. TODO tread partial write of Native Datatypes which is not allowed!
			cur.skipHole(offsetInDatatype);

			cur.createIOJob(accessSize);
			return;
		}

		switch(datatype.getType()){
		case NAMED:
			// handle UB and LB
			if(typeSize == 0){
				return;
			}

			if(offsetInDatatype != 0){
				throw new IllegalArgumentException("Offset in native datatype is not ZERO.");
			}

			// no partial write of integral datatypes is allowed!
			if( accessSize % typeSize != 0){
				throw new IllegalArgumentException("No partial write of datatypes is allowed (nor does it make sense)");
			}

			cur.createIOJob(accessSize);
			break;
		case CONTIGUOUS:{
			ContiguousDatatype type = (ContiguousDatatype) datatype;

			unrollContiguous(type.getPrevious(), cur, offsetInDatatype, accessSize);
			break;
		}case VECTOR:{
			final VectorDatatype type = (VectorDatatype) datatype;
			final Datatype prev = type.getPrevious();
			final long prevTypeSize = prev.getSize();
			final long prevTypeExtend = prev.getExtend();
			final long holeSize = (type.getStride() - type.getBlocklen()) * prevTypeExtend;

			assert(holeSize >= 0);

			final long blockLength = (prevTypeSize * type.getBlocklen());

			// gets unrolled at most once
			if(offsetInDatatype != 0){
				//final long strideLength = type.getStride() * prevTypeExtend;

				// determine which iteration got hit:
				final long remainder = blockLength - offsetInDatatype % blockLength;
				final long preReadSize = (accessSize > remainder) ? remainder : accessSize;

				unrollContiguous(prev, cur, offsetInDatatype, preReadSize);

				accessSize -= preReadSize;
				cur.skipHole(holeSize);
			}

			final long fullRepeats = accessSize / blockLength;
			accessSize -= fullRepeats * blockLength;

			for(int i=0; i < fullRepeats; i++){
				unrollContiguous(prev, cur, 0, blockLength);

				cur.skipHole(holeSize);
			}

			// remainder ?
			if(accessSize > 0){
				unrollContiguous(prev, cur, 0, accessSize);
			}

			break;
		}case STRUCT:{
			final StructDatatype type = (StructDatatype) datatype;
			final int typeCount = type.getCount();

			if(offsetInDatatype != 0){
				final long remainingBytes = typeSize - offsetInDatatype;

				final long maxAccessSize = accessSize < remainingBytes ? accessSize : remainingBytes;
				unrollPartialStruct(cur, type, typeCount, offsetInDatatype, maxAccessSize);

				accessSize -= maxAccessSize;
			}

			long fullRepeats = accessSize / typeSize;
			accessSize -= fullRepeats * typeSize;

			for(int i=0 ; i < fullRepeats; i++){
				long lastPos = 0;

				for(int t= 0 ; t < typeCount; t++){
					final StructType childType = type.getType(t);
					cur.skipHole(childType.getDisplacement() - lastPos);
					final long childBlockSize = childType.getBlocklen()* childType.getType().getSize();
					unrollContiguous(childType.getType(), cur, 0, childBlockSize);

					lastPos = childType.getDisplacement() + childType.getBlocklen()* childType.getType().getExtend();
				}
			}

			// remainder
			if(accessSize > 0){
				unrollPartialStruct(cur, type, typeCount, 0, accessSize);
			}

			break;
		}
		}
	}
}
