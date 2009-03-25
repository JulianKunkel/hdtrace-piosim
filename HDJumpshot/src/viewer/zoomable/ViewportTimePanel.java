/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

public class ViewportTimePanel extends JPanel
{
	private ViewportTime   viewport;

	public ViewportTimePanel( final ViewportTime  vport )
	{
		super( new BorderLayout() );
		viewport  = vport;
		
		super.add( viewport, BorderLayout.CENTER );
		super.setBackground( Color.white );

		super.setMinimumSize( viewport.getMinimumSize() );
		super.setMaximumSize( viewport.getMaximumSize() );
		super.setPreferredSize( viewport.getPreferredSize() );
	}
}
