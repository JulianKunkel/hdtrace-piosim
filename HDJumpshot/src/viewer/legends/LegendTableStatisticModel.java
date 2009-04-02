
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

package viewer.legends;

import java.awt.Color;

import drawable.CategoryStatistic;
import drawable.CategoryStatistic.MaxAdjustment;
import drawable.CategoryStatistic.MinAdjustment;
import drawable.CategoryStatistic.Scaling;


public class LegendTableStatisticModel extends LegendTableTraceModel
{
	private static final long serialVersionUID = 4585012041612589084L;

	public  static final int        ICON_COLUMN            = 0;
	public  static final int        NAME_COLUMN            = 1;
	public  static final int        VISIBILITY_COLUMN      = 2;
	public  static final int        SCALING_COLUMN         = 3;
	public  static final int        ADJUSTMENT_MIN_COLUMN  = 4;
	public  static final int        ADJUSTMENT_MAX_COLUMN  = 5;
	public  static final int        SHOW_AVG_LINE_COLUMN   = 6;

	private static final String[]   COLUMN_TITLES  = { "Topo", "Name         ", "V",  "S", "<", ">", "A"};
	private static final String[]   COLUMN_TOOLTIPS = { "Topology/Color", "Statistic Group Name", "Visibility", "Scaling", "Max Adjustment", "Min Adjustment", "Show average line" };
	private static final Class<?>[]    COLUMN_CLASSES = { CategoryIcon.class, String.class, Boolean.class, TablePopupHandler.class, TablePopupHandler.class, TablePopupHandler.class, Boolean.class };

	private static final Color[]    COLUMN_TITLE_FORE_COLORS = { Color.magenta, Color.pink,  Color.green, Color.yellow, Color.yellow, Color.yellow, Color.yellow, Color.yellow, Color.yellow  };
	private static final Color[]    COLUMN_TITLE_BACK_COLORS = { Color.black, Color.gray, Color.darkGray.darker(),  Color.blue.darker(), Color.gray, Color.gray, Color.gray, Color.gray   };
	private static final boolean[]  COLUMN_TITLE_RAISED_ICONS = { false, false, true, false, true, true, true, false };
	
	@Override
	public IPopupType[] getPopupColumnAlternatives(int column) {
		switch ( column ) {
		case SCALING_COLUMN:{
			return CategoryStatistic.Scaling.values();
		}case ADJUSTMENT_MIN_COLUMN:{
			return CategoryStatistic.MinAdjustment.values();			
		}case ADJUSTMENT_MAX_COLUMN:{
			return CategoryStatistic.MaxAdjustment.values();
		}
		}
		return super.getPopupColumnAlternatives(column);
	}

	@Override
	public Object getValueAt( int irow, int icolumn )
	{
		final CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
		switch ( icolumn ) {
		case SHOW_AVG_LINE_COLUMN:
			return stat.isShowAverageLine();
		case SCALING_COLUMN:{
			return stat.getScaling().getAbbreviationChar();
		}case ADJUSTMENT_MIN_COLUMN:{
			return stat.getMinAdjustment().getAbbreviationChar();
		}case ADJUSTMENT_MAX_COLUMN:{
			return stat.getMaxAdjustment().getAbbreviationChar();			
		}default:
			return super.getValueAt(irow, icolumn);
		}
	}

	@Override
	public void setValueAt( Object value, int irow, int icolumn )
	{
    	if (value == null)
    		return;

		final CategoryStatistic stat = (CategoryStatistic) getCategory(irow);
		switch ( icolumn ) {
		case SHOW_AVG_LINE_COLUMN:
			stat.setShowAverageLine((Boolean) value);
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;
		case SCALING_COLUMN:{
			stat.setScaling(Scaling.valueOf((String) value));
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;
		}case ADJUSTMENT_MIN_COLUMN:{
			stat.setMinAdjustment(MinAdjustment.valueOf((String) value));
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;			
		}case ADJUSTMENT_MAX_COLUMN:{
			stat.setMaxAdjustment(MaxAdjustment.valueOf((String) value));
			fireTableCellUpdated( irow, icolumn );
			
			fireCategoryVisibilityChanged();
			return;
		}default:
			super.setValueAt(value, irow, icolumn);
		return;
		}
	}
	
	@Override
	public boolean getForceFireListenerOnUpdate(int icolumn) {
		if (icolumn == SHOW_AVG_LINE_COLUMN)
			return true;
		return super.getForceFireListenerOnUpdate(icolumn);
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
