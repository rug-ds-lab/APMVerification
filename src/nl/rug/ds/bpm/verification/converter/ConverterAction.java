package nl.rug.ds.bpm.verification.converter;

import nl.rug.ds.bpm.event.EventHandler;
import nl.rug.ds.bpm.specification.jaxb.Variable;
import nl.rug.ds.bpm.verification.comparator.StringComparator;
import nl.rug.ds.bpm.verification.map.IDMap;
import nl.rug.ds.bpm.verification.model.kripke.Kripke;
import nl.rug.ds.bpm.verification.model.kripke.State;
import nl.rug.ds.bpm.verification.stepper.DataMarking;
import nl.rug.ds.bpm.verification.stepper.Marking;
import nl.rug.ds.bpm.verification.stepper.Stepper;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Heerko Groefsema on 20-May-17.
 */
public class ConverterAction extends RecursiveAction {
	private EventHandler eventHandler;
	private Kripke kripke;
	private Stepper stepper;
	private IDMap idMap;
	private Marking marking;
	private State previous;
	
	public ConverterAction(EventHandler eventHandler, Kripke kripke, Stepper stepper, IDMap idMap, Marking marking, State previous) {
		this.eventHandler = eventHandler;
		this.kripke = kripke;
		this.stepper = stepper;
		this.idMap = idMap;
		this.marking = marking;
		this.previous = previous;
	}
	
	@Override
	protected void compute() {
		if(kripke.getStateCount() >= Kripke.getMaximumStates()) {
			eventHandler.logCritical("Maximum state space reached (at " + Kripke.getMaximumStates() + " states)");
		}

		Set<String> vars = new TreeSet<>(new StringComparator());
		if (marking instanceof DataMarking) {
			for (Variable v: ((DataMarking) marking).getVariables())
				vars.add(v.toString());
		}

		for (Set<String> enabled: stepper.parallelActivatedTransitions(marking)) {
			Set<String> AP = new TreeSet<>(new StringComparator());
			AP.addAll(enabled);
			if (marking instanceof DataMarking)
				AP.addAll(vars);
			State found = new State(marking.toString(), mapAp(AP));
			State existing = kripke.addNext(previous, found);
			
			if (found == existing) { //if found is a new state
				if (enabled.isEmpty()) { //if state is a sink
					found.addNext(found);
					found.addPrevious(found);
				}
				Set<ConverterAction> nextActions = new HashSet<>();
				for (String transition: enabled)
					for (Marking step : stepper.fireTransition(marking.clone(), transition))
						nextActions.add(new ConverterAction(eventHandler, kripke, stepper, idMap, step, found));
				
				invokeAll(nextActions);
			}
		}
	}
	
	private TreeSet<String> mapAp(Set<String> ids) {
		TreeSet<String> aps = new TreeSet<String>(new StringComparator());
		
		for (String id: ids) {
			boolean exist = idMap.getIdToAp().containsKey(id);
			
			if (id.startsWith("silent")) id = "silent"; // this line has to be tested more thoroughly
														// It's a quick fix to handle situations where multiple silents starting with "silent"
			idMap.addID(id);
			aps.add(idMap.getAP(id));
			
			if(!exist)
				eventHandler.logVerbose("Mapping " + id + " to " + idMap.getAP(id));
		}
		
		return aps;
	}
}
