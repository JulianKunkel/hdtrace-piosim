package de.hd.pvs.piosim.simulator.output;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;

public class SHDTraceWriter extends STraceWriter {

	final TraceFormatWriter out;
	final ProjectDescription desc;

	final HashMap<SPassiveComponent<?>, TopologyEntry> topMap = new HashMap<SPassiveComponent<?>, TopologyEntry>();
	
	final static int HIERACHY_DEPTH = 3; // root component, component, component

	public SHDTraceWriter(String filename, Simulator sim) {
		super(filename, sim);
		
		final TopologyLabels labels = new TopologyLabels();
		labels.setTopologyLabels(new String []{ "Root Component", "Component", "Component"});
		
		out = new TraceFormatWriter(filename, labels);
		desc = out.getProjectDescription();
	}

	@Override
	public void preregister(SPassiveComponent component) {
		// manufacture topology and cache it.
		
		BasicComponent<?> parent = component.getModelComponent();
		LinkedList<BasicComponent<?>> hierachy = new LinkedList<BasicComponent<?>>();
		while(parent != null){
			hierachy.push(parent);
			parent = parent.getParentComponent();
		}
		
		// manufacture topology:
		// limit max depth
		int maxDepth = hierachy.size() > HIERACHY_DEPTH ? HIERACHY_DEPTH : hierachy.size();
		System.out.println("component: " + maxDepth + component.getIdentifier());
		
		final TopologyEntry rootTopo = new TopologyEntry(
				hierachy.get(0).getIdentifier().toString(), null);
		
		TopologyEntry parentTopo = rootTopo;
		 
		for(int i = 1 ; i < maxDepth-1; i++){
			parentTopo  = new TopologyEntry(hierachy.get(i).getIdentifier().toString(), parentTopo);
		}
		
		// add leaf level:
		final TopologyEntry leafTopo = 
			new TopologyEntry(hierachy.get(hierachy.size()-1).getIdentifier().toString(), parentTopo);
		
		
		out.initalizeTopology(rootTopo);
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
	protected void endStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void eventInternal(Epoch time, SPassiveComponent comp,
			String eventDesc, long userEventValue) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc) {
		// TODO Auto-generated method stub

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
