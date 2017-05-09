package nl.rug.ds.bpm.verification.stepper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nick van Beest 26-Apr-17.
 */
public class Marking {
	private static int maximumTokensAtPlaces = 9;
	
	private Map<String, Integer> tokenmap;
	
	public Marking() {
		tokenmap = new HashMap<String, Integer>();
	}
	
	public void addTokens(String placeId, int tokens) {
		if (tokens > 0) {
			if (!tokenmap.containsKey(placeId)) {
				tokenmap.put(placeId, tokens);
			}
			else {
				tokens += tokenmap.get(placeId);
				if (tokens > maximumTokensAtPlaces) tokens = maximumTokensAtPlaces;
				tokenmap.put(placeId, tokens);
			}
		}
	}
	
	public void addTokens(Set<String> placeIds, int tokens) {
		for (String placeId: placeIds) {
			addTokens(placeId, tokens);
		}
	}
	
	public void emptyPlace(String placeId) {
		if (tokenmap.containsKey(placeId)) tokenmap.remove(placeId);
	}
	
	public Set<String> getMarkedPlaces() {
		return (tokenmap.keySet());
	}
	
	public Boolean hasTokens(String placeId) {
		return (tokenmap.containsKey(placeId));
	}
	
	public int getTokensAtPlace(String placeId) {
		return tokenmap.get(placeId);
	}
	
	public void consumeToken(String placeId) {
		if (hasTokens(placeId)) {
			int tokens = tokenmap.get(placeId);
			
			if (tokens == 1) {
				emptyPlace(placeId);
			}
			else {
				tokenmap.put(placeId, tokens - 1);
			}
		}
	}
	
	public void consumeTokens(Set<String> placeIds) {
		for (String placeId: placeIds) {
			consumeToken(placeId);
		}
	}
	
	public void copyFromMarking(Marking m) {
		tokenmap = new HashMap<String, Integer>();
		
		for (String placeId: m.getMarkedPlaces()) {
			tokenmap.put(placeId, m.getTokensAtPlace(placeId));
		}
	}
	
	@Override
	public String toString() {
		String s = "";
		
		List<String> places = new ArrayList<String>(tokenmap.keySet());
		
		Collections.sort(places);
		
		for (int i = 0; i < places.size(); i++) {
			s = s + "+" + tokenmap.get(places.get(i)) + places.get(i);
		}
		return (s.length() > 0 ? s.substring(1) : "");
	}
	
	public static void setMaximumTokensAtPlaces(int maximum) {
		maximumTokensAtPlaces = maximum;
	}
	
	public static int getMaximumTokensAtPlaces() {
		return maximumTokensAtPlaces;
	}
}
