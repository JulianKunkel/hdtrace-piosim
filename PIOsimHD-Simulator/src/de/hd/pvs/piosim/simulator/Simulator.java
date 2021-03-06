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
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper.ModelObjectMap;
import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.base.IGDynamicImplementationObject;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.ApplicationMap;
import de.hd.pvs.piosim.simulator.components.ClientProcess.DynamicImplementationLoader;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;
import de.hd.pvs.piosim.simulator.network.GNetworkTopology;
import de.hd.pvs.piosim.simulator.network.routing.AGPaketRouting;
import de.hd.pvs.piosim.simulator.output.SDummyTraceWriter;
import de.hd.pvs.piosim.simulator.output.SHDTraceWriter;
import de.hd.pvs.piosim.simulator.output.STraceWriter;

/**
 * The class <code>Simulator</code> manages the simulation. All objects required
 * for the simulation are aggregated and referenced by a Simulator.
 *
 * @author Julian M. Kunkel
 */
public final class Simulator implements IModelToSimulatorMapper {
	private boolean errorState = false;

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

	private LinkedList<GNetworkTopology> existingTopologies = new LinkedList<GNetworkTopology>();

	/**
	 * We have to find the SimulationComponent belonging to a model component.
	 */
	private HashMap<ComponentIdentifier, ISPassiveComponent> existingSimulationObjects = new HashMap<ComponentIdentifier, ISPassiveComponent>();

	public HashMap<ComponentIdentifier, ISPassiveComponent> getExistingSimulationObjects() {
		return existingSimulationObjects;
	}

	public LinkedList<GNetworkTopology> getExistingTopologies() {
		return existingTopologies;
	}

	/**
	 * the current epoch of the simulator i.e. the simulated time we are right
	 * now
	 */
	private Epoch currentEpoch = Epoch.ZERO;

	/**
	 * Check the model and prepare the simulation
	 *
	 * @param model
	 * @param parameters
	 *            (null, then defaults are used) or the real parameters
	 * @throws Exception
	 */
	public void initModel(Model model, RunParameters parameters)
		throws Exception
	{
		if (existingSimulationObjects.size() > 0) {
			throw new IllegalArgumentException(
			"Simulator already initalized, model cannot be changed afterwards!");
		}

		if (parameters == null) {
			parameters = new RunParameters();
		}
		this.runParameters = parameters;
		this.model = model;

		DynamicImplementationLoader.initalize(model.getGlobalSettings());

		// load the logger definition:
		ConsoleLogger.init(parameters.getLoggerDefinitionFile(), parameters
				.isDebugEverything());

		ConsoleLogger.getInstance().info("Version:" + version);

		// check the consistency of the model to detect errors in advance.
		checkModelConsistency(model);

		/* create network routing points */
		for (INetworkEdge com : model.getNetworkEdges()) {
			IGNetworkEdge c = (IGNetworkEdge) instantiateSimObjectForModelObj(com);
		}

		for (INetworkNode com : model.getNetworkNodes()) {
			final IGNetworkNode node = (IGNetworkNode) instantiateSimObjectForModelObj(com);

			// check that IGNetworkExit is implemented when component implements
			// INetworkExit
			if (INetworkExit.class.isInstance(com)
					&& !IGNetworkExit.class.isInstance(node)) {
				throw new IllegalArgumentException(
						"Model component "
						+ com.getIdentifier()
						+ " implements INetworkExit "
						+ "then the simulation component must implement IGNetworkExit, but it does not. Class used was: "
						+ node.getClass().getCanonicalName());
			}
			// check that IGNetworkEntry is implemented when component
			// implements INetworkEntry
			if (INetworkEntry.class.isInstance(com)
					&& !IGNetworkEntry.class.isInstance(node)) {
				throw new IllegalArgumentException(
						"Model component "
						+ com.getIdentifier()
						+ " implements INetworkEntry "
						+ "then the simulation component must implement IGNetworkEntry, but it does not. Class used was: "
						+ node.getClass().getCanonicalName());
			}
		}

		/* create clients and servers from the model */
		final ArrayList<GNode> nodes = new ArrayList<GNode>();
		for (Node com : model.getNodes()) {
			final GNode m = (GNode) instantiateSimObjectForModelObj(com);

			nodes.add(m);

			/* put clients into the application map */
			final Collection<GClientProcess> clients = m.getClients();
			for(GClientProcess gc: clients){
				final ClientProcess c = gc.getModelComponent();
				assert(c != null);
				applicationMap.put(c.getApplication(), c.getRank(), gc);
			}
		}


		// load other components, should not really happen, however for testing...
		for(IBasicComponent com: model.getComponentNameMap().values()){
			if(! existingSimulationObjects.containsKey(com.getIdentifier())){
				// load the object
				final ISPassiveComponent  scom = (ISPassiveComponent) instantiateSimObjectForModelObj(com);
			}
		}

		// at least one topology must exist
		assert(model.getTopologies().size() != 0);

		/* load topology */
		for (INetworkTopology topo : model.getTopologies()) {

			final GNetworkTopology gtopo = new GNetworkTopology();
			final AGPaketRouting routing = (AGPaketRouting) instantiateSimObjectForModelObj(topo.getRoutingAlgorithm());

			if(routing == null){
				throw new IllegalArgumentException("Invalid routing algorithm.");
			}

			gtopo.setName(topo.getName());
			gtopo.setRouting(routing);

			// now load the edges for the topology:
			for (final INetworkEdge com : topo.getNetworkEdges()) {
				final INetworkNode tgt = topo.getEdgeTarget(com);
				final IGNetworkNode tgtNode = (IGNetworkNode) getSimulatedComponent(tgt);

				assert (tgtNode != null);

				((IGNetworkEdge) (getSimulatedComponent(com))).setTargetNode(tgtNode);
			}

			// now load the routing into the nodes
			for (INetworkNode com : topo.getNetworkNodes()) {
				if(com == null){
					throw new IllegalArgumentException("Invalid network node, is Null! ");
				}
				ISPassiveComponent simComm = getSimulatedComponent(com);
				((IGNetworkNode) (simComm)).setPaketRouting(routing);
			}

			routing.buildRoutingTable(topo, this);
			existingTopologies.add(gtopo);
		}

		// notify all components that the model is now build
		for (ISPassiveComponent component : getExistingSimulationObjects().values()) {
			component.simulationModelIsBuild();
		}
	}

