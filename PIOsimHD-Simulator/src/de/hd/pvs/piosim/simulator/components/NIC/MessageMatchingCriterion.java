package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;

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
	final private ISNodeHostedComponent targetComponent;

	/**
	 * Sender of the network message.
	 */
	final private ISNodeHostedComponent sourceComponent;


	public MessageMatchingCriterion(ISNodeHostedComponent sourceComponent,
			ISNodeHostedComponent targetComponent, int tag, Communicator comm)
	{
		assert(targetComponent != null);
		assert(comm != null);

		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.tag = tag;
		this.comm = comm;
	}

	@Override
	public boolean equals(Object obj) {
		MessageMatchingCriterion c = (MessageMatchingCriterion) obj;
		boolean ret = (c.comm == this.comm);
		ret &=(c.getTag() == this.getTag());

		ret &= getSourceComponent() == c.getSourceComponent();

		ret &= getTargetComponent() == c.getTargetComponent();

		return ret;
	}

	@Override
	public String toString() {
		return getSourceComponent() + " - " + getTargetComponent().getIdentifier() + " " + getTag() + " " + getCommunicator();
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

	public ISNodeHostedComponent getSourceComponent() {
		return sourceComponent;
	}

	public int getTag() {
		return tag;
	}

	public ISNodeHostedComponent getTargetComponent() {
		return targetComponent;
	}

	public Communicator getCommunicator() {
		return comm;
	}

}