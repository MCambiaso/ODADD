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

public class AlternatePrecedence implements LCTemplateReplayer {
	
	private HashMap<String, Object> attribute;
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(20);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	
	//private static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private static HashMap<String, LinkedList<String>> eventList = new HashMap<String, LinkedList<String>>();
	private LossyModel mod = new LossyModel();
	int nr = 0, en=0, cc=10;

	//private HashSet<String> activityLabelsAltPrecedence = new HashSet<String>();
	private LinkedList<String> activityLabelsAltPrecedence = new LinkedList<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterAltPrecedence = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> fulfilledConstraintsPerTraceAlt = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> satisfactionsConstraintsPerTrace = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	private LossyCounting<HashMap<String, HashMap<String, Boolean>>> isDuplicatedActivationPerTrace = new LossyCounting<HashMap<String, HashMap<String, Boolean>>>();

	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/Results/OutAltPrecedence.txt");
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
		HashMap<String, HashMap<String, Boolean>> ex3 = new HashMap<String, HashMap<String, Boolean>>();
		@SuppressWarnings("rawtypes")
		Class class1 = ex1.getClass();
		@SuppressWarnings("rawtypes")
		Class class2 = ex2.getClass();
		@SuppressWarnings("rawtypes")
		Class class3 = ex3.getClass();

