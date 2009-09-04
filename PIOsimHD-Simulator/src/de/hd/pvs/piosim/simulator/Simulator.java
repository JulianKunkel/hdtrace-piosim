
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
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

package de.hd.pvs.piosim.simulator;

/* $Id$
 * $Date$
 * $Author$
 * $Revision$
 */

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.ModelVerifier;
import de.hd.pvs.piosim.model.ModelXMLReader;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Switch.Switch;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper.ModelObjectMap;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.ApplicationMap;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.components.Switch.GSwitch;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.output.SHDTraceWriter;
import de.hd.pvs.piosim.simulator.output.STraceWriter;

/**
 * The class <code>Simulator</code> manages the simulation.
 * All objects required for the simulation are aggregated and referenced by a Simulator.
 *
 * @author Julian M. Kunkel
 */
public final class Simulator{
	// simulator version:
	private static final float version = 0.89f;

	// stores global parameters, e.g. file names
	private RunParameters runParameters = null;

	// the model behind the simulation
	private Model model;

	// output trace file
	private STraceWriter traceWriter = null;

	// mapping from application aliases to applications and GClients.
	private ApplicationMap applicationMap = new ApplicationMap();


	public STraceWriter getTraceWriter() {
		return traceWriter;
	}

	/**
	 * @return the runParameters
	 */
	public RunParameters getRunParameters() {
		return runParameters;
	}

	/**
	 * Contains all events which will arrive in the future.
	 */
	private PriorityQueue<InternalEvent> futureEvents = new PriorityQueue<InternalEvent>();

	/**
	 * We have to find the SimulationComponent belonging to a model component.
	 */
	private HashMap<ComponentIdentifier, SPassiveComponent> existingSimulationObjects =
		new HashMap<ComponentIdentifier, SPassiveComponent>();


	public HashMap<ComponentIdentifier, SPassiveComponent> getExistingSimulationObjects() {
		return existingSimulationObjects;
	}

	/** the current epoch of the simulator i.e. the simulated time we are right now */
	private Epoch currentEpoch = Epoch.ZERO;

	/**
	 * Check the model and prepare the simulation
	 * @param model
	 * @param parameters
	 * @throws Exception
	 */
	private void initModel(Model model, RunParameters parameters) throws Exception {
		this.runParameters = parameters;
		this.model = model;

		// load the logger definition:
		ConsoleLogger.init(parameters.getLoggerDefinitionFile(), parameters.isDebugEverything());

		ConsoleLogger.getInstance().info("Version:" + version);

		// check the consistency of the model to detect errors in advance.
		checkModelConsistency(model);

		// load trace writer
		traceWriter = new SHDTraceWriter(runParameters.traceFile, this);

		/* create clients and servers from the model */
		ArrayList<GNode> nodes = new ArrayList<GNode>();
		for(Node com: model.getNodes()){
			GNode m = (GNode) instantiateSimObjectForModelObj(com);


			addSimulatedComponent(m);

			nodes.add(m);

			/* put clients into the application map */
			for(GClientProcess gc: m.getClients()){
				ClientProcess c = gc.getModelComponent();
				assert(c != null);
				applicationMap.put(c.getApplication(), c.getRank(), gc);
			}

		}

		/* create switches */
		ArrayList<GSwitch>  switches  = new ArrayList<GSwitch>();
		for(Switch com: model.getSwitches()){
			GSwitch sw = (GSwitch) instantiateSimObjectForModelObj(com);
			addSimulatedComponent(sw);

			switches.add(sw);
		}

		/* populate routing tables, iterate until the topology is stable which requires
		 * diameter(Cluster) iterations. */
		boolean topologyChanged = true;
		int topDepth = 0;
		while(topologyChanged){
			topologyChanged = false;
			topDepth++;
			for(GSwitch sw: switches){
				if(sw.populateRoutingTable()){
					topologyChanged = true;
				}
			}
		}
		System.out.println("Topology depth (diameter of the model): " + topDepth);

		if(parameters.isDebugEverything()){
			for(Switch com: model.getSwitches()){
				System.out.println("Routing table for " + com.getIdentifier());
				((GSwitch) getSimulatedComponent(com)).printRoutingTable();
			}
		}

		/* register all components for the STraceWriter */
		for(SPassiveComponent bc: existingSimulationObjects.values()){
			getTraceWriter().preregister(bc);
		}

		/* start client processing */
		for(GNode bc: nodes){
			for(GClientProcess bcomp: ((GNode) bc).getClients()){
				bcomp.startProcessing();
			}
		}
	}

