/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import javax.swing.JButton;

public interface ToolBarStatus
{
    public void    resetZoomButtons();

    public JButton getPropertyRefreshButton();

    public JButton getTimelineDeleteButton();

}


