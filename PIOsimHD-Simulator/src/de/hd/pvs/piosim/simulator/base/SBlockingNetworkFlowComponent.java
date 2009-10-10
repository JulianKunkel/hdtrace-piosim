package de.hd.pvs.piosim.simulator.base;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkFlowComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

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

	private static class ChannelStatus{
		// amount of data under way.
		Epoch usedLatency = Epoch.ZERO;

		// transfer to the given exit is blocked?
		boolean blockedDownstream = false;

		// list of blocked incoming data sources
		final LinkedList<IGNetworkFlowComponent> blockedComponentsUpstream = new LinkedList<IGNetworkFlowComponent>();
	}

	/**
	 * The sum of the latency of all jobs which are processed in the following component.
	 * */
	private HashMap<INetworkExit, ChannelStatus> pendingStatus = new HashMap<INetworkExit, ChannelStatus>();

	/**
	 * Maximum time needed to transport a packet through this edge.
	 */
	private Epoch maximumInflightDuration;

	private MessagePart scheduledPart = null;

	/**
	 * Overload to update the maximum duration correctly.
	 */
	public void simulationModelIsBuild() {
		super.simulationModelIsBuild();
		maximumInflightDuration = getMaximumTransportDurationForAMessagePart();
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
	 * Consequently all messages of this exit must be removed from the queue.
	 * This method is called ONLY if it is determined that the next network part
	 * cannot be scheduled i.e. the target denies send by mayISend...
	 */
	abstract protected void blockPushForExit(INetworkExit exit);

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
	 * Might the given message part be submitted to the network flow component at the
	 * current time.
	 *
	 * announceSubmissioOf(part)
	 * processPart
	 * submitMessagePart(part) => packet arrives now
	 *
	 * @param part
	 * @return
	 */
	public boolean announceSubmissionOf(MessagePart part){
		ChannelStatus cur = pendingStatus.get(part.getMessageTarget());
		if(cur == null){
			cur = new ChannelStatus();
			pendingStatus.put(part.getMessageTarget(), cur);
		}

		// register packet.
		//System.out.println( this.getIdentifier() + " announceSubmissionOf latency:" + cur.usedLatency + " " + cur.blockedDownstream + " to:" + part.getMessageTarget().getIdentifier() + " " + state);

		if( cur.usedLatency.compareTo(maximumInflightDuration) < 0 ){
			cur.usedLatency = cur.usedLatency.add(computeTransportTime(part));

			return true;
		}else{
			return false;
		}
	}


	/**
	 * Submit a message part to the edge at the current time!
	 *
	 * @param part
	 */
	public void submitMessagePart(MessagePart part){
		//System.out.println(this.getIdentifier() + " submitMessagePart " + state);

		if(isEmpty() && state == State.READY){
			// this is the first packet from a data stream => allow transport from this source

			// check if we shall reactivate this component.
			// this is actually the first message part received => reactivate this component
			setNewWakeupTimeNow();
		}

		addNetworkPart(part);
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
	final public void unblockExit(INetworkExit exit) {

		//System.out.println( this.getIdentifier() + " Unblock block src  to " + exit.getIdentifier() + " " + state);

		// restart transmission for this exit target
		final ChannelStatus status = pendingStatus.get(exit);

		// it must be blocked if the method is called
		assert(status.blockedDownstream == true);

		status.blockedDownstream = false;

		unblockBlockedExit(exit);

		if(state == State.READY){
			// no other pending packets => reactivate this component
			setNewWakeupTimeNow();
		}
	}

	@Override
	final public void rememberBlockedDataPushFrom(IGNetworkFlowComponent src, INetworkExit exit)
	{
		//System.out.println(this.getIdentifier() + " rememberBlockedDataPushFrom src " + src.getIdentifier() + " to " + exit.getIdentifier());

		final ChannelStatus status = pendingStatus.get(exit);

		// per component it shall be called only once.
		assert(! status.blockedComponentsUpstream.contains(src));

		status.blockedComponentsUpstream.add(src);
	}

	@Override
	final public void processInternalEvent(InternalEvent event, Epoch time) {
		//System.out.println( this.getIdentifier() + " processInternalEvent " + state + " at " + time);

		if(state == State.BUSY){
			assert(scheduledPart != null);

			messageTransferCompletedEvent(scheduledPart);

			// submit packet:
			getTargetComponent(scheduledPart).submitMessagePart(scheduledPart);

			scheduledPart = null;

			getSimulator().getTraceWriter().endState(TraceType.INTERNAL, this, "Msg-Part");

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

			final INetworkExit exit = part.getMessageTarget();

			final IGNetworkFlowComponent next = getTargetComponent(part);
			final ChannelStatus status = pendingStatus.get(exit);

			assert(next!= null);
			assert(status.blockedDownstream == false);

			if( next.announceSubmissionOf(part) ){
				state = State.BUSY;

				getSimulator().getTraceWriter().startState(TraceType.INTERNAL, this, "Msg-Part");

				final Epoch transportTime = computeTransportTime(part);
				setNewWakeupTimerInFuture(transportTime);

				// remove the packet latency.
				status.usedLatency = status.usedLatency.subtract(transportTime);

				// remove first pending message part
				this.scheduledPart = pollScheduledNetworkPart();

				// now try to reactivate blocked senders if necessary
				if(status.blockedComponentsUpstream.size() > 0){
					// sources are blocked => reactivate first blocked.
					status.blockedComponentsUpstream.poll().unblockExit(exit);
				}
				messageTransferStartedEvent(part);

				return;
			}

			// data flow is blocked => try next.
			// remember that data flow is blocked to allow retransmit
			status.blockedDownstream = true;

			next.rememberBlockedDataPushFrom(this, exit);

			blockPushForExit(exit);
		}

		// no matching packet found => state == ready
		//System.out.println(" No matching packet => now ready");

		state = State.READY;
	}

	public void simulationFinished() {
		// check for error states
		assert(state == State.READY);

		for(INetworkExit exit: pendingStatus.keySet()){
			if(pendingStatus.get(exit).blockedDownstream){
				System.out.println(this.getIdentifier() + " my downstream I/O is blocked to " + exit);
			}
		}
	};
}
