package nl.rug.ds.bpm.verification.stepper;

import nl.rug.ds.bpm.specification.jaxb.Condition;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Heerko Groefsema on 20-Apr-17.
 */
public interface Stepper {
	//Sets the conditions that limit which paths can be followed.
	void setConditions(Collection<Condition> conditions);
	
	//Returns the initial marking of your net.
	Marking initialMarking();
	
	//Given a marking, returns Y_par(M).
	Set<Set<String>> parallelActivatedTransitions(Marking marking);
	
	//Given a current marking and the unique identifier of a transition,
	//returns the marking after firing the transition using the given marking.
	Set<Marking> fireTransition(Marking marking, String transition);
}
