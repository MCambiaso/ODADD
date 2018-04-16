package Constraints;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.*;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import moa.classifiers.trees.HoeffdingTree;
import LossyCounting.LossyCounting;
import LossyCounting.LCTemplateReplayer;
import Utils.Pair;
import Utils.Utils;

import com.yahoo.labs.samoa.instances.*;

public class Precedence implements LCTemplateReplayer {
	
	private HashMap<String, Object> attribute;
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(64);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	//private static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private static HashMap<String, LinkedList<String>> eventList = new HashMap<String, LinkedList<String>>();
	private LossyModel mod;// = new LossyModel();

	int nr = 0, en=0, cc=10;
	
	private LinkedList<String> activityLabels = new LinkedList<String>();
	private HashSet<String> activityLabelsPrecedence = new HashSet<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterPrecedence = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> fulfilledConstraintsPerTrace = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();	
	
	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/OutPrecedence.txt");
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
			fulfilledConstraintsPerTrace.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterPrecedence.addObservation(caseId, currentBucket, class2);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setAttribute(Attribute[] allAttr, int[] indVal, double[] attVal){
		mod = new LossyModel(allAttr, indVal, attVal);
	}

	@Override
	public void cleanup(Integer currentBucket) {
		fulfilledConstraintsPerTrace.cleanup(currentBucket);
		activityLabelsCounterPrecedence.cleanup(currentBucket);
	}
	
	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, stop1, stop2, time=0;
		
		//********** Set Parameter for LossyCounting **********
		//bucketWidth=(int)(1000);
		//en++;
		en = 0;
		
		//********** Build the snapshot for events **********
		attribute = new HashMap<String, Object>();
		myAttrTr = new ArrayList<Attribute>(100);		

		ArrayList<String> classe = new ArrayList<String>(2);
		classe.add("FULFILLMENT");
		classe.add("VIOLATION");
		
