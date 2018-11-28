package Constraints;

import java.util.*;
import java.util.Map.Entry;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.SparseInstance;

//import edu.uci.ics.jung.graph.util.Pair;
//import ee.ut.branchminer.Pair;
//import ee.ut.libs.Pair;
import Utils.*;
import moa.classifiers.trees.HoeffdingTree;

public class LossyModel {

	private HashMap<String, Instances> instanceForTree = new HashMap<String, Instances>();
	private ArrayList<Attribute> myAttr = new ArrayList<Attribute>(150);
	private HashMap<String, Object> attribute;
	private HashMap<String, Integer> attIndex = new HashMap<String, Integer>();
	private Attribute[] attr ;
	private double[] attValue;
	private int[] indValue;
	public HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> mm;
	int observation = 0;
	int bucket = 500, er=0;
	private Instances in;
	private InstancesHeader ih;
	private HashMap<String, Instances> instsList = new HashMap<String, Instances>();
	public HashMap<String, int[]> value = new HashMap<String, int[]>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>> addObservation(String eventA, String eventB, ArrayList<Attribute> Attr, HashMap<String, Object> attr, HashMap<String, Integer> attInd, int classif, int bucketWidth){
		long start = System.currentTimeMillis();
		
		myAttr = Attr;
		attribute = attr;
		attIndex = attInd;
		bucket = bucketWidth;
		
		//System.out.print(eventA+"-"+eventB);
		
		SparseInstance ins;
		//Instance ins = createInstance(eventA+"-"+eventB, classif);
		if(instsList.keySet().contains(eventA+"-"+eventB)){			
			ins = createSparseInstance(eventA+"-"+eventB, classif, instsList.get(eventA+"-"+eventB));
		}else{
			in.setRelationName(eventA+"-"+eventB);
			in.setClassIndex(0);
			ih = new InstancesHeader(in);
			ins = createSparseInstance(eventA+"-"+eventB, classif, in);
			instsList.put(eventA+"-"+eventB, in);
		}
				
		if(mm.containsKey(eventA)){
			if(mm.get(eventA).containsKey(eventB)){
				HoeffdingTree tHF = mm.get(eventA).get(eventB).getElement1();
//				try{				
//				int a = ins.numAttributes();
//				tHF.setModelContext(new InstancesHeader(instsList.get(eventA+"-"+eventB)));
//				System.out.println(a);
				tHF.trainOnInstance(ins);
				
				if(classif==0){
					if(tHF.correctlyClassifies(ins)){
						value.get(eventA+"-"+eventB)[0]++;
					}else{
						value.get(eventA+"-"+eventB)[1]++;
					}
				}else{
					if(tHF.correctlyClassifies(ins)){
						value.get(eventA+"-"+eventB)[2]++;
					}else{
						value.get(eventA+"-"+eventB)[3]++;
					}
				}
				
//				}catch(Exception e){
//					System.out.println(e);
//					er++;
//					System.out.println(er);
//				}
			//System.out.println("Frequenza:\t"+mm.get(eventA).get(eventB).getElement0()+"\t\tNumero di regole associate ad "+eventA+":\t"+mm.get(eventA).size());
				Pair<Integer, HoeffdingTree> pair = new Pair(mm.get(eventA).get(eventB).getElement0()+1, tHF);//mm.get(eventA).get(eventB).getElement1());
				mm.get(eventA).remove(eventB);
				mm.get(eventA).put(eventB, pair);
			}else{
				mm.get(eventA).put(eventB, new Pair((int)(observation/bucket)+1,  HF(eventA, eventB, ins, classif)));
			}
		}else{
			HoeffdingTree rr = HF(eventA, eventB, ins, classif);
			Pair<Integer, HoeffdingTree> pair = new Pair((int)(observation/bucket)+1,  rr);
			HashMap<String, Pair<Integer, HoeffdingTree>> sec = new  HashMap<String, Pair<Integer, HoeffdingTree>>();
//			Instance ins = createInstance(eventA+"%"+eventB, classif);						
			sec.put(eventB, pair);
			mm.put(eventA, sec);
		}

		observation ++;	
		
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
					//value.get(A+"-"+it.)
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
	public HoeffdingTree HF(String A, String B, SparseInstance instance, int classif){
		long start = System.currentTimeMillis();
		HoeffdingTree hf = new HoeffdingTree();
				
		//Instances in = new Instances();
		//in.setAttributes(attr, indValue);
		//in.setRelationName(A+"-"+B);
		//InstancesHeader ih = new InstancesHeader(in);
		ih.setClassIndex(0);
		in.setClassIndex(0);
		
		//System.out.println(in.classAttribute());

		hf.prepareForUse();
		hf.setModelContext(ih);
		hf.leafpredictionOption.setChosenLabel("MC");
		hf.gracePeriodOption.setValue(2);
		hf.splitConfidenceOption.setValue(1.0E-4);
		//hf.maxByteSizeOption.setValue(10);// di default 32 MByte espressi in bit
		//hf.stopMemManagementOption.setValue(true);
		int[] val= new int[4];
		//try{
			hf.trainOnInstance(instance);
			if(classif==0){
				//Fulfillment
				if(hf.correctlyClassifies(instance)){
					val[0]++;
				}else{
					val[1]++;
				}
			}else{
				//Violation
				if(hf.correctlyClassifies(instance)){
					val[2]++;
				}else{
					val[3]++;
				}
			}
			value.put(A+"-"+B, val);
//		}catch(Exception e){
//			System.out.println(e);
//		}
		return hf;		
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
	
	public SparseInstance createSparseInstance(String name, int classAt, Instances instances){
		//Instances is = new Instances(); 
		//is.setAttributes(attr, indValue);
		//instances.setRelationName(name);
		//InstancesHeader ih = new InstancesHeader(is);
		//ih.setClassIndex(0);
		//is.setRangeOutputIndices();
		//is.setClassIndex(0);
		SparseInstance  sp = new SparseInstance(1, attValue, indValue, attr.length);
 		sp.setDataset(instances);
 		
		sp.setValue(0, classAt);
		//sp.setClassValue(0);
		int i = attr.length;
		//System.out.println(attr[0].name());
		while(i>1){
			i--;
			String nameAtt = attr[i].name().toString();
			//String str = attr[i].name();
			//String str1 = attribute.get(attr[i].name().toString()).toString();
			//System.out.println(attr[i].name());
			if(!attr[i].name().equals("time:timestamp") && !attr[i].name().contains("date") && !attr[i].name().contains("concept")){
				//sp.setValue(i, indexVal);
				if(attribute.containsKey(attr[i].name())){					
					String attribut = attribute.get(attr[i].name()).toString();
					//if(attribut.equals(nameAtt)){
					//System.out.println("ok");
						
						if(attr[i].name().contains("EventAttribute")){//Age
							double tt = Double.parseDouble(attribute.get(attr[i].name()).toString());
							//System.out.println(tt);
							//double tt = (double) attribute.get(attr[i].name().toString());
							sp.setValue(i, tt);
						}else{
							
							int indexVal = attr[i].indexOfValue(attribut);//!isNumeric(attribute.get(attr[i].name()).toString()) 
//							System.out.println("attribut=\t"+attribut+"\tindex=\t"+indexVal);
//							System.out.println("attr=\t"+attr[i].getAttributeValues());
							sp.setValue(i, indexVal);
						}
//					}else{
//						sp.setMissing(i);
//					}
				}else{
					sp.setMissing(i);
				}
			}
		}
//		System.out.println(sp.toString());
		return sp;
	}
		
	//********** Create new instance for the rule just discovered **********
	public Instance createInstance(String name, int classAt){

		Instances instse;
		if(!instanceForTree.containsKey(name)){
			instse = new Instances(name, myAttr, 100);
			instanceForTree.put(name, instse);
		}else{
			instse = instanceForTree.get(name);
		}
		//		double[] instanceValue = new double[myAttr.size()];
		int n=0;
		InstancesHeader ih = new InstancesHeader(instse);
		DenseInstance instance = new DenseInstance(64);// 64
		instance.setDataset(ih);
		for(Attribute attr : myAttr){	
			String attrName = attr.name();
			if(attrName.contains("class")){
				//					instanceValue[n] = classAt;
				instance.setValue(n, classAt);
			}else{
				if(attribute.containsKey(attrName)){
					//System.out.println(isNumeric(attribute.get(attrName).toString()));
					if(!isNumeric(attribute.get(attrName).toString()) || attrName.equals("Activity code") || attrName.equals("Specialism code") ){//
						//					instanceValue[n] = attIndex.get(attrName);
						
						instance.setValue(attr, attIndex.get(attrName));

					}else{
						double tt = (double) attribute.get(attrName);
						//					instanceValue[n] = tt; //(double) attribute.get(attrName);
						instance.setValue(n, tt);
						//System.out.println(isNumeric(attribute.get(attrName).toString()));
					}
				}

//				try{
//					double tt = (double) attribute.get(attrName);
//					instance.setValue(n, tt);
//					//System.out.println(tt);
//				}catch(Exception e) {
//					instance.setValue(n, 88.88);
//					//System.out.println(attribute.get(attrName));
//				}
			}
			n++;
		}

		instse.add(instance);
		//System.out.println(instse);
		//instanceForTree.remove(name);
		instanceForTree.get(name).add(instance);
		//instanceForTree.put(name, instse);

		return instance;
	}
	
		
	public void printModell(){}
		
	public LossyModel(Attribute[] allAttr, int[] indVal, double[] attVal) {
		attr = allAttr;
		attValue = attVal;
		indValue = indVal;
		mm = new HashMap<String, HashMap<String, Pair<Integer, HoeffdingTree>>>();
		in = new Instances();
		in.setAttributes(attr, indValue);
		in.setClassIndex(0);
		
	}

}
