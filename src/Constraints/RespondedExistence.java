package Constraints;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.trees.HoeffdingTree;
import LossyCounting.LossyCounting;
import LossyCounting.LCTemplateReplayer;
import Utils.ComputeKPI;
//import prompt.onlinedeclare.utils.DeclareModel;
import Utils.Pair;
import Utils.Utils;

import com.yahoo.labs.samoa.instances.*;

import moa.classifiers.trees.HoeffdingTree;
import moa.core.*;


public class RespondedExistence implements LCTemplateReplayer {
	private HashMap<String, ArrayList<HashMap<String, Object>>> snapCollection = new HashMap<String, ArrayList<HashMap<String, Object>>>();
	private HashMap<String, Instances> instanceForTree = new HashMap<String, Instances>();
	private HashMap<String, Object> attribute;
	private HashMap<String, ArrayList<String>> nominal = new HashMap<String, ArrayList<String>>();
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(20);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	private Model modello = new Model();
	private HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private LossyModel mod = new LossyModel();
	int nr = 0, en=0, ff=0, vv=0, cc=5;

	private HashSet<String> activityLabelsRespondedExistence = new HashSet<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterRespondedExistence = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTraceRe = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	
	File file = new File("/home/matte/Scrivania/Out/OutRespondedExistence.txt");
	FileWriter fw = null;
	BufferedWriter brf;
	PrintWriter printout;{			
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
			pendingConstraintsPerTraceRe.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterRespondedExistence.addObservation(caseId, currentBucket, class2);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup(Integer currentBucket) {
		pendingConstraintsPerTraceRe.cleanup(currentBucket);
		activityLabelsCounterRespondedExistence.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
//		Model modello = model;
		int currentBucket , pp=0;
		bucketWidth=(int)(1000);
		long start = System.currentTimeMillis();
		long start1, start2, start3, start4, start5, stop1, stop2, stop3, stop4, stop5, time=0;
		//en++;
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
				String attrib = attr.name();
				attIndex.put(attr.name(), nomin.get(attr.name()).indexOf("0"));
			}
		}
		
		String caseId = Utils.getCaseID(tr);
		String event = Utils.getActivityName(eve);
		activityLabelsRespondedExistence.add(event);
		
