package nl.rug.ds.bpm.pnml;

import nl.rug.ds.bpm.jaxb.specification.Specification;
import nl.rug.ds.bpm.pnml.events.VerificationEvent;
import nl.rug.ds.bpm.pnml.events.VerificationLogEvent;
import nl.rug.ds.bpm.pnml.listeners.VerificationEventListener;
import nl.rug.ds.bpm.pnml.listeners.VerificationLogListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Heerko Groefsema on 10-Apr-17.
 */
public class EventHandler {
	private Set<VerificationEventListener> verificationEventListenerSet;
	private Set<VerificationLogListener> verificationLogListenerSet;
	
	public EventHandler() {
		verificationEventListenerSet = new HashSet<>();
		verificationLogListenerSet = new HashSet<>();
	}
	
	public void addEventListener(VerificationEventListener verificationEventListener) {
		verificationEventListenerSet.add(verificationEventListener);
	}
	
	public void addLogListener(VerificationLogListener verificationLogListener) {
		verificationLogListenerSet.add(verificationLogListener);
	}
	
	public void fireEvent(Specification specification, boolean eval) {
		VerificationEvent verificationEvent = new VerificationEvent(specification, eval);
		
		for (VerificationEventListener listener: verificationEventListenerSet)
			listener.verificationEvent(verificationEvent);
	}
	
	public void logVerbose(String message) {
		VerificationLogEvent verificationLogEvent = new VerificationLogEvent(VerificationLogEvent.eventType.VERBOSE, message);
		
		for(VerificationLogListener listener: verificationLogListenerSet)
			listener.verificationLogEvent(verificationLogEvent);
	}
	
	public void logInfo(String message) {
		VerificationLogEvent verificationLogEvent = new VerificationLogEvent(VerificationLogEvent.eventType.INFO, message);
		
		for(VerificationLogListener listener: verificationLogListenerSet)
			listener.verificationLogEvent(verificationLogEvent);
	}
	
	public void logError(String message) {
		VerificationLogEvent verificationLogEvent = new VerificationLogEvent(VerificationLogEvent.eventType.ERROR, message);
		
		for(VerificationLogListener listener: verificationLogListenerSet)
			listener.verificationLogEvent(verificationLogEvent);
	}
	
	public void logCritical(String message) {
		VerificationLogEvent verificationLogEvent = new VerificationLogEvent(VerificationLogEvent.eventType.CRITICAL, message);
		
		for(VerificationLogListener listener: verificationLogListenerSet)
			listener.verificationLogEvent(verificationLogEvent);
		
		System.exit(-1);
	}
}
