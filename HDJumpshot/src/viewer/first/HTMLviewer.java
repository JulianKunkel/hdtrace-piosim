
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

package viewer.first;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import viewer.common.Dialogs;
import viewer.common.IconManager;
import viewer.common.IconManager.IconType;

public class HTMLviewer extends JDialog
implements HyperlinkListener
{
	private static final long serialVersionUID = 8816631783449977320L;
	
	private JTextField   input_fld;
	private JEditorPane  html_panel;

	private JButton      init_btn;

	private JButton      backward_btn;
	private JButton      forward_btn;
	private JButton      refresh_btn;
	private JButton      close_btn;
	final private Stack<URL>        url_undo_stack = new Stack<URL>();
	final private Stack<URL>        url_redo_stack = new Stack<URL>();

	public HTMLviewer( String title_str )
	{
		super( MainManager.getJumpshotWindow() );
		if ( title_str != null )
			setTitle( title_str );
		else
			setTitle( "HTML viewer" );
		super.setSize( 600, 400 );
		super.setBackground( Color.gray );
		super.getContentPane().setLayout( new BorderLayout() );

		super.getContentPane().add( createToolBar(), BorderLayout.NORTH );

		int         fld_height, fld_width;
		Dimension   min_size, max_size, pref_size;
		fld_width    = 400;
		fld_height   = 25;
		min_size     = new Dimension( 0, fld_height );
		max_size     = new Dimension( Short.MAX_VALUE, fld_height );
		pref_size    = new Dimension( fld_width, fld_height );

		JPanel center_panel = new JPanel();
		center_panel.setLayout( new BoxLayout( center_panel,
				BoxLayout.Y_AXIS ) );
		JPanel top_panel = new JPanel();
		top_panel.setLayout( new BoxLayout( top_panel,
				BoxLayout.X_AXIS ) );
		JLabel URL_label = new JLabel( "    URL : " );
		top_panel.add( URL_label ); 
		input_fld  = new JTextField();
		input_fld.setMinimumSize( min_size );
		input_fld.setMaximumSize( max_size );
		input_fld.setPreferredSize( pref_size );
		top_panel.add( input_fld );
		center_panel.add( top_panel );

		JScrollPane scroll_panel = new JScrollPane();
		scroll_panel.setBorder( BorderFactory.createLoweredBevelBorder() );
		html_panel = new JEditorPane();
		html_panel.setEditable( false );
		scroll_panel.getViewport().add( html_panel );
		center_panel.add( scroll_panel );
		super.getContentPane().add( center_panel, BorderLayout.CENTER );

		input_fld.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				refresh_btn.doClick();
			}
		} );
		html_panel.addHyperlinkListener( this );

		init_btn = null;

		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent evt ) {
				HTMLviewer.this.setVisible( false );
			}
		} );
	}

	public HTMLviewer( String title_str, JButton button )
	{
		this( title_str );
		init_btn = button;
	}

	// Overriden setVisible to enable/disable the init_btn
	public void setVisible( boolean val )
	{
		super.setVisible( val );
		if ( init_btn != null )
			init_btn.setEnabled( !val );
	}

	public void init(String path )
	{
		input_fld.setText( path ); 
		refresh_btn.doClick();
	}

	private JToolBar createToolBar()
	{
		JToolBar     toolbar;
		Insets       btn_insets;
		Dimension    small_spacer_size, medium_spacer_size, big_spacer_size;

		toolbar             = new JToolBar();
		btn_insets          = new Insets( 1, 1, 1, 1 );
		small_spacer_size   = new Dimension( 5, 5 );
		medium_spacer_size  = new Dimension( 10, 5 );
		big_spacer_size     = new Dimension( 20, 5 );
		
		final IconManager icons = MainManager.getIconManager();
		
		backward_btn = new JButton( icons.getActiveToolbarIcon(IconType.Left));
		backward_btn.setMargin( btn_insets );
		backward_btn.setToolTipText( "Go Backward one page" );
		// backward_btn.setPreferredSize( btn_dim );
		backward_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				if ( ! url_undo_stack.empty() ) {
					updateURLStack( url_redo_stack );
					URL url = url_undo_stack.pop();
					input_fld.setText( url.toString() );
					refresh_btn.doClick();
				}
			}
		} );
		toolbar.add( backward_btn );

		toolbar.addSeparator( small_spacer_size );

		forward_btn = new JButton( icons.getActiveToolbarIcon(IconType.Right) );
		
		forward_btn.setMargin( btn_insets );
		forward_btn.setToolTipText( "Go Forward one page" );
		// forward_btn.setPreferredSize( btn_dim );
		forward_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				if ( ! url_redo_stack.empty() ) {
					updateURLStack( url_undo_stack );
					URL url = url_redo_stack.pop();
					input_fld.setText( url.toString() );
					refresh_btn.doClick();
				}
			}
		} );
		toolbar.add( forward_btn );

		toolbar.addSeparator( medium_spacer_size );

		refresh_btn = new JButton(icons.getActiveToolbarIcon(IconType.Refresh) );

		refresh_btn.setMargin( btn_insets );
		refresh_btn.setToolTipText( "Refresh the current page" );
		// refresh_btn.setPreferredSize( btn_dim );
		refresh_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				URL curr_URL = null;
				try {
					curr_URL = new URL( input_fld.getText() );
					html_panel.setPage( curr_URL );
				} catch ( IOException ioerr ) {
					Dialogs.error( HTMLviewer.this,
							"Invalid URL: " + curr_URL.toString() );
				}
			}
		} );
		toolbar.add( refresh_btn );

		toolbar.addSeparator( big_spacer_size );

		close_btn = new JButton( icons.getActiveToolbarIcon(IconType.Close));
		
		close_btn.setMargin( btn_insets );
		close_btn.setToolTipText( "Close the HTMLviewer window" );
		// close_btn.setPreferredSize( btn_dim );
		close_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				HTMLviewer.this.setVisible( false );
			}
		} );
		toolbar.add( close_btn );

		return toolbar;
	}

	private void updateURLStack( Stack<URL> url_stack )
	{
		String url_str = input_fld.getText();
		if ( url_str != null ) {
			try {
				URL url = new URL( url_str );
				url_stack.push( url );
			} catch ( java.net.MalformedURLException ioerr ) {
				Dialogs.error( this, "Malformed URL " + url_str );
			}
		}
	}

	public void hyperlinkUpdate( final HyperlinkEvent evt )
	{
		if ( evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
			html_panel.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					Document  docu     = html_panel.getDocument();
					URL       curr_URL = null;
					try {
						curr_URL   = evt.getURL();
						if ( curr_URL != null ) {
							updateURLStack( url_undo_stack );
							// System.out.println( "  curr_URL = " + curr_URL );
							input_fld.setText( curr_URL.toString() );
							html_panel.setPage( curr_URL );
						}
						else {
							Dialogs.error( HTMLviewer.this,
									"Invalid Link: NULL pointer!" ); 
							html_panel.setDocument( docu );
						}
					} catch ( IOException ioerr ) {
						Dialogs.error( HTMLviewer.this,
								"Invalid Link: " + curr_URL.toString() );
					}
					html_panel.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
				}
			} );
		}
	} 

	public static void main( String args[] )
	{
		HTMLviewer htmlview = new HTMLviewer( null );
		htmlview.init( "doc/html/index.html" );
		htmlview.setVisible( true );
	}
}
