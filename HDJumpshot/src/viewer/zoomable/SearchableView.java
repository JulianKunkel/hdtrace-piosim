/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;


/*
   Define the interface to be implemented by the view object, ScrollableView,
   so that it is searchable in time
*/

public interface SearchableView
{   
    // NEW search starting from the specified time
    public SearchPanel searchPreviousComponent( double searching_time );

    // CONTINUING search
    public SearchPanel searchPreviousComponent();

    // NEW search starting from the specified time
    public SearchPanel searchNextComponent( double searching_time );

    // CONTINUING search
    public SearchPanel searchNextComponent();

}
