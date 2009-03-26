/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.dialog;

import java.awt.Dialog;

import drawable.TimeBoundingBox;



/*
   Define the interface to be implemented by the view object, ScrollableView,
   so that time averaged quantity can be returned.
*/

public interface SummarizableView
{
    public InitializableDialog createSummary( final Dialog          dialog,
                                              final TimeBoundingBox timebox );
}
