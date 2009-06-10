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


public class Histogram2D extends BarGraph2DStatic{		
	// # bin which was mouse over the last time
	private int oldMouseOverBin = -1;
	private HistogramData histogramData = null;
	
	public Histogram2D() {
		setConnectPoints(false);
		setDoCenterDrawing(doCenterDrawing);		
	}
		
	public void addLine(HistogramData data) {
		if ( histogramData != null){
			throw new IllegalArgumentException("Histogram2D data already set!");
		}
		
		super.addLine(data);		
		super.setBarWidth(data.xExtend / data.getBinCount());
		
		histogramData = data;
	}	
	
	@Override
	public void removeAllLines() {		
		super.removeAllLines();
		oldMouseOverBin = -1;
		histogramData = null;
	}
	
	@Override
	public void addLine(GraphData data) {
		throw new IllegalArgumentException("Histogram2D requires histogram Data");
	}
	
	/**
	 * This function is called if the user hovers the mouse over a bin.
	 * @param bin
	 */
	protected void binMouseOver(int bin){
		getDrawingArea().setToolTipText("Bin: " + bin);
	}
	
	@Override
	protected void positionMouseOver(double x, double y) {		
		if(histogramData == null) // maybe computed in background
			return;

		int bin =(int) ((x - histogramData.xOffset) / histogramData.getDeltaPerBin());
		
		if (bin >= histogramData.getBinCount() || bin == oldMouseOverBin || bin < 0)
			return;

		binMouseOver(bin);
		
		oldMouseOverBin = bin;		
	}
}
