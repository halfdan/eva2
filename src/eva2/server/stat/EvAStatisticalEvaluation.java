package eva2.server.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.tools.ReflectPackage;
import eva2.tools.math.Mathematics;

/**
 * Do some statistical tests on a set of job results. Note that the plausibility (comparability of the
 * jobs) is not tested here.
 * 
 * @author mkron
 *
 */
public class EvAStatisticalEvaluation {	
	public static final boolean TRACE=false;
//	public static final String[] order = {"Mean" , "Median", "Variance", "Std. Deviation"};	
	
	public static EvAStatisticalEvaluationParams statsParams = new EvAStatisticalEvaluationParams(); 
	
//	public static void evaluate(EvAJob[] jobList, int[] selectedIndices) {
//		if (TRACE) System.out.println("Job list: " + BeanInspector.toString(jobList));
//		JTextoutputFrame textout = new JTextoutputFrame("Statistics");
//		textout.setShow(true);
//		ArrayList<EvAJob> jobsToWorkWith = new ArrayList<EvAJob>();
//		for (int i=0; i<jobList.length; i++) {
//			// remove jobs which are not finished or not selected
//			if (jobList[i]!=null && (Mathematics.contains(selectedIndices, i)) && (jobList[i].isFinishedAndComplete())) jobsToWorkWith.add(jobList[i]);
//		}
//		List<String> commonFields = getCommonFields(jobsToWorkWith);
//		if (commonFields!=null && !commonFields.isEmpty()) for (String field : commonFields) {
//			textout.println("Checking field " + field);
//
//			for (int i=0; i<jobsToWorkWith.size(); i++) {
//				textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName() + "\t");
//				for (int j=0; j<jobsToWorkWith.size(); j++) {
////					System.out.println("Comparing " + i + " with " + j);
//					textout.print("\t" + compare(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
//				}
//				textout.println("");
//			}
//		}
//	}
	
	public static void evaluate(InterfaceTextListener textout, EvAJob[] jobList, int[] selectedIndices,
			StatsOnSingleDataSetEnum[] singleStats,
			StatsOnTwoSampledDataEnum[] twoSampledStats ) {
		if (TRACE) System.out.println("Job list: " + BeanInspector.toString(jobList));
//		JTextoutputFrame textout = new JTextoutputFrame("Statistics");
//		textout.setShow(true);
		ArrayList<EvAJob> jobsToWorkWith = new ArrayList<EvAJob>();
		for (int i=0; i<jobList.length; i++) {
			// remove jobs which are not finished or not selected
			if (jobList[i]!=null && (Mathematics.contains(selectedIndices, i)) && (jobList[i].isFinishedAndComplete())) jobsToWorkWith.add(jobList[i]);
		}
		List<String> commonFields = getCommonFields(jobsToWorkWith);
		if (commonFields!=null && !commonFields.isEmpty()) for (String field : commonFields) {
			textout.println("###\t"+ field + " statistical evaluation");

			if(singleStats.length > 0){
				textout.println("one-sampled statistics");
				for (int j=-1; j<singleStats.length; j++) {
					if(j<0){
						textout.print("method");
					}else{
						textout.print("\t" + singleStats[j]);
					}
				}
				textout.println("");			
				for(int i=0; i<jobsToWorkWith.size(); i++){
					textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
					for(int j=0; j<singleStats.length; j++){
						switch(singleStats[j]){
						case mean: textout.print("\t" + calculateMean(field, jobsToWorkWith.get(i))); break;
						case median: textout.print("\t" + calculateMedian(field, jobsToWorkWith.get(i))); break;
						case variance: textout.print("\t" + calculateVariance(field, jobsToWorkWith.get(i))); break;
						case stdDev: textout.print("\t" + calculateStdDev(field, jobsToWorkWith.get(i))); break;
						default: textout.println("");
						}
					}
					textout.println("");
				}
			}
			if(twoSampledStats.length > 0){
				textout.println("two-sampled stats:");
				for(int i=0; i<twoSampledStats.length; i++){
					switch(twoSampledStats[i]){
					case tTestEqLenEqVar: 		textout.println(StatsOnTwoSampledDataEnum.getInfoStrings()[twoSampledStats[i].ordinal()]);
												writeTwoSampleFirstLine(textout, jobsToWorkWith);
												writeTTestEqSizeEqVar(textout, jobsToWorkWith, field);
												break;
					case tTestUneqLenEqVar: 	textout.println(StatsOnTwoSampledDataEnum.getInfoStrings()[twoSampledStats[i].ordinal()]);
												writeTwoSampleFirstLine(textout, jobsToWorkWith);
												writeUnEqSizeEqVar(textout, jobsToWorkWith, field);
												break;
					case tTestUneqLenUneqVar:	textout.println(StatsOnTwoSampledDataEnum.getInfoStrings()[twoSampledStats[i].ordinal()]);
												writeTwoSampleFirstLine(textout, jobsToWorkWith);
												writeTTestUnEqSizeUnEqVar(textout, jobsToWorkWith, field);
												break;
					case mannWhitney:			textout.println(StatsOnTwoSampledDataEnum.getInfoStrings()[twoSampledStats[i].ordinal()]);
												writeTwoSampleFirstLine(textout, jobsToWorkWith);
												writeMannWhitney(textout, jobsToWorkWith, field);
					default: 					textout.println("");
												break;
					}
					textout.println("");
				}
			}
		}
	}

