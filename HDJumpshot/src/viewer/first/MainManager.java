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

import java.awt.Rectangle;

import javax.swing.WindowConstants;

import viewer.common.Dialogs;
import viewer.common.IconManager;
import viewer.common.ModelTime;
import viewer.common.Parameters;
import viewer.common.PreferenceFrame;
import viewer.legends.LegendFrame;
import viewer.profile.TraceProfileFrame;
import viewer.timelines.TimelineFrame;
import drawable.Category;

public class MainManager{	
	private static IconManager    iconManager;

	private static FileOperations fileOperations = new FileOperations();
	private static ModelTime      modelTime = null;

	private static Jumpshot jumpshotWindow = null;
	private static LegendFrame legendWindow = null;
	private static TimelineFrame timelineWindow = null;
	private static TraceProfileFrame traceProfileWindow = null;
	private static PreferenceFrame preferenceWindow = null;

	/**
	 * initalize Jumpshot
	 * @param file
	 */
	public static void init(String filename){
		if(jumpshotWindow != null)
			throw new IllegalArgumentException("Jumpshot window already loaded");
		
		/*  Initialization  */
		Parameters.initSetupFile();
		Parameters.readFromSetupFile( jumpshotWindow );
		Parameters.initStaticClasses();

		// load icon theme
		MainManager.setIconManager(new IconManager("buuf"));

		Category.loadColors("jumpshot-color.property");

		jumpshotWindow = new Jumpshot(filename);

		MainManager.layoutIdealLocations();
		jumpshotWindow.setVisible( true );      
	}
		
	/**
	 * Closes all windows except the Main Jumpshot Window and disposes their resources
	 */
	public static void closeAllChildWindows(){

		if(legendWindow != null){
			legendWindow.dispose();
			legendWindow = null;
		}
		
		if(timelineWindow != null){
			timelineWindow.dispose();
			timelineWindow = null;
		}
		
		if(traceProfileWindow != null){
			traceProfileWindow.dispose();
			traceProfileWindow = null;
		}		
		
		jumpshotWindow.getTopPanel().getShowLegendButton().setEnabled(false);
		jumpshotWindow.getTopPanel().getShowTimelineButton().setEnabled(false);
		jumpshotWindow.getTopPanel().getShowTraceProfileButton().setEnabled(false);
	}
	
	private static void createWindow( final TopWindow windows )
	{
		windows.setVisible( true );
		windows.getFrame().toFront();
		MainManager.layoutIdealLocations();
	}

	public static void showTraceProfileFrame(){
		if(traceProfileWindow == null)
			traceProfileWindow = new TraceProfileFrame(fileOperations.getReader(), modelTime);
		
		traceProfileWindow.setVisibilityListener(new VisibilityListenerAdapter(){
			@Override
			public void getsInvisible() {
				traceProfileWindow = null;
				jumpshotWindow.getTopPanel().getShowTraceProfileButton().setEnabled(true);
			}
			
			@Override
			public void getsVisible() {
				jumpshotWindow.getTopPanel().getShowTraceProfileButton().setEnabled(false);
			}
		}, WindowConstants.DISPOSE_ON_CLOSE);
		
		createWindow(traceProfileWindow);
	}

	public static void showLegendWindow()
	{
		if(legendWindow == null)
			legendWindow = new LegendFrame(fileOperations.getReader());		
		legendWindow.setVisibilityListener(new VisibilityListenerAdapter(){
			@Override
			public void getsInvisible() {
				jumpshotWindow.getTopPanel().getShowLegendButton().setEnabled(true);
			}
			
			@Override
			public void getsVisible() {
				jumpshotWindow.getTopPanel().getShowLegendButton().setEnabled(false);
			}
		}, WindowConstants.DO_NOTHING_ON_CLOSE);
		createWindow(legendWindow);
	}

