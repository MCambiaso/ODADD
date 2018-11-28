/**
 * 
 */
package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.operationalsupport.xml.OSXMLConverter;

import com.yahoo.labs.samoa.instances.Attribute;

import Utils.Utils;
import LossyCounting.LCReplayer;
import Utils.ComputeKPI;

/**
 * @author matte
 *
 */
public class Entry {
	
	static PrintWriter log;
	
	private static HashMap<String, ArrayList<String>> nominal = new HashMap<String, ArrayList<String>>();
	protected static OSXMLConverter converter = new OSXMLConverter();
	private static LCReplayer replayer = new LCReplayer();
	private static int bucketWidth=100000;//500
	
	static Attribute[] attlist;			
	static int[] indVal;
	static double[] attVal;
	
//	private static String path = "/home/matte/Scaricati/CompleteHospital"; ///
//	private static String path = "/home/matte/Scaricati/HL"; ///
//	private static String path = "/home/matte/Scaricati/40x20x5000";
//	private static String path = "/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/Log/logTest3";
//	private static String path = "/home/matte/Scaricati/EPL2"; 
//	private static String path = "/home/matte/Scaricati/PRECN";
//	private static String path = "/home/matte/Scaricati/RESP";
	private static String path = "/home/matte/Scaricati/L1";
	
	
	public Entry() {
		
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/Log.txt");
		FileWriter fw = null;
		BufferedWriter brf;
		{			
		try {
			fw = new FileWriter(file);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		brf = new BufferedWriter(fw);
		log = new PrintWriter(brf);}
		
		long start = System.currentTimeMillis();
		
		System.out.println("Start preprocessing");
		log.println("Start preprocessing");
		
		AttributeValue();
		
		System.out.println("\nPreprocessing Time:\t"+(System.currentTimeMillis()-start));
		System.out.println("End preprocessing");
		log.println("\nPreprocessing Time:\t"+(System.currentTimeMillis()-start));
		log.println("End preprocessing");
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		System.out.print("\nStart discovery\t-\t");
		System.out.println(calendar.getTime());	
		log.print("\nStart discovery\t-\t");
		log.println(calendar.getTime());
		
		Run();
		
		System.out.println("\nTotal Time:\t"+(System.currentTimeMillis()-start));
		System.out.print("End discovery\t-\t");
		System.out.println(calendar.getTime());
		log.println("\nTotal Time:\t"+(System.currentTimeMillis()-start));
		log.print("End discovery\t-\t");
		log.println(calendar.getTime());
		
		System.out.println("\nCompute KPI");		
		log.println("\nCompute KPI");
		
		//ComputeKPI.ComputeKpi();
		
		System.out.println("Fine");
		log.println("Fine");
	}
	
	public static void Run() throws IOException {
		long start = System.currentTimeMillis();
		int ne=0;
		//Socket s = new Socket(address, port);

		InputStream in = new FileInputStream(new File(path));//"));// ./test/Log/logTest3 ////home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/Log/logTest2------------logTest3 hospitalStream logTest2 CompleteHospital Experiments/40x20x5000

//		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String str = "";
		
		replayer.setAttribute(attlist, indVal, attVal);

		while ((str = br.readLine()) != null) {
			XTrace t = (XTrace) converter.fromXML(str);
			XEvent event = t.get(0);
			String caseId = Utils.getCaseID(t);
			//String caseId = "casa";
//			String activity = Utils.getActivityName(event);
			ne++;
			int currentBucket = (int)((double)ne / (double)bucketWidth);
			
			replayer.addObservation(caseId, currentBucket);
						
			replayer.process(event, t, nominal, bucketWidth);
			
			//System.out.println(ne);
			
			if(ne%100==0){
				//System.out.println(ne);
				System.out.print("\n"+ne+"\t--Time:\t"+(System.currentTimeMillis()-start));
				//log.println(ne);
				log.print("\n"+ne+"\t--Time:\t"+(System.currentTimeMillis()-start));
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				System.out.print("\t--"+calendar.getTime());	
				log.print("\t--"+calendar.getTime());	
			}

			// events cleanup
			if (ne % bucketWidth == 0) {
				//replayer.cleanup(currentBucket);
			}
		}
		br.close();

		replayer.results();

		//System.out.println("End!");
		//s.close();
	}
	
	public static void AttributeValue(){
		try {
			InputStream in = new FileInputStream(new File(path));// ./test/Log/logTest3 Scrivania/3activitiesResponse logTest3CompleteHospital
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			int i=0;
			String str = "";
			while ((str = br.readLine()) != null) {
				
				if(i%100==0){
					System.out.print(i/100+"  ");
				}
				
				XTrace t = (XTrace) converter.fromXML(str);
				XEvent event = t.get(0);
				
				for(XAttribute attr : event.getAttributes().values()){//500*19.74
					if(!attr.getKey().contains("concept") && !attr.getKey().contains("stream:lifecycle") && !attr.getKey().contains("time:timestamp")){
						//System.out.println(attr.getKey());
						if(nominal.containsKey(attr.getKey())){
							if(!nominal.get(attr.getKey()).contains(attr.toString()))
								nominal.get(attr.getKey()).add(attr.toString());
						}else{
							ArrayList<String> nl = new ArrayList<String>();
							nl.add(attr.toString());
							nominal.put(attr.getKey(), nl);
						}
					}
				}
				
				for(XAttribute tAttr : t.getAttributes().values()){
					if(!tAttr.getKey().contains("concept") && !tAttr.getKey().contains("stream:lifecycle") && !tAttr.getKey().contains("time:timestamp")){
						//System.out.println(tAttr.getKey());
						if(nominal.containsKey(tAttr.getKey())){
							if(!nominal.get(tAttr.getKey()).contains(tAttr.toString()))
								nominal.get(tAttr.getKey()).add(tAttr.toString());
						}else{
							ArrayList<String> nl = new ArrayList<String>();
							nl.add(tAttr.toString());
							nominal.put(tAttr.getKey(), nl);
						}
					}
				}

//				replayer.savingNominalValue(event);
				i++;
			}
			//System.out.println(nominal.size()+2);
			attlist = new Attribute[nominal.size()+1];
			indVal = new int[nominal.size()+1];
			attVal = new double[nominal.size()+1];
			List<String> atli0 = new ArrayList<String>();;//("Elaine", "Guybrush", "Murray", "LeChuck", "Stan", "HT");
			atli0.add("FULFILLMENT");
			atli0.add("VIOLATION"); 
			Attribute attrs0 = new Attribute("cl", atli0);
			attlist[0] = attrs0;
			indVal[0] = 0;
			attVal[0] = 1.0;
			
			int j = 1;
			for(String name: nominal.keySet()){
				//if(!name.equals("lifecycle:transition")&&!name.equals("time:timestamp")&&!name.equals("stream:lifecycle:trace-transition")&&!name.equals("concept:name")&&!name.equals("org:group")){
					List<String> atli = nominal.get(name);
					Attribute attrs;//controllo tutti i possibili attributi se sono solo numeri ok numeric 
					if(name.contains("Age") || name.contains("data") || name.contains("EventAttribute")){
						attrs = new Attribute(name);
						//System.out.println(name);
					}else{
						attrs = new Attribute(name, atli);
					}
					
					attlist[j] = attrs;
					indVal[j] = j;
					attVal[j] = 1.0;
					//System.out.println(attrs.name()+"++++"+attrs.getAttributeValues());
					j++;
				//}
			}			
			
			br.close();
			//s.close();

		} catch (IOException e) {
			System.out.println("Caricamento degli atributi fallito"+e);
		}
	}	
}
