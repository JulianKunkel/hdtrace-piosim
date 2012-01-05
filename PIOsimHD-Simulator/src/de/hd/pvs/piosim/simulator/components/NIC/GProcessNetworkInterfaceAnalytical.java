package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NICAnalytical;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
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
	public ISNetworkComponent getTargetFlowComponent(MessagePart part) {
		if(part.getMessage().getMessageSource() == part.getMessage().getMessageTarget()){
			return this;
		}

		if( part.getMessageTarget().getClass() != this.getModelComponent().getClass()){
			// it works, but be aware that something strange might happen if you transfer between a normal NIC and an analytical NIC
			//throw new IllegalArgumentException("This NIC can send data only to another NIC with an analytical model!");

			return routing.getTargetRouteForMessage(this.getModelComponent(), part);
		}
		return (ISNetworkComponent) getSimulator().getSimulatedComponent(part.getMessageTarget());
	}


	@Override
	public Epoch getMaximumProcessingTime() {
		// allow to transfer messages, although the target might not be ready.
		return new Epoch(10000000);
	}

	@Override
	public Epoch getProcessingLatency(MessagePart part) {
		if(part == null || part.getMessageTarget() == this.getModelComponent()){
			return Epoch.ZERO;
		}

		final NICAnalytical target =  (NICAnalytical) part.getMessageTarget();

		// accumulate time
		Epoch latency = Epoch.ZERO;
		ISNetworkComponent current = routing.getTargetRouteForMessage(this.getModelComponent(), part);
		// track route
		while(current.getModelComponent() != target){
			// add time as given by the component:
			Epoch curLatency = current.getProcessingLatency(part);
			latency =  latency.add(curLatency);
			Epoch myTime = current.getProcessingTime(part);
			part.updateCurrentState(current, myTime, curLatency);

			// follow the route
			current = current.getTargetFlowComponent(part);
		}
		return latency;
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {

		if(part == null || part.getMessageTarget() == this.getModelComponent()){
			return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
		}

		final NICAnalytical target =  (NICAnalytical) part.getMessageTarget();

		// take the maximum processing time
		Epoch maxProcessingTime = Epoch.ZERO;

		ISNetworkComponent current = routing.getTargetRouteForMessage(this.getModelComponent(), part);
		// track route
		while(current.getModelComponent() != target){
			// add time as given by the component:
			Epoch curLatency = current.getProcessingLatency(part);
			Epoch myTime = current.getProcessingTime(part);
			part.updateCurrentState(current, myTime, curLatency);

			if(myTime.compareTo(maxProcessingTime) > 0){
				maxProcessingTime = myTime;
			}

			// follow the route
			current = current.getTargetFlowComponent(part);
		}
		return maxProcessingTime;
	}


// Transport the WHOLE message at once:
//	@Override
//	public void submitNewMessage(Message msg, Epoch startTime) {
//
//		// create one large messagePart from the message
//		final MessagePart part;
//		if(msg.getSize() == 0){
//			part = msg.createEmptyMessage();
//		}else{
//			part = msg.createNextMessagePart(msg.getSize());
//		}
//
//
//		// determine route and latency involved, remember start node and end node
//		ISNetworkComponent current = this;
//
//		if(msg.getMessageTarget().getClass() != this.getModelComponent().getClass()){
//			// it works, but be aware that something strange might happen if you transfer between a normal NIC and an analytical NIC
//			//throw new IllegalArgumentException("This NIC can send data only to another NIC with an analytical model!");
//		}
//
//		final NICAnalytical target =  (NICAnalytical) msg.getMessageTarget();
//
//		// accumulate time
//		Epoch latency = startTime;
//		// take the maximum processing time
//		Epoch maxProcessingTime = Epoch.ZERO;
//		// track route
//		while(current.getModelComponent() != target){
//			// add time as given by the component:
//
//			Epoch curLatency = current.getProcessingLatency(part);
//			latency =  latency.add(curLatency);
//			Epoch myTime = current.getProcessingTime(part);
//			part.updateCurrentState(current, myTime, curLatency);
//
//			if(myTime.compareTo(maxProcessingTime) > 0){
//				maxProcessingTime = myTime;
//			}
//
//			// follow the route
//			current = current.getTargetFlowComponent(part);
//		}
//
//		// submit this event directly to the target NIC and prepend
//		final Event<MessagePart> event = new Event(current, current, latency.add(maxProcessingTime),  part, msg.getRelationToken());
//		getSimulator().submitNewEvent(event);
//	}
}
