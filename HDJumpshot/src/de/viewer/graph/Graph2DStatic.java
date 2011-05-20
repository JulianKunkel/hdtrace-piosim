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

package de.viewer.graph;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Draw a set of lines, data shall not be modified while it gets drawn
 * 
 * @author Julian M. Kunkel
 */
public abstract class Graph2DStatic {
	private DrawingArea panel = new DrawingArea();

	private ArrayList<GraphData> linesToDraw = new ArrayList<GraphData>();

	private boolean connectPoints = true;

	private GraphAxis xAxis = new GraphAxis(false);
	private GraphAxis yAxis = new GraphAxis(true);


	public void addLine(GraphData data){
		linesToDraw.add( data );
	}
	
	public void removeAllLines(){
		linesToDraw.clear();
	}

	/**
	 * User moves the mouse over the object in the graph 
	 */
	abstract protected void positionMouseOver(double x, double y);

	abstract protected void drawGraph(Graphics2D g, GraphData line, GraphAxis xaxis, GraphAxis yaxis, int curGraph, int graphCount);

	private void drawXAxisLabelDouble(Graphics2D g, GraphAxis xAxis, GraphAxis yAxis){
		final FontMetrics metric = g.getFontMetrics();

		int posX = xAxis.getDrawOffset() + 10;
		final int posY = yAxis.getDrawSize() + 2*yAxis.getDrawOffset();
		
		while(true){
			assert(posX >= 0);
			
			double val = xAxis.convertPixelToValue(posX);
			String str = String.format("%f ", val);
			final int plotLabelSize = metric.stringWidth(str);

			if(posX > xAxis.getDrawSize() )
				break;			

			
			posX = xAxis.convertValueToPixel(val);		
			
			g.drawChars( str.toCharArray(), 0, str.length(), posX - plotLabelSize / 2, posY + metric.getAscent() + 2);

			int x = (int) Math.round(posX );
			g.drawLine(x, posY+1, x, posY - 3);

			posX += plotLabelSize + 5;
		}  		
	}

	private void drawXAxisLabelInteger(Graphics2D g, GraphAxis xAxis, GraphAxis yAxis){
		final FontMetrics metric = g.getFontMetrics();

		int posX = xAxis.getDrawOffset() + 10;
		final int posY = yAxis.getDrawSize() + 2* yAxis.getDrawOffset();
		double lastValue = Double.MAX_VALUE;
		// break after 1000 chars have been drawn.
		int count = 1000;
		
		while(count > 0){
			count --;
			double val = xAxis.convertPixelToValue(posX);
			
			String str = String.format("%f ", val);
			final int plotLabelSize = metric.stringWidth(str);

			if(posX > xAxis.getDrawSize() )
				break;			

			if(val != lastValue){
				posX = xAxis.convertValueToPixel(val); 
				g.drawChars( str.toCharArray(), 0, str.length(), posX - plotLabelSize / 2, posY + metric.getAscent() +2);

				g.drawLine(posX, posY +1, posX, posY - 3);
				lastValue = val;

				posX += plotLabelSize + 5;
			}else{
				posX = xAxis.convertValueToPixel(val + 1);
			}
		}  		
	}


	private int drawYAxisLabelDouble(Graphics2D g, GraphAxis yAxis){
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int fontHeight = fontMetrics.getHeight();

		// draw coordinates
		// draw Y axis
		final int yLabelCount = (yAxis.getDrawSize() / fontHeight) / 2;

		final double plotYAxisLabelValue = fontHeight / yAxis.getPixelPerValue() * 0.5;

		int labelWidth = 0;

		// determine maximum label width:
		for(int i=0 ; i <= yLabelCount; i++){
			String str = String.format("%f ",  yAxis.getValExtend() * i * 2.0/ yAxis.getDrawSize() * fontHeight + yAxis.getMin() + plotYAxisLabelValue);
			int strSize = fontMetrics.stringWidth(str);
			labelWidth = strSize > labelWidth ? strSize : labelWidth;  
		}


		for(int i=0 ; i <= yLabelCount; i++){
			String str = String.format("%f ",  yAxis.getValExtend() * i * 2.0/ yAxis.getDrawSize() * fontHeight + yAxis.getMin() + plotYAxisLabelValue);

			int y = yAxis.getDrawSize() - i * 2 * fontHeight; 
			g.drawChars( str.toCharArray(), 0, str.length(), labelWidth - fontMetrics.stringWidth(str), y );
			g.drawLine(labelWidth -1, y - fontMetrics.getAscent()/ 2, labelWidth + 4, y - fontMetrics.getAscent() / 2);
		}

		return labelWidth;
	}

	private int drawYAxisLabelInteger(Graphics2D g, GraphAxis yAxis){
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int fontHeight = fontMetrics.getHeight();

		// draw coordinates
		final int yLabelCount = (yAxis.getDrawSize() / fontHeight) / 2;

		final double plotYAxisLabelValue = fontHeight / yAxis.getPixelPerValue() * 0.5;
		int labelWidth = 0;

		// determine maximum label width:
		for(int i=0 ; i <= yLabelCount; i++){
		
			int val = (int) Math.round(( yAxis.getValExtend() * i * 2.0 / yAxis.getDrawSize() * fontHeight + yAxis.getMin() + plotYAxisLabelValue));

			String str = String.format("%d ", val);
			int strSize = fontMetrics.stringWidth(str);
			// maximum label width:
			labelWidth = strSize > labelWidth ? strSize : labelWidth;  
		}

		// avoid to draw the same integer twice:
		int lastVal = Integer.MIN_VALUE;
		int lastYPos = -1;

		for(int i=0 ; i <= yLabelCount; i++){
			int val = (int) Math.round(( yAxis.getValExtend() * i * 2.0 / yAxis.getDrawSize() * fontHeight + yAxis.getMin() + plotYAxisLabelValue));

			String str = String.format("%d ", val);

			int y = yAxis.convertValueToPixel(val);
			if(lastYPos == y || lastVal == val){
				continue;
			}

			lastYPos = y;
			lastVal = val;

			g.drawChars( str.toCharArray(), 0, str.length(), labelWidth - fontMetrics.stringWidth(str), y +  fontMetrics.getAscent() / 2);
			g.drawLine(labelWidth -1, y , labelWidth + 4, y );
		}

		return labelWidth;
	}