	/**
	 * Instantiate a Simulation Object based on the Model object's class.
	 *
	 * @return
	 */
	public SPassiveComponent instantiateSimObjectForModelObj(BasicComponent modelObject) throws Exception{
		ModelObjectMap mop = DynamicModelClassMapper.getComponentImplementation(modelObject);

		Constructor<SPassiveComponent> ct = ((Class<SPassiveComponent>) Class.forName(mop.getSimulationClass())).getConstructor();

		SPassiveComponent component = ct.newInstance();

		component.setSimulatedModelComponent(modelObject, this);
		// add the Component to the Simulator to allow lookup later.
		//this.addSimulatedComponent(component);

		return component;
	}

	/**
	 * Get the SimulationComponent for a particular ID.
	 * @param cid
	 * @return
	 */
	public SPassiveComponent getSimulatedComponent(ComponentIdentifier cid){
		SPassiveComponent comp = existingSimulationObjects.get(cid);

		assert(comp != null);

		return comp;
	}

	/**
	 * Get the SimulationComponent for a particular model component.
	 * @param mComponent
	 * @return
	 */
	public SPassiveComponent getSimulatedComponent(BasicComponent mComponent){
		return existingSimulationObjects.get(mComponent.getIdentifier());
	}

	/**
	 * Add/Register a Simulation Component to the Simulator.
	 * This is required for lookup of this component.
	 *
	 * @param com
	 */
	public void addSimulatedComponent(SPassiveComponent com){
		if(com.getIdentifier().getID() == null){
			throw new IllegalArgumentException("Identifier is null!");
		}

		if(existingSimulationObjects.get(com) != null){
			throw new IllegalArgumentException("Component " + com.getIdentifier() + " already in Simulator");
		}
		existingSimulationObjects.put(com.getIdentifier(), com);
	}

	/**
	 * Submit a new event to the central queue, the event will be started in the future when it
	 * is needed.
	 *
	 * @param event
	 */
	public void submitNewEvent(InternalEvent event){
		assert(event.getEarliestStartTime().compareTo(currentEpoch) >= 0);

		futureEvents.add(event);
	}


	/**
	 * Try to delete a future event from the job queue
	 *
	 * @param event
	 * @return true if it got removed.
	 */
	public boolean deleteFutureEvent(InternalEvent event){
		if(event == null)
			return false;

		return futureEvents.remove(event);
	}

	/**
	 * Print the pending queue of components
	 */
	private void printQueue(){
		if (ConsoleLogger.getInstance().isDebuggable(this)){
			ConsoleLogger.getInstance().debug(this, "Queue");
			for (InternalEvent c : futureEvents) {
				ConsoleLogger.getInstance().debugFollowUpline(this, "pending: \"" + c.getEarliestStartTime() + " " + c.getTargetComponent());
			}
		}
	}

	/**
	 * If the number of events shall be counted, this is a final value to allow optimizations.
	 */
	static final boolean countEventsPerComponent = false;

	/**
	 * Start the simulation with default run-parameters
	 * @param model
	 */
	public void simulate(Model model) throws Exception{
		simulate(model, new RunParameters());
	}

