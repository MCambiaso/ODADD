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

public class RespondedExistence implements LCTemplateReplayer {
	
	private HashMap<String, LinkedList<HashMap<String, Object>>> snapCollection = new HashMap<String, LinkedList<HashMap<String, Object>>>();
	private HashMap<String, Object> attribute;
	ArrayList<Attribute> myAttr = new ArrayList<Attribute>(64);
	ArrayList<Attribute> myAttrTr ;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	boolean first = true, fulf = true;
	
	//static HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mc = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	private LossyModel mod;// = new LossyModel();
	int nr = 0, en=0, ff=0, vv=0, cc=10;

//	private HashSet<String> activityLabelsRespondedExistence = new HashSet<String>();
	private LinkedList<String> activityLabelsRespondedExistence = new LinkedList<String>();
	private LossyCounting<HashMap<String, Integer>> activityLabelsCounterRespondedExistence = new LossyCounting<HashMap<String, Integer>>();
	private LossyCounting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTraceRe = new LossyCounting<HashMap<String, HashMap<String, Integer>>>();
	
	File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/OutRespondedExistence.txt");
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
			pendingConstraintsPerTraceRe.addObservation(caseId, currentBucket, class1);
			activityLabelsCounterRespondedExistence.addObservation(caseId, currentBucket, class2);
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
		pendingConstraintsPerTraceRe.cleanup(currentBucket);
		activityLabelsCounterRespondedExistence.cleanup(currentBucket);
	}

	@Override
	public void process(XEvent eve, XTrace tr, HashMap<String, ArrayList<String>> nomin, Integer bucketWidth) {
		long start = System.currentTimeMillis();
		long start1, start2, start3, start4, start5, stop1, stop2, stop3, stop4, stop5, time=0;
		//en++;
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
		activityLabelsRespondedExistence.add(event);
		
		if(snapCollection.containsKey(event)){
			//snapCollection.get(event).add(attribute);
			if(snapCollection.get(event).size()>3)
				snapCollection.get(event).removeFirst();
			
			snapCollection.get(event).add(attribute);
		}else{
			LinkedList<HashMap<String, Object>> firstSnap = new LinkedList<HashMap<String, Object>>();
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
					if (!existingEvent.equals(event)){
						HashMap<String, Integer> secondElement = new HashMap<String, Integer>();
						if (pendingForThisTrace.containsKey(existingEvent)) {
							secondElement = pendingForThisTrace.get(existingEvent);
						}
//						int numPend =0;
//						if(secondElement.containsKey(event)){
//							numPend = secondElement.get(event);
//						}else{
//							numPend = snapCollection.get(existingEvent).size();
//						}
						
//						for(int i = 0; i<snapCollection.get(existingEvent).size(); i++){
						if(snapCollection.get(existingEvent).size()>0){
							//if(existingEvent.contains("a-") && event.contains("b-")) ff++;
							attribute = snapCollection.get(existingEvent).getLast();//.get(snapCollection.get(existingEvent).size()-1);
							
//							if(nr>1){
								start1 = System.currentTimeMillis();
								mod.addObservation(existingEvent, event, myAttr, attribute, attIndex, 0, bucketWidth); 
								stop1 = System.currentTimeMillis();
								time = time+stop1-start1;
								en++;
								nr=1;
//							}
							fulf = true;
							nr++;
							
							if(snapCollection.get(event).size()>3)
								snapCollection.get(existingEvent).removeLast();//.remove(snapCollection.get(existingEvent).size()-1);							
						}
						//fulfillment existingEvent+event
						if(secondElement!=null){
						secondElement.put(event, 0);
						pendingForThisTrace.put(existingEvent,secondElement);}
					}

				}
				for (String existingEvent : activityLabelsRespondedExistence) {
					if (!existingEvent.equals(event)) {

						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
						if(pendingForThisTrace.containsKey(event)){
							secondElement = pendingForThisTrace.get(event);
						}
						if(!counter.containsKey(existingEvent)){
							secondElement.put(existingEvent, 1);
						}else{
//							int numPend =0;
//							if(secondElement.containsKey(existingEvent)){
//								numPend = secondElement.get(existingEvent);
//							}else{
//								numPend = snapCollection.get(event).size();
//							}
							
//							for(int i = 0; i<snapCollection.get(event).size(); i++){	
							if(snapCollection.get(event).size()>0){
								if(event.contains("a-") && existingEvent.contains("b-")) ff++;
								attribute = snapCollection.get(event).getLast();//.get(snapCollection.get(event).size()-1);						
								fulf = true;
								nr++;
								
//								if(nr>1){
									start2 = System.currentTimeMillis();
									mod.addObservation(event,existingEvent, myAttr, attribute, attIndex, 0, bucketWidth);
									stop2 = System.currentTimeMillis();
									time = time+stop2-start2;
									en++;
									nr=1;	
//								}
									if(snapCollection.get(event).size()>3)
										snapCollection.get(event).removeLast();//.remove(snapCollection.get(event).size()-1);					
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
				if (!firstElement.equals(event)) {
					HashMap<String, Integer> secondElement = pendingForThisTrace.get(firstElement);
//					int numPend =0;
//					if(secondElement.containsKey(event)){
//						numPend = secondElement.get(event);
//					}else{
//						numPend = snapCollection.get(firstElement).size();
//					}
					
//					for(int i = 0; i<snapCollection.get(firstElement).size(); i++){		
					if(snapCollection.get(firstElement).size()>0){
						attribute = snapCollection.get(firstElement).getLast();//.get(snapCollection.get(firstElement).size()-1);					
						fulf = true;
						nr++;
//						if(nr>1){
							start3 = System.currentTimeMillis();
							mod.addObservation(firstElement, event, myAttr, attribute, attIndex, 0, bucketWidth);	
							stop3 = System.currentTimeMillis();
							time = time+stop3-start3;
							en++;
							nr=1;	
//						}
							if(snapCollection.get(event).size()>3)	
								snapCollection.get(firstElement).removeLast();//.remove(snapCollection.get(firstElement).size()-1);
					}
					//fulfillment firstElement+Event
					secondElement.put(event, 0);
					pendingForThisTrace.put(firstElement, secondElement);
					pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
//					pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);
				}
			}
			
			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			if(secondElement!=null){
				for (String second : secondElement.keySet()) {
					if (!second.equals(event)) {
						if (!counter.containsKey(second)) {
							Integer pendingNo = secondElement.get(second);
							pendingNo ++;
							secondElement.put(second, pendingNo);
						} else {
							//int numPend =0;
							//if(secondElement.containsKey(second)){
							//numPend = secondElement.get(second);
							//}else{
							//numPend = snapCollection.get(event).size();
							//}

							//for(int i = 0; i<snapCollection.get(event).size(); i++){							
							if(snapCollection.get(event).size()>0){
								//							if(event.contains("a-") && second.contains("b-")) ff++;
								attribute = snapCollection.get(event).getLast();//.get(snapCollection.get(event).size()-1);						
								fulf = true;
								nr++;
//								if(nr>1){
									start4 = System.currentTimeMillis();
									mod.addObservation(event, second, myAttr, attribute, attIndex, 0, bucketWidth);
									stop4 = System.currentTimeMillis();
									time = time+stop4-start4;
									en++;
									nr=1;	
//								}					
								//modello.addObservation(event, second, currentBucket, bucketWidth, fulf);
								if(snapCollection.get(event).size()>3)
									snapCollection.get(event).removeLast();//.remove(snapCollection.get(event).size()-1);
							}
							//fulfillment event+second
							secondElement.put(second, 0);
						}
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
//					if(!pendingForThisTrace.get(firstElement).get(secondElement).equals(0)){
//						int numPend = pendingForThisTrace.get(firstElement).size();
//						for(int i = 0; i<=numPend; i++){
					if(snapCollection.get(firstElement).size()>1 && mod.mm.get(firstElement).containsKey(secondElement)){
						attribute = snapCollection.get(firstElement).getLast();//.get(snapCollection.get(firstElement).size()-1);
						fulf = false;
						nr++;

						//if(nr>1){
						start5 = System.currentTimeMillis();
						mod.addObservation(firstElement, secondElement, myAttr, attribute, attIndex, 1, bucketWidth);
						stop5 = System.currentTimeMillis();
						time = time+stop5-start5;
						en++;
						nr=1;	
						//}
						snapCollection.get(firstElement).removeLast();//.remove(snapCollection.get(firstElement).size()-1);	
					}
//						}
//					}						
				}
			}
		}

		if(activityLabelsRespondedExistence.size()>10)
			activityLabelsRespondedExistence.removeFirst();
		
		mod.clean();
		//System.out.println(en);
		//System.out.println("ReEx:\ttprocess:\t"+(System.currentTimeMillis()-start)+"\ttaddObs:\t"+time+"\tnumEv:\t"+en);
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
		//System.out.println("RespExist");
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
		return activityLabelsRespondedExistence.size() +
				activityLabelsCounterRespondedExistence.getSize() +
				pendingConstraintsPerTraceRe.getSize();
	}

}
