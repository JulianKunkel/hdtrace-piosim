package de.hd.pvs.piosim.model.components.superclasses;

import java.util.ArrayList;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;
import de.hd.pvs.piosim.model.interfaces.ISerializableTemplateObject;

public interface IBasicComponent
	extends ISerializableTemplateObject, IDynamicImplementationObject
{

	public ComponentIdentifier getIdentifier();

	public BasicComponent getParentComponent();

	/**
	 * Get all the parent components plus the component itself
	 * @return
	 */
	public LinkedList<IBasicComponent> getParentComponentsPlusMe();
	/**
	 * This function returns the child components (but not their child components).
	 * It uses reflection and the ChildComponents Annotation.
	 *
	 * @return
	 */
	public ArrayList<IBasicComponent> getDirectChildComponents();

	/**
	 * This function returns all subcomponents i.e. child components of child components.
	 *
	 * @return all direct or indirect child components
	 */
	public ArrayList<IBasicComponent> getAllChildComponents();

	/**
	 * This function returns all subcomponents i.e. child components of child components
	 * and the component itself.
	 *
	 * @return
	 */
	public ArrayList<IBasicComponent> getAllChildComponentsPlusSelf();
}
