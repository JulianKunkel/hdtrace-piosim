/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import viewer.common.Const;
import viewer.common.Debug;
import viewer.common.LabeledTextField;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;

public class ModelTimePanel extends JPanel
                            implements TimeListener
{
    private final ModelTime         model;

    private LabeledTextField  fld_iZoom_faktor;
    private LabeledTextField  fld_tGlobal_min;
    private LabeledTextField  fld_tGlobal_max;
    private LabeledTextField  fld_tView_init;
    private LabeledTextField  fld_tView_final;
    private LabeledTextField  fld_tZoom_focus;
    private LabeledTextField  fld_time_per_pixel;

    public ModelTimePanel( ModelTime model )
    {
        super();
        this.model         = model;
        setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

        fld_iZoom_faktor    = new LabeledTextField( "Zoom faktor ", Const.INTEGER_FORMAT );
        fld_iZoom_faktor.setEditable( false );
        fld_iZoom_faktor.setHorizontalAlignment( JTextField.CENTER );
        add( fld_iZoom_faktor ); // addSeparator();

        fld_tGlobal_min    = new LabeledTextField( "Real Global Min Time", Const.PANEL_TIME_FORMAT );
        fld_tGlobal_min.setEditable( false );
        add( fld_tGlobal_min ); // addSeparator();

        fld_tView_init     = new LabeledTextField( "View  Init Time", Const.PANEL_TIME_FORMAT );
        fld_tView_init.setEditable( true );
        add( fld_tView_init ); // addSeparator();

        fld_tZoom_focus    = new LabeledTextField( "Zoom Focus Time", Const.PANEL_TIME_FORMAT );
        fld_tZoom_focus.setEditable( true );
        add( fld_tZoom_focus );

        fld_tView_final    = new LabeledTextField( "View Final Time", Const.PANEL_TIME_FORMAT );
        fld_tView_final.setEditable( true );
        add( fld_tView_final ); // addSeparator();

        fld_tGlobal_max    = new LabeledTextField( "Global Max Time",
                                                   Const.PANEL_TIME_FORMAT );
        fld_tGlobal_max.setEditable( false );
        add( fld_tGlobal_max ); 

        fld_time_per_pixel = new LabeledTextField( "Time Per Pixel",
                                                   Const.PANEL_TIME_FORMAT );
        fld_time_per_pixel.setEditable( false );
        add( fld_time_per_pixel ); 

        super.setBorder( BorderFactory.createEtchedBorder() );

        // Set up ActionListeners

        fld_tView_final.addActionListener(new ViewFinalActionListener());
        fld_tZoom_focus.addActionListener(new ViewFokusActionListener());
        
        fld_tView_init.addActionListener(new ViewInitActionListener());
        
        model.addTimeListener(this);
    }

    public class ViewInitActionListener implements ActionListener{
  		@Override
  		public void actionPerformed(ActionEvent e) {
    		double tview_init = fld_tView_init.getDouble();
    		
    		double end = model.getTimeViewExtent();
    		
    		model.zoomRapidly(tview_init, end - tview_init);
  		}  		
    }
    
    public class ViewFinalActionListener implements ActionListener{
  		@Override
  		public void actionPerformed(ActionEvent e) {
    		double tview_final = fld_tView_final.getDouble();
    		
    		double start = model.getTimeViewPosition();
    		model.zoomRapidly(start, tview_final - start);   
  		}  		
    }
    
    public class ViewFokusActionListener implements ActionListener{
  		@Override
  		public void actionPerformed(ActionEvent e) {
    		double tview_fokus = fld_tZoom_focus.getDouble();
    		
    		double extend = model.getTimeViewExtent() ;
    		model.zoomRapidly( tview_fokus - extend / 2.0  , extend);      		
  		}  		
    }
    
    /*
        timeChanged() is invoked by ModelTime's updateParamDisplay()
    */
    public void timeChanged( TimeEvent evt )
    {
        if ( Debug.isActive() )
            Debug.println( "ModelTimePanel: timeChanged()'s START: " );
        fld_iZoom_faktor.setDouble(model.getZoomFaktor());
        
        fld_tGlobal_min.setDouble( model.getTimeGlobalMinimum().getDouble() );
        fld_tView_init.setDouble( model.getTimeViewPosition() );
        fld_tZoom_focus.setDouble( model.getTimeZoomFocus() );
        fld_tView_final.setDouble( model.getTimeViewPosition()
                                 + model.getTimeViewExtent() );
        fld_tGlobal_max.setDouble( model.getTimeGlobalDuration() );
        fld_time_per_pixel.setDouble( 1.0d/model.getViewPixelsPerUnitTime() );
        if ( Debug.isActive() )
            Debug.println( "ModelTimePanel: timeChanged()'s END: " );
    }


}
