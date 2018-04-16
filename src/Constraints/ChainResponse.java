package Constraints;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import moa.classifiers.trees.HoeffdingTree;
import LossyCounting.LossyCounting;
import LossyCounting.LCTemplateReplayer;
import Utils.Pair;
import Utils.Utils;

import com.yahoo.labs.samoa.instances.*;

public class ChainResponse implements LCTemplateReplayer {

	private HashMap<String, Object> attribute;	
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(64);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	//private static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private LossyModel mod;// = new LossyModel();
	int nr = 0, en=0, cc=10;
	
//	private HashSet<String> activityLabelsChResponse = new HashSet<String>();
	private LinkedList<String> activityLabelsChResponse = new LinkedList<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterChResponse = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> fulfilledConstraintsPerTraceCh = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> violatedConstraintsPerTraceCh = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<String> lastActivity = new LossyCounting<String>();

	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/OutChResponse.txt");
	FileWriter fw = null;
	BufferedWriter brf;
	static PrintWriter printout;{			
	try {
		fw = new FileWriter(file);
	} catch (IOException e2) {
		e2.printStackTrace();
	}
	brf = new BufferedWriter(fw);
	printout = new PrintWriter(brf);}
	
	@Override
	public void addObservation(String caseId, Integer currentBucket) {
		HashMap<String, HashMap<String, Integer>> ex1 = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> ex2 = new HashMap<String, Integer>();
		@SuppressWarnings("rawtypes")
		Class class1 = ex1.getClass();
		@SuppressWarnings("rawtypes")
		Class class2 = ex2.getClass();
		
		try {
			fulfilledConstraintsPerTraceCh.addObservation(caseId, currentBucket, class1);
			violatedConstraintsPerTraceCh.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterChResponse.addObservation(caseId, currentBucket, class2);
			lastActivity.addObservation(caseId, currentBucket, "".getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setAttribute(Attribute[] allAttr, int[] indVal, double[] attVal){
		mod = new LossyModel(allAttr, indVal, attVal);
	}

	@Override
	public void cleanup(Integer currentBucket) {
		fulfilledConstraintsPerTraceCh.cleanup(currentBucket);
		violatedConstraintsPerTraceCh.cleanup(currentBucket);
		activityLabelsCounterChResponse.cleanup(currentBucket);
		lastActivity.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, stop1, stop2, time=0;
		en = 0;
		// Collection of attribute of new event

		attribute = new HashMap<String, Object>();
		myAttrTr = new ArrayList<Attribute>(100);
		
		ArrayList<String> classe = new ArrayList<String>(2);
		classe.add("FULFILLMENT");
		classe.add("VIOLATION");
		
		for(XAttribute attr : eve.getAttributes().values()){
			if(!attribute.containsKey(attr.getKey())){        
//				if(isNumeric(attr.toString()) && !attr.getKey().equals("Activity code") && !attr.getKey().equals("Specialism code")){
//					double d = Double.parseDouble(attr.toString());
//					attribute.put(attr.getKey(), d); 
//				}else{
					attribute.put(attr.getKey(), attr.toString());
//				}								
			}else if(attribute.containsKey(attr.getKey())){               //!attr.getKey().contains(":") && 
				attribute.remove(attr.getKey());
				attribute.put(attr.getKey(), attr.toString());
			}	
			int l = nomin.get(attr.getKey()).indexOf(attr.toString());
			attIndex.put(attr.getKey(), l);
		}		
		
		for(XAttribute attr : tr.getAttributes().values()){			
			if(!attribute.containsKey(attr.getKey())){        
//				if(isNumeric(attr.toString()) && !attr.getKey().equals("Activity code") && !attr.getKey().equals("Specialism code")){
//					double d = Double.parseDouble(attr.toString());
//					attribute.put(attr.getKey(), d); 
//				}else{
					attribute.put(attr.getKey(), attr.toString());
//				}								
			}else if(attribute.containsKey(attr.getKey())){               //!attr.getKey().contains(":") && 
				attribute.remove(attr.getKey());
				attribute.put(attr.getKey(), attr.toString());
			}		
			int l = nomin.get(attr.getKey()).indexOf(attr.toString());
			attIndex.put(attr.getKey(), l);
		}
		
//		for(Attribute attr : myAttr){
//			if(!attIndex.containsKey(attr.name()) && !attr.name().equals("class")){
//				//String attrib = attr.name();
//				attIndex.put(attr.name(), nomin.get(attr.name()).indexOf("0"));
//			}
//		}
		
		String caseId = Utils.getCaseID(tr);
		String event = Utils.getActivityName(eve);
		activityLabelsChResponse.add(event);
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if(!activityLabelsCounterChResponse.containsKey(caseId)){
			activityLabelsCounterChResponse.putItem(caseId, counter);
		}else{
			counter = activityLabelsCounterChResponse.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> fulfilledForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!fulfilledConstraintsPerTraceCh.containsKey(caseId)){
			fulfilledConstraintsPerTraceCh.putItem(caseId, fulfilledForThisTrace);
		}else{
			fulfilledForThisTrace = fulfilledConstraintsPerTraceCh.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> violatedForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!violatedConstraintsPerTraceCh.containsKey(caseId)){
			violatedConstraintsPerTraceCh.putItem(caseId, violatedForThisTrace);
		}else{
			violatedForThisTrace = violatedConstraintsPerTraceCh.getItem(caseId);
		}
		String previous = lastActivity.getItem(caseId);
		
		if(previous!=null && !previous.equals("") && !previous.equals(event)){
			HashMap<String, Integer> secondElementFul = new  HashMap<String, Integer>();
			//HashMap<String, Integer> secondElementViol = new  HashMap<String, Integer>();
			if(fulfilledForThisTrace.containsKey(previous)){
				secondElementFul = fulfilledForThisTrace.get(previous);
			}
			if(violatedForThisTrace.containsKey(previous)){
				secondElementFul = violatedForThisTrace.get(previous); //veniva caricato nella varabile secondElementViol dichiarata sopra 07/09/17
			}
			int nofull = 0;
			if(secondElementFul.containsKey(event)){
				nofull = secondElementFul.get(event);
			}
			//fulfillment
			fulf = true;
			nr++;
			
			if(nr>1){
				start1 = System.currentTimeMillis();
				mod.addObservation(previous, event, myAttr, attribute, attIndex, 0, bucketWidth); 
				stop1 = System.currentTimeMillis();
				time = time+stop1-start1;
				en++;
				nr=1;	
			}
			
			secondElementFul.put(event, nofull+1);
			fulfilledForThisTrace.put(previous,secondElementFul);
			fulfilledConstraintsPerTraceCh.putItem(caseId, fulfilledForThisTrace);
			//qualcosa non va nelle regole da trovare le violazioni sono tra event e second?
			
			for(String second : activityLabelsChResponse){//counter.keySet()){//

				if(!second.equals(event) && !second.equals(previous)){
					int noviol = 0;
					HashMap<String, Integer> secondEl = new  HashMap<String, Integer>();
					if(violatedForThisTrace.containsKey(previous)){
						secondEl = violatedForThisTrace.get(previous);
						if(secondEl.containsKey(second)){
							noviol = secondEl.get(second);
						}
					}
					//violation
					fulf = false;
					nr++;

					if(nr>1){
						start2 = System.currentTimeMillis();
						mod.addObservation(second, event, myAttr, attribute, attIndex, 1, bucketWidth);
						stop2 = System.currentTimeMillis();
						time = time+stop2-start2;
						en++;
						nr=1;	
					}
					secondEl.put(second, noviol+1);
					violatedForThisTrace.put(previous,secondEl);
					violatedConstraintsPerTraceCh.putItem(caseId, violatedForThisTrace);
				}
			}
		}

		//update the counter for the current trace and the current event
		//**********************

		int numberOfEvents = 1;
		if(!counter.containsKey(event)){
			counter.put(event, numberOfEvents);
		}else{
			numberOfEvents = counter.get(event);
			numberOfEvents++;
			counter.put(event, numberOfEvents); 
		}
		activityLabelsCounterChResponse.putItem(caseId, counter);
		lastActivity.putItem(caseId, event);
		//***********************
		
		if(activityLabelsChResponse.size()>10)
			activityLabelsChResponse.removeFirst();
		
		mod.clean();
		//System.out.println(en);
		//System.out.println("ChRe:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
	}
	
	@Override
	public void results(){
		for(String aEvent : mod.mm.keySet()){ 
			for(String bEvent : mod.mm.get(aEvent).keySet()){
				printout.println("@@@@@@@@@@@@@@@@@@@@@@@@\n"+aEvent+"//"+bEvent+"\n@@@@@@@@@@@@");
				printout.println(mod.mm.get(aEvent).get(bEvent).getElement1());
				printout.println("\nCorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[0]+
						"\nUncorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[1]+
						"\nCorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[2]+
						"\nUncorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[3]+"\n");
				
			}
		}	
		//System.out.println("ChResp");
		printout.flush();
		printout.close();
	}
	
	public static boolean isNumeric(String str)  
	{  
		try  
		{  
//			double d = Double.parseDouble(str);  
			Double.parseDouble(str);
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}

	@Override
	public Integer getSize() {
		return activityLabelsChResponse.size() +
				activityLabelsCounterChResponse.getSize() +
				fulfilledConstraintsPerTraceCh.getSize() +
				1; // this is for the lastActivity field
	}

}
