package viewer.graph;
import java.awt.Color;

public class GraphDataDoubleArray extends GraphData{
	final double [] x;
	final double [] y;
	
	final double minX;
	final double maxX;

	final double minY;
	final double maxY;
	
	private static class ArrayEnumeration implements ElementEnumeration {
		final double [] array;
		int curPos = 0;
		
		public ArrayEnumeration(double [] array) {
			this.array = array;
		}
		
		@Override
		public boolean hasMoreElements() {
			return curPos < array.length;
		}
		
		@Override
		public double nextElement() {
			return array[curPos++];
		}
	}
		
	public GraphDataDoubleArray(String title, Color color, double [] x, double [] y) {
		super(title, color);
		this.x = x;
		this.y = y;
		
		if(x.length != y.length)
			throw new IllegalArgumentException("x.length != y.length");
		
		// determine min / max:
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for(double cur: x){
			min = cur < min ? cur : min;
			max = cur > max ? cur : max;
		}
		minX = min;
		maxX = max;
		
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for(double cur: y){
			min = cur < min ? cur : min;
			max = cur > max ? cur : max;
		}
		minY = min;
		maxY = max;

	}
	
	@Override
	public ElementEnumeration getXValues() {
		return new ArrayEnumeration(x);
	}
	
	@Override
	public ElementEnumeration getYValues() {
		return new ArrayEnumeration(y);
	}
	
	@Override
	public double getMaxX() {
		return maxX;
	}
	
	@Override
	public double getMaxY() {
		return maxY;
	}
	
	@Override
	public double getMinX() {
		return minX;
	}
	
	@Override
	public double getMinY() {
		return minY;
	}
}