package viewer.graph;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;



public class BarGraph2DStatic extends Graph2DStatic{
	private double barWidth = 0.1;
	
	public void setBarWidth(double value){
		this.barWidth = value;
	}
	
	public double getBarWidth() {
		return barWidth;
	}
	
	@Override
	void drawGraph(Graphics2D g, GraphData line, Dimension drawSize,
			Point drawOffset, GraphAxis xaxis, GraphAxis yaxis, 
			int curGraph, int graphCount) 
	{
		g.setColor(line.color);
		
		// draw objects:
		int lastPosX = -1;
		int lastPosY = -1;

		final double graphPosMultiplier = ((double) curGraph) / graphCount;
		
		final ElementEnumeration xEnum = line.getXValues();
		final ElementEnumeration yEnum = line.getYValues();
		
		final int barWidth = (int) ( xaxis.getPixelPerValue() * this.barWidth );
		
		final int barWidth2 = barWidth / 2; 
		
		while(xEnum.hasMoreElements()){
			final double xval = xEnum.nextElement();
			final double yval = yEnum.nextElement();
			
			int pointPosX = (int) ((xval - xaxis.getMin()) * xaxis.getPixelPerValue()) + drawOffset.x;
			int pointPosY = drawSize.height - (int) ((yval - yaxis.getMin()) * yaxis.getPixelPerValue()) + drawOffset.y;

			g.fillRect(pointPosX - barWidth2, pointPosY, barWidth, drawSize.height - pointPosY);

			if(isConnectPoints() && lastPosX >= 0){
				g.drawLine(pointPosX, pointPosY, lastPosX, lastPosY);
			}
			
			lastPosX = pointPosX;
			lastPosY = pointPosY;
		}
	}

	@Override
	void positionMouseOver(double x, double y) {
		
	}
	
	
}
