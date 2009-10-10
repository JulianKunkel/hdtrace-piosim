
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

package de.hd.pvs.piosim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.Program;

/**
 * This class contains all objects part of a cluster model.
 * It links several helper classes.
 *
 * @author Julian M. Kunkel
 */
public class Model{

	/**
	 * Contains a list of nodes.
	 */
	ArrayList<Node> nodes = new ArrayList<Node>();
	ArrayList<INetworkTopology> topologies = new ArrayList<INetworkTopology>();

	ArrayList<Server>   servers = new ArrayList<Server>();
	ArrayList<ClientProcess>   clients = new ArrayList<ClientProcess>();
	ArrayList<INetworkEdge>    networkEdges = new ArrayList<INetworkEdge>();
	ArrayList<INetworkNode>    networkNodes = new ArrayList<INetworkNode>();


	/**
	 * The template manager contained objects might use.
	 */
	TemplateManager     templateManager = new TemplateManager();

	/**
	 * Settings meaningful for a subset of components:
	 */
	GlobalSettings      globalSettings = new GlobalSettings();

	/**
	 * contains all objects, especially nameless components and subcomponents
	 */
	HashMap<Integer, IBasicComponent> cidCMap = new HashMap<Integer, IBasicComponent>();

	/**
	 * Maps names to the component. Contains only "named" BasicComponents.
	 */
	HashMap<String, IBasicComponent> componentNameMap = new HashMap<String, IBasicComponent>();

	/**
	 * Maps the Application alias to the application.
	 * The alias could be different than the application name because one application can
	 * be used several times for different clients.
	 */
	HashMap<String, Application> applicationNameMap = new HashMap<String, Application>();

	/**
	 * Maximum component ID ever read.
	 */
	int maxComponentID = 0;

	/**
	 * Return the maximum component ID which is used.
	 * @return
	 */
	public int getMaxComponentID() {
		return maxComponentID;
	}

	/**
	 * @return the nodes
	 */
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public ArrayList<INetworkTopology> getTopologies() {
		return topologies;
	}

	/**
	 * @return the componentNameMap
	 */
	public Map<String, IBasicComponent> getComponentNameMap() {
		return Collections.unmodifiableMap( componentNameMap );
	}

	/**
	 * @return the applicationNameMap
	 */
	public Map<String, Application> getApplicationNameMap() {
		return Collections.unmodifiableMap(  applicationNameMap );
	}

	/**
	 * Return the program of a particular client
	 *
	 * @param client
	 * @return
	 */
	public Program getProgram(ClientProcess client){
		return applicationNameMap.get(client.getApplication()).getClientProgram(
				client.getRank(), client.getThread());
	}

	/**
	 * @return the servers
	 */
	public List<Server> getServers() {
		return Collections.unmodifiableList(servers);
	}

	public List<INetworkEdge> getNetworkEdges() {
		return Collections.unmodifiableList(networkEdges);
	}

	public List<INetworkNode> getNetworkNodes() {
		return Collections.unmodifiableList(networkNodes);
	}
	/**
	 *
	 * @return clients
	 */
	public List<ClientProcess> getClientProcesses(){
		return Collections.unmodifiableList(clients);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Global Settings:" + globalSettings + "\n");

		str.append("Known nodes:\n");
		for(Node m: nodes){
			str.append(m);
		}

		str.append("\nKnown network nodes:\n");
		for(INetworkNode m: networkNodes){
			str.append(m + "\n");
		}

		str.append("\nKnown network edges:\n");
		for(INetworkEdge m: networkEdges){
			str.append(m + "\n");
		}

		str.append("\nKnown topologies\n");
		for(INetworkTopology s: topologies){
			str.append(s+ "\n");
		}

		return str.toString();
	}

	void addTopology(INetworkTopology topology){
		topologies.add(topology);
	}

	/**
	 * Derive a object name which is not used right now (can be the same name if not used).
	 *
	 * @param oldName
	 * @return
	 */
	public String findUnusedObjectName(String oldName){
		String newName;

		if (oldName == null)
			return oldName;

		if (componentNameMap.get(oldName) == null)
			return oldName;

		Pattern p = Pattern.compile("([0-9]+)$");
		Matcher m = p.matcher(oldName);
		int curVal = 0;
		if (m.find()) {
			curVal = (Integer.parseInt(m.group(1)));
			oldName = oldName.substring(0, m.start());
		}
		// find an unused value
		do {
			curVal++;

			if (curVal < 10) {
				newName = oldName + "0" + curVal;
			} else {
				newName = oldName + curVal;
			}
		} while (componentNameMap.get(newName) != null);

		return newName;
	}