		if(snapCollection.containsKey(event)){
			snapCollection.get(event).add(attribute);
		}else{
			ArrayList<HashMap<String, Object>> firstSnap = new ArrayList<HashMap<String, Object>>();
			firstSnap.add(attribute);
			snapCollection.put(event, firstSnap);
		}
		
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		if(!activityLabelsCounterRespondedExistence.containsKey(caseId)) {
			activityLabelsCounterRespondedExistence.putItem(caseId, counter);
		} else {
			counter = activityLabelsCounterRespondedExistence.getItem(caseId);
		}
		HashMap<String,HashMap<String,Integer>> pendingForThisTrace = new HashMap<String,HashMap<String,Integer>>();
		if(!pendingConstraintsPerTraceRe.containsKey(caseId)){
			pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
		}else{
			pendingForThisTrace = pendingConstraintsPerTraceRe.getItem(caseId);
		}
		if (!counter.containsKey(event)) {
			if (activityLabelsRespondedExistence.size()>1) {
				for (String existingEvent : activityLabelsRespondedExistence) {
					if(pp==cc)
					{
						pp=0;
						break;
					}else{
						pp++;
					}
					if (!existingEvent.equals(event)){
						HashMap<String, Integer> secondElement = new HashMap<String, Integer>();
						if (pendingForThisTrace.containsKey(existingEvent)) {
							secondElement = pendingForThisTrace.get(existingEvent);
						}
						int numPend =0;
						if(secondElement.containsKey(event)){
							numPend = secondElement.get(event);
						}else{
							numPend = snapCollection.get(existingEvent).size();
						}
						
//						for(int i = 0; i<snapCollection.get(existingEvent).size(); i++){
						if(snapCollection.get(existingEvent).size()>0){
							if(existingEvent.contains("a-") && event.contains("b-")) ff++;
							attribute = snapCollection.get(existingEvent).get(snapCollection.get(existingEvent).size()-1);
//							createInstance(existingEvent+"-"+event, 0);
//							HF(existingEvent+"-"+event);
							//HF(existingEvent+"%"+event, createInstance(existingEvent+"%"+event, 0), modello);			
							if(nr>1 && nr<50){
								start1 = System.currentTimeMillis();
								mc = mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 0, mc); 
								stop1 = System.currentTimeMillis();
								time = time+stop1-start1;
								en++;
								nr=1;
							}
							fulf = true;
							nr++;
							currentBucket = nr/bucketWidth;						
							//modello.addObservation(existingEvent,event, currentBucket, bucketWidth, fulf);
							snapCollection.get(existingEvent).remove(snapCollection.get(existingEvent).size()-1);							
						}
						//fulfillment existingEvent+event
						secondElement.put(event, 0);
						pendingForThisTrace.put(existingEvent,secondElement);
					}

				}
				for (String existingEvent : activityLabelsRespondedExistence) {
					if(pp==cc)
					{
						pp=0;
						break;
					}else{
						pp++;
					}
					if (!existingEvent.equals(event)) {

						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
						if(pendingForThisTrace.containsKey(event)){
							secondElement = pendingForThisTrace.get(event);
						}
						if(!counter.containsKey(existingEvent)){
							secondElement.put(existingEvent, 1);
						}else{
							int numPend =0;
							if(secondElement.containsKey(existingEvent)){
								numPend = secondElement.get(existingEvent);
							}else{
								numPend = snapCollection.get(event).size();
							}
							
//							for(int i = 0; i<snapCollection.get(event).size(); i++){	
							if(snapCollection.get(event).size()>0){
								if(event.contains("a-") && existingEvent.contains("b-")) ff++;
								attribute = snapCollection.get(event).get(snapCollection.get(event).size()-1);
//								createInstance(existingEvent+"-"+event, 0);
//								HF(existingEvent+"-"+event);
								//HF(event+"%"+existingEvent, createInstance(event+"%"+existingEvent, 0), modello);						
								fulf = true;
								nr++;
								currentBucket = nr/bucketWidth;
								if(nr>1 && nr<50){
									start2 = System.currentTimeMillis();
									mc = mod.addObservation(event,existingEvent, myAttr, attribute, attIndex, 0, mc);
									stop2 = System.currentTimeMillis();
									time = time+stop2-start2;
									en++;
									nr=1;	
								}
								//modello.addObservation(event, existingEvent, currentBucket, bucketWidth, fulf);
								snapCollection.get(event).remove(snapCollection.get(event).size()-1);					
							}
							//fulfillment event+existingEvent
							secondElement.put(existingEvent, 0);
						}
						pendingForThisTrace.put(event,secondElement);

					}
				}
				pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
//				pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);
			}
		} else {
			for (String firstElement : pendingForThisTrace.keySet()) {
				if(pp==cc)
				{
					pp=0;
					break;
				}else{
					pp++;
				}
				if (!firstElement.equals(event)) {
					HashMap<String, Integer> secondElement = pendingForThisTrace.get(firstElement);
					int numPend =0;
					if(secondElement.containsKey(event)){
						numPend = secondElement.get(event);
					}else{
						numPend = snapCollection.get(firstElement).size();
					}
					
//					for(int i = 0; i<snapCollection.get(firstElement).size(); i++){		
						if(snapCollection.get(firstElement).size()>0){
//							if(firstElement.contains("a-") && event.contains("b-")) ff++;
						attribute = snapCollection.get(firstElement).get(snapCollection.get(firstElement).size()-1);
//						createInstance(firstElement+"-"+event, 0);
//						HF(firstElement+"-"+event);
						//HF(firstElement+"%"+event, createInstance(firstElement+"%"+event, 0), modello);						
						fulf = true;
						nr++;
						currentBucket = nr/bucketWidth;	
						if(nr>1 && nr<50){
							start3 = System.currentTimeMillis();
							mc = mod.addObservation(firstElement, event, myAttr, attribute, attIndex, 0, mc);	
							stop3 = System.currentTimeMillis();
							time = time+stop3-start3;
							en++;
							nr=1;	
						}			
						//modello.addObservation(firstElement, event, currentBucket, bucketWidth, fulf);
						snapCollection.get(firstElement).remove(snapCollection.get(firstElement).size()-1);
					}
					//fulfillment firstElement+Event
					secondElement.put(event, 0);
					pendingForThisTrace.put(firstElement, secondElement);
					pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
//					pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);
				}
			}

			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			for (String second : secondElement.keySet()) {
				if(pp==cc)
				{
					pp=0;
					break;
				}else{
					pp++;
				}
				if (!second.equals(event)) {
					if (!counter.containsKey(second)) {
						Integer pendingNo = secondElement.get(second);
						pendingNo ++;
						secondElement.put(second, pendingNo);
					} else {
						int numPend =0;
						if(secondElement.containsKey(second)){
							numPend = secondElement.get(second);
						}else{
							numPend = snapCollection.get(event).size();
						}
						
//						for(int i = 0; i<snapCollection.get(event).size(); i++){							
						if(snapCollection.get(event).size()>0){
//							if(event.contains("a-") && second.contains("b-")) ff++;
							attribute = snapCollection.get(event).get(snapCollection.get(event).size()-1);
//							createInstance(firstElement+"-"+event, 0);
//							HF(firstElement+"-"+event);
							//HF(event+"%"+second, createInstance(event+"%"+second, 0), modello);						
							fulf = true;
							nr++;
							currentBucket = nr/bucketWidth;	
							if(nr>1 && nr<50){
								start4 = System.currentTimeMillis();
								mc = mod.addObservation(event, second, myAttr, attribute, attIndex, 0, mc);
								stop4 = System.currentTimeMillis();
								time = time+stop4-start4;
								en++;
								nr=1;	
							}					
							//modello.addObservation(event, second, currentBucket, bucketWidth, fulf);
							snapCollection.get(event).remove(snapCollection.get(event).size()-1);
						}
						//fulfillment event+second
						secondElement.put(second, 0);
					}
				}
			}
			pendingForThisTrace.put(event,secondElement);
			pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
//			pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);
		}

		//update the counter for the current trace and the current event
		//**********************

		int numberOfEvents = 1;
		if (!counter.containsKey(event)) {
			counter.put(event, numberOfEvents);
		} else {
			numberOfEvents = counter.get(event);
			numberOfEvents++;
			counter.put(event, numberOfEvents); 
		}
		activityLabelsCounterRespondedExistence.putItem(caseId, counter);
		//***********************
		
		if(Utils.isTraceComplete(eve)){
			for (String firstElement : pendingForThisTrace.keySet()) {
				for (String secondElement : pendingForThisTrace.get(firstElement).keySet()) {
					if(pp==cc)
					{
						pp=0;
						break;
					}else{
						pp++;
					}
//					if(!pendingForThisTrace.get(firstElement).get(secondElement).equals(0)){
						int numPend = pendingForThisTrace.get(firstElement).size();
//						for(int i = 0; i<=numPend; i++){
							if(snapCollection.get(firstElement).size()>0){
							attribute = snapCollection.get(firstElement).get(snapCollection.get(firstElement).size()-1);
//							createInstance(firstElement+"-"+secondElement, 1);
//							HF(firstElement+"-"+secondElement);
							//HF(firstElement+"%"+secondElement, createInstance(firstElement+"%"+secondElement, 1), modello);
							fulf = false;
							nr++;
							currentBucket = nr/bucketWidth;
							if(nr>1 && nr<50){
								start5 = System.currentTimeMillis();
								mc = mod.addObservation(firstElement, secondElement, myAttr, attribute, attIndex, 1, mc);
								stop5 = System.currentTimeMillis();
								time = time+stop5-start5;
								en++;
								nr=1;	
							}
							//modello.addObservation(firstElement, secondElement, currentBucket, bucketWidth, fulf);
							snapCollection.get(firstElement).remove(snapCollection.get(firstElement).size()-1);	
							}
//						}
//					}						
				}
			}
		}
		
		if(en==199999){ //instanceForTree.get(name).numInstances()>1000
			double fulfill = 0.0;
			double act = 0.0;
			for(String caseID : activityLabelsCounterRespondedExistence.keySet()) {
				HashMap<String, Integer> Counter = activityLabelsCounterRespondedExistence.getItem(caseID);
				HashMap<String, HashMap<String, Integer>> PendingForThisTrace = pendingConstraintsPerTraceRe.getItem(caseID);
				if (PendingForThisTrace == null) {
					PendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
				}

				if (Counter.containsKey("a-null")) {
					double totnumber = Counter.get("a-null");
					act = act + totnumber;
					if (PendingForThisTrace.containsKey("a-null")) {
						if (PendingForThisTrace.get("a-null").containsKey("b-null")) {	
							double stillpending = PendingForThisTrace.get("a-null").get("b-null");
							fulfill = fulfill + (totnumber - stillpending);
						}
					}
				}
			}
			
			for(String prHF : modello.hoeffCollection.keySet()){ 				
				ComputeKPI ckpi = new ComputeKPI();
				Measurement[] measurement = modello.hoeffCollection.get(prHF).getModelMeasurements();
//				if(measurement[0].getValue() >= 10 ){
//					printout.println("@@@@@@@@@@@@\n"+prHF+"\n"+modello.hoeffCollection.get(prHF).toString());
//				}
				if(measurement[0].getValue() >= 1){
					try {
						//printout.println("@@@@@@@@@@@@\n"+prHF+"\n"+modello.hoeffCollection.get(prHF).toString());
						double[] out = ckpi.ComputeKPI(modello.hoeffCollection.get(prHF).toString());
//						printout.println("act: "+out[0]+"\tratio: "+out[1]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}			
			System.out.println("RespExist"+"\t"+fulfill+"\t"+(act-fulfill)+"\t"+ff);
//			printout.flush();
//			printout.close();
//			for(String name : numberViolFul.keySet()){
//				printout.println("@@@@@@@@@@@@\n"+name+"\t"+numberViolFul.get(name).getFirst()+", "+numberViolFul.get(name).getSecond());
//			}
		}
		//System.out.println(en);
//		System.out.println("ReEx:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time);
		printout.println(System.currentTimeMillis()-start);
		printout.flush();
//		printout.close();
	}
	
	public void HF(String name, Instance instance, Model modello){
		if(!modello.hoeffCollection.containsKey(name)){ //instanceForTree.get(name).numInstances()>=5 && 
			HoeffdingTree hf = new HoeffdingTree();							
			
				Instances in = instanceForTree.get(name);
				InstancesHeader ih = new InstancesHeader(in);
				in.setClassIndex(0);

				Attribute prova = in.attribute("class");

				hf.prepareForUse();
				
				hf.setModelContext(ih);
				hf.leafpredictionOption.setChosenLabel("MC");
				hf.gracePeriodOption.setValue(2);
				hf.splitConfidenceOption.setValue(1.0E-2);

				hf.trainOnInstance(instance);
				modello.hoeffCollection.put(name, hf);

		}else if(modello.hoeffCollection.containsKey(name)){ //instanceForTree.get(name).numInstances()>numInstUpdate && 
			
//				hoeffCollection.get(name).updateClassifier(instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)); //instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)
				Instances in = instanceForTree.get(name);
				
				in.setClassIndex(0);
				modello.hoeffCollection.get(name).trainOnInstance(instance);
				
//				modello.hoeffCollection.get(name).updateClassifier(instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)); //instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)
				instanceForTree.get(name).delete(instanceForTree.get(name).numInstances()-1);
			
		}
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
	
	public Instance createInstance(String name, int classAt){

		Instances instse;
		if(!instanceForTree.containsKey(name)){
			instse = new Instances(name, myAttr, 1000);
		}else{
			instse = instanceForTree.get(name);
		}
		//		double[] instanceValue = new double[myAttr.size()];
		int n=0;
		InstancesHeader ih = new InstancesHeader(instse);
		DenseInstance instance = new DenseInstance(6666);
		instance.setDataset(ih);
		for(Attribute attr : myAttr){	
			String attrName = attr.name();
			if(attrName.contains("class")){
//				instanceValue[n] = classAt;
				instance.setValue(n, classAt);
			}else{
				if(attribute.containsKey(attrName)){
					//System.out.println(isNumeric(attribute.get(attrName).toString()));
					if(!isNumeric(attribute.get(attrName).toString())|| attrName.equals("Activity code") || attrName.equals("Specialism code") ){//
						//					instanceValue[n] = attIndex.get(attrName);

						instance.setValue(attr, attIndex.get(attrName));

					}else{
						double tt = (double) attribute.get(attrName);
						//					instanceValue[n] = tt; //(double) attribute.get(attrName);
						instance.setValue(n, tt);
					}
				}

				try{
					double tt = (double) attribute.get(attrName);
					instance.setValue(n, tt);
					//System.out.println(tt);
				}catch(Exception e) {
					instance.setValue(n, 88.88);
					//System.out.println(attribute.get(attrName));
				}
			}
			n++;
		}
		
		instse.add(instance);
		//		System.out.println(instse);
		instanceForTree.put(name, instse);

		return instance;
	}

//	@Override
//	public void updateModel(DeclareModel d) {
//		for(String param1 : activityLabelsRespondedExistence) {
//			for(String param2 : activityLabelsRespondedExistence) {
//				if(!param1.equals(param2)){
//
//					// let's generate responded existence
//					double fulfill = 0.0;
//					double act = 0.0;
//					for(String caseId : activityLabelsCounterRespondedExistence.keySet()) {
//						HashMap<String, Integer> counter = activityLabelsCounterRespondedExistence.getItem(caseId);
//						HashMap<String, HashMap<String, Integer>> pendingForThisTrace = pendingConstraintsPerTraceRe.getItem(caseId);
//						if (pendingForThisTrace == null) {
//							pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
//						}
//
//						if (counter.containsKey(param1)) {
//							double totnumber = counter.get(param1);
//							act = act + totnumber;
//							if (pendingForThisTrace.containsKey(param1)) {
//								if (pendingForThisTrace.get(param1).containsKey(param2)) {	
//									double stillpending = pendingForThisTrace.get(param1).get(param2);
//									fulfill = fulfill + (totnumber - stillpending);
//								}
//							}
//						}
//					}
//					d.addRespondedExistence(param1, param2, act, fulfill);
//				//	d.addNotCoExistence(param1, param2, act, act - fulfill);
//				}
//			}
//		}
//	}

	@Override
	public Integer getSize() {
		return activityLabelsRespondedExistence.size() +
				activityLabelsCounterRespondedExistence.getSize() +
				pendingConstraintsPerTraceRe.getSize();
	}

}
