package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

/**
 * GBusExit is a routable target, other GBus entities are no targets.
 * @author julian
 *
 */
public class GNetworkNodeExit implements IGNetworkExit, IGNetworkNode{

	private void completeReceive(SingleNetworkJob job, SingleNetworkJob response){

		jobs.jobCompletedRecv(response);
		if(jobs.isCompleted()){
			jobs.getInitialRequestDescription().getInvokingComponent().jobsCompletedCB(jobs, time);
		}
	}
}
