
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import drawable.ColorAlpha;


// Used DefaultCellEditor instead of AbstractCellEditor so jre1.2.2 can be used
// public class CategoryIconEditor extends AbstractCellEditor
public class CategoryIconEditor extends DefaultCellEditor
                                implements TableCellEditor,
                                           ActionListener
{
	private static final long serialVersionUID = -2487506394326350488L;
	
	private JButton      delegate_btn;
    private ColorAlpha   saved_color;
    private Color        prev_color;

    public CategoryIconEditor()
    {
        super( new JCheckBox() );         // super(); for DefaultCellEditor
        delegate_btn  = new JButton();
        delegate_btn.addActionListener( this );
        editorComponent = delegate_btn;   // for DefaultCellEditor
        super.setClickCountToStart(1);    // for DefaultCellEditor
        saved_color   = null;
        prev_color    = null;
    }

    // Called 1st
    public Component getTableCellEditorComponent( JTable   table,
                                                  Object   value,
                                                  boolean  isSelected,
                                                  int      irow,
                                                  int      icolumn )
    {
        // save color in "(CategoryIcon) value" for setting JColorChooser later
        CategoryIcon icon;
        icon        = (CategoryIcon) value;
        prev_color  = icon.getCategory().getColor() ;
        delegate_btn.setIcon( icon );
        return delegate_btn;
    }

    // Called 2nd
    public void actionPerformed( ActionEvent evt )
    {
        Color new_color = JColorChooser.showDialog( delegate_btn,
                                                    "Pick a Color",
                                                    prev_color );
        if ( new_color != null ) 
            saved_color = new ColorAlpha( new_color, ColorAlpha.OPAQUE );
        else
            saved_color = new ColorAlpha( prev_color, ColorAlpha.OPAQUE );
        fireEditingStopped();
    }

    // Called 3rd
    public Object getCellEditorValue()
    {
        return saved_color;
    }
}
