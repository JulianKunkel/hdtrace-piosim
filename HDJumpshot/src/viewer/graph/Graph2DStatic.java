package viewer.graph;
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
 * @author julian
 */
public abstract class Graph2DStatic {
	private DrawingArea panel = new DrawingArea();

	private ArrayList<GraphData> linesToDraw = new ArrayList<GraphData>();

	private boolean connectPoints = true;

	private Dimension drawSize = new Dimension(); 
	private Point     drawOffset = new Point(0,0);

	private GraphAxis xAxis = new GraphAxis();
	private GraphAxis yAxis = new GraphAxis();


	public void addLine(GraphData data){
		linesToDraw.add( data );
	}

	/**
	 * User moves the mouse over the object in the graph 
	 */
	abstract void positionMouseOver(double x, double y);

	abstract void drawGraph(Graphics2D g, GraphData line, Dimension drawSize, Point drawOffset, GraphAxis xaxis, GraphAxis yaxis, int curGraph, int graphCount);

	private void drawXAxisLabelDouble(Graphics2D g, int posY, int posX){
		final FontMetrics metric = g.getFontMetrics();

		while(true){			
			double val = posX / xAxis.getPixelPerValue() + xAxis.getMin();
			String str = String.format("%f", val);
			final int plotLabelSize = metric.stringWidth(str);

			if(posX + plotLabelSize > drawSize.width )
				break;			

			posX = (int)( (val - xAxis.getMin()) * xAxis.getPixelPerValue() ); 
			g.drawChars( str.toCharArray(), 0, str.length(), drawOffset.x + posX - plotLabelSize / 2, posY);

			int x = (int) Math.round(drawOffset.x + posX );
			g.drawLine(x, drawSize.height +3, x, drawSize.height - 3);

			posX += plotLabelSize + 5;
		}  		
	}

	private void drawXAxisLabelInteger(Graphics2D g, int posY){
		final FontMetrics metric = g.getFontMetrics();

		int posX = 0;
		int lastValue = Integer.MIN_VALUE;
		while(true){			
			int val = (int) Math.round(posX / xAxis.getPixelPerValue() + xAxis.getMin());
			String str = String.format("%d", val);
			final int plotLabelSize = metric.stringWidth(str);

			if(posX + plotLabelSize > drawSize.width )
				break;			

			if(val != lastValue){
				posX = (int)( (val - xAxis.getMin()) * xAxis.getPixelPerValue() ); 
				g.drawChars( str.toCharArray(), 0, str.length(), drawOffset.x + posX - plotLabelSize / 2, posY);

				int x = (int) Math.round(drawOffset.x + posX );
				g.drawLine(x, drawSize.height +3, x, drawSize.height - 3);
				lastValue = val;

				posX += plotLabelSize + 5;
			}else{
				posX = (int)( (val + 1 - xAxis.getMin()) * xAxis.getPixelPerValue() );
			}
		}  		
	}


	private int drawYAxisLabelDouble(Graphics2D g, GraphAxis yAxis){
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int fontHeight = fontMetrics.getHeight();

		// draw coordinates
		// draw Y axis
		final int yLabelCount = (drawSize.height / fontHeight) / 2;

		final double plotYAxisLabelValue = fontHeight / yAxis.getPixelPerValue() * 0.5;

		int labelWidth = 0;

		// determine maximum label width:
		for(int i=0 ; i <= yLabelCount; i++){
			String str = String.format("%f",  yAxis.getExtend() * i * 2.0/ drawSize.height * fontHeight + yAxis.getMin() + plotYAxisLabelValue);
			int strSize = fontMetrics.stringWidth(str);
			labelWidth = strSize > labelWidth ? strSize : labelWidth;  
		}


		for(int i=0 ; i <= yLabelCount; i++){
			String str = String.format("%f",  yAxis.getExtend() * i * 2.0/ drawSize.height * fontHeight + yAxis.getMin() + plotYAxisLabelValue);

			int y = drawSize.height - i * 2 * fontHeight; 
			g.drawChars( str.toCharArray(), 0, str.length(), fontMetrics.stringWidth(str) - drawOffset.x, y );
			g.drawLine(labelWidth -1, y - fontMetrics.getAscent()/ 2, labelWidth + 4, y - fontMetrics.getAscent() / 2);
		}

		return labelWidth;
	}

