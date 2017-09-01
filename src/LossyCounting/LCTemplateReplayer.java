package LossyCounting;

import java.util.ArrayList;
import java.util.HashMap;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public interface LCTemplateReplayer {

	/**
	 * Add a new event observation
	 * 
	 * @param caseId
	 * @param currentBucket
	 */
	public void addObservation(String caseId, Integer currentBucket);
	
	/**
	 * Process the given event belonging to the given case id
	 * @param event
	 * @param caseId
	 */
	public void process(XEvent event, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth);
	
	/**
	 * Clean up the data structure
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket);
	
	/**
	 * Write results
	 * 
	 * @param currentBucket
	 */
	public void results();

	/**
	 * 
	 * @return
	 */
	public Integer getSize();
}
