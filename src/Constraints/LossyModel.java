package Constraints;

import java.util.*;
import java.util.Map.Entry;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;


//import edu.uci.ics.jung.graph.util.Pair;
//import ee.ut.branchminer.Pair;
//import ee.ut.libs.Pair;
import Utils.*;
import moa.classifiers.trees.HoeffdingTree;

public class LossyModel {

	private HashMap<String, Instances> instanceForTree = new HashMap<String, Instances>();
	private ArrayList<Attribute> myAttr = new ArrayList<Attribute>(20);
	private HashMap<String, Object> attribute;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	
	public HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mm;
	int observation = 0;
	int bucket;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> addObservation(String eventA, String eventB, ArrayList<Attribute> Attr, HashMap<String, Object> attr, HashMap<String, Integer> attInd, int classif, int bucketWidth){
		long start = System.currentTimeMillis();
		
		myAttr = Attr;
		attribute = attr;
		attIndex = attInd;
		bucket = bucketWidth;
		
		Instance ins = createInstance(eventA+"%"+eventB, classif);
		
		if(observation >39100)		
			System.out.println("Instance:\t"+( System.currentTimeMillis()-start));
		
		if(mm.containsKey(eventA)){
			if(mm.get(eventA).containsKey(eventB)){
				HoeffdingTree tHF = mm.get(eventA).get(eventB).getElement1();
				tHF.trainOnInstance(ins);
				//System.out.println("Frequenza:\t"+mm.get(eventA).get(eventB).getElement0()+"\t\tNumero di regole associate ad "+eventA+":\t"+mm.get(eventA).size());
				Pair<Integer, HoeffdingTree> pair = new Pair(mm.get(eventA).get(eventB).getElement0()+1, tHF);//mm.get(eventA).get(eventB).getElement1());
				mm.get(eventA).remove(eventB);
				mm.get(eventA).put(eventB, pair);
				if(observation >39100)		
					System.out.println("HF1:\t"+( System.currentTimeMillis()-start));
			}else{
				mm.get(eventA).put(eventB, new Pair((int)(observation/bucket)+1,  HF(eventA, eventB, ins)));
//				if(observation >39100)		
//					System.out.println("HF2:\t\t"+( System.currentTimeMillis()-start));
				//mm.get(eventA).get(eventB).createPair(mm.get(eventA).get(eventB).getElement0()+1, HF(eventA, eventB, createInstance(eventA+"%"+eventB, classif)));//controllare se ne mette più di uno
			}
		}else{
			HashMap<String, Pair<Integer, HoeffdingTree>> sec = new  HashMap<String, Pair<Integer, HoeffdingTree>>();
//			Instance ins = createInstance(eventA+"%"+eventB, classif);
			Pair<Integer, HoeffdingTree> pair = new Pair((int)(observation/bucket)+1,  HF(eventA, eventB, ins));			
			sec.put(eventB, pair);
			mm.put(eventA, sec);
			if(observation >39100)		
				System.out.println("HF:\t\t\t"+( System.currentTimeMillis()-start));
		}

		observation ++;	
		
		//if(observation%bucket==0) //chiamala in fondo ad ogni constraint 
			//clean();
		
//		if(observation >39100)		
//			System.out.println("Lossy:"+( System.currentTimeMillis()-start));
		
		return mm;
	}
	
