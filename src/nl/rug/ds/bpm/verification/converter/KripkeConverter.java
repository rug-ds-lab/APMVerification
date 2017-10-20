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

import java.util.Set;
import java.util.TreeSet;

public class KripkeConverter {
    private EventHandler eventHandler;
	private Stepper parallelStepper;
    private Kripke kripke;
    private IDMap idMap;
    
    public KripkeConverter(EventHandler eventHandler, Stepper paralelStepper, IDMap idMap) {
        this.eventHandler = eventHandler;
        this.parallelStepper = paralelStepper;
        
        this.idMap = new IDMap("t", idMap.getIdToAp(), idMap.getApToId());
        
        State.resetStateId();
    }

    public Kripke convert() {
        kripke = new Kripke();

        Set<String> vars = new TreeSet<>(new StringComparator());

        Marking marking = parallelStepper.initialMarking();
        if (marking instanceof DataMarking) {
            for (Variable v : ((DataMarking) marking).getVariables())
                if (v.isTracked())
                    vars.add(v.toString());
        }

		for (Set<String> enabled: parallelStepper.parallelActivatedTransitions(marking)) {
            Set<String> AP = new TreeSet<>(new StringComparator());
            AP.addAll(enabled);
            if (marking instanceof DataMarking)
                AP.addAll(vars);

            State found = new State(marking.toString(), mapAp(AP));
            kripke.addInitial(found);
            
            for (String transition: enabled)
                for (Marking step : parallelStepper.fireTransition(marking, transition)) {
                    ConverterAction converterAction = new ConverterAction(eventHandler, kripke, parallelStepper, idMap, step, found);
                    converterAction.compute();
                }
        }
		
        return kripke;
    }
    
    private TreeSet<String> mapAp(Set<String> ids) {
        TreeSet<String> aps = new TreeSet<String>(new StringComparator());
        
        for (String id: ids) {
            boolean exist = idMap.getIdToAp().containsKey(id);
            
            idMap.addID(id);
            aps.add(idMap.getAP(id));
            
            if(!exist)
                eventHandler.logVerbose("Mapping " + id + " to " + idMap.getAP(id));
        }
        
        return aps;
    }
}
