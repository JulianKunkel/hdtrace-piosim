package viewer.graph;
import java.awt.Graphics2D;



public class BarGraph2DStatic extends Graph2DStatic{
	private double barWidth = 0.1;
	
	/**
	 * If the bar shall be drawn to center the real value
	 */
	boolean doCenterDrawing = true;
	
	public void setBarWidth(double value){
		this.barWidth = value;
	}
	
	public double getBarWidth() {
		return barWidth;
	}
	
	@Override
	protected void drawGraph(Graphics2D g, GraphData line, GraphAxis xaxis, GraphAxis yaxis, 
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
		
		final int barWidth2;
		
		if(doCenterDrawing){
			barWidth2 = 0;
		}else{
			barWidth2 = barWidth / 2;
		}
		
		while(xEnum.hasMoreElements()){
			final double xval = xEnum.nextElement();
			final double yval = yEnum.nextElement();
			
			int pointPosX = xaxis.convertValueToPixel(xval);
			int pointPosY = yaxis.convertValueToPixel(yval);

			g.fillRect(pointPosX - barWidth2, pointPosY, barWidth, yaxis.getDrawSize() - pointPosY + yaxis.getDrawOffset());

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
	
	public void setDoCenterDrawing(boolean doCenterDrawing) {
		this.doCenterDrawing = doCenterDrawing;
	}
}
