package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * This class encapsulates the matching of receive network jobs with send jobs.
 * It compares jobs, jobs are considered to be equal if they can be matched.
 *
 * A send and receive is considered to match if the source, tag, communicator matches AND
 * the CommandImplementation.class of the root and the current command match i.e.
 * as a command can spawn nested commands, the root and the current command class must match.
 *
 * @author Julian M. Kunkel
 *
 */
public class MessageMatchingCriterion{
	static final public int ANY_TAG = -1;
	static final public INodeHostedComponent ANY_SOURCE = null;

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

	final private CommandImplementation rootCommand;

	final private CommandImplementation currentCommand;


	public MessageMatchingCriterion(INodeHostedComponent sourceComponent,
			INodeHostedComponent targetComponent, int tag, Communicator comm,
			CommandImplementation rootCommand, CommandImplementation currentCommand)
	{
		assert(targetComponent != null);
		assert(rootCommand != null);
		assert(currentCommand != null);
		assert(comm != null);
		//assert((tag == MessageMatchingCriterion.ANY_TAG  && sourceComponent == null) || (tag != MessageMatchingCriterion.ANY_TAG && sourceComponent != null) );

		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.tag = tag;
		this.comm = comm;

		this.rootCommand = rootCommand;
		this.currentCommand = currentCommand;
	}

	@Override
	public boolean equals(Object obj) {
		MessageMatchingCriterion c = (MessageMatchingCriterion) obj;
		boolean ret = (c.comm == this.comm);
		ret &=(c.getTag() == this.getTag());

		ret &= getSourceComponent() == c.getSourceComponent();

		ret &= getTargetComponent() == c.getTargetComponent();

		ret &= this.rootCommand == c.rootCommand;

		ret &= this.currentCommand == c.currentCommand;

		return ret;
	}

	/**
	 * This function compares this messageCriterion with another Criterion,
	 * both are considered to be equal if ANY_TAG is specified (in this Criterion) and the tags differ,
	 * also if ANY_SOURCE is specified and the source differs.
	 *
	 * @param c
	 * @return
	 */
	public boolean matchesAnySourceOrTagWith(MessageMatchingCriterion c){
		boolean ret = (c.comm == this.comm);

		ret &= (this.tag == ANY_TAG || (this.tag == c.tag) );
		ret &= (this.sourceComponent == ANY_SOURCE || (this.sourceComponent == c.sourceComponent) );

		ret &= getTargetComponent() == c.getTargetComponent();

		ret &= this.rootCommand == c.rootCommand;

		ret &= this.currentCommand == c.currentCommand;

		return ret;
	}

	@Override
	public String toString() {
		return getSourceComponent() + " - " + getTargetComponent().getIdentifier() + " " + getTag() + " " + getCommunicator() + " " + currentCommand + "<-" + rootCommand;
	}

	@Override
	public int hashCode() {
		// sender wildcard
		if(sourceComponent == null){
			return getTargetComponent().hashCode() + getCommunicator().hashCode() + rootCommand.hashCode();
		}else{
			return getTargetComponent().hashCode() + getSourceComponent().hashCode() + getCommunicator().hashCode() + rootCommand.hashCode();
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

	public CommandImplementation getCurrentCommand() {
		return currentCommand;
	}

	public CommandImplementation getRootCommand() {
		return rootCommand;
	}
}