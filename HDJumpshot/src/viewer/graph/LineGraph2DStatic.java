package viewer.graph;
import java.awt.Graphics2D;


/**
 * Draw a set of lines, data shall not be modified while it gets drawn
 * 
 * @author julian
 */
public class LineGraph2DStatic extends Graph2DStatic {
	protected void drawGraph(Graphics2D g, GraphData line, GraphAxis xaxis, GraphAxis yaxis, int curGraph, int graphCount){		
		g.setColor(line.color);
				
		// draw objects:
		int lastPosX = -1;
		int lastPosY = -1;

		final ElementEnumeration xEnum = line.getXValues();
		final ElementEnumeration yEnum = line.getYValues();
		
		while(xEnum.hasMoreElements()){
			final double xval = xEnum.nextElement();
			final double yval = yEnum.nextElement();
			
			int pointPosX = xaxis.convertValueToPixel(xval);
			int pointPosY = yaxis.convertValueToPixel(yval);

			g.drawRect(pointPosX - 3, pointPosY - 3, 6, 6);

			if(isConnectPoints() && lastPosX >= 0){
				g.drawLine(pointPosX, pointPosY, lastPosX, lastPosY);
			}

			lastPosX = pointPosX;
			lastPosY = pointPosY;
		}
	}
	
	@Override
	protected void positionMouseOver(double x, double y) {
		getDrawingArea().setToolTipText("(X | Y) = " + String.format("%f", x) + " | " + String.format("%f", y) );	
	}
	
}
