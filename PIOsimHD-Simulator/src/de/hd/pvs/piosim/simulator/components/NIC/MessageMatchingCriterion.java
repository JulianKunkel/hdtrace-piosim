package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;

/**
 * This class encapsulates the matching of receive network jobs with send jobs.
 * It compares jobs, jobs are considered to be equal if they can be matched.
 * A send and recv is considered to match if the source, tag and communicator matches.
 *
 * @author Julian M. Kunkel
 *
 */
public class MessageMatchingCriterion{
	static final public int ANY_TAG = -1;

	/**
	 * Analog to MPI: tag, comm:
	 */
	final private int               tag; 	/* Matching of Jobs via TAG && senderIdentity && comm */
	final private Communicator      comm;

	/**
	 * Receiver of the network message
	 */
	final private INodeHostedComponent targetComponent;

	/**
	 * Sender of the network message.
	 */
	final private INodeHostedComponent sourceComponent;

	final private Class<? extends IMessageUserData> matchMessageType;


	public MessageMatchingCriterion(INodeHostedComponent sourceComponent,
			INodeHostedComponent targetComponent, int tag, Communicator comm,
			Class<? extends IMessageUserData> matchMessageType)
	{
		assert(targetComponent != null);
		assert(comm != null);
		assert(matchMessageType != null);

		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.tag = tag;
		this.comm = comm;

		this.matchMessageType = matchMessageType;
	}

	@Override
	public boolean equals(Object obj) {
		MessageMatchingCriterion c = (MessageMatchingCriterion) obj;
		boolean ret = (c.comm == this.comm);
		ret &=(c.getTag() == this.getTag());

		ret &= getSourceComponent() == c.getSourceComponent();

		ret &= getTargetComponent() == c.getTargetComponent();

		ret &= this.matchMessageType == c.matchMessageType;

		return ret;
	}

	@Override
	public String toString() {
		return getSourceComponent() + " - " + getTargetComponent().getIdentifier() + " " + getTag() + " " + getCommunicator() + " " + matchMessageType;
	}

	@Override
	public int hashCode() {
		// sender wildcard
		if(sourceComponent == null){
			return getTargetComponent().hashCode() + getCommunicator().hashCode();
		}else{
			return getTargetComponent().hashCode() + getSourceComponent().hashCode() + getCommunicator().hashCode();
		}
	}

	public INodeHostedComponent getSourceComponent() {
		return sourceComponent;
	}

	public int getTag() {
		return tag;
	}

	public INodeHostedComponent getTargetComponent() {
		return targetComponent;
	}

	public Communicator getCommunicator() {
		return comm;
	}

	public Class<? extends IMessageUserData> getMatchMessageType() {
		return matchMessageType;
	}
}