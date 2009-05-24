package viewer.graph;

import java.awt.Color;

/**
 * Contains the input data for the histogram 
 * @author Julian M. Kunkel
 */
public class HistogramData extends GraphData{
	final int [] bins;
	
	final int maxValueInBin;
	
	final double xOffset;
	final double xExtend;

	public double getXExtend() {
		return xExtend;
	}
	
	public double getDeltaPerBin() {
		return xExtend / bins.length;
	}
	
	public HistogramData(String title, Color color, int [] bins, double xOffset, double xExtend) {
		super(title, color);
		this.bins = bins;
		
		// determine max value:
		int maxValue = 0;
		for(int binVal: bins){
			maxValue = binVal > maxValue ? binVal : maxValue; 
		}
		this.maxValueInBin = maxValue;
		
		this.xOffset = xOffset;
		this.xExtend = xExtend;
	}
	
	@Override
	int getCount() {			
		return bins.length;
	}
	
	@Override
	public double getMaxX() {
		return xOffset + xExtend;
	}
	
	@Override
	public double getMinX() {
		return xOffset;
	}
	
	@Override
	public double getMaxY() {		
		return maxValueInBin;
	}		
	
	@Override
	public double getMinY() {
		return 0;
	}	
	
	@Override
	public ElementEnumeration getXValues() {			
		return new HistogramXAxisEnumeration();
	}
	
	@Override
	public ElementEnumeration getYValues() {
		return new HistogramBinEnumeration(bins);
	}
	
	public int[] getBins() {
		return bins;
	}
	
	private class HistogramXAxisEnumeration  implements ElementEnumeration{
		int curPos = 0;
		
		@Override
		public boolean hasMoreElements() {
			return curPos < bins.length;
		}
		
		@Override
		public double nextElement() {
			return xOffset + getDeltaPerBin() * curPos++;
		}
	}

	
	private class HistogramBinEnumeration  implements ElementEnumeration{
		int pos = 0;
		final int [] values;
		
		public HistogramBinEnumeration(int [] values) {
			this.values = values;
		}
		
		@Override
		public boolean hasMoreElements() {
			return pos < values.length;
		}
		
		@Override
		public double nextElement() {
			return values[pos++];
		}			
	}
	
}