	/**
	 * Instantiate a Simulation Object based on the Model object's class.
	 *
	 * @return
	 */
	public IGDynamicImplementationObject instantiateSimObjectForModelObj(
			IDynamicImplementationObject modelObject) throws Exception {
		ModelObjectMap mop = DynamicModelClassMapper
		.getComponentImplementation(modelObject);

		Constructor<IGDynamicImplementationObject> ct = ((Class<IGDynamicImplementationObject>) Class
				.forName(mop.getSimulationClass())).getConstructor();

		IGDynamicImplementationObject component = ct.newInstance();

		// if the component requires to set the simulator, do so.
		if (SPassiveComponent.class.isInstance(component)) {
			((SPassiveComponent) component).setSimulator(this);
		}

		component.setModelComponent(modelObject);

		return component;
	}

	public ISPassiveComponent instantiateSimObjectForModelObj(IBasicComponent modelObject) throws Exception {
		ModelObjectMap mop = DynamicModelClassMapper.getComponentImplementation(modelObject);

		Constructor<ISPassiveComponent> ct = ((Class<ISPassiveComponent>) Class
				.forName(mop.getSimulationClass())).getConstructor();

		final ISPassiveComponent component = ct.newInstance();

		// if the component requires to set the simulator, do so.
		component.setSimulator(this);

		component.setModelComponent(modelObject);

		addSimulatedComponent(component);

		return component;
	}

	/**
	 * Get the SimulationComponent for a particular component id.
	 *
	 * @param cid
	 * @return
	 */
	public ISPassiveComponent getSimulatedComponent(ComponentIdentifier cid) {
		ISPassiveComponent comp = existingSimulationObjects.get(cid);

		assert (comp != null);

		return comp;
	}

	/**
	 * Get the SimulationComponent for a particular model component.
	 *
	 * @param mComponent
	 * @return
	 */
	public ISPassiveComponent getSimulatedComponent(IBasicComponent mComponent) {
		return existingSimulationObjects.get(mComponent.getIdentifier());
	}

	/**
	 * Add/Register a Simulation Component to the Simulator. This is required
	 * for lookup of this component.
	 *
	 * @param com
	 */
	public void addSimulatedComponent(ISPassiveComponent com) {
		if (com.getIdentifier().getID() == ComponentIdentifier.INVALID_ID) {
			throw new IllegalArgumentException("Identifier is null!");
		}

		if (existingSimulationObjects.get(com) != null) {
			throw new IllegalArgumentException("Component "
					+ com.getIdentifier() + " already in Simulator");
		}
		existingSimulationObjects.put(com.getIdentifier(), com);
	}

