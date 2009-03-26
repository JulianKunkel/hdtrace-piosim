package viewer.zoomable;

import de.hd.pvs.TraceFormat.TraceObject;


/**
 * Encapsulates results of a search
 * @author julian
 *
 */
public class SearchResults {
	
	final int timeline;
	final TraceObject object;
	
	public SearchResults(int timeline, TraceObject object) {
		this.timeline = timeline;
		this.object = object;
	}
	
	public TraceObject getObject() {
		return object;
	}
	
	public int getTimeline() {
		return timeline;
	}
	
	public boolean wasSucessfull(){
		return object != null;
	}
}
