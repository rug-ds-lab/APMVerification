package nl.rug.ds.bpm.main.verifier.apm;

import hub.top.petrinet.PetriNet;
import nl.rug.ds.bpm.event.EventHandler;
import nl.rug.ds.bpm.event.VerificationLog;
import nl.rug.ds.bpm.event.VerificationResult;
import nl.rug.ds.bpm.event.listener.VerificationEventListener;
import nl.rug.ds.bpm.event.listener.VerificationLogListener;
import nl.rug.ds.bpm.main.verifier.IDPTNetStepper;
import nl.rug.ds.bpm.specification.jaxb.BPMSpecification;
import nl.rug.ds.bpm.specification.marshaller.SpecificationUnmarshaller;
import nl.rug.ds.bpm.specification.parser.SetParser;
import nl.rug.ds.bpm.verification.Verifier;
import nl.rug.ds.bpm.verification.checker.CheckerFactory;
import nl.rug.ds.bpm.verification.checker.nusmv2.NuSMVFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick van Beest on 02-June-17.
 */
public class ApmPnmlPTNetVerifier implements VerificationEventListener, VerificationLogListener {
	private EventHandler eventHandler;
	private SetParser setParser;
	private CheckerFactory factory;
	private boolean reduce;
	private PetriNet pn;
	private boolean userFriendly;

	private String eventoutput;
	private List<String> feedback;
	private BPMSpecification bpmSpecification;
	
	public ApmPnmlPTNetVerifier(PetriNet pn, String nusmv2, boolean userFriendly) {
		reduce = true;
		this.userFriendly = userFriendly;
		
		//Make a shared eventHandler
		eventHandler = new EventHandler();
		setParser = new SetParser(eventHandler);

		//Implement listeners and
		//Add listeners to receive log and result notifications
		eventHandler.addLogListener(this);
		eventHandler.addEventListener(this);
		
		eventoutput = "";
		feedback = new ArrayList<String>();
		
		this.pn = pn;
		
		//Create the wanted model checker factory
		factory = new NuSMVFactory(eventHandler, new File(nusmv2));
	}

	public ApmPnmlPTNetVerifier(PetriNet pn, String specxml, String nusmv2, boolean userFriendly) {
		this(pn, nusmv2, userFriendly);
		
		addSpecificationFromXML(specxml);
	}
	
	public ApmPnmlPTNetVerifier(PetriNet pn, String[] specifications, String nusmv2, boolean userFriendly) {
		this(pn, nusmv2, userFriendly);
		
		addSpecifications(specifications);
		
		bpmSpecification = getSpecifications();
	}
	
	public List<String> verify() {
		return verify(false);
	}
	
	public List<String> verify(Boolean getAllOutput) {
		//Make step class for specific Petri net type
		IDPTNetStepper stepper;
		
		if (bpmSpecification == null) {
			bpmSpecification = getSpecifications();
		}
		
		try {
			stepper = new IDPTNetStepper(pn);
			
			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, factory, eventHandler);
			//Start verification
			verifier.verify(bpmSpecification, reduce);
		} 
		catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
		System.out.println(eventoutput);
		
		return feedback;
	}
	
	public void addSpecificationFromXML(String specxml) {
		SpecificationUnmarshaller unmarshaller;
		try {
			unmarshaller = new SpecificationUnmarshaller(eventHandler, new ByteArrayInputStream(specxml.getBytes("UTF-8")));
			bpmSpecification = unmarshaller.getSpecification();
		} 
		catch (UnsupportedEncodingException e) {
			eventHandler.logCritical("Invalid specification xml");
			return;
		}
	}

	public void addSpecification(String line) {
		setParser.parse(line);
	}

	public void addSpecifications(String[] lines) {
		for (String line: lines)
			addSpecification(line);
	}

	public BPMSpecification getSpecifications() {
		return setParser.getSpecification();
	}
	
	public void setReduction(boolean reduce) {
		this.reduce = reduce;
	}

	public boolean getReduction() {
		return reduce;
	}

	//Set maximum amount of tokens at a single place
	//Safety feature, prevents infinite models
	//Standard value of 3
	public void setMaximumTokensAtPlaces(int amount) {
		Verifier.setMaximumTokensAtPlaces(amount);
	}

	public int getMaximumTokensAtPlaces() {
		return Verifier.getMaximumTokensAtPlaces();
	}

	//Set maximum size of state space
	//Safety feature, prevents memory issues
	//Standard value of 7 million
	//(equals models of 4 parallel branches with each 50 activities)
	//Lower if on machine with limited memory
	public void setMaximumStates(int amount) {
		Verifier.setMaximumStates(amount);
	}

	public int getMaximumStates() {
		return Verifier.getMaximumStates();
	}

	//Set log level VerificationLog.DEBUG to VerificationLog.CRITICAL
	public void setLogLevel(int level) {
		Verifier.setLogLevel(level);
	}

	public int getLogLevel() {
		return Verifier.getLogLevel();
	}

	//Listener implementations
	@Override
	public void verificationEvent(VerificationResult event) {
		//Use for user feedback
		//Event returns: specification id, formula, type, result, and specification itself
		if (userFriendly) {
			feedback.add("Specification " + event.getId() + " evaluated " + event.getVerificationResult() + " for " + event.getFormula().getSpecification().getType() + "(" + event.getFormula().getOriginalFormula() + "}");
		}
		else {
			feedback.add(event.toString());
		}
	}
	
	@Override
	public void verificationLogEvent(VerificationLog event) {
		//Use for log and textual user feedback
		eventoutput += "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + event.toString() + "\n";
	}
}