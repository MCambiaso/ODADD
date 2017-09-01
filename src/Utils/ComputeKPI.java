package Utils;

//import java.util.HashMap;

public class ComputeKPI {

	public double[] ComputeKpi(String HTOut) {
		String firstNum[]=null, secondNum[]=null, Num[]=null;

		double ratioFul = 0.0, activation = 0.0;
		double ful=0.0, viol=0.0, sumFul = 0.0, sumViol = 0.0;
		double[] out = new double[2];
//		int n = 0;

		String subString[] = HTOut.split("\\n");
		for(String search : subString){
			if(search.contains("Leaf") && search.contains("FULFILLMENT")){
				Num = search.substring(search.lastIndexOf("{")+1, search.lastIndexOf("}")).split("\\|");
				
				firstNum = Num[0].split(",");
				secondNum = Num[1].split(",");

				if(firstNum.length == 2){
					ful = Double.parseDouble(firstNum[0])+(Double.parseDouble(firstNum[1])*.001);
				}else{
					ful = Double.parseDouble(firstNum[0]);
				}

				if(secondNum.length == 2){
					viol = Double.parseDouble(secondNum[0])+(Double.parseDouble(secondNum[1])*.001);
				}else{
					viol = Double.parseDouble(secondNum[0]);
				}

				sumFul = sumFul+ful;
				sumViol = sumViol+viol;
				
//				activation = ful+viol;
//
//				if(viol == 0.0){
//					ratio = ful/1;
//				}else if(ful == 0.0){
//					ratio = ful;
//				}else{
//					ratio = ful/viol;
//				}
//				
//				sum = sum + activation;

//				System.out.println(activation+", "+ratio);
//				out.put(activation, ratio);
			}
		}		
		
		activation = sumFul+sumViol;
		ratioFul = sumFul/activation;
		out[0] = activation;
		out[1] = ratioFul;
//		System.out.println(sum);		
		
		return out;
	}

}
