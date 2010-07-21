package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import java.util.LinkedList;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.GNoCache.InternalIOData;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;

public class JobQueueSimple implements IOJobQueue{

	/**
	 * Queued read operations, read and write operations are split.
	 */
	final LinkedList<IOJob<InternalIOData,IOOperationData>> queuedReadJobs = new LinkedList<IOJob<InternalIOData,IOOperationData>>();

	/**
	 * Contains write or flush operations.
	 */
	final LinkedList<IOJob<InternalIOData,IOOperationData>> queuedWriteJobs = new LinkedList<IOJob<InternalIOData,IOOperationData>>();

	@Override
	public IOJob getNextSchedulableJob(long freeMemory, GlobalSettings settings) {
		// prefer read requests for write requests
		IOJob io = null;

		if(  ! queuedReadJobs.isEmpty() &&
				freeMemory > (((StreamIOOperation) queuedReadJobs.peek().getOperationData()).getSize())  )
		{
			// reserve memory for READ requests
			io = queuedReadJobs.poll();
		}

		if(io == null){
			// pick up a write call

			io = queuedWriteJobs.poll();

			long size;
			long offset;
//			final FileMetadata file = io.getFile();
//
//			if(true){
//				final StreamIOOperation sop = (StreamIOOperation) io.getOperationData();
//				size = sop.getSize();
//				offset = sop.getOffset();
//
//				// try to combine several write operations.
//				while (! queuedWriteJobs.isEmpty()
//						&& io.getOperationType() ==
//						&& file == queuedWriteJobs.peek().getFile()
//						&& size + offset ==  queuedWriteJobs.peek().getOffset()
//						&& size + queuedWriteJobs.peek().getSize()  <= getSimulator().getModel().getGlobalSettings().getIOGranularity()
//				)
//				{
//					// TODO: check proper working:
//					io = queuedWriteJobs.poll();
//					size += io.getSize();
//				}
//
//				io = new IOJob(io.getFile(), io.getUserData(), size, offset, io.getType());
//			}
		}
		return io;
	}

	@Override
	public void addIOJob(IOJob<InternalIOData,IOOperationData> job){
		switch(job.getOperationType()){
		case FLUSH:
		case WRITE:
			queuedWriteJobs.add(job);
			break;
		case READ:
			queuedReadJobs.add(job);
		}
	}


}
