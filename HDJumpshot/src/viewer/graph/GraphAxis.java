package viewer.graph;


public class GraphAxis {
	private double min;
	private double max;

	private double pixelPerValue;
	
	private boolean isIntegerType;
	
	private int drawSize;
	private int drawOffset;
	
	private final boolean inverted;
	
	public GraphAxis(boolean isInverted) {
		this.inverted = isInverted;
	}
	
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
	
	void setDrawSize(int drawSize, int drawOffset){
		this.pixelPerValue = drawSize / getValExtend();
		this.drawOffset = drawOffset;
		this.drawSize = drawSize;
	}
	
	public double getValExtend(){
		return max - min;
	}
	
	public int convertValueToPixel(double value){
		if(inverted){
			return drawSize - (int) ((value - min) * pixelPerValue) + drawOffset;
		}else{
			return (int) ((value - min) * pixelPerValue) + drawOffset;
		}
	}
	
	public double convertPixelToValue(int pixel){
		if(inverted){
			return (drawSize - pixel + drawOffset) / pixelPerValue + min;
		}else{
			return (pixel - drawOffset) / pixelPerValue + min;
		}
	}
	
	public int getDrawOffset() {
		return drawOffset;
	}
	
	public int getDrawSize() {
		return drawSize;
	}
}
