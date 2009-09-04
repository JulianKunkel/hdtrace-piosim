package de.hd.pvs.piosim.simulator;

import java.util.HashMap;

import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;

/**
 * Parent class providing a simple serialization of the <code>SimulationResults</code> to a StringBuffer.
 * Components are printed by ID.
 *
 * @author julian
 */
public class SimulationResultSerializer {

	/**
	 * Serialize the simulation results into a single string buffer
	 *
	 * @author julian
	 */
	public StringBuffer serializeResults(SimulationResults results){
		final StringBuffer buff = new StringBuffer();

		final HashMap<ComponentIdentifier, ComponentRuntimeInformation>  map = results.getComponentStatistics();
		for(ComponentIdentifier cid: map.keySet()){
			buff.append(results.getComponents().get(cid).getClass().getSimpleName() + " " +  cid + map.get(cid) + "\n");
		}

		return buff;
	}

	protected void serializeSimulationOutput(StringBuffer buff, SimulationResults results){
		buff.append("simulation finished: " + results.getEventCount() + " events" + "\n"
		+ "\t realTime: "+ results.getWallClockTime() + "s\n"
		+ "\t events/sec: " + (results.getEventCount() / results.getWallClockTime()) + "\n"
		+ "\t virtualTime: " + results.getVirtualTime() + "\n"
		+ "\t virtualTime/realTime: " + (results.getVirtualTime().getDouble() / results.getWallClockTime()));
	}
}
