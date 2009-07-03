//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package de.viewer.timelines.topologyPlugins;


import java.util.Enumeration;

import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hdTraceInput.BufferedTraceFileReader;
import de.topology.ITopologyInputPluginObject;
import de.topology.TopologyInputPlugin;
import de.topology.TopologyManager;
import de.viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;


/**
 * This plugin is set exactly once on each thread.
 * 
 * @author Julian M. Kunkel
 *
 */
public class MPIThreadInputPlugin extends TopologyInputPlugin{
	public class MPIThreadObject implements ITopologyInputPluginObject{
		MPIRankObject rankObject;
		
		@Override
		public TopologyInputPlugin getResponsiblePlugin() {
			return getMe();
		}
		
		public MPIThreadObject(TopologyNode node) {
			final TopologyManager manager = getMe().getTopologyManager();
			TopologyNode cur = node;
			while(cur.hasParent()){
				cur = cur.getParent();
				rankObject = manager.getPluginObjectForTopology(cur, MPIRankObject.class);
				if(rankObject != null){
					break;
				}
			}
			
			if(rankObject == null){
				System.err.println("Error did not find rank for thread topology: " + node.toRecursiveString());
				return;
			}
			
			parseTraceFile( (BufferedTraceFileReader) node.getTraceSource() );
		}
		
		public MPIRankObject getParentRankObject(){
			return rankObject;
		}
		
		public int getRank(){
			return rankObject.getRank();
		}
		
		
		private void parseTraceFile(BufferedTraceFileReader reader){
			// read the file and scan for set_view and open operations
			final Enumeration<ITraceEntry> traceEnum = reader.enumerateTraceEntry();
			while(traceEnum.hasMoreElements()){
				final ITraceEntry cur = traceEnum.nextElement();
				final String name = cur.getName();
				if(name.equals(MPIConstants.XML_FILEOPEN)){
					rankObject.threadSeesFileOpen(cur);
				}
				if(name.equals(MPIConstants.XML_FILE_SETVIEW)){
					rankObject.threadSeesFileSetView(cur);
				}				
				if(name.equals(MPIConstants.XML_FILECLOSE)){
					rankObject.threadSeesFileClose(cur);
				}
			}
		}
	}
	
	private MPIThreadInputPlugin getMe(){
		return this;
	}
	
	@Override
	public Class<? extends ITopologyInputPluginObject> getInstanciatedObjectsType() {	
		return MPIThreadObject.class;
	}
	
	@Override
	public boolean tryToActivate(TopologyTypes labels) {		
		for(String label: labels.getTypes()){
			if(label.toLowerCase().contains("thread")){
				return true;
			}
		}
		return false;
	}

	@Override
	public ITopologyInputPluginObject tryToInstantiateObjectFor(TopologyNode topo) {
		if(topo.getType().compareToIgnoreCase("thread") == 0){
			// matches, return rank plugin
			return new MPIThreadObject(topo);
		}
		return null;
	}
}
