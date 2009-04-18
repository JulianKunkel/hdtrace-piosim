package arrow;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Manages arrows: allows to remove and add groups of arrows, for instance a group are
 * all individual MPI communication related arrows.
 * 
 * @author julian
 */
public class ArrowManager {
	final HashMap<String, ArrowGroup> groups = new HashMap<String, ArrowGroup>();
	
	/**
	 * Remove all groups and all arrows.
	 */
	public void clear(){
		groups.clear();
	}
	
	public ArrowGroup getGroup(String name) {
		return groups.get(name);
	}
	
	public void setGroup(ArrowGroup group) {
		groups.put(group.getName(), group);
	}
	
	public ArrowEnumerator getArrowEnumerator(Epoch startTime, Epoch endTime){
		return new ArrowEnumerator(groups.values().iterator(), startTime, endTime);
	}
}