		try {
			satisfactionsConstraintsPerTrace.addObservation(caseId, currentBucket, class1);
			fulfilledConstraintsPerTraceAlt.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterAltPrecedence.addObservation(caseId, currentBucket, class2);
			isDuplicatedActivationPerTrace.addObservation(caseId, currentBucket, class3);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup(Integer currentBucket) {
		satisfactionsConstraintsPerTrace.cleanup(currentBucket);
		fulfilledConstraintsPerTraceAlt.cleanup(currentBucket);
		activityLabelsCounterAltPrecedence.cleanup(currentBucket);
		isDuplicatedActivationPerTrace.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, start3, start4, stop1, stop2, stop3, stop4, time=0;
		
		en=0;
		
		// Collection of attribute of new event
		attribute = new HashMap<String, Object>();
		myAttrTr = new ArrayList<Attribute>(100);		

		ArrayList<String> classe = new ArrayList<String>(2);
		classe.add("FULFILLMENT");
		classe.add("VIOLATION");
		
		if(first){
			myAttr.add(new Attribute("class", classe));
//			myAttr.add(new Attribute("data1"));
//			myAttr.add(new Attribute("data2"));
//			myAttr.add(new Attribute("data3"));
//			myAttr.add(new Attribute("data4"));
//			myAttr.add(new Attribute("data5"));
//			myAttr.add(new Attribute("data6"));
//			myAttr.add(new Attribute("data7"));
//			myAttr.add(new Attribute("data8"));
//			myAttr.add(new Attribute("data9"));
//			myAttr.add(new Attribute("data10"));
//			myAttr.add(new Attribute("data11"));
//			myAttr.add(new Attribute("data12"));
//			myAttr.add(new Attribute("data13"));
//			myAttr.add(new Attribute("data14"));
//			myAttr.add(new Attribute("data15"));
//			myAttr.add(new Attribute("data16"));
//			myAttr.add(new Attribute("data17"));
//			myAttr.add(new Attribute("data18"));
//			myAttr.add(new Attribute("data19"));
//			myAttr.add(new Attribute("data20"));
//			myAttr.add(new Attribute("org:group", nomin.get("org:group")));
//			myAttr.add(new Attribute("Producer code", nomin.get("Producer code")));
			myAttr.add(new Attribute("Section", nomin.get("Section")));
			myAttr.add(new Attribute("Activity code", nomin.get("Activity code")));
//			myAttr.add(new Attribute("Number of executions"));
			myAttr.add(new Attribute("Specialism code", nomin.get("Specialism code")));
//			myAttr.add(new Attribute("lifecycle:transition", nomin.get("lifecycle:transition")));
//			myAttr.add(new Attribute("time:timestamp", nomin.get("time:timestamp")));
//			myAttr.add(new Attribute("stream:lifecycle:trace-transition", nomin.get("stream:lifecycle:trace-transition")));
//			myAttr.add(new Attribute("concept:name", nomin.get("concept:name")));
			myAttr.add(new Attribute("Age", nomin.get("Age")));
			myAttr.add(new Attribute("Age:1", nomin.get("Age:1")));
			myAttr.add(new Attribute("Age:2", nomin.get("Age:2")));
			myAttr.add(new Attribute("Age:3", nomin.get("Age:3")));
			myAttr.add(new Attribute("Age:4", nomin.get("Age:4")));
			myAttr.add(new Attribute("Age:5", nomin.get("Age:5")));			
			myAttr.add(new Attribute("Diagnosis code", nomin.get("Diagnosis code")));
			myAttr.add(new Attribute("Diagnosis code:1", nomin.get("Diagnosis code:1")));
			myAttr.add(new Attribute("Diagnosis code:2", nomin.get("Diagnosis code:2")));
			myAttr.add(new Attribute("Diagnosis code:3", nomin.get("Diagnosis code:3")));
			myAttr.add(new Attribute("Diagnosis code:4", nomin.get("Diagnosis code:4")));
			myAttr.add(new Attribute("Diagnosis code:5", nomin.get("Diagnosis code:5")));
			myAttr.add(new Attribute("Diagnosis code:6", nomin.get("Diagnosis code:6")));
			myAttr.add(new Attribute("Diagnosis code:7", nomin.get("Diagnosis code:7")));
			myAttr.add(new Attribute("Diagnosis code:8", nomin.get("Diagnosis code:8")));
			myAttr.add(new Attribute("Diagnosis code:9", nomin.get("Diagnosis code:9")));
			myAttr.add(new Attribute("Diagnosis code:10", nomin.get("Diagnosis code:10")));
			myAttr.add(new Attribute("Treatment code", nomin.get("Treatment code")));
			myAttr.add(new Attribute("Treatment code:1", nomin.get("Treatment code:1")));
			myAttr.add(new Attribute("Treatment code:2", nomin.get("Treatment code:2")));
			myAttr.add(new Attribute("Treatment code:3", nomin.get("Treatment code:3")));
			myAttr.add(new Attribute("Treatment code:4", nomin.get("Treatment code:4")));
			myAttr.add(new Attribute("Treatment code:5", nomin.get("Treatment code:5")));
			myAttr.add(new Attribute("Treatment code:6", nomin.get("Treatment code:6")));
			myAttr.add(new Attribute("Treatment code:7", nomin.get("Treatment code:7")));
			myAttr.add(new Attribute("Treatment code:8", nomin.get("Treatment code:8")));
			myAttr.add(new Attribute("Treatment code:9", nomin.get("Treatment code:9")));
			myAttr.add(new Attribute("Treatment code:10", nomin.get("Treatment code:10")));
			myAttr.add(new Attribute("Diagnosis", nomin.get("Diagnosis")));
			myAttr.add(new Attribute("Diagnosis:1", nomin.get("Diagnosis:1")));
			myAttr.add(new Attribute("Diagnosis:2", nomin.get("Diagnosis:2")));
			myAttr.add(new Attribute("Diagnosis:3", nomin.get("Diagnosis:3")));
			myAttr.add(new Attribute("Diagnosis:4", nomin.get("Diagnosis:4")));
			myAttr.add(new Attribute("Diagnosis:5", nomin.get("Diagnosis:5")));
			myAttr.add(new Attribute("Diagnosis:6", nomin.get("Diagnosis:6")));
			myAttr.add(new Attribute("Diagnosis:7", nomin.get("Diagnosis:7")));
			myAttr.add(new Attribute("Diagnosis:8", nomin.get("Diagnosis:8")));
			myAttr.add(new Attribute("Diagnosis:9", nomin.get("Diagnosis:9")));
			myAttr.add(new Attribute("Diagnosis:10", nomin.get("Diagnosis:10")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID", nomin.get("Diagnosis Treatment Combination ID")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:1", nomin.get("Diagnosis Treatment Combination ID:1")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:2", nomin.get("Diagnosis Treatment Combination ID:2")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:3", nomin.get("Diagnosis Treatment Combination ID:3")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:4", nomin.get("Diagnosis Treatment Combination ID:4")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:5", nomin.get("Diagnosis Treatment Combination ID:5")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:6", nomin.get("Diagnosis Treatment Combination ID:6")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:7", nomin.get("Diagnosis Treatment Combination ID:7")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:8", nomin.get("Diagnosis Treatment Combination ID:8")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:9", nomin.get("Diagnosis Treatment Combination ID:9")));
			myAttr.add(new Attribute("Diagnosis Treatment Combination ID:10", nomin.get("Diagnosis Treatment Combination ID:10")));
			myAttr.add(new Attribute("Specialism code:1", nomin.get("Specialism code:1")));
			myAttr.add(new Attribute("Specialism code:2", nomin.get("Specialism code:2")));
			myAttr.add(new Attribute("Specialism code:3", nomin.get("Specialism code:3")));
			myAttr.add(new Attribute("Specialism code:4", nomin.get("Specialism code:4")));
			myAttr.add(new Attribute("Specialism code:5", nomin.get("Specialism code:5")));
			myAttr.add(new Attribute("Specialism code:6", nomin.get("Specialism code:6")));
			myAttr.add(new Attribute("Specialism code:7", nomin.get("Specialism code:7")));
			myAttr.add(new Attribute("Specialism code:8", nomin.get("Specialism code:8")));
			myAttr.add(new Attribute("Specialism code:9", nomin.get("Specialism code:9")));
			myAttr.add(new Attribute("Specialism code:10", nomin.get("Specialism code:10")));
//			End date, Age, Diagnosis code, Treatment code, Diagnosis, Diagnosis Treatment Combination ID, Start date
			first=false;
		}	
		
		for(XAttribute attr : eve.getAttributes().values()){
			if(!attribute.containsKey(attr.getKey())){        
				if(isNumeric(attr.toString()) && !attr.getKey().equals("Activity code") && !attr.getKey().equals("Specialism code")){
					double d = Double.parseDouble(attr.toString());
					attribute.put(attr.getKey(), d); 
				}else{
					attribute.put(attr.getKey(), attr.toString());
				}								
			}else if(attribute.containsKey(attr.getKey())){               //!attr.getKey().contains(":") && 
				attribute.remove(attr.getKey());
				attribute.put(attr.getKey(), attr.toString());
			}	
			int l = nomin.get(attr.getKey()).indexOf(attr.toString());
			attIndex.put(attr.getKey(), l);
		}		
		
		for(XAttribute attr : tr.getAttributes().values()){
//			if(!myAttrTr.contains(attr.getKey())){
//				myAttrTr.add(new Attribute(attr.getKey(), nomin.get(attr.getKey())));
//			}		
			int l = nomin.get(attr.getKey()).indexOf(attr.toString());
			attIndex.put(attr.getKey(), l);
		}
		
		for(Attribute attr : myAttr){
			if(!attIndex.containsKey(attr.name()) && !attr.name().equals("class")){
				attIndex.put(attr.name(), nomin.get(attr.name()).indexOf("0"));
			}
		}
		
		String caseId = Utils.getCaseID(tr);
		String event = Utils.getActivityName(eve);
		
		LinkedList<String> list = new LinkedList<String>();
		if(eventList.containsKey(caseId))
			list = eventList.get(caseId);
		else
			eventList.put(caseId, list);
		
		activityLabelsAltPrecedence.add(event);
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if(!activityLabelsCounterAltPrecedence.containsKey(caseId)){
			activityLabelsCounterAltPrecedence.putItem(caseId, counter);
		}else{
			counter = activityLabelsCounterAltPrecedence.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> fulfilledForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!fulfilledConstraintsPerTraceAlt.containsKey(caseId)){
			fulfilledConstraintsPerTraceAlt.putItem(caseId, fulfilledForThisTrace);
		}else{
			fulfilledForThisTrace = fulfilledConstraintsPerTraceAlt.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> satisfactionsForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!satisfactionsConstraintsPerTrace.containsKey(caseId)){
			satisfactionsConstraintsPerTrace.putItem(caseId, satisfactionsForThisTrace);
		}else{
			satisfactionsForThisTrace = satisfactionsConstraintsPerTrace.getItem(caseId);
		}
		HashMap<String,HashMap<String,Boolean>> isDuplicatedForThisTrace = new HashMap<String,HashMap<String,Boolean>>();
		if(!isDuplicatedActivationPerTrace.containsKey(caseId)){
			isDuplicatedActivationPerTrace.putItem(caseId, isDuplicatedForThisTrace);
		}else{
			isDuplicatedForThisTrace = isDuplicatedActivationPerTrace.getItem(caseId);
		}
		if(activityLabelsAltPrecedence.size()>1){//list.size()>1){//
			for(String existingEvent : activityLabelsAltPrecedence){// list){//
				if(!existingEvent.equals(event)){
					boolean violated = false;
					if(isDuplicatedForThisTrace.containsKey(event)){
						if(isDuplicatedForThisTrace.get(event).containsKey(existingEvent) && isDuplicatedForThisTrace.get(event).get(existingEvent)){
							violated = true;
						}
						isDuplicatedForThisTrace.get(event).put(existingEvent, true);
					}
					if(isDuplicatedForThisTrace.containsKey(existingEvent)){
						isDuplicatedForThisTrace.get(existingEvent).put(event, false);
					}
					if(!isDuplicatedForThisTrace.containsKey(event)){
						HashMap<String, Boolean> sec = new HashMap<String,Boolean>(); 
						sec.put(existingEvent, true);
						isDuplicatedForThisTrace.put(event, sec);
					}
					HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
					int fulfillments = 0;
					if(fulfilledForThisTrace.containsKey(existingEvent)){
						secondElement = fulfilledForThisTrace.get(existingEvent);
					}
					if(secondElement.containsKey(event)){
						fulfillments = secondElement.get(event);
					}
					HashMap<String, Integer> secondSat = new  HashMap<String, Integer>();
					if(satisfactionsForThisTrace.containsKey(event)){
						secondSat = satisfactionsForThisTrace.get(event);
					}
					secondSat.put(existingEvent, 0);
					HashMap<String, Integer> secondSat2 = new  HashMap<String, Integer>();
					if(satisfactionsForThisTrace.containsKey(existingEvent)){
						secondSat2 = satisfactionsForThisTrace.get(existingEvent);
					}
					int sat = 0;
					if(secondSat2.containsKey(event)){
						sat = secondSat2.get(event);
					}
					secondSat2.put(existingEvent, sat + 1);
					if(counter.containsKey(existingEvent)){
						if(satisfactionsForThisTrace.get(existingEvent) == null || satisfactionsForThisTrace.get(existingEvent).get(event) == null || satisfactionsForThisTrace.get(existingEvent).get(event)<2){
							if(!violated){
								//fulfillment ex+event event						
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
								secondElement.put(event, fulfillments + 1);
								fulfilledForThisTrace.put(existingEvent, secondElement);
							}else{
								//violation
								fulf = false;
								nr++;

								if(nr>1){
									start2 = System.currentTimeMillis();
									mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 1, bucketWidth);
									stop2 = System.currentTimeMillis();
									time = time+stop2-start2;
									en++;
									nr=1; 
								}
							}
						}else{
							//violation
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
					}else{
						//violation
						fulf = false;
						nr++;
						
						if(nr>1){
							start4 = System.currentTimeMillis();
							mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 1, bucketWidth);
							stop4 = System.currentTimeMillis();
							time = time+stop4-start4;
							en++;
							nr=1;
						}
					}
				}
			}
			fulfilledConstraintsPerTraceAlt.putItem(caseId, fulfilledForThisTrace);
		}
		
		int numberOfEvents = 1;
		if(!counter.containsKey(event)){
			counter.put(event, numberOfEvents);
		}else{
			numberOfEvents = counter.get(event);
			numberOfEvents++;
			counter.put(event, numberOfEvents); 
		}
		activityLabelsCounterAltPrecedence.putItem(caseId, counter);
		
		if(Utils.isTraceComplete(eve)){
			activityLabelsCounterAltPrecedence.remove(caseId);
			fulfilledConstraintsPerTraceAlt.remove(caseId);
			eventList.remove(caseId);
		}
		
		if(activityLabelsAltPrecedence.size()>10)
			activityLabelsAltPrecedence.removeFirst();
		
		if(list.size()==10)
			list.removeFirst();
			
		list.add(event);
		eventList.remove(caseId);
		eventList.put(caseId, list);
		
		mod.clean();
		//System.out.println(en);
		//System.out.println("AltPr:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
	}
	
	@Override
	public void results(){
		for(String aEvent : mod.mm.keySet()){ 
			for(String bEvent : mod.mm.get(aEvent).keySet()){
				printout.println("@@@@@@@@@@@@\n"+aEvent+"%"+bEvent+"\n@@@@@@@@@@@@");
				printout.println(mod.mm.get(aEvent).get(bEvent).getElement1());
			}
		}	
//			System.out.println("AltPrec"+"\t"+fulfill+"\t"+(act-fulfill));
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
		return activityLabelsAltPrecedence.size() +
				activityLabelsCounterAltPrecedence.getSize() +
				fulfilledConstraintsPerTraceAlt.getSize() +
				satisfactionsConstraintsPerTrace.getSize()
				+isDuplicatedActivationPerTrace.getSize();
	}
}