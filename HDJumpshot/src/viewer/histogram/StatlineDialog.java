/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.histogram;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.WindowConstants;

import viewer.dialog.InitializableDialog;
import viewer.zoomable.ActionTimelineRestore;
import viewer.zoomable.ViewportTimeYaxis;
import drawable.TimeBoundingBox;

public class StatlineDialog extends InitializableDialog
{
    private StatlinePanel  top_panel;

    public StatlineDialog( final Dialog              ancestor_dialog,
                           final TimeBoundingBox     timebox,
                           ActionTimelineRestore restore,
                           ViewportTimeYaxis  canvas_viewport)
    {
        super( ancestor_dialog, "Histogram for the duration [ "
                              + (float)timebox.getEarliestTime() + ", "
                              + (float)timebox.getLatestTime() + " ]" );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

        top_panel = new StatlinePanel( this, timebox, restore, canvas_viewport );
        setContentPane( top_panel );

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                StatlineDialog.this.dispose();
            }
        } );
    }

    public void init()
    {
        top_panel.init();
    }
}
