package de.hd.pvs.piosim.simulator.components.ClientProcess;

import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public interface ICommandProcessingMapped {

	/**
	 * This method allows the command to create a set of nested operations, which are run concurrently .
	 * The commands shall not be modified afterwards.
	 * @param enforceProcessingMethod optional parameter, if null the default implementation is used
	 */
	public void invokeChildOperation(final Command nestedCmd, long nextStep,	Class<? extends CommandImplementation> enforceProcessingMethod);

	/**
	 * This method allows the command to create a nested operation.
	 * The command shall not be modified afterwards.
	 *
	 *
	 * Child operations are always tagged as asynchronous operations if multiple are issued.
	 * 	When one child operation is issued it is mapped as a nested operation, otherwise as an asynchronous operation.
	 *
	 */
	public void invokeChildOperations(final Command[] nestedCmd, long nextStep, Class<? extends CommandImplementation>[] enforceProcessingMethod);

	/**
	 * Add a network receive to be performed by the command.
	 *
	 * @param from
	 * @param tag
	 * @param comm
	 */
	public void addNetReceive(int rankFrom, int tag, Communicator comm);

	public void addNetReceive(int from, int tag, Communicator comm,
			Class<? extends CommandImplementation> expectedRootOperation,
			Class<? extends CommandImplementation> expectedProcessingMethod);

	/**
	 * Add a network send to be performed by the command.
	 *
	 * @param to
	 * @param jobData
	 * @param tag
	 * @param comm
	 * @param shouldPartialRecv
	 */
	public void addNetSend(int rankTo, IMessageUserData jobData, int tag, Communicator comm);

	public void addNetSend(int rankTo, IMessageUserData jobData,
			int tag, Communicator comm,
			Class<? extends CommandImplementation> definedRootOperation,
			Class<? extends CommandImplementation> definedProcessingMethod);


	public void addNetSendRoutable(ClientProcess client, INodeHostedComponent finalTarget, INodeHostedComponent nextHop, IMessageUserData jobData, int tag, Communicator comm);


	public void setNextStep(long nextStep);

	public void setBlocking();

	public Command getInvokingCommand();

	public NetworkJobs getNetworkJobs();

	public GClientProcess getInvokingComponent();
}
