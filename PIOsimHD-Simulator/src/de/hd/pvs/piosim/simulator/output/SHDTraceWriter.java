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
import java.util.Stack;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLHelper;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;

public class SHDTraceWriter extends STraceWriter {

	final TraceFormatWriter out;
	final ProjectDescription desc;

	private static class ComponentTraceInfo{
		final TopologyNode topology;
		final Stack<StateTraceEntry> stackedStates = new Stack<StateTraceEntry>();
		
		public ComponentTraceInfo(TopologyNode top) {
			this.topology = top;
		}
	}
	
	/**
	 * Maps the passive component to the initalized topology of the trace writer
	 */
	final HashMap<SPassiveComponent<?>, ComponentTraceInfo> topMap = new HashMap<SPassiveComponent<?>, ComponentTraceInfo>();
	
	// root component, component.., (final) component
	// other hierarchical information is dropped.
	final static int MAX_COMPONENT_NESTING = 5; 

	public SHDTraceWriter(String filename, Simulator sim) {
		super(filename, sim);
		
		final TopologyLabels labels = new TopologyLabels();
		labels.setTopologyLabels(new String []{ "Root Component", "Component", "Component", "Component", "Component"});
		
		out = new TraceFormatWriter(filename, labels);
		desc = out.getProjectDescription();
	}

	@Override
	public void preregister(SPassiveComponent component) {
		// manufacture topology and cache it.
		
		final LinkedList<BasicComponent<?>> path = component.getModelComponent().getParentComponentsPlusMe();
		
		final String [] strPath = new String[path.size()];
		
		// manufacture topology:
		if(MAX_COMPONENT_NESTING < strPath.length){
			throw new IllegalArgumentException("Simulator hierachy is not as deep "+ MAX_COMPONENT_NESTING +  
					" to record full" +	" hierachy: " + path.size());
		}
		

		int pos = 0;
		for(BasicComponent<?> comp: path){
			strPath[pos] = comp.getIdentifier().toString();
			pos++;
		}
		
		final TopologyNode newTopo = out.createInitalizeTopology(strPath);
		topMap.put(component, new ComponentTraceInfo(newTopo));
	}


	@Override
	protected void arrowEndInternal(Epoch time, SPassiveComponent src,
			SPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void arrowStartInternal(Epoch time, SPassiveComponent src,
			SPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc) {
		final ComponentTraceInfo info = topMap.get(comp);
		final String validText = XMLHelper.validTag(eventDesc);
		final StateTraceEntry state = new StateTraceEntry(validText, time);
		
		info.stackedStates.push(state);		
		out.StateStart(info.topology, state);
	}
	
	@Override
	protected void endStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc) {
		final ComponentTraceInfo info = topMap.get(comp);		
		final StateTraceEntry state = info.stackedStates.pop();
		
		final String validText = XMLHelper.validTag(eventDesc);
		if(state == null || ! state.getName().equals(validText)){
			throw new IllegalArgumentException("End state without startstate! " + eventDesc + 
					" current stacked: " + state);
		}
		state.setEndTime(time);
		out.StateEnd(info.topology, state);
	}

	@Override
	protected void eventInternal(Epoch time, SPassiveComponent comp,
			String eventDesc, long userEventValue) {
		final ComponentTraceInfo info = topMap.get(comp);
		final EventTraceEntry event = new EventTraceEntry(XMLHelper.escapeAttribute(eventDesc), time);
		event.addAttribute("value" , "" + userEventValue);
		out.Event(info.topology, event);
	}


	@Override
	protected void finalizeInternal(Epoch endTime,
			Collection<SPassiveComponent> existingComponents) {
		try{
			out.finalizeTrace();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

}
