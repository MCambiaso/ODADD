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

public class AlternateResponse implements LCTemplateReplayer {
	
	private HashMap<String, LinkedList<HashMap<String, Object>>> snapCollection = new HashMap<String, LinkedList<HashMap<String, Object>>>();
	
	private HashMap<String, Object> attribute;
	private HashMap<String, LinkedList<HashMap<String, Object>>> snap = new HashMap<String, LinkedList<HashMap<String, Object>>>();
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(64);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	//private static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private LossyModel mod;// = new LossyModel();
	boolean first = true, fulf = true;
	
	int nr = 0, en=0, cc=10;

//	private HashSet<String> activityLabelsAltResponse = new HashSet<String>();
	private LinkedList<String> activityLabelsAltResponse = new LinkedList<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterAltResponse = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTraceAlt = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> violatedConstraintsPerTrace = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<Boolean> finishedTraces = new LossyCounting<Boolean>();

	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/OutAltResponse.txt");
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
			pendingConstraintsPerTraceAlt.addObservation(caseId, currentBucket, class1);
			violatedConstraintsPerTrace.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterAltResponse.addObservation(caseId, currentBucket, class2);
			finishedTraces.addObservation(caseId, currentBucket, false);
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
		pendingConstraintsPerTraceAlt.cleanup(currentBucket);
		violatedConstraintsPerTrace.cleanup(currentBucket);
		activityLabelsCounterAltResponse.cleanup(currentBucket);
		finishedTraces.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, start3, start4, start5, stop1, stop2, stop3, stop4, stop5, time=0;
		en=0;
		
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
		
		String trace = Utils.getCaseID(tr);
		String event = Utils.getActivityName(eve);
		
		if(snapCollection.containsKey(event)){
			snapCollection.get(event).add(attribute);
			if(snapCollection.get(event).size()>5)
				snapCollection.get(event).removeFirst();
		}else{
			LinkedList<HashMap<String, Object>> firstSnap = new LinkedList<HashMap<String, Object>>();
			firstSnap.add(attribute);
			snapCollection.put(event, firstSnap);
		}
		
