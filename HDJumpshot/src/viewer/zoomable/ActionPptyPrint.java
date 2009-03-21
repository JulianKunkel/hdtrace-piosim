/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import viewer.timelines.CanvasTimeline;

public class ActionPptyPrint implements ActionListener
{
	private CanvasTimeline scrl = null;
	private RulerTime	 time_ruler = null;
	private int special = 0;

	private static int screenshot = 0;


    public ActionPptyPrint( CanvasTimeline scrl, RulerTime	 time_ruler )
    {
        this.scrl = scrl;
        this.time_ruler = time_ruler;
    }

    public ActionPptyPrint( CanvasTimeline scrl, int special )
    {
        this.scrl = scrl;
        this.special = special;
    }

    private void screenshot(){
		screenshot++;
		if ( Debug.isActive() )
			Debug.println( "Action for Print Property button" );

		ArrayList<BufferedImage>  list = scrl.getImages();
		BufferedImage img = list.get(1);
		BufferedImage xaxis = time_ruler.getImages().get(1);
		BufferedImage out = new BufferedImage(img.getWidth(),
				img.getHeight() + xaxis.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		try{
			img.getGraphics().dispose();
			xaxis.getGraphics().dispose();
			File file = new File("/tmp/jumpshot-screenshot-" + screenshot +  ".png"); // Calendar.getInstance().getTimeInMillis() +

			/* copy both pictures into one */
			out.getRaster().setRect(0, 0, img.getRaster());
			out.getRaster().setRect(0, img.getHeight(), xaxis.getRaster());

			final double val=0.25;
			ImageIO.write(out.getSubimage((int)( out.getWidth() * val), 0, (int) (out.getWidth() * ( 1.0 - 2 *val)) , out.getHeight()), "png", file);
		}catch(Exception e){
			System.err.println("Error during ActionPptyPrint:" + e.getMessage());
		}
    }

    public void actionPerformed( ActionEvent event )
    {
    	if(special == 0){
    		screenshot();
    		return;
    	}
    }
}
