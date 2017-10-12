package nl.rug.ds.bpm.specification.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Variable {
	private String name, type, value;

	public Variable() {}

	public Variable(String name, String type, String value) {
		setName(name);
		setType(type);
		setValue(value);
	}

	@XmlAttribute
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	@XmlAttribute
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	@XmlAttribute
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }

	public Variable clone() {
		return new Variable(name, type, value);
	}

	public String toString() {
		return type + "::" + name + " = " + value;
	}
}
