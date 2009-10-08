package de.hd.pvs.piosim.simulator.base;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkFlowComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Components extending this class schedule operations.
 *
 * @author julian
 * @param <ModelComp>
 */
abstract public class SBlockingNetworkFlowComponent<ModelComp extends INetworkFlowComponent>
	extends SBasicComponent<ModelComp>
	implements IGNetworkFlowComponent<ModelComp>
{

	/** Internal states, the busy component processes a job right now */
	static public enum State {
		BUSY, READY
	}

	/**
	 * the internal State of the component, is it busy or ready right now.
	 */
	protected State state = State.READY;

	private static class PendingLatencies{
		Epoch usedLatency = Epoch.ZERO;
	}

	/**
	 * The sum of the latency of all jobs which are processed in the following component.
	 * */
	private HashMap<INetworkExit, PendingLatencies> pendingLatencies = new HashMap<INetworkExit, PendingLatencies>();

	/**
	 * Maximum time needed to transport a packet through this edge.
	 */
	private Epoch maximumTransportDuration;

	/**
	 * Overload to update the maximum duration correctly.
	 */
	public void simulationModelIsBuild() {
		super.simulationModelIsBuild();
		maximumTransportDuration = getMaximumTransportDurationForAMessagePart();
	}

	/**
	 * Return the target component for a given packet
	 * @param part
	 * @return
	 */
	abstract protected IGNetworkFlowComponent getTargetComponent(MessagePart part);

	/**
	 * Compute the maximum time to transfer an message,
	 * it is used to determine the maximum tolerated latency.
	 * @return
	 */
	abstract protected Epoch getMaximumTransportDurationForAMessagePart();

	/**
	 * For a given MessagePart the same time must be returned with
	 * subsequent calls to this method.
	 *
	 * @param part
	 * @return
	 */
	abstract protected Epoch computeTransportTime(MessagePart part);

	/**
	 * Add a network part to be queued.
	 * @param part
	 */
	abstract protected void addNetworkPart(MessagePart part);

	/**
	 * @return the next scheduled network part
	 */
	abstract protected MessagePart peekScheduledNetworkPart();

	/**
	 * @return the next scheduled network part and remove it, it is scheduled now.
	 */
	abstract protected MessagePart pollScheduledNetworkPart();

	/**
	 * The currently scheduled (next) exit shall be blocked.
	 * Consequently all messages of this exit must be removed from the queue
	 */
	abstract protected void blockScheduledExit();

	/**
	 * Unblock a currently blocked exit
	 * @param exit
	 */
	abstract protected void unblockBlockedExit(INetworkExit exit);

	/**
	 * Is another message part schedulable?
	 * @return
	 */
	abstract protected boolean isEmpty();


	/**
	 * Submit a message part to the edge at the current time!
	 *
	 * @param part
	 */
	final public void submitMessagePart(MessagePart part){
		PendingLatencies cur = pendingLatencies.get(part.getNetworkTarget());
		// it is only allowed to submit a job when the latency is not used up.
		assert(cur.usedLatency.compareTo(maximumTransportDuration) < 0);

		cur.usedLatency = cur.usedLatency.add(computeTransportTime(part));

		if(isEmpty()){
			// this is the first packet from a data stream => allow transport from this source

			// check if we shall reactivate this component.
			if(state == State.READY){
				// this is actually the first message part received => reactivate this component
				setNewWakeupTimeNow();
			}
		}

		addNetworkPart(part);
	}

	@Override
	final public void unblockExit(INetworkExit exit) {
		// restart transmission for this exit target
		unblockBlockedExit(exit);

		if(state == State.READY){
			// no other pending packets => reactivate this component
			setNewWakeupTimeNow();
		}
	}

	/**
	 * Might the given message part be submitted to the network flow component at the
	 * current time.
	 *
	 * @param part
	 * @return
	 */
	public boolean mayISubmitAMessagePart(MessagePart part){
		PendingLatencies cur = pendingLatencies.get(part.getNetworkTarget());
		if(cur == null){
			cur = new PendingLatencies();
			pendingLatencies.put(part.getNetworkTarget(), cur);
		}
		return cur.usedLatency.compareTo(maximumTransportDuration) < 0;
	}

	/**
	 * This function gets called if a job completed.
	 */
	protected void messageTransferStartedEvent(MessagePart part){

	}

	/**
	 * This function gets called if a job is started to be serviced.
	 * It should be overridden if needed.
	 */
	protected void messageTransferCompletedEvent(MessagePart part){

	}

	/**
	 * We shall never get an event (except an internal one)
	 */
	@Override
	final public void processEvent(Event event, Epoch time) {
		throw new IllegalArgumentException("You shall never send an event to " + this.getClass().getCanonicalName());
	}

	@Override
	final public void processInternalEvent(InternalEvent event, Epoch time) {
		assert(! isEmpty());

		if(state == State.BUSY){
			// remove first pending message part
			final MessagePart part = pollScheduledNetworkPart();
			final INetworkExit exit = part.getNetworkTarget();
			final PendingLatencies latency = pendingLatencies.get(exit);

			// remove the packet latency.
			latency.usedLatency = latency.usedLatency.subtract(computeTransportTime(part));

			messageTransferCompletedEvent(part);

			// submit packet:
			getTargetComponent(part).submitMessagePart(part);

			if(isEmpty()){
				state = State.READY;
				return;
			}

			// otherwise continue processing
		}

		// state == Ready

		// function shall never be called if empty
		assert(! isEmpty());

		// start transfer of next message part if possible:
		while(! isEmpty()){
			final MessagePart part = peekScheduledNetworkPart();
			assert(part != null);

			// check if we are the final target.
			if(part.getNetworkTarget() == this.getModelComponent()){
				final IGNetworkExit exit = ((IGNetworkExit) this);

				// check if we can receive the packet:
				if(exit.mayIReceiveAMessagePart(part)){
					// remove the message part from the queue
					pollScheduledNetworkPart();

					// yes, so we received the packet
					exit.messagePartReceived(part);
					continue;
				}else{
					// data flow is blocked => try next.
					blockScheduledExit();
					continue;
				}
			}

			final IGNetworkFlowComponent next = getTargetComponent(part);

			assert(next!= null);

			if( next.mayISubmitAMessagePart(part) ){
				messageTransferStartedEvent(part);
				setNewWakeupTimerInFuture(computeTransportTime(part));

				state = State.BUSY;
				return;
			}
			// data flow is blocked => try next.
			blockScheduledExit();
		}
	}
}
