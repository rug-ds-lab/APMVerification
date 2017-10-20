package nl.rug.ds.bpm.main.verifier;

import com.google.common.collect.Sets;
import hub.top.petrinet.*;
import nl.rug.ds.bpm.pnml.ptnet.IDPTNet;
import nl.rug.ds.bpm.pnml.ptnet.reader.IDPTNetReader;
import nl.rug.ds.bpm.specification.jaxb.Condition;
import nl.rug.ds.bpm.verification.comparator.StringComparator;
import nl.rug.ds.bpm.verification.stepper.Marking;
import nl.rug.ds.bpm.verification.stepper.Stepper;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Nick van Beest on 26-04-2017
 */
public class IDPTNetStepper implements Stepper {
	private File net;
	private IDPTNet pn;
	private Map<String, Transition> transitionmap;
	private Map<String, Place> placemap;
	private Map<String, Set<String>> transitionIdmap;

	public IDPTNetStepper(File pnml) throws JDOMException, IOException {
		if (!pnml.exists() || !pnml.canRead())
			throw new IOException();
		
		this.net = pnml;
		getPN();
		initializeTransitionMaps();
		initializePlaceMap();
	}

	public IDPTNetStepper(PetriNet pn) throws JDOMException, IOException {
		this.pn = getExtPN(pn);
		initializeTransitionMaps();
		initializePlaceMap();
	}

	private void getPN() throws JDOMException, IOException {
		pn = IDPTNetReader.parse(net);
	}
	
	private IDPTNet getExtPN(PetriNet pn) {
		IDPTNet epn = new IDPTNet();
		HashMap<Node, Object> map = new HashMap<>();

		for (Transition t: pn.getTransitions()) {
			Object tNew  = epn.addTransition(t.getName());
			map.put(t, tNew);
		}
		
		for (Place p: pn.getPlaces()) {
			Object pNew = epn.addPlace(p.getName());
			map.put(p, pNew);
		}
		
		for (Arc a: pn.getArcs()) {
			if (a.getSource() instanceof Place)
				epn.addArc((Place)map.get(a.getSource()), (Transition)map.get(a.getTarget()));
			else
				epn.addArc((Transition)map.get(a.getSource()), (Place)map.get(a.getTarget()));
		}
		
		return epn;
	}
	
	private void initializeTransitionMaps() {
		transitionmap = new TreeMap<String, Transition>(new StringComparator());
		transitionIdmap = new TreeMap<String, Set<String>>(new StringComparator());
		
		for (Transition t: pn.getTransitions()) {
			transitionmap.put(getId(t), t);
			
			if (!transitionIdmap.containsKey(t.getName()))
				transitionIdmap.put(t.getName(), new HashSet<String>());
			
			transitionIdmap.get(t.getName()).add(getId(t));
		}
	}
	
	private void initializePlaceMap() {
		placemap = new TreeMap<String, Place>(new StringComparator());
		
		for (Place p: pn.getPlaces()) {
			placemap.put(getId(p), p);
		}
	}
	
	// Create a map with all enabled transitions and their corresponding bitset presets
	private Map<String, BitSet> getEnabledPresets(Marking marking) {
		List<Place> filled = new ArrayList<Place>();
		Set<Transition> enabled = new HashSet<Transition>();
		Map<String, BitSet> enabledpresets = new HashMap<String, BitSet>();
		
		for (String place: marking.getMarkedPlaces()) {
			filled.add(placemap.get(place));
			enabled.addAll(placemap.get(place).getPostSet());
		}
		
		for (Transition t: new HashSet<Transition>(enabled)) {
			if (!filled.containsAll(t.getPreSet())) {
				enabled.remove(t);
			}
			else {
				enabledpresets.put(getId(t), getPresetBitSet(t, filled));
			}
		}
		
		return enabledpresets;
	}
	
	// Create a bitset that holds the positions in the list allplaces that are part of the preset of trans
	private BitSet getPresetBitSet(Transition trans, List<Place> allplaces) {
		BitSet b = new BitSet();
		
		for (Place p: trans.getPreSet()) {
			b.set(allplaces.indexOf(p));
		}
		
		return b;
	}
	
	private String getId(Place p) {
		return p.getName() + "(" + p.id + ")";
	}
	
	private String getId(Transition t) {
//		return t.getName() + "(" + t.id + ")";
//		return t.getUniqueIdentifier();
		return t.getName();
	}
	
	@Override
	public void setConditions(Collection<Condition> conditions) {
		//TODO
	}
	
	@Override
	public Marking initialMarking() {
		Marking initial = new Marking();
		
		// add all places with no incoming arcs to initial marking
		for (Place p: pn.getPlaces()) {
			if (p.getIncoming().size() == 0) {
				initial.addTokens(getId(p), 1);
			}
		}
		
		return initial;
	}
	
	public Set<String> getEnabledTransitions(Marking marking) {
		return getEnabledPresets(marking).keySet();
	}
	
	public Map<String, Set<String>> getTransitionIdMap() {
		return transitionIdmap;
	}

	@Override
	public Set<Set<String>> parallelActivatedTransitions(Marking marking) {
		Set<Set<String>> ypar = new HashSet<Set<String>>();
		
		Map<String, BitSet> enabledpresets = getEnabledPresets(marking);

		// create a power set of all curently enabled transitions
		ypar = new HashSet<Set<String>>(Sets.powerSet(enabledpresets.keySet()));
		
		// remove empty set
		ypar.remove(new HashSet<String>());

		BitSet overlap;
		for (Set<String> sim: new HashSet<Set<String>>(ypar)) {
			overlap = new BitSet();
			for (String t: sim) {
				// check if presets overlap for the set of transitions
				// if yes, remove (i.e. they cannot fire simultaneously)
				if (!overlap.intersects(enabledpresets.get(t))) {
					overlap.or(enabledpresets.get(t));
				}
				else {
					ypar.remove(sim);
					break;
				}
			}
		}
		
		Set<Set<String>> subsets = new HashSet<Set<String>>();
		
		// remove subsets to obtain the largest sets
		for (Set<String> par1: ypar) {
			for (Set<String> par2: ypar) {
				if ((par1.containsAll(par2)) && (par1.size() != par2.size())) {
					subsets.add(par2);
				}
			}
		}
		
		ypar.removeAll(subsets);
		
		return ypar;
	}
	
	@Override
	public Set<Marking> fireTransition(Marking marking, String transitionId) {
		Set<Marking> afterfire = new HashSet<Marking>();

		Boolean enabled = true;
		Marking currentfire = new Marking();
		currentfire.copyFromMarking(marking);

		Transition selected = transitionmap.get(transitionId);
		
		// check if selected transition is indeed enabled
		Set<String> placeIds = new HashSet<String>();
		for (Place p: selected.getPreSet()) {
			if (currentfire.hasTokens(getId(p))) {
				placeIds.add(getId(p));
			}
			else {
				enabled = false;
				break;
			}
		}
		
		// fire
		if (enabled) {
			// remove 1 token from each incoming place
			currentfire.consumeTokens(placeIds);
			
			// place 1 token in each outgoing place
			for (Place p: selected.getPostSet()) {
				currentfire.addTokens(getId(p), 1);
			}
		}
		
		afterfire.add(currentfire);			
		
		return afterfire;
	}
	
	public String getTransitionMap() {
		String str = "";
		for (String t: transitionmap.keySet()) {
			str += t + ": " + transitionmap.get(t).getName() + "\n";
		}
		
		return str;
	}
	
}
