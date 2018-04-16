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

import com.yahoo.labs.samoa.instances.Attribute;

import moa.classifiers.trees.HoeffdingTree;
import LossyCounting.LossyCounting;
import LossyCounting.LCTemplateReplayer;
import Utils.Pair;
import Utils.Utils;

public class ChainPrecedence implements LCTemplateReplayer {

	private HashMap<String, Object> attribute;
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(64);
	ArrayList<Attribute> myAttrTr = new ArrayList<Attribute>(100);
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	
	//private static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private static HashMap<String, LinkedList<String>> eventList = new HashMap<String, LinkedList<String>>();
	private LossyModel mod;// = new LossyModel();
	int nr = 0, en=0, cc=10;
	
//	private HashSet<String> activityLabelsChPrecedence = new HashSet<String>();
	private LinkedList<String> activityLabelsChPrecedence = new LinkedList<String>();
	private LossyCounting<HashMap<String,Integer>> activityLabelsCounterChPrecedence = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> fulfilledConstraintsPerTraceChPrecedence = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> violatedConstraintsPerTraceChPrecedence = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<String> lastActivity = new LossyCounting<String>();

	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/OutChPrecedence.txt");
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
			fulfilledConstraintsPerTraceChPrecedence.addObservation(caseId, currentBucket, class1);
			violatedConstraintsPerTraceChPrecedence.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterChPrecedence.addObservation(caseId, currentBucket, class2);
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
		fulfilledConstraintsPerTraceChPrecedence.cleanup(currentBucket);
		activityLabelsCounterChPrecedence.cleanup(currentBucket);
		lastActivity.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, stop1, stop2, time=0;
//		en++;
		en = 0;
		
		// Collection of attribute of new event
		attribute = new HashMap<String, Object>();
		
		

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
		
		LinkedList<String> list = new LinkedList<String>();
		if(eventList.containsKey(caseId))
			list = eventList.get(caseId);
		else
			eventList.put(caseId, list);
		
		activityLabelsChPrecedence.add(event);
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if(!activityLabelsCounterChPrecedence.containsKey(caseId)){
			activityLabelsCounterChPrecedence.putItem(caseId, counter);
		}else{
			counter = activityLabelsCounterChPrecedence.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> fulfilledForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!fulfilledConstraintsPerTraceChPrecedence.containsKey(caseId)){
			fulfilledConstraintsPerTraceChPrecedence.putItem(caseId, fulfilledForThisTrace);
		}else{
			fulfilledForThisTrace = fulfilledConstraintsPerTraceChPrecedence.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> violatedForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!violatedConstraintsPerTraceChPrecedence.containsKey(caseId)){
			violatedConstraintsPerTraceChPrecedence.putItem(caseId, violatedForThisTrace);
		}else{
			violatedForThisTrace = violatedConstraintsPerTraceChPrecedence.getItem(caseId);
		}
		String previousChPrecedence = lastActivity.getItem(caseId);
		
		if(previousChPrecedence!=null && !previousChPrecedence.equals("") && !previousChPrecedence.equals(event)){
			HashMap<String, Integer> secondElementFul = new  HashMap<String, Integer>();
			//HashMap<String, Integer> secondElementViol = new  HashMap<String, Integer>();
			if(fulfilledForThisTrace.containsKey(previousChPrecedence)){
				secondElementFul = fulfilledForThisTrace.get(previousChPrecedence);
			}
			int nofull = 0;
			if(secondElementFul.containsKey(event)){
				nofull = secondElementFul.get(event);
			}

			fulf = true;
			nr++;

			if(nr>1){
				start1 = System.currentTimeMillis();
				mod.addObservation(previousChPrecedence, event, myAttr, attribute, attIndex, 0, bucketWidth);
				stop1 = System.currentTimeMillis();
				time = time+stop1-start1;
				en++;
				nr=1;
			}
			secondElementFul.put(event, nofull+1);
			fulfilledForThisTrace.put(previousChPrecedence,secondElementFul);
			fulfilledConstraintsPerTraceChPrecedence.putItem(caseId, fulfilledForThisTrace);

			for(String first : activityLabelsChPrecedence){				
				if(!first.equals(event) && !first.equals(previousChPrecedence)){
					//violatedConstraintsPerTraceCh
					int noviol = 0;
					HashMap<String, Integer> second = new  HashMap<String, Integer>();
					if(violatedForThisTrace.containsKey(first)){
						second = violatedForThisTrace.get(first);
						if(second.containsKey(event)){
							noviol = second.get(event);
						}
					}

					fulf = false;
					nr++;

					if(nr>1){
						start2 = System.currentTimeMillis();
						mod.addObservation(first, event, myAttr, attribute, attIndex, 1, bucketWidth);
						stop2 = System.currentTimeMillis();
						time = time+stop2-start2;
						en++;
						nr=1;
					}
					second.put(event, noviol+1);
					violatedForThisTrace.put(first,second);
					violatedConstraintsPerTraceChPrecedence.putItem(caseId, violatedForThisTrace);
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
		activityLabelsCounterChPrecedence.putItem(caseId, counter);
		//***********************
		lastActivity.putItem(caseId, event);	
		
		if(Utils.isTraceComplete(eve)){
			violatedConstraintsPerTraceChPrecedence.remove(caseId);
			fulfilledConstraintsPerTraceChPrecedence.remove(caseId);
			activityLabelsCounterChPrecedence.remove(caseId);
			eventList.remove(caseId);
		}
		
		if(activityLabelsChPrecedence.size()>10)
			activityLabelsChPrecedence.removeFirst();
		
		if(list.size()==10)
			list.removeFirst();
			
		list.add(event);
		eventList.remove(caseId);
		eventList.put(caseId, list);
		
		mod.clean();
		//System.out.println(en);
		//System.out.println("ChPr:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
	}

	@Override
	public void results(){
		for(String aEvent : mod.mm.keySet()){ 
			for(String bEvent : mod.mm.get(aEvent).keySet()){
				printout.println("@@@@@@@@@@@@@@@@@@@@@@@@\n"+aEvent+"%"+bEvent+"\n@@@@@@@@@@@@");
				printout.println(mod.mm.get(aEvent).get(bEvent).getElement1());
				printout.println("\nCorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[0]+
						"\nUncorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[1]+
						"\nCorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[2]+
						"\nUncorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[3]+"\n");
			}
		}	
		//System.out.println("ChPrec");
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
		return activityLabelsChPrecedence.size() +
				activityLabelsCounterChPrecedence.getSize() +
				fulfilledConstraintsPerTraceChPrecedence.getSize() +
				1; // this is for lastActivity field
	}
}
