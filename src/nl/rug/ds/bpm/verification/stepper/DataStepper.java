package nl.rug.ds.bpm.verification.stepper;

import nl.rug.ds.bpm.specification.jaxb.Variable;

import java.util.Collection;

/**
 * Created by Heerko Groefsema on 20-Oct-17.
 */
public interface DataStepper extends Stepper {
	void setInitialVariables(Collection<Variable> variables);
}
