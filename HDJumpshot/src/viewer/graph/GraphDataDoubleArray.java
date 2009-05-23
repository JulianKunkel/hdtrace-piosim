package viewer.graph;
import java.awt.Color;

class GraphDataDoubleArray extends GraphData{
	final double [] x;
	final double [] y;
	
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
	}
	
	@Override
	int getCount() {	
		return x.length;
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
		return x[x.length -1];
	}
	
	@Override
	public double getMaxY() {
		return y[y.length -1];
	}
	
	@Override
	public double getMinX() {
		return x[0];
	}
	
	@Override
	public double getMinY() {
		return y[0];
	}
}