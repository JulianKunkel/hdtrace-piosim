
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import drawable.Category;


public class LegendTableTraceModel extends AbstractTableModel
{
	private static final long serialVersionUID = 4585012041612589084L;

	public  static final int        ICON_COLUMN            = 0;
	public  static final int        NAME_COLUMN            = 1;
	public  static final int        VISIBILITY_COLUMN      = 2;
	public  static final int        SEARCHABILITY_COLUMN   = 3;

	private static final String[]   COLUMN_TITLES  = { "Topo", "Name      ", "V", "S" };
	private static final String[]   COLUMN_TOOLTIPS
	= { "Topology/Color", "Category Name",
		"Visibility", "Searchability" };
	private static final Class<?>[]    COLUMN_CLASSES = { CategoryIcon.class, String.class, Boolean.class, Boolean.class };

	private static final Color[]    COLUMN_TITLE_FORE_COLORS
	= { Color.magenta, Color.pink,
		Color.green, Color.yellow };
	private static final Color[]    COLUMN_TITLE_BACK_COLORS
	= { Color.black, Color.gray,
		Color.darkGray.darker(),
		Color.blue.darker() };
	private static final boolean[]  COLUMN_TITLE_RAISED_ICONS
	= { false, false, true, false };

	final private List<Category>   categories = new LinkedList<Category>();
	private List<CategoryIcon>   icon_list      = new ArrayList<CategoryIcon>();

	/**
	 * Only if true then the listeners are activated
	 */
	private boolean enableCategoryUpdateListener = true;

	private List<CategoryUpdatedListener> categoryUpdateListeners = new LinkedList<CategoryUpdatedListener>();

	/**
	 * Remove all existing categories
	 */
	public void clearCategories(){
		categories.clear();
		icon_list.clear();
	}

	/**
	 * Add a category
	 * @param cat
	 */
	public void addCategory(Category cat){
		categories.add(cat);
	}

	public void addCategoryUpdateListener(CategoryUpdatedListener listener){
		categoryUpdateListeners.add(listener);
	}
	
	public void removeCategoryUpdateListener(CategoryUpdatedListener listener){
		categoryUpdateListeners.remove(listener);
	}

	//  Sorting into various order
	private void initIconListFromCategoryList()
	{
		icon_list    = new ArrayList<CategoryIcon>( categories.size() );

		CategoryIcon icon;
		Category  objdef;
		Iterator<Category>  objdefs = categories.iterator();
		while ( objdefs.hasNext() ) {
			objdef = (Category) objdefs.next();
			icon   = new CategoryIcon( objdef );
			icon_list.add( icon );
		}
	}

	/**
	 * If the model categories are completed, then commit them.
	 */
	public void commitModel(){
		this.sortNormally( LegendComparators.TOPOLOGY_NAME_ORDER );

		for(CategoryUpdatedListener listener: categoryUpdateListeners){
			listener.categoriesAddedOrRemoved();
		}
	}

	private void sortNormally( Comparator<Category> comparator )
	{
		Collections.sort( categories, comparator );
		this.initIconListFromCategoryList();        
	}

	private void sortReversely( Comparator<Category> comparator )
	{
		Collections.sort( categories, comparator );
		Collections.reverse( categories );
		this.initIconListFromCategoryList();        
	}

	public void arrangeOrder( Comparator<Category> comparator )
	{
		this.sortNormally( comparator );
		super.fireTableDataChanged(); 
	}

	public void reverseOrder( Comparator<Category> comparator )
	{
		this.sortReversely( comparator );
		super.fireTableDataChanged(); 
	}


	/**
	 * Return the list of alternatives for a jpopup
	 * @param column
	 * @return
	 */
	public IPopupType[] getPopupColumnAlternatives(int column){
		return null;
	}

	public int getRowCount()
	{
		return categories.size();
	}

	public int getColumnCount()
	{
		return COLUMN_TITLES.length;
	}

	public Class getColumnClass( int icolumn )
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

