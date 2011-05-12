package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NICAnalytical;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;


/**
 *  Use analytical model to compute time for completion.
 *  The arrival of a message is determined without taking any congestion into account.
 *  The route is determined, then the packet gets routed from start to end and arrives in one large messagePart at the destination.
 */
public class GProcessNetworkInterfaceAnalytical extends
		GProcessNetworkInterface
{
	@Override
	public void submitNewMessage(Message msg, Epoch startTime) {
		assert(msg.getSize() > 0);

		// create one large messagePart from the message
		final MessagePart part = msg.createNextMessagePart(msg.getSize());
		if(part == null){
			/* does not make any sense to send an empty message, it will be appended later */
			return;
		}

		// determine route and latency involved, remember start node and end node
		ISNetworkComponent current = this;

		if(msg.getMessageTarget().getClass() != this.getModelComponent().getClass()){
			// it works, but be aware that something strange might happen if you transfer between a normal NIC and an analytical NIC
			//throw new IllegalArgumentException("This NIC can send data only to another NIC with an analytical model!");
		}

		final NICAnalytical target =  (NICAnalytical) msg.getMessageTarget();

		// accumulate time
		Epoch latency = startTime;
		// take the maximum processing time
		Epoch maxProcessingTime = Epoch.ZERO;
		// track route
		while(current.getModelComponent() != target){
			// add time as given by the component:
			latency =  latency.add(current.getProcessingLatency());

			Epoch myTime = current.getProcessingTime(part);
			if(myTime.compareTo(maxProcessingTime) > 0){
				maxProcessingTime = myTime;
			}

			// follow the route
			current = current.getTargetFlowComponent(part);
		}

		// submit this event directly to the target NIC and prepend
		final Event<MessagePart> event = new Event(current, current, latency.add(maxProcessingTime),  part, msg.getRelationToken());
		getSimulator().submitNewEvent(event);
	}
}
