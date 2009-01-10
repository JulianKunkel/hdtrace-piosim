
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

/**
 * 
 */
package de.hd.pvs.piosim.simulator.program.Wait;

import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
import de.hd.pvs.piosim.simulator.program.IWaitCommand;

/**
 * @author Julian M. Kunkel
 *
 */
public class Blocking 
	extends CommandImplementation<Wait>
	implements IWaitCommand
{
	
	HashSet<Integer> pendingAIOs = new HashSet<Integer>();
	
	@Override
	public void process(Wait cmd,  CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {			
		// two possibilities, either all pending AIO ops are already finished or not
		HashMap<Integer, Command> stillPendingOps = client.getPendingNonBlockingOps();
		
		for(Integer i: cmd.getWaitFor()){
			if(stillPendingOps.containsKey(i)){
				pendingAIOs.add(i);
			}
		}
		
		if(pendingAIOs.size() != 0){
			OUTresults.setBlocking();
		}
		
	}
	
	/**
	 * A pending AIO operation finished
	 * 
	 * @param which one finished
	 */
	@Override
	public void pendingAIOfinished(Wait cmd, CommandProcessing prevStep, GClientProcess client, Integer which){
		assert(pendingAIOs.contains(which));
		pendingAIOs.remove(which);
		
		if(pendingAIOs.size() == 0)		
			client.activateBlockedCommand(prevStep);
	}
}
