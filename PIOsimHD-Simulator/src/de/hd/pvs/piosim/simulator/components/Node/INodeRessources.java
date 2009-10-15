package de.hd.pvs.piosim.simulator.components.Node;



/**
 * Allows node hosted components to use node resources like CPU and network.
 *
 * @author julian
 *
 */
public interface INodeRessources {

	/**
	 * Add a compute job at the given time, once the job finishes the
	 * issuing NodeHostedComponents callback is called immediately.
	 *
	 * @param job the new job we want to add.
	 */
	public void addComputeJob(ComputeJob job);

	/**
	 * Memory managment, check if enough memory is free
	 * @param required
	 * @return
	 */
	public boolean isEnoughFreeMemory(long required);

	/**
	 * Reserve an amount of main memory on this node.
	 * @param required
	 */
	public void reserveMemory(long required);

	/**
	 * Free some memory. Beware to free not more than which got used.
	 * @param howMuch
	 */
	public void freeMemory(long howMuch);
}
