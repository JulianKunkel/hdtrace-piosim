package de.hd.pvs.piosim.model.components.NetworkNode;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;

/**
 * Node receives data, buffers it, then transfers it to one outgoing edge.
 *
 * @author Julian M. Kunkel
 */
public class StoreForwardNode extends NetworkNode
{

	/** The total bandwidth of the <code>Switch</code>.
	 * The switch needs some time to transfer data incoming from some ports to target Ports */
	@Attribute
	@NotNegativeOrZero
	private long totalBandwidth = -1;

	@Override
	public String toString() {
		return super.toString() + " TotalBandwidth: " + totalBandwidth;
	}

	/**
	 * @return the totalBandwidth
	 */
	@AttributeGetters
	public final long getTotalBandwidth() {
		return totalBandwidth;
	}

	/**
	 * Set the total bandwidth the Switch is capable to process.
	 */
	public final void setTotalBandwidth(long totalBandwidth) {
		this.totalBandwidth = totalBandwidth;
	}
}
