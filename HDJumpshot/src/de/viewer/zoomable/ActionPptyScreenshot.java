
 /** Version Control Information $Id: ActionPptyScreenshot.java 206 2009-04-12 17:40:09Z kunkel $
  * @lastmodified    $Date: 2009-04-12 19:40:09 +0200 (So, 12. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 206 $ 
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


package de.viewer.zoomable;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.viewer.common.Debug;


/**
 * Create a screenshot to /tmp by copying timeline and ruler together
 * @author Julian M. Kunkel
 *
 */
public class ActionPptyScreenshot implements ActionListener
{
	private final ScrollableObject timelines;
	private final RulerTime	 timeRuler;
	private final JFrame     frame;

	private static int screenshotNumber = 0;


	public ActionPptyScreenshot( ScrollableObject scrl, RulerTime	 time_ruler, JFrame parentFrame )
	{
		this.timelines = scrl;
		this.timeRuler = time_ruler;
		this.frame = parentFrame;
	}


	private void screenshot(){
		screenshotNumber++;
		if ( Debug.isActive() )
			Debug.println( "Action for Print Property button" );


		final Rectangle rectRuler = timeRuler.getVisibleRect();
		final Rectangle rect = timelines.getVisibleRect();
		
		final int offset = timeRuler.getXaxisViewPosition();
		
		final int outWidth = rect.width;
		final int outHeight = rect.height;
		
		BufferedImage outTl = new BufferedImage(outWidth + offset,
				outHeight + rectRuler.height, BufferedImage.TYPE_4BYTE_ABGR);
		
		BufferedImage outR = new BufferedImage(outWidth + offset,
				rectRuler.height, BufferedImage.TYPE_4BYTE_ABGR);
		
		final String title = frame.getTitle().replaceAll("[^a-zA-Z_0-9-.]", "");
		
		try{
			final File file = new File("/tmp/jumpshot-" + screenshotNumber + "-" + title + ".png"); 
			
			timelines.paint(outTl.getGraphics());
			timeRuler.paint(outR.getGraphics());
			
			/* copy both pictures into one */
			outTl.getRaster().setRect(0, outHeight, outR.getRaster());

			ImageIO.write(outTl.getSubimage(offset, 0, 
					outWidth, outTl.getHeight()), "png", file);
			
			System.out.println("Wrote screenshot to " + file.getAbsolutePath());
		
		}catch(Exception e){
			System.err.println("Error during creation of screenshot:" + e.getMessage());
		}
	}

	public void actionPerformed( ActionEvent event )
	{
		screenshot();
	}
}
