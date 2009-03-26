package viewer.legends;

import java.util.EventListener;

public interface CategoryUpdatedListener extends EventListener{
	public void categoryVisibilityChanged();
	public void categoryColorChanged();
}
