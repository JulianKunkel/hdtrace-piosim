/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.timelines;

import java.awt.*;
import javax.swing.*;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;

import java.util.Map;

import base.drawable.DrawObjects;
import viewer.zoomable.InfoDialog;

public class InfoDialogForTraceObjects extends InfoDialog
{
    public InfoDialogForTraceObjects( final Frame     frame, 
                                  final double    clicked_time,
                                  TraceObject obj )
    {
        super( frame, "Traceable Object Info Box", clicked_time );

        Container root_panel = this.getContentPane();
        root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

        //root_panel.add( new InfoPanelForDrawable( map_line2treenodes,
        //                                          y_colnames, clicked_dobj ) );
        
        JLabel label = new JLabel(obj.getType().toString());
        root_panel.add(label);        
        

        Font notBold = new Font("SansSerif", Font.PLAIN, label.getFont().getSize());
        
        String text = "";
        
        switch (obj.getType()){
        case STATE:
        case EVENT:
        	label = new JLabel("Contained XML data:");
          root_panel.add(label);
          
        	text = ((XMLTraceEntry) obj).toString();
        	break;
        case STATISTICENTRY:        	        
        	break;
        }
        
        label = new JLabel(text);
        label.setFont(notBold);
        label.setBackground(Color.LIGHT_GRAY);
        
        root_panel.add(label);
        

        root_panel.add( super.getCloseButtonPanel() );
    }
}
