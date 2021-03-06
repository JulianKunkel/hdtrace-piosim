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

package de.viewer.first;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public abstract class TopWindow {
	private JFrame frame;	

	private boolean visibleTheFirstTime = true;
	
	private VisibilityListenerAdapter listener;
	/**
	 * Only one visibility listener can be set. Be careful to free pointers to a frame or components
	 * in the frame that it can be cleaned up by garbage collection. 
	 * @param listener
	 * @param defaultCloseOperation
	 */
	public void setVisibilityListener(VisibilityListenerAdapter listener, int defaultCloseOperation){
		this.listener = listener;
		frame.setDefaultCloseOperation(defaultCloseOperation);
	}
	
	public TopWindow() {
		frame = new JFrame();

		frame.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				setVisible(false);			
			}
		} );
	}

	/**
	 * Called when the window gets visible the first time
	 */
	protected void initWindow(){

	}

	/**
	 * Gets called when the window gets destroyed
	 */
	protected void destroyWindow(){
		
	}
	
	protected JFrame getFrame() {
		return frame;
	}	


	protected void setTitle(String text){
		getFrame().setTitle(text);
	}	

	final public void setVisible( boolean val )
	{
		if(val == frame.isVisible() )
			return;
		
		if(val == true && visibleTheFirstTime){
			frame.pack();

			visibleTheFirstTime = false;
			initWindow();
		}
		
		if(val == true){
			if(listener != null)
				listener.getsVisible();					
		}else{
			if(listener != null)
				listener.getsInvisible();				
		}
		
		frame.setVisible( val );
	}
	
	/**
	 * Free all resources, before doing so it will be invisible
	 */
	final public void dispose(){
		destroyWindow();
		
		getFrame().dispose();
	}
}
