
/** Version Control Information $Id: InfoDialogForTraceObjects.java 187 2009-04-05 12:36:44Z kunkel $
 * @lastmodified    $Date: 2009-04-05 14:36:44 +0200 (So, 05 Apr 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 187 $ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of HDJumpshot.

//HDJumpshot is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//HDJumpshot is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


package viewer.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import topology.TopologyStatisticTreeNode;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class InfoDialogForStatisticEntries extends InfoDialog
{
	private static final long serialVersionUID = 1L;

	public InfoDialogForStatisticEntries( final Frame     frame, 
			final Epoch modelTimeOffset,
			final Epoch clicked_time,
			TopologyStatisticTreeNode topologyTreeNode, 
			StatisticsEntry obj)
	{
		super( frame, "Traceable Object Info Box", clicked_time, modelTimeOffset);

		Container root_panel = this.getContentPane();
		root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

		JLabel label = new JLabel(obj.getType().toString());
		root_panel.add(label);        


		final Font notBold = new Font("SansSerif", Font.PLAIN, label.getFont().getSize());

		String text = "";

		label = new JLabel(text);
		label.setFont(notBold);
		label.setBackground(Color.LIGHT_GRAY);

		root_panel.add(label);


		root_panel.add( super.getCloseButtonPanel() );
	}
}
