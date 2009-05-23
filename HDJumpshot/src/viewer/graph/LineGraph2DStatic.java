package viewer.graph;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;


/**
 * Draw a set of lines, data shall not be modified while it gets drawn
 * 
 * @author julian
 */
public class LineGraph2DStatic extends Graph2DStatic {
	protected void drawGraph(Graphics2D g, GraphData line, Dimension drawSize, Point drawOffset, GraphAxis xaxis, GraphAxis yaxis, int curGraph, int graphCount){		
		g.setColor(line.color);
				
		// draw objects:
		int lastPosX = -1;
		int lastPosY = -1;

		final ElementEnumeration xEnum = line.getXValues();
		final ElementEnumeration yEnum = line.getYValues();
		
		while(xEnum.hasMoreElements()){
			final double xval = xEnum.nextElement();
			final double yval = yEnum.nextElement();

			int pointPosX = (int) ((xval - xaxis.getMin()) * xaxis.getPixelPerValue()) + drawOffset.x;
			int pointPosY = drawSize.height - (int) ((yval - yaxis.getMin()) * yaxis.getPixelPerValue()) + drawOffset.y;

			g.drawRect(pointPosX - 3, pointPosY - 3, 6, 6);

			if(isConnectPoints() && lastPosX >= 0){
				g.drawLine(pointPosX, pointPosY, lastPosX, lastPosY);
			}

			lastPosX = pointPosX;
			lastPosY = pointPosY;
		}
	}
	
	@Override
	void positionMouseOver(double x, double y) {
		getDrawingArea().setToolTipText("(X | Y) = " + String.format("%f", x) + " | " + String.format("%f", y) );	
	}
	
}
