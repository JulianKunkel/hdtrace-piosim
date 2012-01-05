package de.hd.pvs.piosim.simulator.program.Filesetview;

import de.hd.pvs.piosim.model.program.commands.Filesetview;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class FileSetViewNormal
extends CommandImplementation<Filesetview>
{
	@Override
	public void process(Filesetview cmd, ICommandProcessing outCommand,
			GClientProcess client, long step, NetworkJobs compNetJobs) {
		// TODO Auto-generated method stub

	}
}
