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


/**
 * Draw a set of lines, data shall not be modified while it gets drawn
 * 
 * @author Julian M. Kunkel
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
