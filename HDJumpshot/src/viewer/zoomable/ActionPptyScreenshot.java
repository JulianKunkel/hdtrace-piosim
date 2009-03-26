package viewer.zoomable;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import viewer.common.Debug;
import viewer.timelines.CanvasTimeline;

/**
 * Create a screenshot
 * @author julian
 *
 */
public class ActionPptyScreenshot implements ActionListener
{
	private final CanvasTimeline timelines;
	private final RulerTime	 time_ruler;

	private static int screenshotNumber = 0;


	public ActionPptyScreenshot( CanvasTimeline scrl, RulerTime	 time_ruler )
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
