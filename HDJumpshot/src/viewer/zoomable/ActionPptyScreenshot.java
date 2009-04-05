
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


package viewer.zoomable;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import viewer.common.Debug;

/**
 * Create a screenshot
 * @author Julian M. Kunkel
 *
 */
public class ActionPptyScreenshot implements ActionListener
{
	private final ScrollableObject timelines;
	private final RulerTime	 time_ruler;

	private static int screenshotNumber = 0;


	public ActionPptyScreenshot( ScrollableObject scrl, RulerTime	 time_ruler )
	{
		this.timelines = scrl;
		this.time_ruler = time_ruler;
	}


	private void screenshot(){
		screenshotNumber++;
		if ( Debug.isActive() )
			Debug.println( "Action for Print Property button" );


		final Rectangle rectRuler = time_ruler.getVisibleRect();
		final Rectangle rect = timelines.getVisibleRect();
		
		final int offset = time_ruler.getXaxisViewPosition();
		
		final int outWidth = rect.width;
		final int outHeight = rect.height;
		
		BufferedImage outTl = new BufferedImage(outWidth + offset,
				outHeight + rectRuler.height, BufferedImage.TYPE_4BYTE_ABGR);
		
		BufferedImage outR = new BufferedImage(outWidth + offset,
				rectRuler.height, BufferedImage.TYPE_4BYTE_ABGR);
		
		try{
			File file = new File("/tmp/jumpshot-screenshot-" + screenshotNumber +  ".png"); // Calendar.getInstance().getTimeInMillis() +
			
			timelines.paintComponent(outTl.getGraphics());
			time_ruler.paint(outR.getGraphics());
			
			/* copy both pictures into one */
			outTl.getRaster().setRect(0, outHeight, outR.getRaster());

			ImageIO.write(outTl.getSubimage(offset, 0, 
					outWidth, outTl.getHeight()), "png", file);
		
		}catch(Exception e){
			System.err.println("Error during ActionPptyPrint:" + e.getMessage());
		}
	}

	public void actionPerformed( ActionEvent event )
	{
		screenshot();
	}
}
