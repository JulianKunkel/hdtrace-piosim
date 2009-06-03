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
