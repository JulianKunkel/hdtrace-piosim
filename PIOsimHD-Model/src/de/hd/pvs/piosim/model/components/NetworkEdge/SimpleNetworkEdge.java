package de.hd.pvs.piosim.model.components.NetworkEdge;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;

/**
 * The component is linked to at most one other node
 */
public class SimpleNetworkEdge extends NetworkEdge {
	@Attribute
	@NotNegativeOrZero
	/** The <code>latency</code> of this interconnection. */
	protected Epoch latency = Epoch.ZERO;

	@Attribute
	@NotNegativeOrZero
	/** The <code>bandwidth</code> == throughput of this component. */
	protected long bandwidth = -1;

	/**
	 * Gets the <code>latency</code>.
	 *
	 * @return the latency
	 */
	@AttributeGetters
	public Epoch getLatency() {
		return this.latency;
	}

	/**
	 * Gets the <code>bandwidth</code>.
	 *
	 * @return the bandwidth
	 */
	@AttributeGetters public long getBandwidth() {
		return this.bandwidth;
	}

	/**
	 * Sets the <code>bandwidth</code>.
	 *
	 * set the bandwidth
	 */
	public void setBandwidth(long b) {
		this.bandwidth = b;
	}

	/**
	 * Sets the <code>latency</code>.
	 *
	 * set the latency
	 */
	public void setLatency(Epoch latency) {
		this.latency = latency;
	}

	@Override
	public String toString() {
		return super.toString() + " Bandwidth:" + bandwidth + " Latency:" + latency;
	}

}
