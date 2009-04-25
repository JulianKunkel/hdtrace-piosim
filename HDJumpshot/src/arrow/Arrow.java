package arrow;

import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * An arrow interconnects two topologies at a given time.
 * @author julian
 *
 */
public class Arrow{
	final private TopologyEntry startTopology;
	final private TopologyEntry endTopology;

	final private Epoch startTime;
	final private Epoch endTime;
	final private ArrowCategory category;

	/**
	 * @param firstTopology
	 * @param firstTime The absolute time (not viewer time) for the start topology
	 * @param scndTopology
	 * @param scndTime The absolute time (not viewer time) for the end topology
	 * @param category
	 */
	public Arrow(TopologyEntry firstTopology, Epoch firstTime, TopologyEntry scndTopology , 
			Epoch scndTime, ArrowCategory category) {
		if(firstTime.compareTo(scndTime) <= 0){
			this.endTime = scndTime;
			this.startTime = firstTime;		
			this.endTopology = scndTopology;
			this.startTopology = firstTopology;
		}else{
			this.endTime = firstTime;
			this.startTime = scndTime;		
			this.endTopology = firstTopology;
			this.startTopology = scndTopology;
		}
		this.category = category;
	}

	public Epoch getEndTime() {
		return endTime;
	}

	public Epoch getStartTime() {
		return startTime;
	}

	public TopologyEntry getEndTopology() {
		return endTopology;
	}

	public TopologyEntry getStartTopology() {
		return startTopology;
	}

	public ArrowCategory getCategory() {
		return category;
	}
}