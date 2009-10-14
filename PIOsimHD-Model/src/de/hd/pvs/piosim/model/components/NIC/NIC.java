package de.hd.pvs.piosim.model.components.NIC;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

public class NIC
	extends BasicComponent<Node>
	implements INetworkEntry, INetworkExit
{

	/** Data transfer speed between two processes in this node */
	@Attribute
	@NotNegativeOrZero
	private long internalDataTransferSpeed = -1;

	public String getObjectType() {
		return NIC.class.getSimpleName();
	}

	/**
	 * @return the internalDataTransferSpeed
	 */
	@AttributeGetters
	public long getInternalDataTransferSpeed() {
		return internalDataTransferSpeed;
	}

	/**
	 * @param internalDataTransferSpeed the internalDataTransferSpeed to set
	 */
	public void setInternalDataTransferSpeed(long internalDataTransferSpeed) {
		this.internalDataTransferSpeed = internalDataTransferSpeed;
	}
}