	public Object getValueAt( int irow, int icolumn )
	{
		Category  objdef;
		switch ( icolumn ) {
		case ICON_COLUMN :
			return icon_list.get( irow );
		case NAME_COLUMN :
			objdef = categories.get( irow );
			return objdef.getName();
		case VISIBILITY_COLUMN :
			objdef = categories.get( irow );
			if ( objdef.isVisible() )
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		case SEARCHABILITY_COLUMN :
			objdef =  categories.get( irow );
			if ( objdef.isSearchable() )
				return Boolean.TRUE;
			else
				return Boolean.FALSE;
		default:
			System.err.println( "LegendTableModel.getValueAt("
					+ irow + "," + icolumn + ") fails!" );
		return null;
		}
	}

	public boolean isCellEditable( int irow, int icolumn )
	{
		if(icolumn != NAME_COLUMN)
			return true;
		return false;
	}

	/**
	 * Notify all components that the visibility changed
	 */
	protected void fireCategoryVisibilityChanged(Category category, boolean value){
		for(CategoryUpdatedListener list: categoryUpdateListeners){
			list.categoryVisibilityModified(category, value);
		}

		if(enableCategoryUpdateListener)
			fireCategoryVisibilityWasModified();
	}


	/**
	 * Notify all components that the color changed
	 */
	protected void fireCategoryAttributeChanged(Category category, Object newValue){
		for(CategoryUpdatedListener list: categoryUpdateListeners){
			list.categoryAttributeModified(category, newValue);
		}
		
		if(enableCategoryUpdateListener)
			fireCategoryAttributesWereModified();
	}    

	protected void fireCategoryVisibilityWasModified(){
		if(! enableCategoryUpdateListener)
			return;

		for(CategoryUpdatedListener list: categoryUpdateListeners){
			list.categoryVisibilityWasModified();
		}
	}
	
	final protected void fireCategoryAttributesWereModified(){
		if(! enableCategoryUpdateListener)
			return;

		for(CategoryUpdatedListener list: categoryUpdateListeners){
			list.categoryAttributesWereModified();
		}
	}
	
	public void fireCategoryModified(int column, Category category, Object newValue){
		if(! enableCategoryUpdateListener)
			return;

		if (column == VISIBILITY_COLUMN){
			fireCategoryVisibilityChanged(category, (Boolean) newValue);
		}else{
			fireCategoryAttributeChanged(category, newValue);
		}
	}
	
	
	public void fireCategoryModificationFinished(int column){
		if(! enableCategoryUpdateListener)
			return;

		if (column == VISIBILITY_COLUMN){
			fireCategoryVisibilityWasModified();
		}else{
			fireCategoryAttributesWereModified();
		}
	}    

	/**
	 * Enable or disable automatic notification of listeners to avoid to trigger them by
	 * each modification. This is needed for mass updates.
	 * 
	 * Maybe you should fire the listeners by hand after you made all your updates.
	 * 
	 * @param enable
	 */
	public void enableCategoryListener(boolean enable){
		enableCategoryUpdateListener = enable;
	}

	public void setValueAt( Object value, int irow, int icolumn )
	{
		if (value == null)
			return;

		Category      objdef;
		CategoryIcon  icon;
		Color    color;

		objdef = categories.get( irow );
		switch ( icolumn ) {
		case ICON_COLUMN :
			color  = (Color) value;
			objdef.setColor( color );
			icon   = (CategoryIcon) icon_list.get( irow );
			icon.getCategory().setColor( color );
			fireTableCellUpdated( irow, icolumn );        
			
			fireCategoryAttributeChanged(objdef, color);
			break;
		case NAME_COLUMN :
			//objdef.setName( (String) value );
			//fireTableCellUpdated( irow, icolumn );
			break;
		case VISIBILITY_COLUMN :
			boolean val = ( (Boolean) value ).booleanValue() ;
			objdef.setVisible( val );
			fireCategoryVisibilityChanged(objdef, val);
			
			fireTableCellUpdated( irow, icolumn );
			break;
		case SEARCHABILITY_COLUMN :
			objdef.setSearchable( ( (Boolean) value ).booleanValue() );
			fireTableCellUpdated( irow, icolumn );
			break;
		default:
			System.err.print( "LegendTableModel.setValueAt("
					+ irow + "," + icolumn + ") fails!" );
		}
	}

	protected Category getCategory(int irow){
		return categories.get( irow );
	}

}
