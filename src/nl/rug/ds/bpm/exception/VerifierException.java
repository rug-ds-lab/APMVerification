package nl.rug.ds.bpm.exception;

import nl.rug.ds.bpm.log.LogEvent;
import nl.rug.ds.bpm.log.Logger;

/**
 * Created by Heerko Groefsema on 01-Mar-18.
 */
public class VerifierException extends Exception {
	public VerifierException(String message) {
		super(message);
		Logger.log(message, LogEvent.CRITICAL);
		System.exit(1);
	}
}
