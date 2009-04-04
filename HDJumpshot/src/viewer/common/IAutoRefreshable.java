package viewer.common;

/**
 * Components implementing this interface can be triggered by <code>ButtonAutoRefresh</code>.
 * @author julian
 *
 */
public interface IAutoRefreshable {
	public boolean isAutoRefresh();
	
	public void setAutoRefresh(boolean autoRefresh);
}