	private int drawYAxisLabelInteger(Graphics2D g, GraphAxis yAxis){
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int fontHeight = fontMetrics.getHeight();

		// draw coordinates
		final int yLabelCount = (drawSize.height / fontHeight) / 2;

		final double plotYAxisLabelValue = fontHeight / yAxis.getPixelPerValue() * 0.5;
		int labelWidth = 0;

		// determine maximum label width:
		for(int i=0 ; i <= yLabelCount; i++){
			String str = String.format("%d", (int) (yAxis.getExtend() * i * 2/ drawSize.height * fontHeight + yAxis.getMin() + plotYAxisLabelValue));
			int strSize = fontMetrics.stringWidth(str);
			// maximum label width:
			labelWidth = strSize > labelWidth ? strSize : labelWidth;  
		}

		// avoid to draw the same int twice:
		int lastVal = Integer.MIN_VALUE;
		int lastYPos = -1;

		for(int i=0 ; i <= yLabelCount; i++){
			int val = (int) Math.round(( yAxis.getExtend() * i * 2.0 / drawSize.height * fontHeight + yAxis.getMin() + plotYAxisLabelValue));

			String str = String.format("%d", val);

			int y = (int) (drawSize.height - yAxis.getPixelPerValue() * (val - yAxis.getMin()));
			if(lastYPos == y || lastVal == val){
				continue;
			}

			lastYPos = y;
			lastVal = val;

			g.drawChars( str.toCharArray(), 0, str.length(), fontMetrics.stringWidth(str) - drawOffset.x, y +  fontMetrics.getAscent() / 2);
			g.drawLine(labelWidth -1, y , labelWidth + 4, y );
		}

		return labelWidth;
	}

	protected void drawGraph(Graphics2D g){
		final Dimension size = panel.getSize();

	
		//g.setColor(Color.YELLOW);
		//g.fillRect(0, 0, size.width, size.height);


		// determine key position
		int maxKeySize = 0;

		// determine X and Y axis layout:
		xAxis.reset();
		yAxis.reset();


		final Font oldFont = g.getFont();
		g.setFont(g.getFont().deriveFont(Font.BOLD, 16));
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
			g.setColor(line.color);

			g.drawChars(line.title.toCharArray(), 0, line.title.length(), keyX, keyY);
			keyY += fontMetrics.getHeight();
		}

		g.setFont(oldFont);
		g.setColor(Color.BLACK);

		//
		
		fontMetrics = g.getFontMetrics();
		drawSize.height = size.height - fontMetrics.getHeight();
		yAxis.fixate(drawSize.height);

		if(yAxis.isIntegerType()){
			drawOffset.x = drawYAxisLabelInteger(g, yAxis);
		}else{
			drawOffset.x = drawYAxisLabelDouble(g, yAxis);
		}

		g.drawLine(drawOffset.x, drawOffset.y, drawOffset.x , drawOffset.y + drawSize.height);

		drawSize.width = size.width - drawOffset.x;
		xAxis.fixate(drawSize.width);

		// draw X axis
		if(xAxis.isIntegerType()){
			drawXAxisLabelInteger(g, size.height);
		}else{
			drawXAxisLabelDouble(g, size.height, drawOffset.x);
		}

		g.drawLine(drawOffset.x, drawSize.height, drawOffset.x + drawSize.width, drawSize.height);

		// End axis

		int curGraph = 0;
		for(GraphData line: linesToDraw){
			curGraph++;
			drawGraph(g, line, drawSize, drawOffset, xAxis, yAxis, curGraph, linesToDraw.size());
		}
	}

	private class DrawingArea extends JPanel{
		private static final long serialVersionUID = 2136968262477009294L;

		public DrawingArea() {
			final Dimension min = new Dimension(500, 200);
			this.setMinimumSize(min);
			this.setPreferredSize(min);

			this.addMouseMotionListener(new MouseMotionAdapter(){
				@Override
				public void mouseMoved(MouseEvent e) {
					final Point p = e.getPoint();
					double x = (p.getX() - drawOffset.x) / xAxis.getPixelPerValue() + xAxis.getMin();
					double y = (drawSize.getHeight() - p.getY() + drawOffset.y) / yAxis.getPixelPerValue() + yAxis.getMin();

					if ( x < xAxis.getMin() || x > xAxis.getMax() || y > yAxis.getMax() || y < yAxis.getMin() ){
						setToolTipText(null);
						return;
					}

					positionMouseOver(x, y);
				}
			});
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
}
