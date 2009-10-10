package de.hd.pvs.piosim.simulator.base;

import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkFlowComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Basic network transport ideas:
 * First push data, then data flow can be blocked, once data is processed pull new data.
 *
 * - edges contain a list of operations in flight to saturate the network bandwidth product - like a FIFO puffer.
 * -- for each network exit one FIFO puffer is held (avoid starvation of fast edges by slow edges)
 * - nodes are routers i.e. determine for each packet the right edge to flow on => n-input and m-output edges
 * -- edges deliver packets to nodes, nodes can block further incoming packets when the target is blocked as well.
 * -- store and forward nodes use the same push/pull strategy as edges to account for low internal bandwidth to move pakets to outgoing edges
 *
 * @author julian
 *
 * @param <ModelType>
 */
public interface IGNetworkFlowComponent<ModelType extends INetworkFlowComponent> extends ISPassiveComponent<ModelType>{

	/**
	 * The node is responsible to handle local traffic (i.e. if it is a network exit).
	 * @param part
	 */
	public void submitMessagePart(MessagePart part);

	/**
	 * Might the given message part be submitted to the network flow component at the
	 * current time.
	 *
	 * @param part
	 * @return
	 */
	public boolean announceSubmissionOf(MessagePart part);

	/**
	 * If a messagePart might not be submitted to a component, then
	 * the component shall block further transfer and remember this decision.
	 * Once data flows out incoming (and blocked components) shall be reactivated in
	 * round robin.
	 */
	public void rememberBlockedDataPushFrom(IGNetworkFlowComponent src, INetworkExit exit);

	/**
	 * Unblock the given network exit i.e. allow transport of new packets for the given exit
	 * This function can be called ONLY if:
	 * - the transfer was blocked earlier i.e. mayISubmit... returned false the last time for this exit
	 * If it it is called multiple times then it breaks.
	 *
	 * @param exit
	 */
	public void unblockExit(INetworkExit exit);
}
