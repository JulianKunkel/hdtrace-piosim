package de.hd.pvs.piosim.simulator.output;

import java.util.Collection;

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;

/**
 * Dummy Trace writer does not write any trace
 * @author julian
 *
 */
public class SDummyTraceWriter extends STraceWriter {
	public SDummyTraceWriter(Simulator sim) {
		super("/XtmpX/dummy-stuff", sim);
	}

	@Override
	protected void arrowEndInternal(Epoch time, ISPassiveComponent src,
			ISPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) {
	}

	@Override
	protected void arrowStartInternal(Epoch time, ISPassiveComponent src,
			ISPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) {
	}

	@Override
	protected void endStateInternal(Epoch time, ISPassiveComponent comp,
			String eventDesc, String[] attrNameValues) {
	}

	@Override
	protected void eventInternal(Epoch time, ISPassiveComponent comp,
			String eventDesc, long userEventValue) {
	}

	@Override
	protected void finalizeInternal(Epoch endTime,
			Collection<ISPassiveComponent> existingComponents) {
	}

	@Override
	public void preregister(ISPassiveComponent<IBasicComponent> component) {
	}

	@Override
	protected void startStateInternal(Epoch time, ISPassiveComponent comp,
			String eventDesc) {

	}

	@Override
	protected RelationToken relCreateTopLevelRelationInternal(ISPassiveComponent comp, Epoch time) {

		return null;
	}

	@Override
	protected void relDestroyInternal(ISPassiveComponent comp, RelationToken relation, Epoch time) {


	}

	@Override
	protected void relEndStateInternal(ISPassiveComponent comp, RelationToken relation, Epoch time) {


	}

	@Override
	protected void relEndStateInternal(ISPassiveComponent comp, RelationToken relation, Epoch time, String childTags,
			String[] attrNameValues) {


	}

	@Override
	protected RelationToken relRelateMultipleProcessLocalTokensInternal(ISPassiveComponent comp,
			RelationToken[] parents, Epoch time) {

		return null;
	}

	@Override
	protected RelationToken relRelateProcessLocalTokenInternal(ISPassiveComponent comp, RelationToken parent, Epoch time) {

		return null;
	}

	@Override
	protected void relStartStateInternal(ISPassiveComponent comp, RelationToken relation, Epoch time, String name) {


	}

	@Override
	protected void relStartStateInternal(ISPassiveComponent comp, RelationToken relation, Epoch time, String name,
			String childTags, String[] attrNameValues) {

	}

}