	/**
	 * @param textout
	 * @param jobsToWorkWith
	 * @param field
	 */
	private static void writeTTestUnEqSizeUnEqVar(
			InterfaceTextListener textout, ArrayList<EvAJob> jobsToWorkWith,
			String field) {
		for (int i=0; i<jobsToWorkWith.size(); i++) {
			textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
			for (int j=0; j<jobsToWorkWith.size(); j++) {
				textout.print("\t" + calculateTTestUnEqSizeUnEqVar(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
			}
			textout.println("");
		}
	}

	/**
	 * @param textout
	 * @param jobsToWorkWith
	 * @param field
	 */
	private static void writeUnEqSizeEqVar(InterfaceTextListener textout,
			ArrayList<EvAJob> jobsToWorkWith, String field) {
		for (int i=0; i<jobsToWorkWith.size(); i++) {
			textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
			for (int j=0; j<jobsToWorkWith.size(); j++) {
				textout.print("\t" + calculateTTestUnEqSizeEqVar(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
			}
			textout.println("");
		}
	}

	/**
	 * @param textout
	 * @param jobsToWorkWith
	 * @param field
	 */
	private static void writeTTestEqSizeEqVar(InterfaceTextListener textout,
			ArrayList<EvAJob> jobsToWorkWith, String field) {
		for (int i=0; i<jobsToWorkWith.size(); i++) {
			textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
			for (int j=0; j<jobsToWorkWith.size(); j++) {
				textout.print("\t" + calculateTTestEqSizeEqVar(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
			}
			textout.println("");
		}
	}
	
	private static void writeMannWhitney(InterfaceTextListener textout,
			ArrayList<EvAJob> jobsToWorkWith, String field) {
		for (int i=0; i<jobsToWorkWith.size(); i++) {
			textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
			for (int j=0; j<jobsToWorkWith.size(); j++) {
				textout.print("\t" + calculateMannWhintey(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
			}
			textout.println("");
		}
	}

	/**
	 * @param textout
	 * @param jobsToWorkWith
	 */
	private static void writeTwoSampleFirstLine(InterfaceTextListener textout,
			ArrayList<EvAJob> jobsToWorkWith) {
		for(int i=0; i<jobsToWorkWith.size(); i++){
			textout.print("\t" + jobsToWorkWith.get(i).getParams().getOptimizer().getName());
		}
		textout.println("");
	}
	
	public static double roundTo2DecimalPlaces(double value){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		String b = twoDForm.format(value);
		b = b.replace(',', '.');
		Double c = Double.valueOf(b);
		return c;
	}
	
	private static String calculateMean(String field, EvAJob job1){
		double[] dat = job1.getDoubleDataColumn(field);
		double mean = Double.NaN;
		if (dat!=null) {
			mean = Mathematics.mean(dat);
			mean = EvAStatisticalEvaluation.roundTo2DecimalPlaces(mean);
		}
		return ""+mean;
	}
	
	private static String calculateMedian(String field, EvAJob job1){
		double[] dat = job1.getDoubleDataColumn(field);
		double median = Double.NaN;
		if (dat!=null) {
			median = Mathematics.median2(dat, true);
			median = EvAStatisticalEvaluation.roundTo2DecimalPlaces(median);
		}
		
		return ""+median;
	}
	
	private static String calculateVariance(String field, EvAJob job1){
		double[] dat = job1.getDoubleDataColumn(field);
		double variance = Double.NaN;
		if (dat!=null) {
			variance = Mathematics.variance(dat);
			variance = EvAStatisticalEvaluation.roundTo2DecimalPlaces(variance);
		}
		return ""+variance;
	}
	
	private static String calculateStdDev(String field, EvAJob job1){
		double[] dat = job1.getDoubleDataColumn(field);
		double stdDev = Double.NaN;
		if (dat!=null) {
			stdDev = Mathematics.stdDev(dat);
			stdDev = EvAStatisticalEvaluation.roundTo2DecimalPlaces(stdDev);
		}
		return ""+stdDev;
	}
	
	private static String calculateTTestEqSizeEqVar(String field, EvAJob job1, EvAJob job2){
		double[] dat1 = job1.getDoubleDataColumn(field);
		double[] dat2 = job2.getDoubleDataColumn(field);
		double t=Double.NaN;
		if (dat1!=null && dat2!=null) {
			t = Mathematics.tTestEqSizeEqVar(dat1, dat2);
		}
//		MannWhitneyTest mwt = new MannWhitneyTest(job1.getDoubleDataColumn(field), job2.getDoubleDataColumn(field));
//		double t = mwt.getSP();
//		t = roundTo2DecimalPlaces(t);
		return ""+t;
	}
	
	private static String calculateTTestUnEqSizeEqVar(String field, EvAJob job1, EvAJob job2){
		double[] dat1 = job1.getDoubleDataColumn(field);
		double[] dat2 = job2.getDoubleDataColumn(field);
		double t=Double.NaN;
		if (dat1!=null && dat2!=null) {
			t = Mathematics.tTestUnEqSizeEqVar(dat1, dat2);
		}
//		t = roundTo2DecimalPlaces(t);
		return ""+t;
	}
	
	private static String calculateTTestUnEqSizeUnEqVar(String field, EvAJob job1, EvAJob job2){
		double[] dat1 = job1.getDoubleDataColumn(field);
		double[] dat2 = job2.getDoubleDataColumn(field);
		double t=Double.NaN;
		if (dat1!=null && dat2!=null) {
			t = Mathematics.tTestUnEqSizeUnEqVar(dat1, dat2);
		}
//		t = roundTo2DecimalPlaces(t);
		return ""+t;
	}
	
	private static String calculateMannWhintey(String field, EvAJob job1, EvAJob job2){
		double[] dat1 = job1.getDoubleDataColumn(field);
		double[] dat2 = job2.getDoubleDataColumn(field);
		double t=Double.NaN;
		if (dat1!=null && dat2!=null) {
			Object obj =  ReflectPackage.instantiateWithParams("jsc.independentsamples.MannWhitneyTest", new Object[]{dat1,dat2}, null);
			if (obj!=null) {
				Object sp = BeanInspector.callIfAvailable(obj, "getSP", new Object[]{});
//				System.out.println(BeanInspector.niceToString(obj));
//				System.out.println("SP val is " + sp);
				t = (Double) sp;
			} else System.err.println("For the MannWhitney test, the JSC package is required on the class path!");
		}
		return ""+t;
	}

	private static String compare(String field, EvAJob job1, EvAJob job2) {
//		TODO do some statistical test
		int numRuns1 = job1.getNumRuns();
		int numRuns2 = job2.getNumRuns();
		if (TRACE) System.out.println("Run 1: " + numRuns1 + " runs, Run 2: " + numRuns2);
		if (TRACE) System.out.println("Data of run 1: " + BeanInspector.toString(job1.getDataColumn(field)));
		if (TRACE) System.out.println("Data of run 2: " + BeanInspector.toString(job2.getDataColumn(field)));
		double avg1=Mathematics.mean(job1.getDoubleDataColumn(field));
		double avg2=Mathematics.mean(job2.getDoubleDataColumn(field));
		
		if (avg1<avg2) return "-1";
		else if (avg1>avg2) return "1";
		else return "0";
	}

	/**
	 * Return a list of field names which occur in all jobs.
	 * @param jobList
	 * @return
	 */
	private static List<String> getCommonFields(List<EvAJob> jobList) {
		List<String> lSoFar=null, tmpL = new LinkedList<String>();
		for (EvAJob j:jobList) {
			if (lSoFar==null) {
				lSoFar = new LinkedList<String>();
				for (String f : j.getFieldHeaders()) lSoFar.add(f);
			} else {
				for (String f : lSoFar) {
					if (j.getFieldIndex(f)>=0) tmpL.add(f);
				}
				lSoFar=tmpL;
				tmpL = new LinkedList<String>();
			}
		}
		if (TRACE) System.out.println("Common fields are " + BeanInspector.toString(lSoFar));
		return lSoFar;
	}

}
