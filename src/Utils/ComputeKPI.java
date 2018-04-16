package Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//import java.util.HashMap;

public class ComputeKPI {

	public static void ComputeKpi(){

		String[] name = {"OutAltPrecedence", "OutAltResponse", "OutChPrecedence", "OutChResponse", "OutPrecedence", "OutRespondedExistence", "OutResponse"};
		int i = 0;

		for(i = 0; i < 7; i++){
			String FILENAME = "/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/"+name[i]+".txt";///home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/Results
			File file = new File("/home/matte/workspace/OnlineDataAwareDeclareDiscovery/test/SynteticResults/KPI_"+name[i]+".csv");
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

			BufferedReader br = null;
			FileReader fr = null;

			boolean min = false, split=false;
			String l1=null, l2=null, l3=null, nameRule=null;
			List<String> attr= null;
			double a =.0, f=.0, v=.0, ff=.0, fv=.0;
			double cf =.0, uf=.0, cv=.0, uv=.0;

			try {

				fr = new FileReader(FILENAME);
				br = new BufferedReader(fr);

				String sCurrentLine;

				//printout.println("***\na --> activation\nf --> fulfillment\nv --> violation\nfa --> activation only for fulfillment\nff --> fulfillment only for fulfillment\nfv --> violation only for fulfillment\n***\n");
				//printout.println("Rule;Activation;Correct fulfillment;Uncorrect fulfillment;Correct Violation;Uncorrect Violation;Fulfillment Ratio;Fulfillment No Data;Violation No Data;Fulfillment ratio No Data");
//				printout.println("Rule;Fulfillment Ratio;Fulfillment ratio No Data");
				
				while ((sCurrentLine = br.readLine()) != null) {						
					if(sCurrentLine.contains("@@@@@@@@@@@@@@@@@@@@@@@@")){
						nameRule = br.readLine();
					}
					
					if(sCurrentLine.contains("active learning leaves")){
						String activeLeaves = sCurrentLine.substring(sCurrentLine.indexOf('=')+2);
						if(formatting(activeLeaves)>1)
							split=true;
						else
							split=false;
					}
					
					if(sCurrentLine.contains("model training instances")){					
						l1 = sCurrentLine.substring(27);
						if(f!=.0 || v!=.0){
//							printout.println("Tutti");
//							printout.println("f = "+d2+"\tv = "+v);
//							printout.println("fratio = "+d2/d1);
							if(split){
//								printout.println("\nRule: "+nameRule+"\n");
								//printout.println("\nSplit Attribute: "+attr+"\n");
								if(a<(f+v))
									min=true;
								printout.println("----------------");
								printout.println("\nRule: "+nameRule+"\n");
								printout.println("Activations = "+a);
								printout.println("\nCorrect fulfillment = "+cf+"\nUncorrect Fulfillment = "+uf+"\nCorrect Violation = "+cv+"\nUncorrect Violation = "+uv);
								printout.println("Fulfillment Ratio = "+(cf/(cf+uf)));
								printout.println("\nSenza dati");
								printout.println("Fulfillment = "+(cf+uf)+"\nViolation = "+(cv+uv));
								printout.println("Fulfillment ratio = "+((cf+uv)/a));
								//printout.println(nameRule+";"+a+";"+cf+";"+uf+";"+cv+";"+uv+";"+(cf/(cf+uf))+";"+(cf+uf)+";"+(cv+uv)+";"+((cf+uv)/a));
//								printout.println(nameRule+";"+(cf/(cf+uf))+";"+((cf+uv)/a));
								//printout.println("a = "+a+"\tf = "+f+"\tv = "+v+"\tf/a = "+(f/a));
								//printout.println("fa = "+(ff+fv)+"\tff = "+ff+"\tfv = "+fv+"\tff/fa = "+(ff/(ff+fv))+"\tff/a = "+(ff/a));
//								printout.println("Activ a:\t"+a);
//								printout.println("Activ f+v:\t"+(f+v));
							}
						}
						if(l1.contains(".")){
							a = Double.parseDouble(l1.replace(".", ""));
						}else{
							a = Double.parseDouble(l1);
						}
						//printout.println("Activ:\t"+d1);

						f=.0;
						v=.0;
						ff=.0;
						fv=.0;
						cf=.0;
						cv=.0;
						uf=.0;
						uv=.0;
						attr =new ArrayList<String>();
					}else if(sCurrentLine.contains("Leaf [class")){ //aggiungere qui il controllo solo sui fulfillment
						if(sCurrentLine.contains("{") && sCurrentLine.contains("|") && sCurrentLine.contains("}")){
							l2 = sCurrentLine.substring(sCurrentLine.indexOf('{')+1, sCurrentLine.indexOf('|'));
							l3 = sCurrentLine.substring(sCurrentLine.indexOf('|')+1, sCurrentLine.indexOf('}'));
							f += formatting(l2); //fulfillment
							v += formatting(l3); //violation
						}else{
							l2 = sCurrentLine.substring(sCurrentLine.indexOf('{')+1, sCurrentLine.indexOf('}'));
							if(sCurrentLine.contains("FULFILLMENT")){
								f += formatting(l2); //fulfillment
							}else{
								v += formatting(l2); //violation
							}
						}
						
						if(sCurrentLine.contains("FULFILLMENT")){
							if(sCurrentLine.contains("{") && sCurrentLine.contains("|") && sCurrentLine.contains("}")){
								l2 = sCurrentLine.substring(sCurrentLine.indexOf('{')+1, sCurrentLine.indexOf('|'));
								l3 = sCurrentLine.substring(sCurrentLine.indexOf('|')+1, sCurrentLine.indexOf('}'));
								ff += formatting(l2); //only fulfillment
								fv += formatting(l3); //only violation
							}else{
								l2 = sCurrentLine.substring(sCurrentLine.indexOf('{')+1, sCurrentLine.indexOf('}'));
								ff += formatting(l2); //only fulfillment
							}
						}
					}	
					
					if(sCurrentLine.contains("Correct Fulfillment")){
						String cfs = sCurrentLine.substring(sCurrentLine.indexOf('=')+2);
						cf = cf+formatting(cfs);
					}else if(sCurrentLine.contains("Uncorrect Fulfillment")){
						String 	ufs = sCurrentLine.substring(sCurrentLine.indexOf('=')+2);
						uf = uf+formatting(ufs);
					}else if(sCurrentLine.contains("Correct Violation")){
						String cvs = sCurrentLine.substring(sCurrentLine.indexOf('=')+2);
						cv = cv+formatting(cvs);
					}else if(sCurrentLine.contains("Uncorrect Violation")){
						String uvs = sCurrentLine.substring(sCurrentLine.indexOf('=')+2);
						uv = uv+formatting(uvs);
					}
					
//					Correct Fulfillment = 2
//							Uncorrect Fulfillment = 0
//							Correct Violation = 0
//							Uncorrect Violation = 2
					
					if(sCurrentLine.contains("if [")){
						String attrName = sCurrentLine.substring(sCurrentLine.indexOf('[')+1, sCurrentLine.indexOf(']'));
						String attrVal = sCurrentLine.substring(sCurrentLine.indexOf(']')+1);
						attr.add(attrName+" "+attrVal);
					}
				}
				
				
				//printout.println("f = "+d2+"\tv = "+v);

				printout.flush();
				printout.close();

			} catch (IOException e) {
				System.out.println("First error");

				e.printStackTrace();

			} finally {

				try {

					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {
					System.out.println("Second error");
					ex.printStackTrace();

				}
			}
			System.out.println(min);
		}

		System.out.println("Fine");
	}
	
	public static double formatting(String ll){
		double d=.0;
		if(ll.contains(".")){
			if(ll.contains(",")){
				ll = ll.replace(".", "");
				ll = ll.replace(",", ".");
			}else{
				ll = ll.replace(".", "");
			}
			d += Double.parseDouble(ll);
		}else if(ll.contains(",")){
			d += Double.parseDouble(ll.replace(",", "."));
		}else{
			d += Double.parseDouble(ll);
		}
		return d;		
	}

}
