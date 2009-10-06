package de.hd.pvs.piosim.model.interfaces;

import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;

public interface IChildObject<ParentType extends IBasicComponent>
{
	public ParentType getParentComponent();
	public void setParentComponent(ParentType parentComponent);
}
