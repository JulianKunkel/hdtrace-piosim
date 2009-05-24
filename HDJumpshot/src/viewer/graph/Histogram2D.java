package viewer.graph;


public class Histogram2D extends BarGraph2DStatic{		
	// # bin which was mouse over the last time
	private int oldMouseOverBin = -1;
	private HistogramData histogramData = null;
	
	public Histogram2D() {
		setConnectPoints(false);
		setDoCenterDrawing(doCenterDrawing);
		getYAxis().setIntegerType(true);
	}
		
	public void addLine(HistogramData data) {
		if ( histogramData != null){
			throw new IllegalArgumentException("Histogram2D data already set!");
		}
		
		super.addLine(data);		
		super.setBarWidth(data.xExtend / data.getCount());
		
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
		
		if (bin > histogramData.getCount() || bin == oldMouseOverBin || bin < 0)
			return;

		binMouseOver(bin);
		
		oldMouseOverBin = bin;		
	}
}
