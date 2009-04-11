
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
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
import viewer.common.ModelTime;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;

public class ModelTimePanel extends JPanel implements TimeListener
{
	private static final long serialVersionUID = -7851480632675797787L;

	private final ModelTime         model;
	private final ScrollbarTimeModel scrollbarModel;

	private LabeledTextField  fld_iZoom_faktor;
	private LabeledTextField  fld_tGlobal_min;
	private LabeledTextField  fld_tGlobal_max;
	private LabeledTextField  fld_tView_init;
	private LabeledTextField  fld_tView_final;
	private LabeledTextField  fld_tZoom_focus;
	private LabeledTextField  fld_time_per_pixel;

	public ModelTimePanel( ScrollbarTimeModel scrollbarModel )
	{
		super();
		this.scrollbarModel = scrollbarModel;
		this.model         = scrollbarModel.getModelTime();
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

		timeChanged(null);
	}

	public class ViewInitActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			double tview_init = fld_tView_init.getDouble();

			double end = model.getViewExtent();

			model.zoomRapidly(tview_init, end - tview_init);
		}  		
	}

	public class ViewFinalActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			double tview_final = fld_tView_final.getDouble();

			double start = model.getViewPosition();
			model.zoomRapidly(start, tview_final - start);   
		}  		
	}

	public class ViewFokusActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			double tview_fokus = fld_tZoom_focus.getDouble();

			double extend = model.getViewExtent() ;
			model.zoomRapidly( tview_fokus - extend / 2.0  , extend);      		
		}  		
	}

	/*
        timeChanged() is invoked by ModelTime's updateParamDisplay()
	 */
	@Override
	public void timeChanged( TimeEvent evt )
	{
		if ( Debug.isActive() )
			Debug.println( "ModelTimePanel: timeChanged()'s START: " );
		fld_iZoom_faktor.setDouble(model.getZoomFaktor());

		fld_tGlobal_min.setDouble( model.getGlobalMinimum().getDouble() );
		fld_tView_init.setDouble( model.getViewPosition() );
		fld_tZoom_focus.setDouble( model.getTimeZoomFocus() );
		fld_tView_final.setDouble( model.getViewPosition()
				+ model.getViewExtent() );
		fld_tGlobal_max.setDouble( model.getGlobalDuration() );
		fld_time_per_pixel.setDouble( 1.0d/scrollbarModel.getViewPixelsPerUnitTime() );
		if ( Debug.isActive() )
			Debug.println( "ModelTimePanel: timeChanged()'s END: " );
	}


}
