package viewer.timelines.topologyPlugins;

import hdTraceInput.BufferedTraceFileReader;

import java.util.Enumeration;

import topology.ITopologyInputPluginObject;
import topology.TopologyInputPlugin;
import topology.TopologyManager;
import viewer.timelines.topologyPlugins.MPIRankInputPlugin.MPIRankObject;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.TraceEntry;


/**
 * This plugin is set exactly once on each thread.
 * 
 * @author julian
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
			
			assert(rankObject != null);
			
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
			final Enumeration<TraceEntry> traceEnum = reader.enumerateTraceEntry();
			while(traceEnum.hasMoreElements()){
				final TraceEntry cur = traceEnum.nextElement();
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
