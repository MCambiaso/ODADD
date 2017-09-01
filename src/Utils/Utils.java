package Utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
//import org.processmining.framework.util.Pair;
//import org.processmining.log.csvimport.handler.XESConversionHandlerImpl;

/**
 * A collection of user interface utilities
 * 
 * @author Andrea Burattin
 */
public class Utils {
	
	public static XFactory xesFactory = new XFactoryBufferedImpl();
	
	/**
	 * This method tells whether the given event is the tagged as the first
	 * event of a stream
	 * 
	 * @param e an event
	 * @return <tt>true</tt> if the event is tagged as the first event of a
	 * trace, <tt>false</tt> otherwise
	 */
	public static boolean isTraceStart(XEvent e) {
		if (e.getAttributes().containsKey("stream:lifecycle:trace-transition")) {
			return ((XAttributeLiteral) e.getAttributes().get("stream:lifecycle:trace-transition")).getValue().equals("start");
		}
		return false;
	}
	
	/**
	 * This method tells whether the given event is the tagged as the last
	 * event of a stream
	 * 
	 * @param e an event
	 * @return <tt>true</tt> if the event is tagged as the last event of a
	 * trace, <tt>false</tt> otherwise
	 */
	public static boolean isTraceComplete(XEvent e) {
		if (e.getAttributes().containsKey("stream:lifecycle:trace-transition")) {
			return ((XAttributeLiteral) e.getAttributes().get("stream:lifecycle:trace-transition")).getValue().equals("complete");
		}
		return false;
	}
	
	/**
	 * This method converts a time duration, expressed in milliseconds, into a
	 * human readable version (e.g. "5 seconds", "6,7 hours", "12 days").
	 * 
	 * @param duration the time duration, expressed in milliseconds
	 * @return a string representation of the duration
	 */
	public static String fromMillisecToStringDuration(long duration) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		if (duration < 0) {
			return "Error";
		} else if (duration == 0) {
			return "0 milliseconds";
		} else if (duration < 100) {
			return duration + " milliseconds";
		} else if (duration < 60000) {			
			return twoDForm.format(duration/1000.0) + " seconds";
		} else if (duration < 3600000) {
			return twoDForm.format(duration/60000.0) + " minutes";
		} else if (duration < 216000000) {
			return twoDForm.format(duration/3600000.0) + " hours";
		} else {
			double hours = duration/3600000.0; // really necessary? we are getting out of int
			return twoDForm.format(hours/24.0) + " days";
		}
	}
	
	/**
	 * This method check if, given a value, it's inside the interval defined by
	 * its left and right area
	 * 
	 * @param value the value to be checked
	 * @param left the leftmost bound
	 * @param right the rightmost bound
	 * @return true if the given value is inside the interval
	 */
	public static boolean inRange(int value, int left, int right) {
		return value >= left && value <= right;
	}
	
	/**
	 * This method check if, given a point, it's inside the given rectangle
	 * 
	 * @param p the point to be checked
	 * @param r the rectangle bound
	 * @return true if the given value is inside the interval
	 */
	public static boolean inRectangle(Point p, Rectangle r) {
		return inRange(p.x, r.x, r.x + r.width) && inRange(p.y, r.y, r.y + r.height);
	}
	
	/**
	 * This method, given a trace, returns its case id
	 * 
	 * @param t the given trace
	 * @return the case id associated with the trace
	 */
	public static String getCaseID(XTrace t) {
		return t.getAttributes().get("concept:name").toString();
	}
	
	/**
	 * This method, given an event, returns the name of the activity
	 * 
	 * @param e the log event
	 * @return the name of the activity of the event
	 */
	public static String getActivityName(XEvent e) {
		return XConceptExtension.instance().extractName(e) + "-" + XLifecycleExtension.instance().extractTransition(e);
	}
	
	/**
	 * Returns the harmonic mean of the given list of values
	 * 
	 * @see org.processmining.plugins.joosbuijs.blockminer.genetic.TreeEvaluator
	 * @author jbuijs
	 * @param values the list of values
	 * @return the harmonic mean
	 */
	public static double harmonicMean(List<Double> values) {
		//A list of 1 returns 1.0
		if (values.size() == 1) {
			return values.get(0);
		}
		double sum = 0;
		double product = 1;
		for (Double dub : values) {
			sum += dub;
			product *= dub;
		}

		double numerator = values.size() * product;
		double denominator = sum;
		if (denominator == 0)
			return 0;
		return numerator / denominator;
	}
	
	/**
	 * This method rounds the provided value to the provided number of digits.
	 * 
	 * @param value
	 * @param digit
	 * @return
	 */
	public static double round(double value, int digit) {
		double multiplier = Math.pow(10, digit);
		return Math.round(value*multiplier)/multiplier;
	}
	
	/**
	 * This method parses a string (typically given as argument on the command
	 * line) like "(a,b);(c,d);(e,f)    ; ( ggg  ,  hhh)" and generates an array
	 * list of pairs of string
	 * 
	 * @param argument
	 * @return
	 */
	public static ArrayList<Pair<String, String>> parseString(String argument) {
		ArrayList<Pair<String, String>> toReturn = new ArrayList<Pair<String, String>>();
		
		Pattern p1 = Pattern.compile("\\s*;\\s*");
		Pattern p2 = Pattern.compile("\\(\\s*(\\w*)\\s*,\\s*(\\w*)\\s*\\)");
		
		for(String s : p1.split(argument)) {
			Matcher m = p2.matcher(s);
			if (m.matches()) {
				toReturn.add(new Pair<String, String>(m.group(1), m.group(2)));
			}
		}
		
		return toReturn;
	}
	
	/**
	 * 
	 * @param eventA
	 * @param eventB
	 * @return
	 */
	public static Pair<String, String> fromEventsToActs(XEvent eventA, XEvent eventB) {
		Pair<String, String> activitiesPair;
		
		String nameA = Utils.getActivityName(eventA);
		String nameB = Utils.getActivityName(eventB);
		
		// get lexicographic order
		if (nameA.compareTo(nameB) < 0) {
			activitiesPair = new Pair<String, String>(nameA, nameB);
		} else {
			activitiesPair = new Pair<String, String>(nameB, nameA);
		}
		
		return activitiesPair;
	}
	
	/**
	 * 
	 * @param events
	 * @return
	 */
