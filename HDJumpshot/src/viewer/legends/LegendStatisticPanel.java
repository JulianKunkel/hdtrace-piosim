
 /** Version Control Information $Id: LegendPanel.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $ 
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

package viewer.legends;

import hdTraceInput.TraceFormatBufferedFileReader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;


public class LegendStatisticPanel extends JPanel
{
	private static final long serialVersionUID = -8960185595512393171L;
	
	private LegendTable  legend_table;

	public LegendStatisticPanel( final TraceFormatBufferedFileReader  reader )
	{
		super();
		super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

		Border  lowered_border, empty_border, etched_border;
		lowered_border = BorderFactory.createLoweredBevelBorder();
		empty_border   = BorderFactory.createEmptyBorder( 4, 4, 4 ,4 );
		etched_border  = BorderFactory.createEtchedBorder();

		legend_table  = new LegendTable(reader.getLegendStatisticModel());
		final JScrollPane scroller = new JScrollPane( legend_table,	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,	JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scroller.setBorder( BorderFactory.createCompoundBorder( lowered_border, BorderFactory.createCompoundBorder(empty_border, etched_border ) ) );
		super.add( scroller );
	}
}
