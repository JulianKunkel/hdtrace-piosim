package de.hd.pvs.piosim.model.program.fileView;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.SubarrayDatatype.DimensionSpec;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.piosim.model.inputOutput.ListIO;

/**
 * Encapsulates logical to physical file byte mapping.
 *
 * @author julian
 */
public class FileView {

	final private Datatype etype;
	final private Datatype datatype;
	final private long displacement;

	class DatatypeCompletelyUnrolledException extends Exception{

	}

	public FileView(Datatype etype, Datatype datatype, long displacement) {
		this.datatype = datatype;
		this.displacement = displacement;

		if(datatype.getExtend() <= 0){
			throw new IllegalArgumentException("Datatype extend <= 0!");
		}

		if(datatype.getSize() <= 0){
			throw new IllegalArgumentException("Datatype size <= 0!");
		}

		this.etype = etype;
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
	 * @param offsetInTermsOfTheEType
	 * @param sizes
	 */
	public void createIOOperationWithDatatypeOffset(ListIO listIO, long offsetInTermsOfTheEType, long accessSize){
		if(accessSize == 0){
			return;
		}

		assert(offsetInTermsOfTheEType >= 0);

		final long typeSize = datatype.getSize();
		final long eTypeSize = etype.getSize();

		// calculate the number of etypes per data type
		final long etypesPerDatatype = typeSize / eTypeSize;

		// calculate the number of data types which are skipped
		final long skippedDatatypeCount = offsetInTermsOfTheEType / etypesPerDatatype;

		// determine start position in the data type
		final long offsetInDatatype = (offsetInTermsOfTheEType % etypesPerDatatype ) * eTypeSize;

		try{
		unrollContiguous(datatype, new CurrentPosition(	skippedDatatypeCount * datatype.getExtend() + displacement,
				offsetInDatatype , 0, accessSize, listIO), accessSize / datatype.getSize() + 2);
		}catch(DatatypeCompletelyUnrolledException e){

		}
	}

	/**
	 * Create and add an I/O operation to the given ListIO by applying the view.
	 *
	 * @param listIO
	 * @param physicalOffset
	 * @param sizes
	 */
	public void createIOOperationWithPhysicalOffset(ListIO listIO, long physicalOffset, long accessSize){
		if(accessSize == 0){
			return;
		}

		assert(physicalOffset >= 0);

		final long extent = datatype.getExtend();

		// determine start position in the data type
		final long offsetInDatatype = (physicalOffset % extent);

		try{
		unrollContiguous(datatype, new CurrentPosition(physicalOffset, 0 , offsetInDatatype , accessSize, listIO),
				accessSize / datatype.getSize() + 2);
		}catch(DatatypeCompletelyUnrolledException e){

		}
	}

	private class CurrentPosition{
		private long currentPhysicalPosition;

		// amount of bytes to skip inside the accessible parts of the data type
		private long logicalOffsetToSkip;

		// amount of physical bytes to skip until data from the accessible parts should be accessed
		private long physicalOffsetToSkip;

		// amount of physical bytes which shall be accessed from the accessible parts of the data type
		private long amountOfDataToAccess;

		// resulting listIO
		private ListIO listIO;

		public CurrentPosition(long physicalOffset, long logicalOffsetToSkip, long physicalOffsetToSkip, long amountOfDataToAccess, ListIO listIO) {
			assert(physicalOffset >= 0);
			assert(logicalOffsetToSkip >= 0);
			assert(physicalOffsetToSkip >= 0);

			// both skip values shall not be bigger than zero.
			assert(! (logicalOffsetToSkip > 0 && physicalOffsetToSkip > 0) );

			assert(amountOfDataToAccess > 0);

			this.currentPhysicalPosition = physicalOffset;
			this.listIO = listIO;

			this.amountOfDataToAccess = amountOfDataToAccess;
			this.logicalOffsetToSkip = logicalOffsetToSkip;
			this.physicalOffsetToSkip = physicalOffsetToSkip;
		}

		// Indicates that the data type has actually some data at the current position.
		public void datatypeData(long size) throws DatatypeCompletelyUnrolledException{

			// behave as a hole, when physical or logical offsets must be skipped
			if(logicalOffsetToSkip > 0){
				if (logicalOffsetToSkip >= size){
					currentPhysicalPosition += size;
					logicalOffsetToSkip -= size;
					return;
				}else{
					currentPhysicalPosition += logicalOffsetToSkip;
					size = size - logicalOffsetToSkip;
					logicalOffsetToSkip = 0;
				}
			}
			if( physicalOffsetToSkip > 0){
				if (physicalOffsetToSkip >= size){
					currentPhysicalPosition += size;
					physicalOffsetToSkip -= size;
					return;
				}else{
					currentPhysicalPosition += physicalOffsetToSkip;
					size = size - physicalOffsetToSkip;
					physicalOffsetToSkip = 0;
				}
			}

			assert(size > 0);
			System.out.println("Adding size:" + size + " @ offset: " + currentPhysicalPosition );

			if (size < amountOfDataToAccess ){
				listIO.addIOOperation(currentPhysicalPosition, size);
			}else{
				assert(amountOfDataToAccess > 0);
				listIO.addIOOperation(currentPhysicalPosition, amountOfDataToAccess);
				throw new DatatypeCompletelyUnrolledException();
			}

			currentPhysicalPosition += size;
			amountOfDataToAccess -= size;
			assert(amountOfDataToAccess >= 0);
		}

		// Indicates the data type has a hole of a given size at the current position.
		public void datatypeHole(long size){
			assert(size >= 0);

			currentPhysicalPosition += size;

			// skip the physical bytes if necessary
			if( physicalOffsetToSkip > 0){
				physicalOffsetToSkip = size > physicalOffsetToSkip ? 0 : physicalOffsetToSkip - size;
			}
		}

		public long getCurrentPhysicalPosition() {
			return currentPhysicalPosition;
		}
	}

	private void unrollContiguous(Datatype prevdatatype, CurrentPosition cur, long repeats) throws DatatypeCompletelyUnrolledException{
		final long typeSize = prevdatatype.getSize();

		if(typeSize == 0){
			return;
		}

		if(typeSize == prevdatatype.getExtend()){
			// data type contains no holes! Just go ahead.
			cur.datatypeData(typeSize * repeats);
			return;
		}

		// treat view as a contiguous data type with holes, handle the first (half datatype) and the last (half datatype)
		for(int i= 0 ; i < repeats; i++){
			addDatatypeIOOperation(cur, prevdatatype);
		}
	}

	/**
	 * Internal function, recursively unrolls the datatype.
	 *
	 * @param physicalOffset The offset at which the datatype starts (does not include displacement)
	 * @param offsetInDatatype
	 * @param accessSize
	 */
	private void addDatatypeIOOperation(CurrentPosition cur, Datatype datatype)  throws DatatypeCompletelyUnrolledException{
		System.out.println("addDatatypeIOOperation " + datatype.getType() + " " + datatype.getTid() + " cur: " + cur.getCurrentPhysicalPosition());

		final long typeSize = datatype.getSize();

		if(typeSize == datatype.getExtend()){
			// no holes! Just go ahead.
			cur.datatypeData(typeSize);

			return;
		}

		switch(datatype.getType()){
		case NAMED:
			// handle UB and LB
			if(typeSize == 0){
				return;
			}

			cur.datatypeData(typeSize);
			break;
		case CONTIGUOUS:{
			ContiguousDatatype type = (ContiguousDatatype) datatype;

			unrollContiguous(type.getPrevious(), cur, type.getCount());
			break;
		}case VECTOR:{
			final VectorDatatype type = (VectorDatatype) datatype;
			final Datatype prev = type.getPrevious();
			final long prevTypeSize = prev.getSize();
			final long prevTypeExtend = prev.getExtend();
			final long holeSize = (type.getStride() - type.getBlocklen()) * prevTypeExtend;

			assert(holeSize >= 0);

			for(int i=0; i < type.getBlockCount(); i++){
				unrollContiguous(prev, cur, type.getBlocklen());

				cur.datatypeHole(holeSize);
			}

			break;
		}case STRUCT:{
			final StructDatatype type = (StructDatatype) datatype;
			final int typeCount = type.getCount();

			long lastPos = 0;

			for(int t= 0 ; t < typeCount; t++){
				final StructType childType = type.getType(t);
				cur.datatypeHole(childType.getDisplacement() - lastPos);
				unrollContiguous(childType.getType(), cur, childType.getBlocklen());

				lastPos = childType.getDisplacement() + childType.getBlocklen()* childType.getType().getExtend();
			}


			break;

		}case SUBARRAY:{
			// see http://www.cs.vu.nl/~kielmann/mpi/standard-2/node79.html
			final SubarrayDatatype type = (SubarrayDatatype) datatype;

			final Datatype prev = type.getPrevious();
			final long prevExtend = type.getPrevious().getExtend();
			final long prevSize = type.getPrevious().getSize();

			final long startPos = cur.getCurrentPhysicalPosition();

			final DimensionSpec[] dimSpec = type.getDimensionSpec();

			// TODO generalize this method, works for 2D only
			assert(dimSpec.length == 2);

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

			// walk through one sub-array

			// determine start of the array which shall be skipped
			cur.datatypeHole(skipStart);

			// TODO extend 2D loop:

			for(int d2= 0 ; d2 < dimSpec[1].getSubsize(); d2++){
				// now process the first block of data
				unrollContiguous(prev , cur, dimSpec[0].getSubsize());
				// skip to the next element, but not in the last iteration
				if( d2 < dimSpec[1].getSubsize() - 1 )
					cur.datatypeHole( ( dimSpec[0].getSize() - dimSpec[0].getSubsize() ) * prevExtend);
			}


			// skip to the end of the data type:
			// determine start of the array which shall be skipped
			cur.datatypeHole(skipEnd);

			break;
		}default:
			throw new IllegalArgumentException("Data type " + datatype.getType() + " not supported, yet!");
		}
	}
}
