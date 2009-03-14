
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model.components.superclasses;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.ChildComponents;

/**
 * The <code>Component</code> is an abstract superclass for any logical component in a cluster.
 *
 * ParentType sets the type of the parent component.
 *
 * @author Julian M. Kunkel
 */
public abstract class BasicComponent<ParentType extends BasicComponent> 
implements Comparable{
	/**
	 * The identifier of the component.
	 */
	private ComponentIdentifier identifier = new ComponentIdentifier();
	
	/**
	 * The parent component hosting this component.
	 */
	private ParentType parentComponent = null;

	/**
	 * The templateName if the template is set.
	 */
	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	private String template = null;

	
	public String getNiceName(){
		return "\"" + getName() + "\"";
	}

	@AttributeGetters public String getName() {
		return getIdentifier().getName();
	}

	/**
	 * Components are only defined by the ComponentIdentifier.
	 */
	public int compareTo(Object o) {
		return this.identifier.compareTo(((BasicComponent<?>) o).identifier);
	}
	
	public ComponentIdentifier getIdentifier() {
		return identifier;
	}

	@AttributeGetters 
	public String getTemplate() {
		return template;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	@Override
	public String toString() {
			return this.getClass().getSimpleName() + "-" + identifier + " <" + template  + "> ";
	}

	final public void setTemplate(String template) {
		this.template = template;
	}
	
	public void setName(String name){
		getIdentifier().setName(name);
	}

	/**
	 * This function returns the child components (but not their child components).
	 * It uses reflection and the ChildComponents Annotation.
	 * 
	 * @return
	 */
	final public ArrayList<BasicComponent<?>> getDirectChildComponents(){
		ArrayList<BasicComponent<?>> ret =  new ArrayList<BasicComponent<?>>();

		// use reflection to add all child components
		Class<?> classIterate = this.getClass();	

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();		
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(ChildComponents.class))
					continue;

				try{
					
					field.setAccessible(true);
					if(Collection.class.isAssignableFrom(field.getType()) ){
						Collection coll = ((Collection) field.get(this));
						// add all element of collection:
						for(Object o: coll){
							ret.add((BasicComponent) o);
						}
					}else{// single object
						BasicComponent c = (BasicComponent) field.get(this);						
						if(c != null){
							ret.add(c);
						}
					}
					field.setAccessible(false);
					
				}catch(Exception e){
					throw new IllegalArgumentException(e);
				}
			}

			classIterate = classIterate.getSuperclass();
		}

		return ret;
	}

	/**
	 * This function returns all subcomponents i.e. child components of child components.
	 * 
	 * @return all direct or indirect child components
	 */
	final public ArrayList<BasicComponent> getAllChildComponents(){
		// implements a Breadth First Search		
		// contains objects which children have been processed
		ArrayList<BasicComponent> ret = new ArrayList<BasicComponent>();

		// contains all newly found children, i.e. all objects of the same depth
		ArrayList<BasicComponent> foundProcessing = new ArrayList<BasicComponent>();

		// contains all objects of the next current depth
		ArrayList<BasicComponent> foundNew = new ArrayList<BasicComponent>();		

		foundProcessing.addAll(getDirectChildComponents());

		while(foundProcessing.size() != 0){
			for (BasicComponent c: foundProcessing){
				foundNew.addAll(c.getDirectChildComponents());
			}

			ret.addAll(foundProcessing);
			foundProcessing = foundNew;

			foundNew = new ArrayList<BasicComponent>();
		}

		return ret;
	}
	
	/**
	 * This function returns all subcomponents i.e. child components of child components
	 * and the component itself.
	 * 
	 * @return
	 */
	final public ArrayList<BasicComponent> getAllChildComponentsPlusSelf(){
		ArrayList<BasicComponent> ret = getAllChildComponents();
		ret.add(this);
		return ret;
	}

	/**
	 * Return the component type. 
	 * 
	 * @return
	 */
	abstract public String getComponentType();
	
	/**
	 * 
	 * @return null if this is a top level component (or a template) 
	 */
	final public ParentType getParentComponent() {
		return parentComponent;
	}
	
	final public void setParentComponent(ParentType parentComponent) {
		this.parentComponent = parentComponent;
	}
}
