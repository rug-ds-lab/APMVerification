package nl.rug.ds.bpm.verification.stepper;

import nl.rug.ds.bpm.specification.jaxb.Variable;

import java.util.Collection;

/**
 * Created by Heerko Groefsema on 20-Oct-17.
 */
public interface DataStepper extends Stepper {
	
	//Sets the variables that are included as atomic proportions, and (optionally) their initial values.
	void setInitialVariables(Collection<Variable> variables);
	
	//Always use DataMarking as returns and results for the methods from the Stepper interface when using this interface.
}
