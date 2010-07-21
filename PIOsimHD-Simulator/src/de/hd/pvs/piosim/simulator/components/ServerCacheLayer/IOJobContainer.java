package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

/**
 * Multiple IOJobs could be aggregated into one combined IO Job.
 * i.e. multiple client operations (even from multiple clients) could be aggregated into
 * one big I/O job.
 * Once this job finishes callbacks for all individual operations must be executed.
 *
 * @author julian
 *
 */
public interface IOJobContainer {
	public int getNumberOfJobs();
}
