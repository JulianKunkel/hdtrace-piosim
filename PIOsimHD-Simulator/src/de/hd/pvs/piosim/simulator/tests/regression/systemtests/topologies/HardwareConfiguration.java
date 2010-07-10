package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Describes a single hardware model / configuration.
 * The constructor of derived classes might add any parameter they like.
 *
 * @author julian
 */
public abstract class HardwareConfiguration {
	public final long KBYTE = 1024;
	public final long MBYTE = 1024 * KBYTE;
	public final long GBYTE = 1024 * MBYTE;

	/**
	 * Instantiate a model for a given type e. g. a cluster.
	 * The return is the component which shall be interconnected via network if desired.
	 *
	 * @param mb
	 * @param topology
	 * @return The interconnectable component.
	 * @throws Exception
	 */
	abstract public NetworkNode createModel( String prefix, ModelBuilder mb, INetworkTopology topology ) throws Exception;
}
