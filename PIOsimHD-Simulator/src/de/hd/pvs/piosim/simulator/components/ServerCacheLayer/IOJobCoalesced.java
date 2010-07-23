package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.GNoCache.InternalIOData;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;

/**
 * Multiple IOJobs could be aggregated into one combined IO Job.
 * i.e. multiple client operations (even from multiple clients) could be aggregated into
 * one big I/O job.
 * Once this job finishes callbacks for all individual operations must be executed.
 *
 * @author julian
 *
 */
public class IOJobCoalesced extends IOJob<InternalIOData, IOOperationData>{
	final LinkedList<IOJob<InternalIOData, IOOperationData>> aggregatedJobs;

	public IOJobCoalesced(FileMetadata file, IOOperationType operationType, IOOperationData operationData) {
		// does not have any user data!
		super(file, null, operationType, operationData);
		aggregatedJobs = new LinkedList<IOJob<InternalIOData, IOOperationData>>();
	}

	public IOJobCoalesced(FileMetadata file, IOOperationType operationType, IOOperationData operationData,  LinkedList<IOJob<InternalIOData, IOOperationData>> aggregatedJobs) {
		super(file, null, operationType, operationData);
		this.aggregatedJobs = aggregatedJobs;
	}

	protected void addJob(IOJob<InternalIOData, IOOperationData> newJob){
		aggregatedJobs.add(newJob);
	}

	@Override
	final public int getNumberOfJobs(){
		return aggregatedJobs.size();
	}

	public LinkedList<IOJob<InternalIOData, IOOperationData>> getAggregatedJobs() {
		return aggregatedJobs;
	}
}
