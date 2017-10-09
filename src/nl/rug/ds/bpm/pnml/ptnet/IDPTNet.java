package nl.rug.ds.bpm.pnml.ptnet;

import hub.top.petrinet.PetriNet;
import hub.top.petrinet.Transition;
import nl.rug.ds.bpm.pnml.ptnet.IDTransition;

public class IDPTNet extends PetriNet {
	
	public Transition addTransition(String uniqueId, String label) {
		Transition trans = new IDTransition(this, label, uniqueId);
		
		this.getTransitions().add(trans);
		
		return trans;
	}
}
