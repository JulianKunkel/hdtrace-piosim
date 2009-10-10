/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;

public interface TestExecution{
	public void preSimulation(Simulator sim, ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits) throws Exception;
	public void postSimulation(Simulator sim, SimulationResults results, ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits) throws Exception;
}