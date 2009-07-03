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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.ITopologyInputPluginObject;
import de.topology.TopologyInputPlugin;


/**
 * This plugin is set exactly once on each rank!
 * 
 * @author Julian M. Kunkel
 *
 */
public class MPIRankInputPlugin extends TopologyInputPlugin{

	public class MPIRankObject implements ITopologyInputPluginObject{
		private final int rank;

		/* Contains interesting operations, sorted by fid and then by time, therefore
		 * each of the following lists shall contain [fid0 t0 t1 t2]+[fid1 t0 t1 t2]+... */

		/**
		 * Remember file opens
		 */
		private ArrayList<ITraceEntry> fileOpens = new ArrayList<ITraceEntry>();

		/**
		 * Remember file views
		 */
		private ArrayList<ITraceEntry> fileViews = new ArrayList<ITraceEntry>();
		
		/**
		 * Remember file close (used together with file views)
		 */
		private ArrayList<ITraceEntry> fileClose = new ArrayList<ITraceEntry>();

		/**
		 * Checks whether the operations are sorted or not.
		 */
		boolean isOperationsSorted = false;

		@Override
		public TopologyInputPlugin getResponsiblePlugin() {
			return getMe();
		}

		public MPIRankObject(TopologyNode node) {
			rank = Integer.parseInt(node.getName());
		}

		public int getRank() {
			return rank;
		}

		private void checkOperationsSorted(){
			if(! isOperationsSorted){
				// sort them
				Comparator<ITraceEntry> comp = new Comparator<ITraceEntry>(){
					@Override
					public int compare(ITraceEntry o1, ITraceEntry o2) {
						final String fidStr = o1.getAttribute("fid");
						final String fidStr2 = o2.getAttribute("fid");
						
						int ret = fidStr.compareTo(fidStr2);
						if(ret != 0){ 
							return o1.getEarliestTime().compareTo(o2.getEarliestTime());
						}else{
							return ret;
						}						
					}
				};

				Collections.sort(fileOpens, comp);
				Collections.sort(fileViews, comp);

				isOperationsSorted = true;
			}			
		}

		private ITraceEntry binSearchBeforeTraceObj(ArrayList<ITraceEntry> list, Epoch earlierThan, String fid){
			int pos = binSearchBefore(list, earlierThan, fid); 			
			if (pos == -1 ){
				return null;
			}			
			return list.get(pos); 
		}
		
		private int binSearchBefore(ArrayList<ITraceEntry> list, Epoch earlierThan, String fid){
			
			checkOperationsSorted();
			
			int min = 0;
			int max = list.size() -1;

			if (max == -1){
				return -1;
			}

			while(true){
				final int cur = (min + max) / 2;
				final ITraceEntry entry = list.get(cur);
				
				if(min == max){ // found entry or stopped.
					
					final String fidStr = entry.getAttribute("fid");
					if(fidStr.compareTo(fid) != 0 || entry.getEarliestTime().compareTo(earlierThan) > 0)
						return -1;
					
					return cur;
				} 

				// not found => continue bin search:

				// first compare fid:
				final String fidStr = entry.getAttribute("fid");
				final int ret = fidStr.compareTo(fid);
				if(ret > 0 || (ret == 0 && entry.getEarliestTime().compareTo(earlierThan) >= 0)){
					max = cur;
				}else{
					min = cur + 1;
				}
			}
		}

		public ITraceEntry getPreviousFileOpen(Epoch earlierThan, String fid){
			return binSearchBeforeTraceObj(fileOpens, earlierThan, fid);
		}

		public ITraceEntry getPreviousFileSetView(Epoch earlierThan, String fid){			
			// now we know there happens sth. before, however it might be sth. like 
			// [(open), close,] (open), setview, close, close, close, (open), setview
			
			final ITraceEntry view = binSearchBeforeTraceObj(fileViews, earlierThan, fid);
			final ITraceEntry close = binSearchBeforeTraceObj(fileClose, earlierThan, fid);
			
			if(close != null && close.getEarliestTime().compareTo(view.getEarliestTime()) > 0) {
				// if close later than view got set => no view any more.
				return null;
			}else{			
				return view;
			}
		}
		
		void threadSeesFileOpen(ITraceEntry fopen){
			assert(! isOperationsSorted);

			fileOpens.add(fopen);
		}

		void threadSeesFileSetView(ITraceEntry fview){
			assert(! isOperationsSorted);
			
			fileViews.add(fview);
		}

		void threadSeesFileClose(ITraceEntry fclose){
			assert(! isOperationsSorted);

			fileClose.add(fclose);
		}		
	}

	private MPIRankInputPlugin getMe(){
		return this;
	}

	@Override
	public Class<? extends ITopologyInputPluginObject> getInstanciatedObjectsType() {
		return MPIRankObject.class;
	}

	@Override
	public boolean tryToActivate(TopologyTypes labels) {		
		for(String label: labels.getTypes()){
			if(label.toLowerCase().contains("rank")){
				return true;
			}
		}
		return false;
	}

	@Override
	public ITopologyInputPluginObject tryToInstantiateObjectFor(TopologyNode topo) {
		if(topo.getType().compareToIgnoreCase("rank") == 0){
			// matches, return rank plugin
			return new MPIRankObject(topo);
		}
		return null;
	}
}
