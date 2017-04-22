package nl.rug.ds.bpm.verification.converter;

import nl.rug.ds.bpm.specification.jaxb.Condition;
import nl.rug.ds.bpm.verification.comparator.StringComparator;
import nl.rug.ds.bpm.verification.model.kripke.Kripke;
import nl.rug.ds.bpm.verification.model.kripke.State;
import nl.rug.ds.bpm.verification.stepper.Marking;
import nl.rug.ds.bpm.verification.stepper.Stepper;
import nl.rug.ds.bpm.verification.event.EventHandler;
import nl.rug.ds.bpm.verification.map.IDMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class KripkeConverter {
    private EventHandler eventHandler;
	private Stepper paralelStepper;
    private Kripke kripke;
    private Set<String> conditions;
    private IDMap idMap;
    private int eventCount = 16;

    public KripkeConverter(EventHandler eventHandler, Stepper paralelStepper, List<Condition> conditions, IDMap idMap) {
        this.eventHandler = eventHandler;
        this.paralelStepper = paralelStepper;
        this.conditions = new HashSet<>();
        
        for (Condition condition: conditions)
            this.conditions.add(condition.getCondition());
        
        this.idMap = new IDMap("t", idMap.getIdToAp(), idMap.getApToId());
        
        State.resetStateId();
    }

    public Kripke convert() {
        kripke = new Kripke();

        Marking marking = paralelStepper.initialMarking();
		for (Set<String> enabled: paralelStepper.parallelActivatedTransitions(marking)) {
            TreeSet<String> ap = mapAp(enabled);
            
            kripke.addAtomicPropositions(ap);
            
            State found = new State(marking.toString(), ap);
            kripke.addState(found);
            kripke.addInitial(found);
            
            for (String transition: enabled)
                for (Marking step: paralelStepper.fireTransition(marking, transition, conditions))
                    convertStep(step, found);
        }
		
        return kripke;
    }
    
    private void convertStep(Marking marking, State previous) {
        if (kripke.getStateCount() >= eventCount) {
            eventHandler.logVerbose("Calculating state space (" + kripke.getStateCount() + ")");
            eventCount *= 2;
        }
        
        for (Set<String> enabled: paralelStepper.parallelActivatedTransitions(marking)) {
            TreeSet<String> ap = mapAp(enabled);
        
            kripke.addAtomicPropositions(ap);
        
            State found = new State(marking.toString(), ap);
            
            State existing = kripke.getStates().ceiling(found);
            if (found.equals(existing))
                found = existing;
            else
                kripke.addState(found);
    
            previous.addNext(found);
            found.addPrevious(previous);
    
            if (found != existing) {
                if (enabled.isEmpty()) {
                    found.addNext(found);
                    found.addPrevious(found);
                }
                for (String transition: enabled)
                    for (Marking step: paralelStepper.fireTransition(marking, transition, conditions))
                        convertStep(step, found);
            }
        }
    }
    
    private TreeSet<String> mapAp(Set<String> ids) {
        TreeSet<String> aps = new TreeSet<String>(new StringComparator());
        
        for (String id: ids) {
            idMap.addID(id);
            aps.add(idMap.getAP(id));
        }
        
        return aps;
    }
}
