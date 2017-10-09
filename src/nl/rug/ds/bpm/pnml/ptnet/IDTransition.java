package nl.rug.ds.bpm.pnml.ptnet;

import hub.top.petrinet.PetriNet;
import hub.top.petrinet.Transition;

public class IDTransition extends Transition {

	private String uniqueId;
	
	public IDTransition(PetriNet net, String name) {
		this(net, name, "");
	}
		
	public IDTransition(PetriNet net, String name, String uniqueId) {
		super(net, name);
		this.uniqueId = uniqueId;
	}
	
	public void setUniqueIdentifier(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	@Override
	public String getUniqueIdentifier() {
		return uniqueId;
	}

}