	/**
	 * Add a BasicComponent and all subcomponents to the Model.
	 * This function assigns a valid component ID to the component.
	 * Beware of later modifications of the BasicComponent (especially addition of new subelements).
	 *
	 * @param com
	 */
	public void addComponent(IBasicComponent com){
		ComponentIdentifier ci = com.getIdentifier();

		if(ci.getID() == null){
			ci.setID(maxComponentID++);
		}else{
			if (cidCMap.keySet().contains(ci.getID())) {
				throw new IllegalArgumentException("Component ID already used: "
						+ ci.getID() + "\n Component: " + cidCMap.get(ci.getID()));
			}

			if( maxComponentID < ci.getID()){
				maxComponentID = ci.getID();
			}
		}
		cidCMap.put(ci.getID(), com);

		if (ci != null && ci.getName() != null && ci.getName().length() != 0) {
			if (componentNameMap.get(ci.getName()) != null) {
				ci.setName(findUnusedObjectName(ci.getName()));
				/*throw new IllegalArgumentException(
						"Component with name "
						+ ci.getName()
						+ " already exists, you need a unique component name ");
				 */
			}
			componentNameMap.put(ci.getName(), com);
		}

		// depending on the type add it to the specific list, TODO, this should be done via
		// a function addObject(<Type> type) and exploiting polymorphism...
		if(com.getObjectType().equals("Node")){
			nodes.add((Node) com);
		}else if(com.getObjectType().equals("ClientProcess")){
			clients.add((ClientProcess) com);
		}else if(com.getObjectType().equals("Server")){
			servers.add((Server) com);
		}else if(com.getObjectType().equals("NetworkEdge")){
			networkEdges.add((INetworkEdge) com);
		}else if(com.getObjectType().equals("NetworkNode")){
			networkNodes.add((INetworkNode) com);
		}

		// add subelement
		ArrayList<BasicComponent> list = com.getDirectChildComponents();

		for (BasicComponent child: list){
			addComponent(child);
		}
	}

	/**
	 * This method renames the given component component identifier name and updates the model
	 * to ensure that all references to the particular object are updated. In case the name
	 * already exists the name gets an unused integer value attached.
	 *
	 * @param com The component to rename
	 * @param newName The new name
	 */
	void renameComponent(BasicComponent com, String newName){
		componentNameMap.remove(com.getName());

		com.setName(newName);

		if (newName != null && newName.length() != 0) {
			if (componentNameMap.get(newName) != null) {
				com.setName(findUnusedObjectName(newName));
			}
			componentNameMap.put(com.getName(), com);
		}
	}

	/**
	 * Remove the component and all subcomponents from the model.
	 * Connections within the object are not updated! Thus beware of invalid interconnects.
	 *
	 * @param com
	 */
	void removeComponent(IBasicComponent com){
		/* remove from all maps, remove necessary connection should be done by the caller */
		if ( cidCMap.remove(com) == null ){
			throw new IllegalArgumentException("Component " + com + " not part of model!");
		}
		componentNameMap.remove(com.getName());

		if(com.getObjectType().equals("Node")){
			nodes.remove((Node) com);
		}else if(com.getObjectType().equals("ClientProcess")){
			clients.remove((ClientProcess) com);
		}else if(com.getObjectType().equals("Server")){
			servers.remove((Server) com);
		}else if(com.getObjectType().equals("NetworkEdge")){
			networkEdges.remove((INetworkEdge) com);
		}else if(com.getObjectType().equals("NetworkNode")){
			networkNodes.remove((INetworkNode) com);
		}

		// remove subelement
		ArrayList<BasicComponent> list = com.getDirectChildComponents();

		for (BasicComponent child: list){
			removeComponent(child);
		}
	}

	/**
	 * Return a unmodifiable Map of object IDs to BasicComponents.
	 * @return
	 */
	public Map<Integer, IBasicComponent> getCidCMap() {
		return Collections.unmodifiableMap(cidCMap);
	}

	/**
	 * Set the CID Map of object IDs to BasicComponents.
	 * @return
	 */
	void setCidCMap(HashMap<Integer, IBasicComponent> newMap) {
		cidCMap = newMap;

		// set the max component ID
		int max = 0;
		for (Integer i: cidCMap.keySet()) {
			if (i > max) max = i;
		}
		maxComponentID = max;
	}


	/**
	 * @return the templateManager
	 */
	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	/**
	 * @return the globalSettings
	 */
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	/**
	 * Checks if the component is part of the model.
	 * @param component
	 * @return
	 */
	public boolean isComponentInModel(IBasicComponent component){
		return cidCMap.containsKey(component.getIdentifier().getID());
	}

	/**
	 * generate a new and unused component ID. This function is not thread-safe.
	 *
	 * @return
	 */
	private int getFreeComponentID() {
		for (int i = 0; i < cidCMap.keySet().size(); i++) {
			if (!cidCMap.keySet().contains(i)) {
				return i;
			}
		}
		return cidCMap.keySet().size() + 1;
	}

}