	public void clean(){
		//long start = System.currentTimeMillis();
		//int canc=0, tot=0, size=0; 
		//Iterator<HashMap<String, Pair<Integer, HoeffdingTree>>> it = 
		for(String A: mm.keySet()){
			//size =  mm.get(A).size();
			//ArrayList<String> deleted = new ArrayList<String>();
			for(Iterator<Entry<String, Pair<Integer, HoeffdingTree>>> it = mm.get(A).entrySet().iterator(); it.hasNext();){
				if(it.next().getValue().getElement0()<=(observation/bucket)){//mm.get(A).get(B).getElement0()<=(observation/bucket)){
					it.remove();
					//deleted.add(B);
					//mm.get(A).remove(B);//if sulla dimensione di A   colleziono le b e le rimuovo dopo il for			
				//canc++;
				}
				
				//tot++;
			}
			//canc=mm.get(A).size();
//			for(String ev: deleted)
//				mm.get(A).remove(ev);
			//colleziono le b e le rimuovo dopo il for
			//if(size>4)
				//System.out.println(size+"-"+canc+"="+(size-canc));
		}
		
		//System.out.println("Tot:\t"+tot+"\tAfter:\t"+(tot-canc));
		//long stop = System.currentTimeMillis();
		//System.out.println("Clean:\t"+(stop-start));
	}
	
	//********** Create and feed the Hoeffding tree **********
	public HoeffdingTree HF(String A, String B, Instance instance){
		long start = System.currentTimeMillis();
		HoeffdingTree hf = new HoeffdingTree();
		
		Instances in = instanceForTree.get(A+"%"+B);
		if(observation >39100)		
			System.out.println("1:\t\t"+( System.currentTimeMillis()-start));
		InstancesHeader ih = new InstancesHeader(in);
		in.setClassIndex(0);

		hf.prepareForUse();

		hf.setModelContext(ih);
		hf.leafpredictionOption.setChosenLabel("MC");
		hf.gracePeriodOption.setValue(10);
		hf.splitConfidenceOption.setValue(1.0E-2);
		hf.maxByteSizeOption.setValue(10);
		
		if(observation >39100)		
			System.out.println("2:\t\t\t"+( System.currentTimeMillis()-start));
		
		hf.trainOnInstance(instance);
		if(observation >39100)		
			System.out.println("3:\t\t\t\t"+( System.currentTimeMillis()-start));
		return hf;
		
//		if(mm.containsKey(A)&&!mm.get(A).containsKey(B)){ //instanceForTree.get(name).numInstances()>=5 && 
//
//			Instances in = instanceForTree.get(A+"%"+B);
//			InstancesHeader ih = new InstancesHeader(in);
//			in.setClassIndex(0);
//
//			hf.prepareForUse();
//
//			hf.setModelContext(ih);
//			hf.leafpredictionOption.setChosenLabel("MC");
//			hf.gracePeriodOption.setValue(5);
//			hf.splitConfidenceOption.setValue(1.0E-2);
//
//			hf.trainOnInstance(instance);
//			return hf;
//
//		}else if(mm.containsKey(A)&&mm.get(A).containsKey(B)) { 
//			//if(modello.hoeffCollection.containsKey(name)){ //instanceForTree.get(name).numInstances()>numInstUpdate && 
//
//			//hoeffCollection.get(name).updateClassifier(instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)); //instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)
//			Instances in = instanceForTree.get(A+"%"+B);
//
//			in.setClassIndex(0);
////			System.out.println(mm.get(A).get(B).getElement1().trainingHasStarted());
//			if(mm.get(A).get(B).getElement1().trainingHasStarted())
//				mm.get(A).get(B).getElement1().trainOnInstance(instance);
//			
//			//hf = mm.get(A).get(B).getElement1();
//			//modello.hoeffCollection.get(name).updateClassifier(instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)); //instanceForTree.get(name).instance(instanceForTree.get(name).numInstances()-1)
//			instanceForTree.get(A+"%"+B).delete(instanceForTree.get(A+"%"+B).numInstances()-1);
//			//return hf;
//
//		}
//		return hf;
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
		
	//********** Create new instance for the rule just discovered **********
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
		DenseInstance instance = new DenseInstance(66);// 64
		instance.setDataset(ih);
		for(Attribute attr : myAttr){	
			String attrName = attr.name();
			if(attrName.contains("class")){
				//					instanceValue[n] = classAt;
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
		//System.out.println(instse);
		instanceForTree.remove(name);
		instanceForTree.put(name, instse);

		return instance;
	}
	
		
	public void printModell(){}
		
	public LossyModel() {
		mm = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
	}

}
