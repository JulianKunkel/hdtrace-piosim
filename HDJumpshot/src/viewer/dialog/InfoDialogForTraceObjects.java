package viewer.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;

public class InfoDialogForTraceObjects extends InfoDialog
{
    public InfoDialogForTraceObjects( final Frame     frame, 
                                  final double    clicked_time,
                                  TraceObject obj )
    {
        super( frame, "Traceable Object Info Box", clicked_time );

        Container root_panel = this.getContentPane();
        root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

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
