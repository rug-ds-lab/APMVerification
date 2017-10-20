package nl.rug.ds.bpm.verification.stepper;

import nl.rug.ds.bpm.specification.jaxb.Condition;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Heerko Groefsema on 20-Apr-17.
 */
public interface Stepper {
	void setConditions(Collection<Condition> conditions);
	
	Marking initialMarking();
	
	Set<Set<String>> parallelActivatedTransitions(Marking marking);
	
	Set<Marking> fireTransition(Marking marking, String transition);
}
