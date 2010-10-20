//	Copyright (C) 2010 Timo Minartz
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.
package de.hd.pvs.piosim.power.data.visualizer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * Visualizes arrays of values
 * 
 * @author Timo Minartz
 * 
 */
public class StepChartVisualizer extends JFreeChartVisualizer {

	@Override
	protected void addPanels(JFrame frame) {
		frame.getContentPane().add(new ChartPanel(setFontsAndSizes(ChartFactory.createXYStepChart("",xAxisTitle,utilizationYAxisTitle,visualizationData.getUtilizationAsXYSeriesCollection(),PlotOrientation.VERTICAL,printLegend,false,false),true)), BorderLayout.WEST);
		frame.getContentPane().add(new ChartPanel(setFontsAndSizes(ChartFactory.createXYStepChart("",xAxisTitle,powerConsumptionYAxisTitle,visualizationData.getPowerConsumptionAsXYSeriesCollection(),PlotOrientation.VERTICAL,printLegend,false,false),false)), BorderLayout.EAST);

	}
	
	private JFreeChart setFontsAndSizes(JFreeChart chart, boolean isPowerConsumptionAxis) {
		XYPlot plot = (XYPlot) chart.getPlot();
		
		if(plot.getDataset().getSeriesCount() < 2) {
			
			// for strategy pictures
			if(plot.getDataset().getSeriesCount() == 1) {
				XYItemRenderer renderer = plot.getRenderer(0);
				renderer.setSeriesPaint(0, Color.BLACK);
				plot.setRenderer(renderer);
			}
			
			for(int i=0; i<plot.getRendererCount(); ++i) {
				XYItemRenderer renderer = plot.getRenderer(i);
				renderer.setSeriesStroke(i, new BasicStroke(1.5f));
				plot.setRenderer(renderer);
			}
		} else {
			for(int i=0; i<plot.getRendererCount(); ++i) {
				XYItemRenderer renderer = plot.getRenderer(i);
				renderer.setSeriesStroke(i, new BasicStroke(1.0f));
				plot.setRenderer(renderer);
			}
		}
		
		plot.setBackgroundPaint(Color.WHITE);
		
		NumberAxis domainAxis = new NumberAxis();
        
        NumberTickUnit domainTickUnit = new NumberTickUnit(visualizationData.getTimeScaling());
        
        domainAxis.setTickUnit(domainTickUnit);
        domainAxis.setLabelFont(deriveFont(domainAxis.getLabelFont()));
		
		domainAxis.setLabel(plot.getDomainAxis().getLabel());
        domainAxis.setTickLabelFont(deriveFont(domainAxis.getTickLabelFont()));
        
        plot.setDomainAxis(domainAxis);	
        
        
        NumberAxis rangeAxis = new NumberAxis();

        if(isPowerConsumptionAxis) {
        	NumberTickUnit rangeTickUnit = new NumberTickUnit(visualizationData.getUtilizationScaling());
        	rangeAxis.setTickUnit(rangeTickUnit);
        } else {
        	NumberTickUnit rangeTickUnit = new NumberTickUnit(visualizationData.getPowerConsumptionScaling());	
        	rangeAxis.setTickUnit(rangeTickUnit);
        }
        
        rangeAxis.setLabel(plot.getRangeAxis().getLabel());
        
        rangeAxis.setLabelFont(deriveFont(rangeAxis.getLabelFont()));
		rangeAxis.setTickLabelFont(deriveFont(rangeAxis.getTickLabelFont()));
		 
		plot.setRangeAxis(rangeAxis);
		
		plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
	
		return chart;
	}
	
	private Font deriveFont(Font font) {
		
		font = font.deriveFont((float) 14.0);
		
		return font.deriveFont(Font.BOLD);
	}

}
