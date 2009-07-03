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

package de.tests.viewer;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.viewer.graph.GraphData;
import de.viewer.graph.GraphDataDoubleArray;
import de.viewer.graph.Histogram2D;
import de.viewer.graph.HistogramData;
import de.viewer.graph.HistogramIntData;
import de.viewer.graph.LineGraph2DStatic;


public class HistogramTest {
	public static void main(String[] args) {
		JFrame f = new JFrame();
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		f.add(pan);
		
		Histogram2D hist = new Histogram2D();
		HistogramData data = new HistogramIntData("test", Color.DARK_GRAY, new int[]{1,1,2,3,10,3,2,1,1,2}, 10, 10);
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
