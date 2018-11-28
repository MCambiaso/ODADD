package LossyCounting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import com.yahoo.labs.samoa.instances.Attribute;

import Constraints.AlternatePrecedence;
import Constraints.ChainResponse;
import Constraints.AlternateResponse;
import Constraints.ChainPrecedence;
import Constraints.Precedence;
import Constraints.RespondedExistence;
import Constraints.Response;

public class LCReplayer {

	List<LCTemplateReplayer> replayers = new ArrayList<LCTemplateReplayer>();
	
	public LCReplayer() {
//		replayers.add(new AlternatePrecedence());
//		replayers.add(new AlternateResponse());
//		replayers.add(new ChainPrecedence());
//		replayers.add(new ChainResponse());
		replayers.add(new Precedence());
//		replayers.add(new Response());
//		replayers.add(new RespondedExistence());

//		replayers.add(new Succession());
//		replayers.add(new CoExistence());
		// new constraints
//		replayers.add(new AlternateSuccession());
//		replayers.add(new ChainSuccession());
	}

	/**
	 * 
	 * @param caseId
	 * @param currentBucket
	 */
	public void addObservation(String caseId, Integer currentBucket) {
		for(LCTemplateReplayer t : replayers) {
			t.addObservation(caseId, currentBucket);
		}
	}
	
	/**
	 * 
	 * @param event
	 * @param caseId
	 */
	public void process(XEvent event, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		for(LCTemplateReplayer t : replayers) {
			t.process(event, tr, nomin, bucketWidth);
		}
		long stop = System.currentTimeMillis();
		//System.out.println("Tutte le process:"+(stop-start));
	}
	
	/**
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket) {
		for(LCTemplateReplayer t : replayers) {
			t.cleanup(currentBucket);
		}
	}
	
	/**
	 * 
	 * @param 
	 */
	public void results() {
		for(LCTemplateReplayer t : replayers) {
			t.results();
		}
	}
	
	public void setAttribute(Attribute[] allAttr, int[] indVal, double[] attVal) {
		for(LCTemplateReplayer t : replayers) {
			t.setAttribute(allAttr, indVal, attVal);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getSize() {
		Integer i = 0;
		for(LCTemplateReplayer t : replayers) {
			i += t.getSize();
		}
		return i;
	}
}