		if(snap.containsKey(event)){
			snap.get(event).add(attribute);

			if(snap.get(event).size()>10)
				snap.get(event).removeFirst();
		}else{
			LinkedList<HashMap<String, Object>> firstSnap = new LinkedList<HashMap<String, Object>>();
			firstSnap.add(attribute);
			snap.put(event, firstSnap);			
		}
		
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if(!activityLabelsCounterAltResponse.containsKey(trace)){
			activityLabelsCounterAltResponse.putItem(trace, counter);
		}else{
			counter = activityLabelsCounterAltResponse.getItem(trace);
		}
		HashMap<String,HashMap<String,Integer>> pendingForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!pendingConstraintsPerTraceAlt.containsKey(trace)){
			pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);
		}else{
			pendingForThisTrace = pendingConstraintsPerTraceAlt.getItem(trace);
		}
		HashMap<String,HashMap<String,Integer>> violatedForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!violatedConstraintsPerTrace.containsKey(trace)){
			violatedConstraintsPerTrace.putItem(trace, violatedForThisTrace);
		}else{
			violatedForThisTrace = violatedConstraintsPerTrace.getItem(trace);
		}
		activityLabelsAltResponse.add(event);

		if(!counter.containsKey(event)){
			if(activityLabelsAltResponse.size()>1){	
				for(String existingEvent : activityLabelsAltResponse){
					if(!existingEvent.equals(event)){
						int pend = 0;
						if(activityLabelsCounterAltResponse.containsKey(trace)){
							if(activityLabelsCounterAltResponse.getItem(trace).containsKey(existingEvent)){
								pend = activityLabelsCounterAltResponse.getItem(trace).get(existingEvent);
							}
						}
						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
						if(pendingForThisTrace.containsKey(existingEvent)){
							secondElement = pendingForThisTrace.get(existingEvent);
						}
						if(pend>1){
							HashMap<String, Integer> secondEl = new  HashMap<String, Integer>();
							if(violatedForThisTrace.containsKey(existingEvent)){
								secondEl = violatedForThisTrace.get(existingEvent);
							}
//							int numPend =0;
//							if(secondElement!=null && secondElement.containsKey(event)){
//								numPend = secondElement.get(event);
//							}else{
//								numPend = snapCollection.get(existingEvent).size();
//							}
//							if(!snapCollection.get(existingEvent).isEmpty()){
//								attribute = snapCollection.get(existingEvent).get(snapCollection.get(existingEvent).size()-1);
							if(!snap.get(existingEvent).isEmpty()){
								attribute = snap.get(existingEvent).getLast();
								fulf = true;
								nr++;
								
								if(nr>1){
									start1 = System.currentTimeMillis();
									mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 0, bucketWidth);
									stop1 = System.currentTimeMillis();
									time = time+stop1-start1;
									en++;
									nr=1;
								}
								//snapCollection.get(existingEvent).remove(snapCollection.get(existingEvent).size()-1);		
								if(snap.get(existingEvent).size()>10)
									snap.get(existingEvent).removeFirst();
							}
							//fulfillment solo per l'ultima attivazione
							secondEl.put(event, pend-1);
							violatedForThisTrace.put(existingEvent, secondEl);
							violatedConstraintsPerTrace.putItem(trace, violatedForThisTrace);
						}
						if(pend==1 && snap.get(existingEvent).size()>0){ //snapCollection.get(existingEvent).size()>0){
							attribute = snap.get(existingEvent).getFirst();//snapCollection.get(existingEvent).get(0);						
							fulf = true;
							nr++;
							
							if(nr>1){
								start2 = System.currentTimeMillis();
								mod.addObservation(existingEvent,event, myAttr, attribute, attIndex, 0, bucketWidth);
								stop2 = System.currentTimeMillis();
								time = time+stop2-start2;
								en++;
								nr=1;
							}
							//fulfillment existingEvent+event
						}
						if(secondElement!=null){
						secondElement.put(event, 0);
						pendingForThisTrace.put(existingEvent, secondElement);
						}
						//pendingConstraintsPerTraceAlt.put(trace, pendingForThisTrace);
					}
				}
				
				for(String existingEvent : activityLabelsAltResponse){
					if(!existingEvent.equals(event)){
						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
						if(pendingForThisTrace.containsKey(event)){
							secondElement = pendingForThisTrace.get(event);
						}
						if(secondElement.containsKey(existingEvent)){
							int pend = secondElement.get(existingEvent);
							if(pend==1){
								attribute = snap.get(existingEvent).getLast();//snapCollection.get(existingEvent).get(pend-1);					
								fulf = false;
								nr++;			
								if(nr>1){
									start3 = System.currentTimeMillis();
									mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 1, bucketWidth);
									stop3 = System.currentTimeMillis();
									time = time+stop3-start3;
									en++;
									nr=1;
								}
							}
							//violation dell'attivazione precedente (ricorda attributi)
						}
						secondElement.put(existingEvent, 1);
						pendingForThisTrace.put(event,secondElement);
					}
				}
				pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);
			}
		}else{

			for(String firstElement : activityLabelsAltResponse){
				if(!firstElement.equals(event)){
					HashMap<String, Integer> secondEl = new  HashMap<String, Integer>();
					if(violatedForThisTrace.containsKey(firstElement)){
						secondEl = violatedForThisTrace.get(firstElement);
					}
					HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
					if(pendingForThisTrace.containsKey(firstElement)){
						secondElement = pendingForThisTrace.get(firstElement);
					}
					
					if(secondElement.containsKey(event) && secondElement.get(event)>1){
						Integer violNo = secondElement.get(event);
						Integer totviol = 0;
						if(secondEl.containsKey(event)){
							totviol = secondEl.get(event);
						}
						
						if(snap.get(firstElement).size()>1){//snapCollection.get(firstElement).size()>1){
							attribute = snap.get(firstElement).getFirst();//snapCollection.get(firstElement).get(snapCollection.get(firstElement).size()-1);						
							fulf = true;
							nr++;	
							if(nr>1){
								start4 = System.currentTimeMillis();
								mod.addObservation(firstElement, event, myAttr, attribute, attIndex, 0, bucketWidth);
								stop4 = System.currentTimeMillis();
								time = time+stop4-start4;
								en++;
								nr=1;
							}
						}
						//solo l'ultima è una fulfillment tra firstElement ed event
						secondEl.put(event, totviol + violNo-1);
						violatedForThisTrace.put(firstElement, secondEl);
						violatedConstraintsPerTrace.putItem(trace, violatedForThisTrace);
					}
					secondElement.put(event, 0);
					pendingForThisTrace.put(firstElement, secondElement);

					pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);
				}
			}
			
			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			for(String second : activityLabelsAltResponse){
				if(!second.equals(event) && secondElement!=null){
					Integer pendingNo = 1;
					if(secondElement.containsKey(second)){
						pendingNo = secondElement.get(second);	
						pendingNo ++;
						attribute = snap.get(event).getLast();//snapCollection.get(event).get(snapCollection.get(event).size()-1);			
						fulf = false;
						nr++;	
						if(nr>1){
							start5 = System.currentTimeMillis();
							mod.addObservation(event, second, myAttr, attribute, attIndex, 1, bucketWidth);
							stop5 = System.currentTimeMillis();
							time = time+stop5-start5;
							en++;
							nr=1;
						}
						//violation tra event e second prendendo lo snapshot del event precedente						
					}
					secondElement.put(second, pendingNo);
				}
			}
			pendingForThisTrace.put(event,secondElement);
			pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);

			//activityLabelsCounter.put(trace, counter);

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
		activityLabelsCounterAltResponse.putItem(trace, counter);
		//***********************
		if(activityLabelsAltResponse.size()>10)
			activityLabelsAltResponse.removeFirst();
		mod.clean();
		//System.out.println("AltRe:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
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
		//System.out.println("AltPrec");
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
		return activityLabelsAltResponse.size() +
				activityLabelsCounterAltResponse.getSize() +
				pendingConstraintsPerTraceAlt.getSize() +
				violatedConstraintsPerTrace.getSize() +
				finishedTraces.getSize();
	}
}
