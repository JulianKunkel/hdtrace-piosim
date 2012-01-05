package de.hd.pvs.piosim.simulator.base;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
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
	 * The time a job takes to arrive on the component it gets submitted, i.e. transfer time between
	 * cable and receiver.
	 * @param part TODO
	 * @return
	 */
	public abstract Epoch getProcessingLatency(MessagePart part);

	public abstract Epoch getMaximumProcessingLatency();

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
	public abstract void blockFlowManually();

	/**
	 * Unblock a flow which was blocked with blockFlow to a specific target
	 * @param target
	 */
	public abstract void unblockFlowManually();

	/**
	 * This function is called once a packet is transfered by the next component.
	 * @param target
	 */
	//public abstract void packetIsTransferedToTarget(INetworkExit target);
}