package viewer.zoomable;

import java.util.EventListener;

/**
 * Fired if the number of rows displayed change
 * @author julian
 *
 */
public interface RowNumberChangedListener extends EventListener {
	public void rowNumberChanged();
}