		//************* Formatting the attribute for hoeffding tree **********
		for(XAttribute attr : eve.getAttributes().values()){
			if(!attribute.containsKey(attr.getKey())){        
//				if(isNumeric(attr.toString()) && !attr.getKey().equals("Activity code") && !attr.getKey().equals("Specialism code")){
//					
//					//double d = Double.parseDouble(attr.toString());
//					attribute.put(attr.getKey(), attr.toString()); 
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
		
		//********** Mining fulfillment/violation **********
		String caseId = Utils.getCaseID(tr);
		String event = Utils.getActivityName(eve);
		
		LinkedList<String> list = new LinkedList<String>();
		if(eventList.containsKey(caseId))
			list = eventList.get(caseId);
		else
			eventList.put(caseId, list);
		
		activityLabelsPrecedence.add(event);
		activityLabels.add(event);
		
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if (!activityLabelsCounterPrecedence.containsKey(caseId)) {
			activityLabelsCounterPrecedence.putItem(caseId, counter);
		} else {
			counter = activityLabelsCounterPrecedence.getItem(caseId);
		}
		
		HashMap<String, HashMap<String, Integer>> fulfilledForThisTrace = new HashMap<String, HashMap<String, Integer>>();
		if (!fulfilledConstraintsPerTrace.containsKey(caseId)) {
			fulfilledConstraintsPerTrace.putItem(caseId, fulfilledForThisTrace);
		} else {
			fulfilledForThisTrace = fulfilledConstraintsPerTrace.getItem(caseId);
		}		
		
		if (activityLabels.size()>1){//activityLabelsPrecedence.size() > 1) {//list.size()>1){/
			for (String existingEvent : activityLabels){//activityLabelsPrecedence) {//list){//
				if (!existingEvent.equals(event)) {
					HashMap<String, Integer> secondElement = new HashMap<String, Integer>();
					int fulfillments = 0;
					if (fulfilledForThisTrace.containsKey(existingEvent)) {
						secondElement = fulfilledForThisTrace.get(existingEvent);
					}
					if (secondElement.containsKey(event)) {
						fulfillments = secondElement.get(event);
					}
					if (counter.containsKey(existingEvent)) {
						secondElement.put(event, fulfillments + 1);
						fulfilledForThisTrace.put(existingEvent, secondElement);

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
					}	
				}				
			}

			if(mod.mm.containsKey(event)){
				ArrayList<String> actEve = new ArrayList<String>(mod.mm.get(event).keySet());
				for(String secEl : actEve){
					if(!counter.containsKey(secEl)){
						fulf = false;
						nr++;

						if(nr>1){
							start2 = System.currentTimeMillis();
							mod.addObservation(secEl, event, myAttr, attribute, attIndex, 1, bucketWidth); 
							stop2 = System.currentTimeMillis();
							time = time+stop2-start2;
							en++;
							nr=1;	
						}						
					}					
				}
			}
			fulfilledConstraintsPerTrace.putItem(caseId, fulfilledForThisTrace);
		}
		
		// update the counter for the current trace and the current event
		// **********************

		int numberOfEvents = 1;
		if (!counter.containsKey(event)) {
			counter.put(event, numberOfEvents);
		} else {
			numberOfEvents = counter.get(event);
			numberOfEvents++;
			counter.put(event, numberOfEvents);
		}
		activityLabelsCounterPrecedence.putItem(caseId, counter);
				
		if(Utils.isTraceComplete(eve)){
			activityLabelsCounterPrecedence.remove(caseId);
			fulfilledConstraintsPerTrace.remove(caseId);
		}
		
		if(activityLabels.size()>10)
			activityLabels.removeFirst();
		
		if(list.size()==10)
			list.removeFirst();
			
		list.add(event);
		eventList.remove(caseId);
		eventList.put(caseId, list);
		
		//System.out.println(list.size());
		
		//*********************** Hoeffding tree **************************
		mod.clean();
		//System.out.println(en);
		//System.out.println("Pr:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
	}
	
	@Override
	public void results(){
		for(String aEvent : mod.mm.keySet()){ 
			for(String bEvent : mod.mm.get(aEvent).keySet()){
				printout.println("@@@@@@@@@@@@@@@@@@@@@@@@\n"+aEvent+"%"+bEvent+"\n@@@@@@@@@@@@");
//				System.out.println(mc.get(aEvent).get(bEvent).getElement0());
//				System.out.println(mc.get(aEvent).get(bEvent).getElement1());
				printout.println(mod.mm.get(aEvent).get(bEvent).getElement1());
				printout.println("\nCorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[0]+
						"\nUncorrect Fulfillment = "+mod.value.get(aEvent+"-"+bEvent)[1]+
						"\nCorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[2]+
						"\nUncorrect Violation = "+mod.value.get(aEvent+"-"+bEvent)[3]+"\n");
			}
		}	
		//System.out.println("Prec");
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
		return activityLabelsPrecedence.size() +
				activityLabelsCounterPrecedence.getSize() +
				fulfilledConstraintsPerTrace.getSize();
	}
}
/**
for(XAttribute attr : eve.getAttributes().values()){
	attribute.put(attr.getKey(), attr.toString());
	if(isNumeric(attr.toString())){			
		myAttr.add(new Attribute(attr.getKey()));//!attr.getKey().contains(":") && 
	}else{
		if(nominal.containsKey(attr.getKey())){
			if(!nominal.get(attr.getKey()).contains(attr.toString()))
				nominal.get(attr.getKey()).add(attr.toString());
			
			myAttr.add(new Attribute(attr.getKey(), nominal.get(attr.getKey())));
		}else{
			ArrayList<String> nl = new ArrayList<String>();
			nl.add(attr.toString());
			nominal.put(attr.getKey(), nl);
			myAttr.add(new Attribute(attr.getKey(), nl));
		}
	}
}*/


/**for(String exEv : fulfilledForThisTrace.keySet()){
	HashMap<String, Integer> El1 = new HashMap<String, Integer>();
	El1 = fulfilledForThisTrace.get(exEv);
	for(String elEv: counter.keySet()){
		if(!El1.containsKey(elEv) && !elEv.equals(exEv)){
			HashMap<String, Integer> secondEl = new HashMap<String, Integer>();
			int violations = 0;
			if (violatedForThisTrace.containsKey(exEv) && !violatedForThisTrace.get(exEv).containsKey(elEv)) {
				secondEl = violatedForThisTrace.get(exEv);
			}
			if (secondEl.containsKey(elEv)) {
				violations = secondEl.get(elEv);
			}
			if (counter.containsKey(exEv)) {
				secondEl.put(elEv, violations + 1);
				violatedForThisTrace.put(exEv, secondEl);
				createInstances(exEv+"-"+elEv, 1);
				if(instanceForTree.get(exEv+"-"+elEv).numInstances()>=5 && !hoeffCollection.containsKey(exEv+"-"+elEv)){
					HoeffdingTree hf = new HoeffdingTree();
					try {
						instanceForTree.get(exEv+"-"+elEv).setClassIndex(0);
						hf.setOptions(opt);
						hf.buildClassifier(instanceForTree.get(exEv+"-"+elEv));
						hoeffCollection.put(exEv+"-"+elEv, hf);							
					} catch (Exception e) {
						System.out.println("BBB:   "+exEv+"-"+elEv+"@@@@"+instanceForTree.get(exEv+"-"+elEv).numInstances());
						e.printStackTrace();
					}
				}else if(instanceForTree.get(exEv+"-"+elEv).numInstances()>numInstUpdate && hoeffCollection.containsKey(exEv+"-"+elEv)){
					try {
						hoeffCollection.get(exEv+"-"+elEv).updateClassifier(instanceForTree.get(exEv+"-"+elEv).instance(instanceForTree.get(exEv+"-"+elEv).numInstances()-1));
						if(instanceForTree.get(exEv+"-"+event).numInstances()>500){
							printout.println("@@@@@@@@@@@@\n"+hoeffCollection.get(exEv+"-"+event).toString()+"\n");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			if(fulfilledForThisTrace.get(exEv).get(elEv)!=counter.get(elEv)){
				HashMap<String, Integer> secondEl = new HashMap<String, Integer>();
				int violations = 0;
				if (violatedForThisTrace.containsKey(exEv)) {
					secondEl = violatedForThisTrace.get(exEv);
				}
				if (secondEl.containsKey(elEv)) {
					violations = secondEl.get(elEv);
				}
				if (counter.containsKey(exEv)) {
					secondEl.put(elEv, violations + 1);
					violatedForThisTrace.put(exEv, secondEl);
					createInstances(exEv+"-"+elEv, 1);
					if(instanceForTree.get(exEv+"-"+elEv).numInstances()>=5 && !hoeffCollection.containsKey(exEv+"-"+elEv)){
						HoeffdingTree hf = new HoeffdingTree();
						try {
							instanceForTree.get(exEv+"-"+elEv).setClassIndex(0);
							hf.setOptions(opt);
							hf.buildClassifier(instanceForTree.get(exEv+"-"+elEv));
							hoeffCollection.put(exEv+"-"+elEv, hf);							
						} catch (Exception e) {
							System.out.println("CCC:   "+exEv+"-"+elEv+"@@@@"+instanceForTree.get(exEv+"-"+elEv).numInstances());
							e.printStackTrace();
						}
					}else if(instanceForTree.get(exEv+"-"+elEv).numInstances()>numInstUpdate && hoeffCollection.containsKey(exEv+"-"+elEv)){
						try {
							hoeffCollection.get(exEv+"-"+elEv).updateClassifier(instanceForTree.get(exEv+"-"+elEv).instance(instanceForTree.get(exEv+"-"+elEv).numInstances()-1));
							if(instanceForTree.get(exEv+"-"+event).numInstances()>500){
								printout.println("@@@@@@@@@@@@\n"+hoeffCollection.get(exEv+"-"+event).toString()+"\n");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}*/

/**
 * if(first){
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
 */
//violatedConstraintsPerTrace.putItem(caseId, violatedForThisTrace);
