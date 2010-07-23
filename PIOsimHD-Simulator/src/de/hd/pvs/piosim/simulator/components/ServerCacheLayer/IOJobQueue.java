package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.GNoCache.InternalIOData;

/**
 * Controls the I/O job order.
 *
 * @author julian
 */
public interface IOJobQueue {
	/**
	 * Return the next schedulable job, make sure the amount of memory fits (if read!)
	 * The job might coalesce many subjobs even from disjoint requests.
	 * The memory for the job is reserved automatically.
	 *
	 * @param freeMemory
	 * @param settings
	 * @return
	 */
	public IOJob getNextSchedulableJob(long freeMemory, GlobalSettings settings);

	/**
	 * Add an I/O job
	 * @param job
	 */
	public void addIOJob(IOJob<InternalIOData, IOOperationData> job);

}
