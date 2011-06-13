//	Copyright (C) 2009 Julian M. Kunkel
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

package de.hd.pvs.piosim.simulator.output;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLHelper;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;

public class SHDTraceWriter extends STraceWriter {

	final TraceFormatWriter writer;

	private static class ComponentTraceInfo{
		final TopologyNode topology;

		public ComponentTraceInfo(TopologyNode top) {
			this.topology = top;
		}
	}

	/**
	 * Maps the passive component to the initialized topology of the trace writer
	 */
	final HashMap<ISPassiveComponent<?>, ComponentTraceInfo> topMap = new HashMap<ISPassiveComponent<?>, ComponentTraceInfo>();

	final static int MAX_COMPONENT_NESTING = 7;

	public SHDTraceWriter(String filename, Simulator sim) {
		super(filename, sim);

		writer = new TraceFormatWriter(filename, "", "", new String []{ "Root Component", "Component", "Component", "Component", "Component", "Component", "Component"});
	}

	@Override
	public void preregister(ISPassiveComponent<IBasicComponent> component) {
		// manufacture topology and cache it.

		LinkedList<IBasicComponent> path = null;

		// handle NICs specially
		if(component.getModelComponent().getObjectType().equals("NetworkEdge")){
			NetworkEdge edge = (NetworkEdge) component.getModelComponent();
			INetworkNode node = edge.getTopology().getEdgeTarget(edge);


			if (node.getObjectType().equals( "NIC") || node.getObjectType().equals("NetworkNode")){
				path = node.getParentComponentsPlusMe();
				path.add(edge);
			}
		}

		if(path == null){
			path = component.getModelComponent().getParentComponentsPlusMe();
		}



		final String [] strPath = new String[path.size()];

		// manufacture topology:
		if(MAX_COMPONENT_NESTING < strPath.length){
			throw new IllegalArgumentException("Simulator hierachy is not as deep "+ MAX_COMPONENT_NESTING +
					" to record full" +	" hierachy: " + path.size());
		}


		int pos = 0;
		for(IBasicComponent comp: path){
			strPath[pos] = comp.getIdentifier().toString();

			if(comp.getObjectType().equals("NetworkEdge")){
				strPath[pos] = "RX " + strPath[pos];
			}else{
				// add the type
				strPath[pos] = comp.getObjectType() + " " + strPath[pos];
			}

			pos++;
		}

		final TopologyNode newTopo = writer.createInitalizeTopology(strPath);
		topMap.put(component, new ComponentTraceInfo(newTopo));
	}


	@Override
	protected void arrowEndInternal(Epoch time, ISPassiveComponent src,
			ISPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void arrowStartInternal(Epoch time, ISPassiveComponent src,
			ISPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void startStateInternal(Epoch time, ISPassiveComponent comp, String eventDesc) {
		final ComponentTraceInfo info = topMap.get(comp);
		final String validText = XMLHelper.validTag(eventDesc);

		try{
			writer.writeStateStart(info.topology, validText, time);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void endStateInternal(Epoch time, ISPassiveComponent comp, String eventDesc, String[] attrNameValues) {
		final ComponentTraceInfo info = topMap.get(comp);

		final String validText = XMLHelper.validTag(eventDesc);

		try{
			HashMap<String, String> attributes = null;
			if(attrNameValues != null){
				attributes = new HashMap<String, String>();
				for(int i=0; i < attrNameValues.length; i+=2){
					attributes.put(attrNameValues[i], attrNameValues[i+1]);
				}
			}

			writer.writeStateEnd(info.topology, validText, time, attributes, null );
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	@Override
	protected void eventInternal(Epoch time, ISPassiveComponent comp,	String eventDesc, long userEventValue) {
		final ComponentTraceInfo info = topMap.get(comp);
		try{
			writer.writeEvent(info.topology, eventDesc, time );
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	@Override
	protected void finalizeInternal(Epoch endTime,	Collection<ISPassiveComponent> existingComponents) {
		try{
			writer.finalizeTrace();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected RelationToken relCreateTopLevelRelationInternal(ISPassiveComponent comp, Epoch time) {
		return writer.getRelationForTopology(topMap.get(comp).topology).createTopLevelRelation(time);
	}

	@Override
	protected RelationToken relRelateMultipleProcessLocalTokensInternal( ISPassiveComponent comp, RelationToken[] parents,  Epoch time) {
		return writer.getRelationForTopology(topMap.get(comp).topology).relateMultipleProcessLocalTokens(parents, time);
	}

	@Override
	protected RelationToken relRelateProcessLocalTokenInternal(ISPassiveComponent comp, RelationToken parent, Epoch time) {
		return writer.getRelationForTopology(topMap.get(comp).topology).relateProcessLocalToken(parent, time);
	}

	@Override
	protected void relDestroyInternal(RelationToken relation, Epoch time) {
		relation.getRelationXMLWriter().destroyRelation(relation, time);
	}

	@Override
	protected void relEndStateInternal( RelationToken relation, Epoch time) {
		relation.getRelationXMLWriter().endState(relation, time);
	}

	@Override
	protected void relEndStateInternal(RelationToken relation, Epoch time, String childTags, String[] attrNameValues)
	{
		relation.getRelationXMLWriter().endState(relation, time, childTags, attrNameValues);
	}

	@Override
	protected void relStartStateInternal(RelationToken relation, Epoch time, String name) {
		relation.getRelationXMLWriter().startState(relation, time, XMLHelper.validTag(name));
	}

	@Override
	protected void relStartStateInternal(RelationToken relation, Epoch time, String name,
			String childTags, String[] attrNameValues) {
		relation.getRelationXMLWriter().startState(relation, time, XMLHelper.validTag(name), childTags, attrNameValues);
	}

}
