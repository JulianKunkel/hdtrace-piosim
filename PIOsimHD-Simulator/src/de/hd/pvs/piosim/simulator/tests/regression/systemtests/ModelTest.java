/** Version Control Information $Id: ClusterTest.java 769 2010-07-03 18:38:47Z kunkel $
 * @lastmodified    $Date: 2010-07-03 20:38:47 +0200 (Sa, 03. Jul 2010) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 769 $
 */

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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import junit.framework.Assert;
import junit.framework.TestSuite;

import org.junit.After;

import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelXMLWriter;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.HardwareConfiguration;

abstract public class ModelTest extends TestSuite {
	protected final long KB = 1000;
	protected final long MB = 1000 * KB;
	protected final long GB = 1000 * MB;

	protected final long KiB = 1024;
	protected final long MiB = 1024 * KiB;
	protected final long GiB = 1024 * MiB;

	protected ModelBuilder mb;
	protected ApplicationBuilder aB;
	protected ProgramBuilder pb;
	protected Application app;
	protected Model model;
	protected Simulator sim;
	protected SimulationResults simRes;
	protected Communicator world;

	protected INetworkTopology topology;

	protected RunParameters parameters = new RunParameters();

	@After
	public void tearDown() {
		System.out.println();
		Assert.assertTrue(true); /* to ensure assert stays */
	}

	protected void printStack() {
		System.err.println(new Exception().getStackTrace()[1]);
	}

	abstract protected void postSetup();

	protected void setup(HardwareConfiguration config) throws Exception {
		System.err.println(new Exception().getStackTrace()[1]);

		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");

		parameters.setTraceFile("/tmp/output");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(false);
		parameters.setTraceClientSteps(true);
		parameters.setTraceServers(true);
//		parameters.setDebugEverything(true);

		PaketRoutingAlgorithm routingAlgorithm = new PaketFirstRoute();

		mb = new ModelBuilder();

		topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routingAlgorithm);

		config.createModel("", mb, topology);

		aB = new ApplicationBuilder("Jacobi", "Example Jacobi", mb.getModel().getClientProcesses().size(), 1);
		app = aB.getApplication();

		pb = new ProgramBuilder(aB);

		// assign client processes - TODO abstract and re-factor similar as hardware tests.

		for(int rank = 0; rank < mb.getModel().getClientProcesses().size(); rank++){
			ClientProcess c = mb.getModel().getClientProcesses().get(rank);
			c.setApplication("Jacobi");

			if(c.getRank() < 0){
				c.setRank(rank);
			}
		}

		world = aB.getWorldCommunicator();

		assert (world != null);

		model = mb.getModel();

		postSetup();
	}

	public void writeModelToTMP() throws Exception{
		// write out model to /tmp
		final ModelXMLWriter writer = new ModelXMLWriter();
		writer.writeXMLFromModel(model, "/tmp/model.mxml");
	}

	protected SimulationResults runSimulationAllExpectedToFinish() throws Exception {

		if(mb.getModel().getApplicationNameMap().get("Jacobi") == null){
			mb.setApplication("Jacobi", app);
		}

		//ModelSortIDbySubcomponents sorter = new ModelSortIDbySubcomponents();
		//sorter.sort(model);

		// ModelXMLWriter xmlW = new ModelXMLWriter();
		// xmlW.writeXMLFromProject(model, "/tmp/", "test.xml");
		// TESTING

		// parameters.setTraceInternals(false);

		sim = new Simulator();
		sim.initModel(model, parameters);
		simRes = sim.simulate();

		final SimulationResultSerializer serializer = new SimulationResultSerializer();
		System.out.println(serializer.serializeResults(simRes));

		if(simRes.isErrorDuringProcessing()){
			throw new IllegalArgumentException("Errors occured during processing");
		}

		return simRes;
	}


	protected SimulationResults runSimulationWithoutOutput() throws Exception {

		if(mb.getModel().getApplicationNameMap().get("Jacobi") == null){
			mb.setApplication("Jacobi", app);
		}

		sim = new Simulator();
		sim.initModel(model, parameters);
		simRes = sim.simulate();

		System.out.println("events: " + simRes.getEventCount() + " time: " + simRes.getWallClockTime());

		if(simRes.isErrorDuringProcessing()){
			throw new IllegalArgumentException("Errors occured during processing");
		}

		return simRes;
	}

	/**
	 * One might change the method to invoke...
	 *
	 * @return
	 */
	public GlobalSettings getGlobalSettings() {
		return model.getGlobalSettings();
	}
}
