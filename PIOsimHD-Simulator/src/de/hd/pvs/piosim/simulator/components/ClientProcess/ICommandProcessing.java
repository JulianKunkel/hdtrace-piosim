package de.hd.pvs.piosim.simulator.components.ClientProcess;

import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;


public interface ICommandProcessing extends ICommandProcessingMapped{
	/**
	 * @param from if you want to receive from any source, then use addNetReceiveAnySource
	 * @param tag
	 * @param comm
	 * @param matchMessageType
	 */
	public void addNetReceive(INodeHostedComponent from, int tag, Communicator comm);

	public void addNetReceive(INodeHostedComponent from, int tag, Communicator comm,  Class<? extends CommandImplementation> expectedRootOperation,  Class<? extends CommandImplementation> expectedProcessingMethod);

	public void addNetSend(INodeHostedComponent to, IMessageUserData jobData, int tag, Communicator comm);

	public void addNetSend(INodeHostedComponent to,
			IMessageUserData jobData, int tag, Communicator comm,  Class<? extends CommandImplementation> definedRootOperation,  Class<? extends CommandImplementation> definedProcessingMethod);


	/**
	 * Allow to override the rootOperation and processingMethod to enable communication between collective calls (or client/server matching).
	 *
	 * @param from
	 * @param tag
	 * @param comm
	 * @param expectedRootOperation
	 * @param expectedProcessingMethod
	 */
	public void addNetSend(INodeHostedComponent to,
			IMessageUserData jobData, int tag, Communicator comm, CommandImplementation definedRootOperation, CommandImplementation definedProcessingMethod);


}