	public static void showTimelineWindow()
	{
		if(timelineWindow == null)
			timelineWindow = new TimelineFrame(fileOperations.getReader(), modelTime);
		
		timelineWindow.setVisibilityListener(new VisibilityListenerAdapter(){
			@Override
			public void getsInvisible() {
				jumpshotWindow.getTopPanel().getShowTimelineButton().setEnabled(true);
			}
			
			@Override
			public void getsVisible() {
				jumpshotWindow.getTopPanel().getShowTimelineButton().setEnabled(false);
			}
		}, WindowConstants.DO_NOTHING_ON_CLOSE);
		
		createWindow(timelineWindow);
	}

	public static void showPreferenceWindow()
	{
		if(preferenceWindow == null)
			preferenceWindow = new PreferenceFrame();		
		
		preferenceWindow.setVisibilityListener(new VisibilityListenerAdapter(){
			@Override
			public void getsInvisible() {
				preferenceWindow = null;
				jumpshotWindow.getTopPanel().getShowPreferenceButton().setEnabled(true);
			}
			
			@Override
			public void getsVisible() {
				jumpshotWindow.getTopPanel().getShowPreferenceButton().setEnabled(false);
			}
		}, WindowConstants.DISPOSE_ON_CLOSE);
		
		createWindow(preferenceWindow);
	}

	public static void exitJumpshot(){
		if ( ! Dialogs.confirm( jumpshotWindow,  "Are you sure you want to exit HDJumpshot ?" ) ) {
			return;
		}
		closeAllChildWindows();
		System.exit(1);
	}

	public static void setIconManager(IconManager iconManager) {
		MainManager.iconManager = iconManager;
	}

	public static IconManager getIconManager() {
		return iconManager;
	}



	/**
	 * Try to reposition the visible frames to show all of them.
	 */
	public static void layoutIdealLocations() {
		if (!Parameters.AUTO_WINDOWS_LOCATION)
			return;

		//final Dimension screenSize = Routines.getScreenSize();

		Rectangle bounds = new Rectangle();
		if(legendWindow != null){
			bounds = legendWindow.getFrame().getBounds();
		}
		
		if (jumpshotWindow != null) {
			bounds.x += bounds.width;
			jumpshotWindow.setLocation(bounds.getLocation());
			bounds = jumpshotWindow.getBounds();
		}
		
		if (timelineWindow != null) {
			if (jumpshotWindow != null)
				bounds.y += bounds.height;
			else
				bounds.x += bounds.width;
			timelineWindow.getFrame().setLocation(bounds.getLocation());
			bounds = timelineWindow.getFrame().getBounds();
		}
	}

	public static Jumpshot getJumpshotWindow() {
		return jumpshotWindow;
	}

	public static TimelineFrame getTimelineWindow() {
		return timelineWindow;
	}

	public static LegendFrame getLegendWindow() {
		return legendWindow;
	}

	public static PreferenceFrame getPreferenceWindow() {
		return preferenceWindow;
	}

	public static TraceProfileFrame getTraceProfileWindow() {
		return traceProfileWindow;
	}

	public static ModelTime getModelTime() {
		return modelTime;
	}
	
	public static FileOperations getFileOperations() {
		return fileOperations;
	}

	public static void openTraceProject(String filename) throws Exception{
		closeAllChildWindows();
		
		fileOperations.openTraceProject(filename);
		modelTime = new ModelTime(fileOperations.getReader().getGlobalMinTime(), fileOperations.getReader().getGlobalMaxTime()); 
		
		jumpshotWindow.getTopPanel().getShowLegendButton().setEnabled(true);
		jumpshotWindow.getTopPanel().getShowTimelineButton().setEnabled(true);
		jumpshotWindow.getTopPanel().getShowTraceProfileButton().setEnabled(true);
		
		showLegendWindow();		
		
	}

	public static void addTraceProject(String filename) throws Exception {
		fileOperations.addTraceProject(filename);
		
		//Maybe the global time should be adapted:
		modelTime.adjustGlobalTime( fileOperations.getReader().getGlobalMinTime(), fileOperations.getReader().getGlobalMaxTime());
		
		// Zoom home:
		modelTime.zoomHomeWithoutStacking();
	}	
}