//	public static Pair<String, String> fromEventsToActs(Pair<XEvent, XEvent> events) {
//		return fromEventsToActs(events.getFirst(), events.getSecond());
//	}
	
	public static void l(String message) {
		System.out.println(/*new Date().getTime() + " -- " +*/ message);
	}
	
	/**
	 * 
	 * @param originalLog
	 * @param logStartsEnd
	 * @return
	 */
	public static XLog traceSet2Log(ArrayBlockingQueue<XTrace> originalLog) {
		
		HashMap<String, XTrace> tempLog = new HashMap<String, XTrace>();
		for (XTrace t : originalLog) {
			String caseId = t.getAttributes().get("concept:name").toString();
			if (tempLog.keySet().contains(caseId)) {
				tempLog.get(caseId).add(t.get(0));// fa il record di un solo evento tra tutti quelli con lo stesso caseId
			} else {
				tempLog.put(caseId, (XTrace) t.clone());
			}
		}
//		System.out.println("tempLog size "+tempLog.size());
		XLog log = xesFactory.createLog();
	
		for (String s : tempLog.keySet()) {
			log.add(tempLog.get(s));
		}
		
		return log;
	}
	//MC
	public static XLog traceSet1Log(ArrayBlockingQueue<XTrace> originalLog) {
		
		HashMap<String, XTrace> tempLog = new HashMap<String, XTrace>();
		for (XTrace t : originalLog) {
			String caseId = t.getAttributes().get("concept:name").toString();
				tempLog.put(caseId, (XTrace) t.clone());
		}
//		System.out.println("tempLog size "+tempLog.size());
		XLog log = xesFactory.createLog();
		for(XTrace t2 : originalLog){
			log.add(t2);
		}
		
		return log;
	}//MC
}
