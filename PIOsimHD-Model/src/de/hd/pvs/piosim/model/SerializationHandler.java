package de.hd.pvs.piosim.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.SerializeChild;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.interfaces.IChildObject;
import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;
import de.hd.pvs.piosim.model.interfaces.IDynamicModelComponent;
import de.hd.pvs.piosim.model.interfaces.ISerializableObject;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;

public class SerializationHandler {
	final AttributeAnnotationHandler commonAttributeHandler = new AttributeAnnotationHandler();

	/**
	 * Fill an already created object with data from the XML
	 * @param obj
	 */
	public void readXML(XMLTag xml, ISerializableObject component) throws Exception{
		try{
			commonAttributeHandler.readSimpleAttributes(xml, component);

			readChildComponents(xml, component);
		}catch(Exception e){
			System.err.println("Error  in " + xml);
			throw e;
		}
	}

	/**
	 * This method parses the XML and creates a single component of the type as specified in the XML
	 *
	 * @param model
	 * @param xml The root node containing the element and all sub-elements
	 * @throws Exception
	 */
	public IDynamicModelComponent createDynamicObjectFromXML(XMLTag xml) throws Exception{
		final String implementation = xml.getAttribute("implementation");

		if(implementation == null){
			throw new IllegalArgumentException("Error implementation not found: " + implementation);
		}

//		ConsoleLogger.getInstance().debug(this, "will create: " + implementation);

		return (IDynamicModelComponent) createSerializableObjectFromXML(xml, ((Class<IDynamicModelComponent>) Class.forName(implementation)));
	}

	public ISerializableObject createSerializableObjectFromXML(XMLTag xml, Class<? extends ISerializableObject> type) throws Exception{
		// use reflection to instantiate the object
		final Constructor<? extends ISerializableObject> ct = type.getConstructor();

		final ISerializableObject component;
		try{
			component = ct.newInstance();
		}catch(InstantiationException e){
			throw new IllegalArgumentException("Constructor for the class " + type + " invalid");
		}

		readXML(xml, component);

		return component;
	}

	/**
	 * Read all child components of a component from the XML based on the <code>ChildComponents</code>
	 * annotation.
	 *
	 * @param xml The node containing the object.
	 * @param comp
	 * @throws Exception
	 */
	private void readChildComponents(XMLTag xml, ISerializableObject comp) throws Exception{
		// Walk through the object hierarchy
		Class<?> classIterate = comp.getClass();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(SerializeChild.class))
					continue;
				// check if a default class shall be loaded.
				final SerializeChild annotation = field.getAnnotation(SerializeChild.class);

				final XMLTag parentNode = xml.getFirstNestedXMLTagWithName(field.getName().toUpperCase());

				if(parentNode == null) {
					// not set!
					continue;
				}

				field.setAccessible(true);

				final List<XMLTag>  elements = parentNode.getNestedXMLTags();
				if(elements != null){
					// create child components
					for(XMLTag e: elements){
						final ISerializableObject newComponent;
						final Class<?> type = field.getType();
						final boolean isCollection = Collection.class.isAssignableFrom(type);

						if(IDynamicModelComponent.class.isAssignableFrom(type) || isCollection){
							newComponent = createDynamicObjectFromXML(e);
						}else{
							// object must implement ISerializableObject

							if(! ISerializableObject.class.isAssignableFrom(type)){

								for(Class<?> cls: type.getInterfaces()){
									System.err.println("Implements interface " + cls.getCanonicalName());
								}
								throw new IllegalArgumentException(type.getCanonicalName() +
										" does not implement the interface " +  ISerializableObject.class.getCanonicalName());
							}
							newComponent = createSerializableObjectFromXML(e, (Class<ISerializableObject>) type);
						}

						if(IChildObject.class.isAssignableFrom(newComponent.getClass())){
							//now set the child's parent components if needed:
							((IChildObject) newComponent).setParentComponent((IBasicComponent) comp);
						}

						if(isCollection){
							((Collection<ISerializableObject>) field.get(comp)).add(newComponent);
						}else{
							field.set(comp, newComponent);
						}
					}
				}


