package topology;

import java.util.EventListener;

public interface TopologyChangeListener  extends EventListener{
	/**
	 * The toplogy is modified i.e. moved around, renamed,...
	 */
	public void topologyChanged();
}

