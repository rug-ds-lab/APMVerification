package nl.rug.ds.bpm.verification.stepper;

import nl.rug.ds.bpm.specification.jaxb.Variable;
import nl.rug.ds.bpm.verification.comparator.StringComparator;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DataMarking extends Marking {
	private Map<String, Variable> variables;

	public DataMarking() {
		super();
		variables = new TreeMap<>(new StringComparator());
	}

	public void setVariable(Variable var) {
		variables.put(var.getName(), var);
	}

	public Variable getVariable(String name) {
		return variables.get(name);
	}

	public Collection<Variable> getVariables() {
		return variables.values();
	}

	public void setVariables(Collection<Variable> vars) {
		for (Variable variable: vars)
			setVariable(variable);
	}

	public boolean setVariableValue(String variable, String value) {
		boolean exists = true;
		if (variables.containsKey(variable))
			variables.get(variable).setValue(value);
		else exists = false;
		return exists;
	}

	public boolean variableExists(String variable) {
		return variables.containsKey(variable);
	}

	public DataMarking clone() {
		DataMarking dataMarking = (DataMarking) super.clone();
		for (String key: variables.keySet())
			dataMarking.setVariable(variables.get(key).clone());
		return dataMarking;
	}
}
