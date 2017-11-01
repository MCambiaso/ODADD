/**
 * 
 */
package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.operationalsupport.xml.OSXMLConverter;

import Utils.Utils;
import LossyCounting.LCReplayer;

//import 
/**
 * @author matte
 *
 */
public class Entry {
	
	private static HashMap<String, ArrayList<String>> nominal = new HashMap<String, ArrayList<String>>();
	protected static OSXMLConverter converter = new OSXMLConverter();
	private static LCReplayer replayer = new LCReplayer();
	private static int bucketWidth=50;
	
	private static String path = "/home/matte/Scaricati/CompleteHospital"; ///
//	private static String path = "/home/matte/10x5x1000";
	
	
	public Entry() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		AttributeValue();
		System.out.println("Preprocessing Time:\t"+(System.currentTimeMillis()-start));
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		System.out.println(calendar.getTime());
		Run();
		System.out.println("Total Time:\t"+(System.currentTimeMillis()-start));
	}
	
	public static void Run() throws IOException {
		long start2 = System.currentTimeMillis();
		int ne=0;
		//Socket s = new Socket(address, port);

		InputStream in = new FileInputStream(new File(path));//"));// ./test/Log/logTest3 ////home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/Log/logTest2------------logTest3 hospitalStream logTest2 CompleteHospital Experiments/40x20x5000

//		status.setText("Miner started. Collecting events...");
//		status.setIcon(UIColors.loadingIcon);

//		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String str = "";
		long start = System.currentTimeMillis();
		long start1 = System.currentTimeMillis();

		while ((str = br.readLine()) != null) {
//			System.out.println(str);			

			XTrace t = (XTrace) converter.fromXML(str);
			XEvent event = t.get(0);
			String caseId = Utils.getCaseID(t);
//			String activity = Utils.getActivityName(event);
			ne++;
			int currentBucket = (int)((double)ne / (double)bucketWidth);
			long start3 = System.currentTimeMillis();
			replayer.addObservation(caseId, currentBucket);
			
			long start4 = System.currentTimeMillis();
			replayer.process(event, t, nominal, bucketWidth);
			
			if(ne%1000==0 && ne<10001){
				System.out.println("Time under 10000:\t"+(System.currentTimeMillis()-start2));
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTimeInMillis(System.currentTimeMillis());
//				System.out.println(calendar.getTime());
				System.out.println(ne);
			}
			
			if(ne%10000==0){
				System.out.println("Time:\t"+(System.currentTimeMillis()-start2));
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				System.out.println(calendar.getTime());
				System.out.println(ne);
			}
			//System.out.println(ne);
			
			long start5 = System.currentTimeMillis();
			// events cleanup
			if (ne % bucketWidth == 0) {
				replayer.cleanup(currentBucket);
			}
			//if(ne>50000)
				//System.out.println("CleanUP:\t"+(System.currentTimeMillis()-start5));//stampa le size di mm per tutte le a e per questa a la size di tutte la b
		}
		br.close();

		replayer.results();
		System.out.println("End!");
		//s.close();
	}
	
	public static void AttributeValue(){
		try {
			InputStream in = new FileInputStream(new File(path));// ./test/Log/logTest3 Scrivania/3activitiesResponse logTest3CompleteHospital
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			int i=0;
			String str = "";
			while ((str = br.readLine()) != null) {
				
				System.out.println(i);
				XTrace t = (XTrace) converter.fromXML(str);
				XEvent event = t.get(0);
				
				for(XAttribute attr : event.getAttributes().values()){
					if(nominal.containsKey(attr.getKey())){
						if(!nominal.get(attr.getKey()).contains(attr.toString()))
							nominal.get(attr.getKey()).add(attr.toString());

					}else{
						ArrayList<String> nl = new ArrayList<String>();
						nl.add(attr.toString());
						nominal.put(attr.getKey(), nl);

					}
				}
				for(XAttribute tAttr : t.getAttributes().values()){
					if(nominal.containsKey(tAttr.getKey())){
						if(!nominal.get(tAttr.getKey()).contains(tAttr.toString()))
							nominal.get(tAttr.getKey()).add(tAttr.toString());

					}else{
						ArrayList<String> nl = new ArrayList<String>();
						nl.add(tAttr.toString());
						nominal.put(tAttr.getKey(), nl);
//						System.out.println(tAttr.getKey());
					}
				}

//				replayer.savingNominalValue(event);
				i++;
			}
			
			for(String name : nominal.keySet()){
				nominal.get(name).add(0, "88.88");
			}
			
			br.close();
			//s.close();

		} catch (IOException e) {
			System.out.println("Caricamento degli atributi fallito"+e);
		}
	}
	
}
