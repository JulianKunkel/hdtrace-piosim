
/** Version Control Information $Id: LegendTableModel.java 158 2009-03-29 15:24:32Z kunkel $
 * @lastmodified    $Date: 2009-03-29 17:24:32 +0200 (So, 29. Mär 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 158 $ 
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

import java.awt.Color;

import drawable.CategoryStatistic;
import drawable.CategoryStatistic.Adjustment;
import drawable.CategoryStatistic.Scaling;


public class LegendTableStatisticModel extends LegendTableTraceModel
{
	private static final long serialVersionUID = 4585012041612589084L;

	public  static final int        ICON_COLUMN            = 0;
	public  static final int        NAME_COLUMN            = 1;
	public  static final int        VISIBILITY_COLUMN      = 2;
	public  static final int        SCALING_COLUMN         = 3;
	public  static final int        ADJUSTMENT_COLUMN      = 4;

	private static final String[]   COLUMN_TITLES  = { "Topo", "Name      ", "V",  "S", "A"};
	private static final String[]   COLUMN_TOOLTIPS = { "Topology/Color", "Statistic Group Name", "Visibility", "Scaling", "Adjustment" };
	private static final Class<?>[]    COLUMN_CLASSES = { CategoryIcon.class, String.class, Boolean.class, TablePopupHandler.class, TablePopupHandler.class };

	private static final Color[]    COLUMN_TITLE_FORE_COLORS = { Color.magenta, Color.pink,  Color.green, Color.yellow, Color.yellow, Color.yellow , Color.yellow  };
	private static final Color[]    COLUMN_TITLE_BACK_COLORS = { Color.black, Color.gray, Color.darkGray.darker(),  Color.blue.darker(), Color.gray, Color.gray   };
	private static final boolean[]  COLUMN_TITLE_RAISED_ICONS = { false, false, true, false, false, false };

	@Override
	public Object[] getColumnAlternatives(int column) {
		switch ( column ) {
		case SCALING_COLUMN:{
			return CategoryStatistic.Scaling.values();
		}case ADJUSTMENT_COLUMN:{
			return CategoryStatistic.Adjustment.values();
		}
		}
		return super.getColumnAlternatives(column);
	}

	@Override
	public Object getValueAt( int irow, int icolumn )
	{
		switch ( icolumn ) {
		case SCALING_COLUMN:{
			CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
			return stat.getScaling();
		}case ADJUSTMENT_COLUMN:{
			CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
			return stat.getAdjustment();
		}default:
			return super.getValueAt(irow, icolumn);
		}
	}

	@Override
	public void setValueAt( Object value, int irow, int icolumn )
	{
		switch ( icolumn ) {
		case SCALING_COLUMN:{
			CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
			stat.setScaling(Scaling.valueOf((String) value));
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;
		}case ADJUSTMENT_COLUMN:{
			CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
			stat.setAdjustment(Adjustment.valueOf((String) value));
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;
		}default:
			super.setValueAt(value, irow, icolumn);
		return;
		}
	}


	public int getColumnCount()
	{
		return COLUMN_TITLES.length;
	}

	public Class<?> getColumnClass( int icolumn )
	{
		return COLUMN_CLASSES[ icolumn ];
	}

	public String getColumnName( int icolumn )
	{
		return COLUMN_TITLES[ icolumn ];
	}

	public Color getColumnNameForeground( int icolumn )
	{
		return COLUMN_TITLE_FORE_COLORS[ icolumn ];
	}

	public Color getColumnNameBackground( int icolumn )
	{
		return COLUMN_TITLE_BACK_COLORS[ icolumn ];
	}

	public boolean isRaisedColumnNameIcon( int icolumn )
	{
		return COLUMN_TITLE_RAISED_ICONS[ icolumn ];
	}

	public String getColumnToolTip( int icolumn )
	{
		return COLUMN_TOOLTIPS[ icolumn ];
	}

}
