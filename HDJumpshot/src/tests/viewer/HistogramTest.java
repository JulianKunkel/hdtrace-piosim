package tests.viewer;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import viewer.graph.GraphData;
import viewer.graph.GraphDataDoubleArray;
import viewer.graph.Histogram2D;
import viewer.graph.HistogramData;
import viewer.graph.LineGraph2DStatic;

public class HistogramTest {
	public static void main(String[] args) {
		JFrame f = new JFrame();
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		f.add(pan);
		
		Histogram2D hist = new Histogram2D();
		HistogramData data = new HistogramData("test", Color.DARK_GRAY, new int[]{1,1,2,3,10,3,2,1,1,2}, 10, 10);
		hist.addLine(data);
		hist.getXAxis().setIntegerType(true);
		
		pan.add(hist.getDrawingArea());

		LineGraph2DStatic graph = new LineGraph2DStatic();
		GraphData doubleData = new GraphDataDoubleArray("test", Color.DARK_GRAY, new double[]{1,2,3,4,5,6,7,8,9,11},  new double[]{1,1,2,3,10,3,2,1,1,2});
		graph.addLine(doubleData);
		//graph.getXAxis().setIntegerType(true);
		//graph.getYAxis().setIntegerType(true);
		
		pan.add(graph.getDrawingArea());

		
		f.setVisible(true);
	}
}