	/**
	 * Submit a new event to the central queue, the event will be started in the
	 * future when it is needed.
	 *
	 * @param event
	 */
	public void submitNewEvent(InternalEvent event) {
		assert (event.getEarliestStartTime().compareTo(currentEpoch) >= 0);

		// TODO
		//for(InternalEvent ev: futureEvents){
		//	assert(ev.getTargetComponent() != event.getTargetComponent());
		//}

		futureEvents.add(event);
	}

	/**
	 * Try to delete a future event from the job queue
	 *
	 * @param event
	 * @return true if it got removed.
	 */
	public boolean deleteFutureEvent(InternalEvent event) {
		if (event == null)
			return false;

		return futureEvents.remove(event);
	}

	/**
	 * Print the pending queue of components
	 */
	private void printQueue() {
		if (ConsoleLogger.getInstance().isDebuggable(this)) {
//			ConsoleLogger.getInstance().debug(this, "Queue");
			for (InternalEvent c : futureEvents) {
				ConsoleLogger.getInstance().debugFollowUpline(
						this,
						"pending: \"" + c.getEarliestStartTime() + " "
						+ c.getTargetComponent());
			}
		}
	}

	/**
	 * If the number of events shall be counted, this is a final value to allow
	 * optimizations.
	 */
	static final boolean countEventsPerComponent = false;

	public void buildModel(Model model) throws Exception {
		initModel(model, new RunParameters());
	}

	public void buildModel(RunParameters parameters) throws Exception {
		initModel(model, parameters);
	}

	/**
	 * Start the simulation of the model and print some statistics.
	 */
	public SimulationResults simulate() throws Exception {
		if (traceWriter != null) {
			throw new IllegalArgumentException("Simulator already used!");
		}

		if (runParameters.isTraceEnabled()) {
			// load trace writer
			traceWriter = new SHDTraceWriter(runParameters.traceFile, this);
		} else {
			traceWriter = new SDummyTraceWriter(this);
		}

		/* register all components for the STraceWriter */
		for (ISPassiveComponent bc : existingSimulationObjects.values()) {
			getTraceWriter().preregister(bc);
		}

		/* initialize file sizes */
		long sTime = new Date().getTime();

		System.out.println("Simulator simulate() " + new Date());

		// map ID to number of events processed
		HashMap<Integer, Integer> mapIDEventCount = new HashMap<Integer, Integer>();

		long eventCount = 0;

		// last time we have written out the system information
		long everySecondInformation = new Date().getTime();

		while (!futureEvents.isEmpty()) {

			if (eventCount == getRunParameters()
					.getMaximumNumberOfEventsToSimulate()) {
				break;
			}

			eventCount++;

			if (eventCount % 5000 == 0) {
				// test for update
				long curTime = new Date().getTime();

				if (curTime - everySecondInformation >= 1000){

					everySecondInformation = curTime;

					// show some activity for the user.
					System.out.println( (curTime - sTime) / 1000.0 + "s sim-time: " + getVirtualTime() + " #events: " + eventCount);
				}
			}

//			ConsoleLogger.getInstance().debug(this,	"\n\nSimulator Main Iteration");

			printQueue();

			// the component which gets the next event scheduled and therefore
			// has one of the earlierst events.
			InternalEvent serviceEvent = futureEvents.poll();

			final Epoch newTime = serviceEvent.getEarliestStartTime();

//			ConsoleLogger.getInstance().debug(	this, "SCHEDULING component: "	+ serviceEvent.getTargetComponent()	.getIdentifier());

			// safety check, wrong component implementations can lead to this:
			if (currentEpoch.compareTo(newTime) > 0) {
				throw new IllegalStateException(
						"Current time smaller than excepted " + " "
						+ currentEpoch + " newTime:" + newTime + " ");
			}

			currentEpoch = newTime;

			if (countEventsPerComponent) {
				Integer id = serviceEvent.getTargetComponent().getIdentifier()
				.getID();
				Integer cnt = mapIDEventCount.get(id);
				if (cnt == null) {
					cnt = new Integer(0);
				}
				mapIDEventCount.put(id, cnt + 1);
			}

			// now let the component process its earliest event:
			try {
				if (serviceEvent.getClass() == Event.class)
					serviceEvent.getTargetComponent().processEvent(
							(Event) serviceEvent, currentEpoch);
				else
					serviceEvent.getTargetComponent().processInternalEvent(
							serviceEvent, currentEpoch);
			} catch (Exception e) {
				e.printStackTrace();

				System.err.println("Component: "
						+ serviceEvent.getTargetComponent().getIdentifier()
						+ " " + serviceEvent.getTargetComponent());

				System.exit(1);
			}
		}

		// compute real time spent for simulation:
		long diffTime = (new Date().getTime() - sTime);


		final HashMap<ComponentIdentifier, ComponentRuntimeInformation> idtoRuntimeInformationMap = new HashMap<ComponentIdentifier, ComponentRuntimeInformation>();

		for (ISPassiveComponent component : getExistingSimulationObjects().values()) {
			Integer count = mapIDEventCount.get(component.getIdentifier()
					.getID());
			if (count != null) {
				System.out.println(component.getIdentifier() + " event # "
						+ count);
			}

			component.simulationFinished();
			// add statistics or other information about run.
			final ComponentRuntimeInformation information = component
			.getComponentInformation();
			if (information != null) {
				idtoRuntimeInformationMap.put(component.getIdentifier(),
						information);
			}
		}

		// for (Integer id: mapIDEventCount.keySet()){
		// System.out.println(id + " " + mapIDEventCount.get(id));
		// }

		traceWriter.finalize(getExistingSimulationObjects().values());

		return new SimulationResults(existingSimulationObjects, eventCount,
				getVirtualTime(), diffTime / 1000.0, idtoRuntimeInformationMap,
				isErrorState());
	}

