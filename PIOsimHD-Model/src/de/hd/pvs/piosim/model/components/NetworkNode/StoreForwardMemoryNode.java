package de.hd.pvs.piosim.model.components.NetworkNode;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;


/**
 * Provides half the bandwidth to local processes, when they communicate.
 *
 * @author julian
 */
public class StoreForwardMemoryNode extends StoreForwardNode {


	/**
	 * Bandwidth when data is transferred between two neighboring processes
	 */
	@Attribute
	@NotNegativeOrZero
	private long localBandwidth = -1;


	public void setLocalBandwidth(long localBandwidth) {
		this.localBandwidth = localBandwidth;
	}

	public long getLocalBandwidth() {
		return localBandwidth;
	}
}
