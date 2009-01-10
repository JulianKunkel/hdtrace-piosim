package de.hd.pvs.piosim.simulator.program.Allreduce;

import de.hd.pvs.piosim.model.program.commands.Allreduce;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class ReduceBroadcast 
extends CommandImplementation<Allreduce>
{
	@Override
	public void process(Allreduce cmd, CommandProcessing OUTresults,
			GClientProcess client, int step, NetworkJobs compNetJobs) 
	{
		final int BROADCAST = 2;
		
		if (step == CommandProcessing.STEP_START){
			// Reduce to root.
			Reduce red = new Reduce();
			red.setCommunicator(cmd.getCommunicator());
			red.setSize(cmd.getSize());
			red.setRootRank(0);
			
			OUTresults.invokeChildOperation(red, BROADCAST);
		}else if(step == BROADCAST){
			Bcast bc = new Bcast();
			bc.setCommunicator(cmd.getCommunicator());
			bc.setSize(cmd.getSize());
			bc.setRootRank(0);
			
			// broadcast from root.
			OUTresults.invokeChildOperation(bc, CommandProcessing.STEP_COMPLETED);
		}
	}
}
