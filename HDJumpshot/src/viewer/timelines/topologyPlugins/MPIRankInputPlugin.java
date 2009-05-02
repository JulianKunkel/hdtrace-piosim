package viewer.timelines.topologyPlugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import topology.ITopologyInputPluginObject;
import topology.TopologyInputPlugin;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * This plugin is set exactly once on each rank!
 * 
 * @author julian
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
		private ArrayList<TraceEntry> fileOpens = new ArrayList<TraceEntry>();

		/**
		 * Remember file views
		 */
		private ArrayList<TraceEntry> fileViews = new ArrayList<TraceEntry>();
		
		/**
		 * Remember file close (used together with file views)
		 */
		private ArrayList<TraceEntry> fileClose = new ArrayList<TraceEntry>();

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
				Comparator<TraceEntry> comp = new Comparator<TraceEntry>(){
					@Override
					public int compare(TraceEntry o1, TraceEntry o2) {
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

		private TraceEntry binSearchBeforeTraceObj(ArrayList<TraceEntry> list, Epoch earlierThan, String fid){
			int pos = binSearchBefore(list, earlierThan, fid); 			
			if (pos == -1 ){
				return null;
			}			
			return list.get(pos); 
		}
		
		private int binSearchBefore(ArrayList<TraceEntry> list, Epoch earlierThan, String fid){
			
			checkOperationsSorted();
			
			int min = 0;
			int max = list.size() -1;

			if (max == -1){
				return -1;
			}

			while(true){
				final int cur = (min + max) / 2;
				final TraceEntry entry = list.get(cur);
				
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

		public TraceEntry getPreviousFileOpen(Epoch earlierThan, String fid){
			return binSearchBeforeTraceObj(fileOpens, earlierThan, fid);
		}

		public TraceEntry getPreviousFileSetView(Epoch earlierThan, String fid){			
			// now we know there happens sth. before, however it might be sth. like 
			// [(open), close,] (open), setview, close, close, close, (open), setview
			
			final TraceEntry view = binSearchBeforeTraceObj(fileViews, earlierThan, fid);
			final TraceEntry close = binSearchBeforeTraceObj(fileClose, earlierThan, fid);
			
			if(close != null && close.getEarliestTime().compareTo(view.getEarliestTime()) > 0) {
				// if close later than view got set => no view any more.
				return null;
			}else{			
				return view;
			}
		}
		
		void threadSeesFileOpen(TraceEntry fopen){
			assert(! isOperationsSorted);

			fileOpens.add(fopen);
		}

		void threadSeesFileSetView(TraceEntry fview){
			assert(! isOperationsSorted);
			
			fileViews.add(fview);
		}

		void threadSeesFileClose(TraceEntry fclose){
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
	public boolean tryToActivate(TopologyLabels labels) {		
		for(String label: labels.getLabels()){
			if(label.toLowerCase().contains("rank")){
				return true;
			}
		}
		return false;
	}

	@Override
	public ITopologyInputPluginObject tryToInstantiateObjectFor(TopologyNode topo) {
		if(topo.getLabel().compareToIgnoreCase("rank") == 0){
			// matches, return rank plugin
			return new MPIRankObject(topo);
		}
		return null;
	}
}