				field.setAccessible(false);
			}

			classIterate = classIterate.getSuperclass();
		}
	}

	public void writeXMLBody(ISerializableObject obj, StringBuffer sb)  throws Exception{
		final StringBuffer attributes = new StringBuffer();
		commonAttributeHandler.writeSimpleAttributeXML(obj, attributes, sb);

		sb.append(">\n");

		sb.append(attributes);

		/////////////////////////////////////

		// Next create subcomponents
		createSubComponentXML(obj, sb);
	}

	public void writeXML(String tagName, ISerializableObject obj, StringBuffer sb) throws Exception{
		sb.append("<" + tagName);

		writeXMLBody(obj, sb);

		sb.append("</" + tagName + ">\n");
	}

	/**
	 * Create the nested XML for all the contained child components.
	 * Each field annotated with the <code>ChildComponents</code> Annotation is seralized to XML.
	 * Collections or Simple Object references are followed.
	 *
	 * @param component The component which child components' XML should be created.
	 * @param buff The StringBuffer
	 * @throws Exception
	 */
	private void createSubComponentXML(ISerializableObject component, StringBuffer buff) throws Exception{
		Class<?> classIterate = component.getClass();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(SerializeChild.class))
					continue;

				//ChildComponents annotation = field.getAnnotation(ChildComponents.class);

				field.setAccessible(true);
				Object value = field.get(component);
				field.setAccessible(false);

				if(value == null)
					continue;

				buff.append("<" + field.getName().toUpperCase()  +  ">\n");

				// if it is a collection serialize all contained elements
				if(Collection.class.isAssignableFrom(value.getClass()) ){
					final Collection<Object> collArb = (Collection<Object>) value;
					if(! collArb.isEmpty()){
						final Object first = collArb.iterator().next();
						if(Number.class.isInstance(first)){
							for(Object o: collArb){
								createXMLFromNumber((Number) o, buff);
							}
						}else{
							for(Object e: collArb){
								createXMLFromInstance((ISerializableObject) e, buff);
							}
						}
					}
				}else{ // single object
					createXMLFromInstance(((ISerializableObject) value).getClass().cast(value), buff);
				}

				buff.append("</" + field.getName().toUpperCase()  +  ">\n");

			}

			classIterate = classIterate.getSuperclass();
		}
	}

	public void createXMLFromNumber(Number number, StringBuffer sb){
		sb.append("<VALUE val=\"" + number.toString() + "\"/>");
	}


	/**
	 * Create an XML from a serializable object.
	 * The XML representation will be: <TYPE> ... </TYPE> where type is the simple class name.
	 *
	 * @param obj
	 * @param sb
	 * @throws Exception
	 */
	public void createXMLFromInstance(ISerializableObject obj, StringBuffer sb) throws Exception{
		if(IDynamicImplementationObject.class.isInstance(obj)){
			createXMLFromInstance((IDynamicImplementationObject) obj, sb);
			return;
		}else if(IDynamicModelComponent.class.isInstance(obj)){
			createXMLFromInstance((IDynamicModelComponent) obj, sb);
			return;
		}

		final String type =  obj.getClass().getSimpleName();
		writeXML(type, obj, sb);
	}

	/**
	 * Create an XML from a serializable object.
	 * The XML representation will be: <TYPE> ... </TYPE> where type is the canonical class name.
	 *
	 * @param obj
	 * @param sb
	 * @throws Exception
	 */
	public void createXMLFromInstance(IDynamicModelComponent obj, StringBuffer sb) throws Exception{
		if(IDynamicImplementationObject.class.isInstance(obj)){
			createXMLFromInstance((IDynamicImplementationObject) obj, sb);
			return;
		}

		final String type =  obj.getClass().getCanonicalName();
		writeXML(type, obj, sb);
	}

	/**
	 * Serialize a given BasicComponent into the <code>StringBuffer</code>
	 * @param obj The Component which should be serialized.
	 * @param sb The StringBuffer to which the XML data is written.
	 * @throws Exception
	 */
	public void createXMLFromInstance(IDynamicImplementationObject obj, StringBuffer sb) throws Exception{
		final String type =  obj.getObjectType();

		sb.append("<" + type  +  " implementation=\"" + obj.getClass().getCanonicalName() +"\"");

		writeXMLBody(obj, sb);

		sb.append("</" + type + ">\n");
	}

	///////////////////////////////////////////////////////////////////////////////

}
