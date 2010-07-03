package de.hd.pvs.piosim.simulator.base;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface ISNetworkComponent<Type extends IBasicComponent>
	extends ISBasicComponent<Type>
{
	/**
	 * Lookup the component the particular job must be delivered.
	 * @param event
	 * @return
	 */
	abstract public ISNetworkComponent getTargetFlowComponent(MessagePart event);

	/**
	 * Gets called if a job got transferred to the right target.
	 * @param part
	 */
	public abstract void messagePartTransmitted(MessagePart part, Epoch endTime);

	/**
	 * Gets called if an event is destroyed i.e. has no target.
	 * @param part
	 */
	public abstract void messagePartDestroyed(MessagePart part, Epoch endTime);

	/**
	 * The component can control the Flow either by sending at most a number of jobs with a runtime
	 * of the latency + processing time OR by directly blocking and unblocking the Flow.
	 */
	public abstract boolean isDirectlyControlledByBlockUnblock();

	/**
	 * The time a job takes to arrive on the component it gets submitted, i.e. transfer time between
	 * cable and receiver.
	 * @return
	 */
	public abstract Epoch getProcessingLatency();

	/**
	 * The maximum time a job needs to get processed.
	 * @return
	 */
	public abstract Epoch getMaximumProcessingTime();

	/**
	 * Return the processing time.
	 * Must be invariant for subsequent calls to this method.
	 * @param part
	 * @return
	 */
	abstract public Epoch getProcessingTime(MessagePart part);

	/**
	 * Block the data flow to a specific target, the <code>unblockFlow</code> is used to restart it.
	 * @param target
	 */
	public abstract void blockFlow(INetworkExit target);

	/**
	 * Unblock a flow which was blocked with blockFlow to a specific target
	 * @param target
	 */
	public abstract void unblockFlow(INetworkExit target);

	/**
	 * This function is called once a packet is transfered by the next component.
	 * @param target
	 */
	public abstract void packetIsTransferedToTarget(INetworkExit target);
}