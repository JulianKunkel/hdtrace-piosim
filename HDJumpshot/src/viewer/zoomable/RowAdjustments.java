/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import viewer.common.Const;
import viewer.common.LabeledTextField;
import viewer.topology.TopologyChangeListener;
import viewer.topology.TopologyManager;

public class RowAdjustments
{
	private ViewportTimeYaxis      canvas_vport;
	private TopologyManager        topologyManager;

	private JSlider                slider_VIS_ROW_COUNT;
	private LabeledTextField       fld_VIS_ROW_COUNT;
	private JButton                fitall_btn;

	private JPanel                 slider_panel;

	private int oldRowCount = -1;
	
	private MyTopologyChangeListener topoChangeListener = new MyTopologyChangeListener();
	
	private class MyTopologyChangeListener implements TopologyChangeListener{
		@Override
		public void topologyChanged() {
			refreshSlidersAndTextFields();
		}
	}
	
	public RowAdjustments( ViewportTimeYaxis y_vport, TopologyManager topologyManager )
	{
		this.canvas_vport  = y_vport;
		this.topologyManager     = topologyManager;
		topologyManager.addTopologyChangedListener(topoChangeListener);


		// For constant visible row count during timeline canvas resizing
		slider_VIS_ROW_COUNT = new JSlider( JSlider.VERTICAL );
		slider_VIS_ROW_COUNT.setMajorTickSpacing(10);
		slider_VIS_ROW_COUNT.setMinorTickSpacing(1);
		slider_VIS_ROW_COUNT.setInverted( false );
		slider_VIS_ROW_COUNT.addChangeListener(	new RowCountSliderListener() );
		slider_VIS_ROW_COUNT.setAlignmentX(Component.CENTER_ALIGNMENT);
		slider_VIS_ROW_COUNT.setPaintTicks(true);
		slider_VIS_ROW_COUNT.setPaintLabels(true);

		
		fld_VIS_ROW_COUNT = new LabeledTextField( "Row Count", "###0.0#" );
		// Const.INTEGER_FORMAT );
		fld_VIS_ROW_COUNT.setToolTipText(
		"Visible row count in canvas during timeline window resizing." );
		fld_VIS_ROW_COUNT.setHorizontalAlignment( JTextField.CENTER );
		fld_VIS_ROW_COUNT.setEditable( true );
		fld_VIS_ROW_COUNT.addActionListener( new RowCountTextListener() );

		fitall_btn  = new JButton( "Fit All Rows" );
		fitall_btn.setBorder( BorderFactory.createRaisedBevelBorder() );
		fitall_btn.setToolTipText(
				"Compute the optimal row height that fits all the rows "
				+ "in the Timeline canvas" );
		fitall_btn.addActionListener( new ButtonActionListener() );
		fitall_btn.setFont(Const.FONT);
		
		slider_panel = new JPanel();			
		
		slider_panel.setLayout( new BoxLayout( slider_panel, BoxLayout.Y_AXIS ) );
		slider_panel.addComponentListener( new SliderComponentListener() );
		
		fld_VIS_ROW_COUNT.setAlignmentX(Component.CENTER_ALIGNMENT);
		slider_VIS_ROW_COUNT.setAlignmentX(Component.CENTER_ALIGNMENT);
		fitall_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		slider_panel.add(fld_VIS_ROW_COUNT);
		slider_panel.add(slider_VIS_ROW_COUNT);
		slider_panel.add(fitall_btn);
		
		slider_VIS_ROW_COUNT.setMinimumSize(fitall_btn.getMinimumSize());
	}


	public void refreshSlidersAndTextFields()
	{
		int row_count   = topologyManager.getRowCount();
		int row_height  = topologyManager.getRowHeight();

		slider_VIS_ROW_COUNT.setMinimum( row_count < 2 ? 1 : 2 );
		slider_VIS_ROW_COUNT.setMaximum( row_count );


		fld_VIS_ROW_COUNT.setInteger( row_count );

		slider_VIS_ROW_COUNT.setValue(row_count);
		oldRowCount = row_count;

	}


	public JPanel getSliderPanel()
	{
		return slider_panel;
	}

	private void initPanelsToRowCountMode()
	{
		slider_panel.removeAll();
		slider_panel.add( slider_VIS_ROW_COUNT );
		slider_panel.revalidate();
		slider_panel.repaint();
	}

	public void updateSlidersAfterTreeExpansion()
	{
		int row_count   = topologyManager.getRowCount();
		slider_VIS_ROW_COUNT.setMaximum( row_count );
		fld_VIS_ROW_COUNT.fireActionPerformed();
	}


	private class SliderComponentListener extends ComponentAdapter
	{
		public void componentResized( ComponentEvent evt )
		{
			double vis_row_count, row_height;
			vis_row_count  = fld_VIS_ROW_COUNT.getDouble();
			row_height     = (double) canvas_vport.getHeight() / vis_row_count;
			topologyManager.setRowHeight( (int) row_height );
			canvas_vport.fireComponentRedrawEvent();
		}
	}

	private class RowCountSliderListener implements ChangeListener
	{
		public void stateChanged( ChangeEvent evt )
		{
			adjustRowCount( slider_VIS_ROW_COUNT.getValue() );
		}		
	}

	private class RowCountTextListener implements ActionListener
	{
		public void actionPerformed( ActionEvent evt )
		{
			adjustRowCount( fld_VIS_ROW_COUNT.getInteger());
		}
	}

	private void adjustRowCount(int row_count){    	

		int min_vis_row_count, max_vis_row_count;
		min_vis_row_count = (int) slider_VIS_ROW_COUNT.getMinimum();
		max_vis_row_count = (int) slider_VIS_ROW_COUNT.getMaximum();

		if ( row_count > max_vis_row_count ) {
			row_count  = max_vis_row_count;

		}
		else if ( row_count < min_vis_row_count ) {
			row_count   = min_vis_row_count;
		}

		int irow_count = (int) row_count;

		if( oldRowCount == irow_count )
			return;

		oldRowCount = irow_count;

		fld_VIS_ROW_COUNT.setInteger( irow_count );
		slider_VIS_ROW_COUNT.setValue( irow_count );

		double row_height     = (double) canvas_vport.getHeight() /  irow_count ;
		topologyManager.setRowHeight( (int) row_height );

		canvas_vport.fireComponentRedrawEvent();
	}

	private class ButtonActionListener implements ActionListener
	{
		public void actionPerformed( ActionEvent evt )
		{
			adjustRowCount(topologyManager.getRowCount());            
		}
	}
}
