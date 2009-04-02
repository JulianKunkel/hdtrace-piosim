
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class CategoryIconEditor implements TableCellEditor, ActionListener                                           
{
	private static final long serialVersionUID = -2487506394326350488L;
	
	private JButton      delegate_btn;
	private CellEditorListener listener;
	
    final private JColorChooser colorChooser = new JColorChooser();    
    
    private JDialog      colorDialog;
    private ColorListener colorListener = new ColorListener();
    
    private class ColorListener implements ActionListener{
    	@Override
    	public void actionPerformed(ActionEvent e) {     
            listener.editingStopped(null);	
    	}
    }

    public CategoryIconEditor()
    {
        delegate_btn  = new JButton();
        delegate_btn.addActionListener( this );
        colorDialog = JColorChooser.createDialog(null, "Pick a Color", false, colorChooser, colorListener, null);
    }

    // Called 1st
    public Component getTableCellEditorComponent( JTable   table,
                                                  Object   value,
                                                  boolean  isSelected,
                                                  int      irow,
                                                  int      icolumn )
    {    	
        CategoryIcon icon;
        icon        = (CategoryIcon) value;
        colorChooser.setColor(icon.getCategory().getColor());
        delegate_btn.setIcon( icon );
        
        return delegate_btn;
    }

    @Override
    public void actionPerformed( ActionEvent evt )
    {
    	// called upon click on the table:
    	colorDialog.setVisible(true);
    }
    
    @Override
    public Object getCellEditorValue()
    {      
    	return colorChooser.getColor();    
    }
    
    @Override
    public void addCellEditorListener(CellEditorListener l) {
    	listener = l;
    }
    
    @Override
    public void cancelCellEditing() {
    }
    
    @Override
    public boolean isCellEditable(EventObject anEvent) {
    	return true;
    }
    
    @Override
    public void removeCellEditorListener(CellEditorListener l) {
    }
    
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
    	return true;
    }
    
    @Override
    public boolean stopCellEditing() {
    	return true;
    }
}
