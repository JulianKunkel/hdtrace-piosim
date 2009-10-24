package de.hd.pvs.piosim.model.interfaces;

/**
 * This serializable object can be handled directly by the template manager.
 *
 * @author julian
 *
 */
public interface ISerializableTemplateObject extends IDynamicModelComponent {
	public String getTemplate();

	public String getName();

	public void setTemplate(String name);

	public void setName(String name);
}
