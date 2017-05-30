package nl.rug.ds.bpm.pnml.verifier;

import java.io.File;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hub.top.petrinet.PetriNet;
import nl.rug.ds.bpm.event.EventHandler;
import nl.rug.ds.bpm.event.VerificationEvent;
import nl.rug.ds.bpm.event.VerificationLogEvent;
import nl.rug.ds.bpm.event.listener.VerificationEventListener;
import nl.rug.ds.bpm.event.listener.VerificationLogListener;
import nl.rug.ds.bpm.specification.jaxb.BPMSpecification;
import nl.rug.ds.bpm.specification.marshaller.SpecificationMarshaller;
import nl.rug.ds.bpm.specification.parser.SetParser;
import nl.rug.ds.bpm.verification.Verifier;

/**
 * Created by Heerko Groefsema on 07-Apr-17.
 */
public class PnmlVerifier implements VerificationEventListener, VerificationLogListener {
	private EventHandler eventHandler;
	private SetParser setParser;
	private File nusmv2Binary;
	private boolean reduce;

	public static void main(String[] args) {
		if (args.length > 2) {
			//Normal call
			PnmlVerifier pnmlVerifier = new PnmlVerifier(args[2]);
			pnmlVerifier.setLogLevel(VerificationLogEvent.INFO);
			if(args.length > 3)
				pnmlVerifier.setReduction(Boolean.parseBoolean(args[4]));
			pnmlVerifier.verify(args[0], args[1]);
			
			//Custom Set Call
			pnmlVerifier.addSpecification("Group(group1, t5, t3)");
			pnmlVerifier.addSpecification("AlwaysResponse(group1, t11)");
			
			//Save custom set (optional)
			pnmlVerifier.saveSpecification(new File("./test/spec.xml"));
			
			pnmlVerifier.verify(args[0]);
		} else {
			System.out.println("Usage: PNMLVerifier PNML_file Specification_file NuSMV2_binary_path");
		}
	}

	public PnmlVerifier() {
		reduce = true;

		//Make a shared eventHandler
		eventHandler = new EventHandler();
		setParser = new SetParser(eventHandler);

		//Implement listeners and
		//Add listeners to receive log and result notifications
		eventHandler.addLogListener(this);
		eventHandler.addEventListener(this);
	}

	public PnmlVerifier(File nusmv2) {
		this();
		this.nusmv2Binary = nusmv2;

		if (!(nusmv2Binary.exists() && nusmv2Binary.canExecute()))
			eventHandler.logCritical("No such file: " + nusmv2Binary.getPath());
	}

	public PnmlVerifier(String nusmv2) {
		this(new File(nusmv2));
	}
	
	public PnmlVerifier(String pnml, String specification, String nusmv2) {
		this(nusmv2);
		verify(pnml, specification);
	}
	
	public PnmlVerifier(PetriNet pn, String specification, String nusmv2) {
		this(nusmv2);
		verify(pn, specification);
	}
	
	public PnmlVerifier(String pnml, BPMSpecification specification, String nusmv2) {
		this(nusmv2);
		verify(pnml, specification);
	}
	
	public PnmlVerifier(String pnml, String specification, File nusmv2) {
		this(nusmv2);
		verify(pnml, specification);
	}
	
	public PnmlVerifier(PetriNet pn, String specification, File nusmv2) {
		this(nusmv2);
		verify(pn, specification);
	}
	
	public PnmlVerifier(String pnml, BPMSpecification specification, File nusmv2) {
		this(nusmv2);
		verify(pnml, specification);
	}
	
	public void verify(String pnml) {
		File pnmlFile = new File(pnml);
		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pnmlFile);
			
			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);
			//Start verification
			verifier.verify(getSpecifications(), nusmv2Binary, reduce);
		} catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
	}
	
	public void verify(PetriNet pn) {
		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pn);
			
			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);
			//Start verification
			verifier.verify(getSpecifications(), nusmv2Binary, reduce);
		} catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
	}

	public void verify(PetriNet pn, BPMSpecification specification) {
		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pn);

			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);
			//Start verification
			verifier.verify(specification, nusmv2Binary, reduce);
		} catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
	}
	
	public void verify(String pnml, BPMSpecification specification) {
		File pnmlFile = new File(pnml);
		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pnmlFile);
			
			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);
			//Start verification
			verifier.verify(specification, nusmv2Binary, reduce);
		} catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
	}
	
	public void verify(PetriNet pn, String specification) {
		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pn);
			
			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);
			//Start verification
			verifier.verify(specification, nusmv2Binary, reduce);
		} catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
		}
	}

	public void verify(String pnml, String specification) {
		File pnmlFile = new File(pnml);
		File specificationFile = new File(specification);

		//Make step class for specific Petri net type
		ExtPnmlStepper stepper;
		try {
			stepper = new ExtPnmlStepper(pnmlFile);

			//Make a verifier which uses that step class
			Verifier verifier = new Verifier(stepper, eventHandler);

			//Start verification
			verifier.verify(specificationFile, nusmv2Binary, reduce);
		}
		catch (Exception e) {
			eventHandler.logCritical("Failed to load pnml");
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
	
	public void saveSpecification(File file) {
		SpecificationMarshaller marshaller = new SpecificationMarshaller(eventHandler, getSpecifications(), file);
	}
	
	public void saveSpecification(OutputStream stream) {
		SpecificationMarshaller marshaller = new SpecificationMarshaller(eventHandler, getSpecifications(), stream);
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

	//Set log level VerificationLogEvent.DEBUG to VerificationLogEvent.CRITICAL
	public void setLogLevel(int level) {
		Verifier.setLogLevel(level);
	}

	public int getLogLevel() {
		return Verifier.getLogLevel();
	}

	//Listener implementations
	@Override
	public void verificationEvent(VerificationEvent event) {
		//Use for user feedback
		//Event returns: specification id, formula, type, result, and specification itself
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] FEEDBACK\t: " + event.toString());
	}
	
	@Override
	public void verificationLogEvent(VerificationLogEvent event) {
		//Use for log and textual user feedback
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + event.toString());
	}
}
