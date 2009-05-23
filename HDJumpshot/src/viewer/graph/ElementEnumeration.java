package viewer.graph;
/**
 * Similar to Enumeration, however returns primitive double values
 */
public interface ElementEnumeration {
	boolean hasMoreElements();
	double nextElement();
}
