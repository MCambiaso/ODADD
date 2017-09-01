package Constraints;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

//import org.processmining.framework.util.Pair;

import moa.classifiers.trees.HoeffdingTree;
//import prompt.onlinedeclare.utils.DeclareModel;
import Utils.Pair;
//import moa.recommender.rc.utils.Pair;

public class Model extends HashMap<String, HashMap<String, Pair<Integer, Integer> > > {

	public HashMap<String, HashMap<String, Integer>> AtoB = new HashMap<String, HashMap<String, Integer>>(); 
	public HashMap<String, HoeffdingTree> hoeffCollection = new HashMap<String, HoeffdingTree>();
	public HashMap<String, HashMap<String, HashMap<Integer, HoeffdingTree>>> mm = new HashMap<String, HashMap<String, HashMap<Integer, HoeffdingTree>>>();

	private int size = 0;
	public int numberOfRules = 0;
	
	File file = new File("/home/matte/Scrivania/LossyTime.txt");
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

	/**
	 * 
	 * @param caseId
	 * @param currentBucket
	 * @param inCaseOfNull
	 */
	public void addObservation(String second, String first, Integer currentBucket, Integer bucketWidth, Boolean fulf) {
		long start = System.currentTimeMillis();
		if (containsKey(second)) {
			//Triple<HashMap<String, Integer>, Integer, Integer> v = get(second);
			//put(second, new HashMap<String, Pair<Integer, Integer> >);
		} else {
			put(second, new HashMap<String, Pair<Integer, Integer>>());
			size++;
		}
		putItem(second, first, currentBucket, fulf);
		numberOfRules++;
		if(numberOfRules % 100 == 0){
			printout.println(hoeffCollection.size());
			System.out.println("LossyEV:\t"+hoeffCollection.size());
			printout.flush();
		}
		
		if(numberOfRules % bucketWidth == 0)
			cleanup(currentBucket);
		
		long stop = System.currentTimeMillis();
		//System.out.println("CAncellazione cancellazione:"+(stop-start));
	}

	/**
	 * 
	 * @param caseId
	 * @param currentBucket
	 * @param inCaseOfNull
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void addObservation1(String second, String first, Integer currentBucket, Integer bucketWidth, Boolean fulf) throws InstantiationException, IllegalAccessException {
		addObservation(second, first, currentBucket, bucketWidth, fulf);
	}

	/**
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket) {int aa = 0;
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String key = it.next();
			HashMap<String, Pair<Integer, Integer> > v = get(key);
			/**
			HashMap<String, Tuple<Integer, Integer> v = get(key);
			
			for (Iterator<String> it_internal = v.first.keySet().iterator(); it_internal.hasNext();) {
				String key_internal = it_internal.next();
				Tuple<Integer, Integer> v_internal = v.first.get(key);
				Integer age_internal= v_internal.getFirst() + v_internal.getSecond();
				
			*/
				
		for (Iterator<String> itInternal = v.keySet().iterator(); itInternal.hasNext();){ 
			String keyInternal = itInternal.next();	
			Pair<Integer, Integer> vInternal = v.get(keyInternal);
			
		
			Integer age = vInternal.getElement0() + vInternal.getElement1();
			if (age <= currentBucket) {
				itInternal.remove();
//				if(AtoB.containsKey(keyInternal)){
						hoeffCollection.remove(keyInternal+"%"+key);
						aa++;
				//la riga seguente va fatta solo se non hai più hoeffdingtree, cioe size(hoeffcollection)==0
//				}
//				AtoB.remove(key);
				
				
				//probabilemnte
			}
		}
		size--;
		//dopo il for di riga 70
		put(key, v);
		}
		System.out.println("Cancellazioni"+aa);
	}

	/**
	 * 
	 * @param caseId
	 * @return
	 */
	public HashMap<String, Pair<Integer, Integer>> getItem(String caseId) {
		return get(caseId);
	}

	/**
	 * 
	 * @param caseId
	 * @param item
	 * @param currentBucket
	 */
	public void putItem(String second, String first, Integer currentBucket, Boolean fulf) {
		HashMap<String, Pair<Integer, Integer> > v = get(second);
		if (v.containsKey(first)){
			//aggiorna il primo Integer di v[first] (frequenza++)
			int el0 = v.get(first).getElement0();
			if(fulf){
				el0++;
			}
			int el1 = v.get(first).getElement1();
			v.remove(first);
			v.put(first, new Pair(el0, el1));
		}
		else{
			//aggiungi una nuova riga, first, Pair(1,currentBucket)
			v.put(first, new Pair(1, currentBucket));
		}
		put(second, v);
		//			put(caseId, new Triple<HashMap<String, HashMap<String, Integer>>, Integer, Integer>(trace, 1, currentBucket - 1));
	}

	/**
	 * 
	 * @return
	 */
	//		public Integer getSize() {
	//			return size;
	//		}
	public Integer getSize() {
		int tmp = super.size();
		if (tmp > 0) {
//			for (HashMap<String, Pair<Integer, Integer> > i : values()) {
//				HashMap<String, Integer> item = i.getFirst();
//				if (item instanceof Collection<?>) {
//					tmp += ((Collection<?>) item).size();
//				}
//			}
		}
		return tmp;
	}


}



