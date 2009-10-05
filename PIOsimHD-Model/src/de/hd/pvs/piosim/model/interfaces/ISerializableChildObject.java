package de.hd.pvs.piosim.model.interfaces;

import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;

public interface ISerializableChildObject<ParentType extends IBasicComponent>
	extends ISerializableObject
{
	public ParentType getParentComponent();
	public void setParentComponent(ParentType parentComponent);
}
