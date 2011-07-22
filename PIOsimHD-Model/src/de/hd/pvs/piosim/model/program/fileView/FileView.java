package de.hd.pvs.piosim.model.program.fileView;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype.DimensionSpec;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype.Order;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
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

		// determine start position in data type
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

			System.out.println("Adding size:" + size + " @ offset: " + (currentPhysicalPosition + displacement)  );

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
			assert(remainder >= 0);
			final long preReadSize = (accessSize > remainder) ? remainder : accessSize;

			assert(preReadSize >= 0);

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
	 * @param physicalOffset The offset at which the datatype starts (does not include displacement)
	 * @param offsetInDatatype
	 * @param accessSize
	 */
	private void addDatatypeIOOperation(CurrentPosition cur, Datatype datatype, long offsetInDatatype, long accessSize){
		System.out.println("addDatatypeIOOperation " + datatype.getType() + " " + datatype.getTid() + " " + accessSize +  " offset: " + offsetInDatatype + " cur: " + cur.getCurrentPhysicalPosition());

		assert(offsetInDatatype >= 0);
		assert(accessSize > 0);
		final long typeSize = datatype.getSize();
		assert(typeSize >= accessSize + offsetInDatatype);

		if(typeSize== datatype.getExtend()){
			// no holes! Just go ahead. TODO treat partial write of native data types which is not allowed here!
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

		}case SUBARRAY:{
			// see http://www.cs.vu.nl/~kielmann/mpi/standard-2/node79.html
			final SubarrayDatatype type = (SubarrayDatatype) datatype;

			// support only C order right now.
			assert(type.getOrder() == Order.MPI_ORDER_C);

			// how often is the full data type accessed
			final long fullIterations = accessSize / type.getSize();

			final Datatype prev = type.getPrevious();
			final long prevExtend = type.getPrevious().getExtend();
			final long prevSize = type.getPrevious().getSize();

			final long startPos = cur.getCurrentPhysicalPosition();

			final DimensionSpec[] dimSpec = type.getDimensionSpec();

			// TODO generalize this method, works for 2D only
			assert(dimSpec.length == 2);

			// number of times the data type is repeated inside the subarray.
			long arrayDimension = dimSpec[0].getSize();
			for(int d=1;  d < dimSpec.length ; d++){
				arrayDimension = arrayDimension * dimSpec[d].getSize();
			}

			// determine start of the array which shall be skipped
			long skipStart =  prevExtend * dimSpec[0].getStart();
			{
				long previSize = prevExtend;
				previSize = dimSpec[0].getSize() * previSize;
				for (int d=1; d < dimSpec.length ; d++){
					skipStart = skipStart + dimSpec[d].getStart() * previSize;
					previSize = dimSpec[d].getSize() * previSize;
				}
			}

			long skipEnd =  prevExtend * (dimSpec[0].getSize() - dimSpec[0].getSubsize() - dimSpec[0].getStart()) ;
			{
				long previSize = prevExtend;
				previSize = dimSpec[0].getSize() * previSize;
				for (int d=1; d < dimSpec.length ; d++){
					skipEnd = skipEnd + (dimSpec[d].getSize() - dimSpec[d].getSubsize() - dimSpec[d].getStart()) * previSize;
					previSize = dimSpec[d].getSize() * previSize;
				}
			}

			if(offsetInDatatype > 0){

				// determine start of the array which shall be skipped
				cur.skipHole(skipStart);

				// TODO 2D loop:

				for(int d2= 0 ; d2 < dimSpec[1].getSubsize(); d2++){
					// now process the first block of data
					for(int c=0; c < dimSpec[0].getSubsize(); c++){
						if (offsetInDatatype < prevSize){
							if(accessSize <= prevSize){
								addDatatypeIOOperation(cur, prev, offsetInDatatype, accessSize);

								// skip to the end of the current datatype.
								cur.skipHole( type.getExtend() - ( cur.getCurrentPhysicalPosition() - startPos ) );
								return;
							}
							addDatatypeIOOperation(cur, prev, offsetInDatatype, prevSize);
							accessSize -= prevSize;
						}else{ // do not access the data type at this position
							cur.skipHole(prevSize);
							offsetInDatatype -= prevSize;
						}
					}
					// skip to the next element, but not in the last iteration
					if( d2 < dimSpec[1].getSubsize() - 1 )
						cur.skipHole( ( dimSpec[0].getSize() - dimSpec[0].getSubsize() ) * prevExtend);
				}


				// skip to the end of the data type:
				// determine start of the array which shall be skipped
				cur.skipHole(skipEnd);
			}

			// write the remainder
			for(int i=0; i < fullIterations + 1 ; i++){

				// walk through one sub-array

				// determine start of the array which shall be skipped
				cur.skipHole(skipStart);

				// TODO 2D loop:

				for(int d2= 0 ; d2 < dimSpec[1].getSubsize(); d2++){
					// now process the first block of data
					for(int c=0; c < dimSpec[0].getSubsize(); c++){
						if(accessSize <= prevSize){
							addDatatypeIOOperation(cur, prev, offsetInDatatype, accessSize);

							// skip to the end of the current datatype.
							cur.skipHole( type.getExtend() - ( cur.getCurrentPhysicalPosition() - startPos ) );
							return;
						}
						addDatatypeIOOperation(cur, prev, 0, prevSize);
						accessSize -= prevSize;
					}
					// skip to the next element, but not in the last iteration
					if( d2 < dimSpec[1].getSubsize() - 1 )
						cur.skipHole( ( dimSpec[0].getSize() - dimSpec[0].getSubsize() ) * prevExtend);
				}


				// skip to the end of the data type:
				// determine start of the array which shall be skipped
				cur.skipHole(skipEnd);
			}

			break;
		}default:
			throw new IllegalArgumentException("Data type " + datatype.getType() + " not supported, yet!");
		}
	}
}