	/**
	 * Returns the current simulation time.
	 *
	 * @return
	 */
	public Epoch getVirtualTime() {
		return currentEpoch;
	}

	/**
	 * Return the mapping from aliases to applications and clients
	 *
	 * @return the applicationMap
	 */
	public ApplicationMap getApplicationMap() {
		return applicationMap;
	}

	/**
	 * This method allows to sort a list of components based on the IDs of the
	 * components.
	 *
	 * @param <IType>
	 * @param input
	 * @return
	 */
	private <IType extends ISPassiveComponent> LinkedList<IType> getSortedList(
			Collection<IType> input) {
		LinkedList<IType> list = new LinkedList<IType>(input);
		final class comp implements Comparator<IType> {
			@Override
			public int compare(IType o1, IType o2) {
				int id1 = o1.getIdentifier().getID();
				int id2 = o2.getIdentifier().getID();
				return (id1 > id2) ? +1 : ((id1 < id2) ? -1 : 0);
			}
		}
		Collections.sort(list, new comp());
		return list;
	}

	/**
	 * This method tries to check the consistency of the model.
	 *
	 * @param model
	 * @throws Exception
	 */
	private void checkModelConsistency(Model model) throws Exception {
		ModelVerifier mv = new ModelVerifier();
		mv.checkConsistency(model);
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename
	 *            , the XML model
	 * @param extraApplicationFileMapping
	 *            , overrides application-alias to program-file mapping, to use
	 *            the same model with different programs.
	 * @param parameters
	 * @return The Simulator.
	 * @throws Exception
	 */
	public static SimulationResults runProjectDescription(String filename,
			HashMap<String, String> extraApplicationFileMapping,
			RunParameters parameters) throws Exception {
		System.out.println("Loading model: " + filename);
		ModelXMLReader xmlreader = new ModelXMLReader();
		xmlreader.setReadCompleteProgramIntoMemory(parameters
				.isLoadProgramToRamOnLoad());

		Model model = xmlreader.parseProjectXML(filename,
				extraApplicationFileMapping);
		if (parameters == null) {
			parameters = new RunParameters();
		}
		Simulator sim = new Simulator();

		if (parameters.isDebugEverything()) {
			System.out.println("Model: ");
			System.out.println(model);
		}

		try{
			sim.initModel(model, parameters);
		}catch(IllegalArgumentException e){
			System.err.println("Model: ");
			System.err.println(model);
			throw e;
		}
		return sim.simulate();
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename
	 *            , the XML model
	 */
	static public SimulationResults runProjectDescription(String file,
			RunParameters parameters) throws Exception {
		return runProjectDescription(file, null, parameters);
	}

	/**
	 * Create a Simulator from a model description.
	 *
	 * @param filename
	 *            , the XML model
	 */
	static public SimulationResults parseProjectDescription(String file)
	throws Exception {
		return runProjectDescription(file, null, null);
	}

	/**
	 * @return the cluster model behind this simulator.
	 */
	public Model getModel() {
		assert (model != null);
		return model;
	}

	public void errorDuringProcessing() {
		this.errorState = true;
	}

	public boolean isErrorState() {
		return errorState;
	}
}