	protected void drawGraph(Graphics2D g){
		final Dimension size = panel.getSize();
		
		if(linesToDraw.isEmpty())
			return;
	
		g.clearRect(0, 0, size.width, size.height);

		// determine key position
		int maxKeySize = 0;

		// determine X and Y axis layout:
		xAxis.reset();
		yAxis.reset();


		final Font oldFont = g.getFont();
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		FontMetrics fontMetrics = g.getFontMetrics();
		
		for(GraphData line: linesToDraw){

			int curKeySize = fontMetrics.stringWidth(line.title);
			maxKeySize = (curKeySize > maxKeySize) ? curKeySize : maxKeySize;

			double curMinX = line.getMinX(); 
			double curMaxX = line.getMaxX();
			double curMinY = line.getMinY();
			double curMaxY = line.getMaxY();

			xAxis.setMin((curMinX < xAxis.getMin()) ? curMinX : xAxis.getMin());
			yAxis.setMin((curMinY < yAxis.getMin()) ? curMinY : yAxis.getMin());
			xAxis.setMax((curMaxX > xAxis.getMax()) ? curMaxX : xAxis.getMax());
			yAxis.setMax((curMaxY > yAxis.getMax()) ? curMaxY : yAxis.getMax());			
		}
		
		// draw the keys:
		final int keyX = size.width - maxKeySize - 2;

		int keyY = fontMetrics.getHeight(); //size.height - fontMetrics.getHeight();		
		
		for(GraphData line: linesToDraw){
			if(line.title.length() == 0)
				continue;
			
			g.setColor(line.color);

			g.drawChars(line.title.toCharArray(), 0, line.title.length(), keyX, keyY);
			keyY += fontMetrics.getHeight();
		}

		g.setFont(oldFont);
		g.setColor(Color.BLACK);

		final Dimension drawSize = panel.getSize();
		
		// draw Y-axis
		fontMetrics = g.getFontMetrics();
		drawSize.height = size.height - fontMetrics.getHeight() - 10;
		yAxis.setDrawSize(drawSize.height, 5);

		int drawOffsetX;
		if(yAxis.isIntegerType()){
			drawOffsetX = drawYAxisLabelInteger(g, yAxis) +5;
		}else{
			drawOffsetX = drawYAxisLabelDouble(g, yAxis) +5;
		}

		g.drawLine(drawOffsetX, 0, drawOffsetX , yAxis.getDrawOffset() * 2 + drawSize.height);

		drawSize.width = size.width - drawOffsetX - 5;
		xAxis.setDrawSize(drawSize.width, drawOffsetX);

		// draw X-axis
		if(xAxis.isIntegerType()){
			drawXAxisLabelInteger(g, xAxis, yAxis);
		}else{
			drawXAxisLabelDouble(g, xAxis, yAxis);
		}

		g.drawLine(drawOffsetX, drawSize.height + yAxis.getDrawOffset() * 2, drawOffsetX + drawSize.width, drawSize.height + yAxis.getDrawOffset() * 2);

		// End axis

		int curGraph = 0;
		for(GraphData line: linesToDraw){
			curGraph++;
			drawGraph(g, line, xAxis, yAxis, curGraph, linesToDraw.size());
		}
	}

	private class DrawingArea extends JPanel{
		private static final long serialVersionUID = 2136968262477009294L;

		public DrawingArea() {
			final Dimension min = new Dimension(200, 100);
			this.setMinimumSize(min);
			this.setPreferredSize(min);

			this.addMouseMotionListener(new MouseMotionAdapter(){
				@Override
				public void mouseMoved(MouseEvent e) {
					final Point p = e.getPoint();
					double x = xAxis.convertPixelToValue(p.x);
					double y = yAxis.convertPixelToValue(p.y);
					
					if ( x < xAxis.getMin() || x > xAxis.getMax() || y > yAxis.getMax() || y < yAxis.getMin() ){
						setToolTipText(null);
						return;
					}

					positionMouseOver(x, y);
				}
			});
			
			this.setOpaque(true);
		}

		@Override
		public void paint(Graphics g) {
			drawGraph((Graphics2D) g);
		}				
	}

	public void setConnectPoints(boolean connectPoints) {
		this.connectPoints = connectPoints;
	}

	public boolean isConnectPoints() {
		return connectPoints;
	}

	/**
	 * Load the data again and repaint
	 */
	public void reloadData(){
		panel.repaint();
	}

	public JComponent getDrawingArea() {
		return panel;
	}

	public GraphAxis getXAxis() {
		return xAxis;
	}

	public GraphAxis getYAxis() {
		return yAxis;
	}
	
	public int getNumberOfLines(){
		return linesToDraw.size();
	}
}
