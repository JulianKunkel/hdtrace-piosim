package viewer.graph;


public class GraphAxis {
	private double min;
	private double max;

	private double pixelPerValue;
	
	private boolean isIntegerType;
	
	public void setMax(double max) {
		this.max = max;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getMin() {
		return min;
	}
	
	
	public double getPixelPerValue() {
		return pixelPerValue;
	}
	
	public void setIntegerType(boolean isIntegerType) {
		this.isIntegerType = isIntegerType;
	}
	
	public boolean isIntegerType() {
		return isIntegerType;
	}
	
	void reset(){
		this.max = Double.MIN_VALUE;
		this.min = Double.MAX_VALUE; 
	}
	
	void fixate(double drawSize){
		double delta = max - min;
		min -= delta * 0.02;
		max += delta * 0.02;
		
		this.pixelPerValue = drawSize / (max - min);
	}
	
	public double getExtend(){
		return max - min;
	}
}