	/**
	 * Start the simulation of the model and print some statistics.
	 */
	public SimulationResults simulate(Model model, RunParameters parameters) throws Exception{
		if(existingSimulationObjects.size() > 0){
			throw new IllegalArgumentException("Simulator already used!");
		}

		initModel(model, parameters);

		/* initialize file sizes */
		long sTime = new Date().getTime();

		System.out.println("Simulator simulate() " + new Date());

		// map ID to number of events processed
		HashMap<Integer, Integer> mapIDEventCount = new HashMap<Integer, Integer>();

		long eventCount = 0;

		while (! futureEvents.isEmpty()) {

			if( eventCount == getRunParameters().getMaximumNumberOfEventsToSimulate() ){
				break;
			}

			eventCount++;

			if(eventCount % 100000 == 0){
				// show some activity for the user.
				System.out.println(" processing " + eventCount + " "  + " sim-time: " +  getVirtualTime());
			}

			ConsoleLogger.getInstance().debug(this, "\n\nSimulator Main Iteration");

			printQueue();

			// the component which gets the next event scheduled and therefore has one of the earlierst events.
			InternalEvent serviceEvent = futureEvents.poll();

			final Epoch newTime = serviceEvent.getEarliestStartTime();

			ConsoleLogger.getInstance().debug(this, "SCHEDULING component: " + serviceEvent.getTargetComponent().getIdentifier());

			// safety check, wrong component implementations can lead to this:
			if (currentEpoch.compareTo(newTime) > 0){
				throw new IllegalStateException("Current time smaller than excepted " +
						" " + currentEpoch + " newTime:" + newTime + " ");
			}

			currentEpoch = newTime;

			if(countEventsPerComponent){
				Integer id = serviceEvent.getTargetComponent().getIdentifier().getID();
				Integer cnt = mapIDEventCount.get(id);
				if(cnt == null){
					cnt = new Integer(0);
				}
				mapIDEventCount.put(id, cnt + 1);
			}


			// now let the component process its earliest event:
			try {
				if(serviceEvent.getClass() == Event.class)
					serviceEvent.getTargetComponent().processEvent(	(Event) serviceEvent, currentEpoch	);
				else
					serviceEvent.getTargetComponent().processInternalEvent(	serviceEvent, currentEpoch	);
			}catch(Exception e) {
				e.printStackTrace();

				System.err.println("Component: " + serviceEvent.getTargetComponent().getIdentifier() + " " +
						serviceEvent.getTargetComponent());

				System.exit(1);
			}
		}

		final HashMap<ComponentIdentifier, ComponentRuntimeInformation> idtoRuntimeInformationMap = new HashMap<ComponentIdentifier, ComponentRuntimeInformation>();

		for(SPassiveComponent component:  getSortedList(getExistingSimulationObjects().values())) {
			Integer count = mapIDEventCount.get(component.getIdentifier().getID());
			if(count != null){
				System.out.println(component.getIdentifier() + " event # " + count);
			}

			component.simulationFinished();
			// add statistics or other information about run.
			final ComponentRuntimeInformation information = component.getComponentInformation();
			if(information != null){
				idtoRuntimeInformationMap.put(component.getIdentifier(), information);
			}
		}

		for (Integer id: mapIDEventCount.keySet()){
			//System.out.println(id + " " + mapIDEventCount.get(id));
		}

		// compute realtime spent for simulation:
		long diffTime = (new Date().getTime() - sTime);

		traceWriter.finalize(getExistingSimulationObjects().values());

		return new SimulationResults(existingSimulationObjects, eventCount, getVirtualTime(), diffTime/1000.0, idtoRuntimeInformationMap);
	}

	/**
	 * Returns the current simulation time.
	 * @return
	 */
	public Epoch getVirtualTime() {
		return currentEpoch;
	}

	/**
	 * Return the mapping from aliases to applications and clients
	 * @return the applicationMap
	 */
	public ApplicationMap getApplicationMap() {
		return applicationMap;
	}

	/**
	 * This method allows to sort a list of components based on the IDs of the components.
	 * @param <IType>
	 * @param input
	 * @return
	 */
	private <IType extends SPassiveComponent> LinkedList<IType> getSortedList(Collection<IType> input){
		LinkedList<IType> list = new LinkedList<IType>(input);
		final class comp implements Comparator<IType>{
			@Override
			public int compare(IType o1, IType o2) {
				int id1 = o1.getIdentifier().getID();
				int id2 = o2.getIdentifier().getID();
				return (id1 > id2) ? +1 : ((id1 < id2) ? -1 : 0) ;
			}
		}
		Collections.sort(list, new comp());
		return list;
	}

	/**
	 * This method tries to check the consistency of the model.
	 * @param model
	 * @throws Exception
	 */
	private void checkModelConsistency(Model model) throws Exception{
		ModelVerifier mv = new ModelVerifier();
		mv.checkConsistency(model);
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename, the XML model
	 * @param extraApplicationFileMapping, overrides application-alias to program-file mapping,
	 * 																		to use the same model with different programs.
	 * @param parameters
	 * @return The Simulator.
	 * @throws Exception
	 */
	public static SimulationResults 	runProjectDescription(String filename,
			HashMap<String, String> extraApplicationFileMapping, RunParameters parameters)
	throws Exception
	{
		System.out.println("Loading model: " + filename);
		ModelXMLReader xmlreader = new ModelXMLReader();
		xmlreader.setReadCompleteProgramIntoMemory(parameters.isLoadProgramToRamOnLoad());

		Model model = xmlreader.parseProjectXML(filename ,extraApplicationFileMapping);
		if (parameters == null){
			parameters = new RunParameters();
		}
		Simulator sim = new Simulator();

		if(parameters.isDebugEverything()){
			System.out.println("Model: ");
			System.out.println(model);
		}

		return sim.simulate(model, parameters);
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename, the XML model
	 */
	static public SimulationResults runProjectDescription(String file, RunParameters parameters) throws Exception{
		return runProjectDescription(file, null, parameters);
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename, the XML model
	 */
	static public SimulationResults parseProjectDescription(String file) throws Exception{
		return runProjectDescription(file,  null, null);
	}

	/**
	 * @return the cluster model behind this simulator.
	 */
	public Model getModel() {
		assert(model != null);
		return model;
	}
}
