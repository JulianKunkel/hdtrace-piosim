package viewer.zoomable;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * This interface allows to search through the contained TraceObjects
 * 
 * @author julian
 */
public interface SearchableView
{	
    public SearchResults searchPreviousComponent(Epoch earlierThan);
    public SearchResults searchNextComponent(Epoch laterThan);

}
