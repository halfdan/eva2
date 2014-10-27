package eva2.optimization.statistics;

import eva2.gui.BeanInspector;
import eva2.optimization.enums.StatisticsOnSingleDataSet;
import eva2.optimization.enums.StatisticsOnTwoSampledData;
import eva2.tools.ReflectPackage;
import eva2.tools.StringTools;
import eva2.tools.math.Mathematics;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Do some statistical tests on a set of job results. Note that the plausibility (comparability of the
 * jobs) is not tested here.
 */
public class EvAStatisticalEvaluation {

    private static Logger LOGGER = Logger.getLogger(EvAStatisticalEvaluation.class.getName());
    public static EvAStatisticalEvaluationParams statsParams = new EvAStatisticalEvaluationParams();


    public static void evaluate(InterfaceTextListener textout, OptimizationJob[] jobList, int[] selectedIndices,
                                StatisticsOnSingleDataSet[] singleStats,
                                StatisticsOnTwoSampledData[] twoSampledStats) {
        ArrayList<OptimizationJob> jobsToWorkWith = new ArrayList<>();
        for (int i = 0; i < jobList.length; i++) {
            // remove jobs which are not finished or not selected
            if (jobList[i] != null && (Mathematics.contains(selectedIndices, i)) && (jobList[i].isFinishedAndComplete())) {
                jobsToWorkWith.add(jobList[i]);
            }
        }
        List<String> commonFields = getCommonFields(jobsToWorkWith);
        if (commonFields != null && !commonFields.isEmpty()) {
            for (String field : commonFields) {
                textout.println("###\t" + StringTools.humaniseCamelCase(field) + " statistical evaluation");

                if (singleStats.length > 0) {
                    textout.println("One-sampled Statistics\n");
                    for (int j = -1; j < singleStats.length; j++) {
                        if (j < 0) {
                            textout.print("method");
                        } else {
                            textout.print("\t" + singleStats[j]);
                        }
                    }
                    textout.println("");
                    for (int i = 0; i < jobsToWorkWith.size(); i++) {
                        textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
                        for (int j = 0; j < singleStats.length; j++) {
                            switch (singleStats[j]) {
                                case mean:
                                    textout.print("\t" + calculateMean(field, jobsToWorkWith.get(i)));
                                    break;
                                case median:
                                    textout.print("\t" + calculateMedian(field, jobsToWorkWith.get(i)));
                                    break;
                                case variance:
                                    textout.print("\t" + calculateVariance(field, jobsToWorkWith.get(i)));
                                    break;
                                case stdDev:
                                    textout.print("\t" + calculateStdDev(field, jobsToWorkWith.get(i)));
                                    break;
                                default:
                                    textout.println("");
                            }
                        }
                        textout.println("\n");
                    }
                }
                if (twoSampledStats.length > 0) {
                    textout.println("Two-sampled Statistics:\n");
                    for (int i = 0; i < twoSampledStats.length; i++) {
                        switch (twoSampledStats[i]) {
                            case tTestEqLenEqVar:
                                textout.println(StatisticsOnTwoSampledData.getInfoStrings()[twoSampledStats[i].ordinal()]);
                                writeTwoSampleFirstLine(textout, jobsToWorkWith);
                                writeTTestEqSizeEqVar(textout, jobsToWorkWith, field);
                                break;
                            case tTestUneqLenEqVar:
                                textout.println(StatisticsOnTwoSampledData.getInfoStrings()[twoSampledStats[i].ordinal()]);
                                writeTwoSampleFirstLine(textout, jobsToWorkWith);
                                writeUnEqSizeEqVar(textout, jobsToWorkWith, field);
                                break;
                            case tTestUneqLenUneqVar:
                                textout.println(StatisticsOnTwoSampledData.getInfoStrings()[twoSampledStats[i].ordinal()]);
                                writeTwoSampleFirstLine(textout, jobsToWorkWith);
                                writeTTestUnEqSizeUnEqVar(textout, jobsToWorkWith, field);
                                break;
                            case mannWhitney:
                                textout.println(StatisticsOnTwoSampledData.getInfoStrings()[twoSampledStats[i].ordinal()]);
                                writeTwoSampleFirstLine(textout, jobsToWorkWith);
                                writeMannWhitney(textout, jobsToWorkWith, field);
                            default:
                                textout.println("");
                                break;
                        }
                        textout.println("");
                    }
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
            InterfaceTextListener textout, ArrayList<OptimizationJob> jobsToWorkWith,
            String field) {
        for (int i = 0; i < jobsToWorkWith.size(); i++) {
            textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
            for (int j = 0; j < jobsToWorkWith.size(); j++) {
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
                                           ArrayList<OptimizationJob> jobsToWorkWith, String field) {
        for (int i = 0; i < jobsToWorkWith.size(); i++) {
            textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
            for (int j = 0; j < jobsToWorkWith.size(); j++) {
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
                                              ArrayList<OptimizationJob> jobsToWorkWith, String field) {
        for (int i = 0; i < jobsToWorkWith.size(); i++) {
            textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
            for (int j = 0; j < jobsToWorkWith.size(); j++) {
                textout.print("\t" + calculateTTestEqSizeEqVar(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
            }
            textout.println("");
        }
    }

    private static void writeMannWhitney(InterfaceTextListener textout, ArrayList<OptimizationJob> jobsToWorkWith, String field) {
        for (int i = 0; i < jobsToWorkWith.size(); i++) {
            textout.print(jobsToWorkWith.get(i).getParams().getOptimizer().getName());
            for (int j = 0; j < jobsToWorkWith.size(); j++) {
                textout.print("\t" + calculateMannWhitney(field, jobsToWorkWith.get(i), jobsToWorkWith.get(j)));
            }
            textout.println("");
        }
    }

    /**
     * @param textout
     * @param jobsToWorkWith
     */
    private static void writeTwoSampleFirstLine(InterfaceTextListener textout,
                                                ArrayList<OptimizationJob> jobsToWorkWith) {
        for (int i = 0; i < jobsToWorkWith.size(); i++) {
            textout.print("\t" + jobsToWorkWith.get(i).getParams().getOptimizer().getName());
        }
        textout.println("");
    }

    public static double formatOutput(double value) {
        DecimalFormat twoDForm = new DecimalFormat("##0.####E0");
        String b = twoDForm.format(value);
        b = b.replace(',', '.');
        return Double.valueOf(b);
    }

    private static String calculateMean(String field, OptimizationJob job1) {
        double[] dat = job1.getDoubleDataColumn(field);
        double mean = Double.NaN;
        if (dat != null) {
            mean = Mathematics.mean(dat);
            mean = EvAStatisticalEvaluation.formatOutput(mean);
        }
        return "" + mean;
    }

    private static String calculateMedian(String field, OptimizationJob job1) {
        double[] dat = job1.getDoubleDataColumn(field);
        double median = Double.NaN;
        if (dat != null) {
            median = Mathematics.median2(dat, true);
            median = EvAStatisticalEvaluation.formatOutput(median);
        }

        return "" + median;
    }

    private static String calculateVariance(String field, OptimizationJob job1) {
        double[] dat = job1.getDoubleDataColumn(field);
        double variance = Double.NaN;
        if (dat != null) {
            variance = Mathematics.variance(dat);
            variance = EvAStatisticalEvaluation.formatOutput(variance);
        }
        return "" + variance;
    }

    private static String calculateStdDev(String field, OptimizationJob job1) {
        double[] dat = job1.getDoubleDataColumn(field);
        double stdDev = Double.NaN;
        if (dat != null) {
            stdDev = Mathematics.stdDev(dat);
            stdDev = EvAStatisticalEvaluation.formatOutput(stdDev);
        }
        return "" + stdDev;
    }

    private static String calculateTTestEqSizeEqVar(String field, OptimizationJob job1, OptimizationJob job2) {
        double[] dat1 = job1.getDoubleDataColumn(field);
        double[] dat2 = job2.getDoubleDataColumn(field);
        double t = Double.NaN;
        if (dat1 != null && dat2 != null) {
            t = Mathematics.tTestEqSizeEqVar(dat1, dat2);
        }
        return "" + t;
    }

    private static String calculateTTestUnEqSizeEqVar(String field, OptimizationJob job1, OptimizationJob job2) {
        double[] dat1 = job1.getDoubleDataColumn(field);
        double[] dat2 = job2.getDoubleDataColumn(field);
        double t = Double.NaN;
        if (dat1 != null && dat2 != null) {
            t = Mathematics.tTestUnEqSizeEqVar(dat1, dat2);
        }
        return "" + t;
    }

    private static String calculateTTestUnEqSizeUnEqVar(String field, OptimizationJob job1, OptimizationJob job2) {
        double[] dat1 = job1.getDoubleDataColumn(field);
        double[] dat2 = job2.getDoubleDataColumn(field);
        double t = Double.NaN;
        if (dat1 != null && dat2 != null) {
            t = Mathematics.tTestUnEqSizeUnEqVar(dat1, dat2);
        }
        return "" + t;
    }

    private static String calculateMannWhitney(String field, OptimizationJob job1, OptimizationJob job2) {
        double[] dat1 = job1.getDoubleDataColumn(field);
        double[] dat2 = job2.getDoubleDataColumn(field);
        double t = Double.NaN;
        if (dat1 != null && dat2 != null) {
            Object obj = ReflectPackage.instantiateWithParams("jsc.independentsamples.MannWhitneyTest", new Object[]{dat1, dat2}, null);
            if (obj != null) {
                Object sp = BeanInspector.callIfAvailable(obj, "getSP", new Object[]{});
                t = (Double) sp;
            } else {
                LOGGER.warning("For the MannWhitney test, the JSC package is required on the class path!");
            }
        }
        return "" + t;
    }

    /**
     * ToDo: Figure out why this gives different results than jsc.independentsamples.MannWhitneyTest#getSP
     * @param field The field for which the test should be executed for.
     * @param job1 First job to test
     * @param job2 Second job to test
     * @return
     */
    private static String calculateMannWhitneyU(String field, OptimizationJob job1, OptimizationJob job2) {
        double[] dat1 = job1.getDoubleDataColumn(field);
        double[] dat2 = job2.getDoubleDataColumn(field);
        double t = Double.NaN;

        // We can't compute the MannWhitney test if one of the samples is empty
        if (dat1 != null && dat2 != null) {
            double n1 = dat1.length;
            double n2 = dat2.length;

            ArrayList<Double> sortedValues = new ArrayList<>();
            // This is stupid. Find a better way.
            for (Double d : dat1) {
                sortedValues.add(d);
            }
            for (Double d : dat2) {
                sortedValues.add(d);
            }
            Collections.sort(sortedValues);
            double tA = 0.0;
            for (Double value : dat1) {
                tA += (sortedValues.indexOf(value) + 1.0 + sortedValues.lastIndexOf(value) + 1.0) / 2.0;;
            }
            double tB = 0.0;
            for (Double value : dat2) {
                tB += (sortedValues.indexOf(value) + 1 + sortedValues.lastIndexOf(value) + 1) / 2.0;
            }
            double uA = (n1 * n2) + ((0.5 * n1) * (n1 + 1.0)) - tA;
            double uB = (n1 * n2) + ((0.5 * n2) * (n2 + 1.0)) - tB;

            assert(uA + uB == n1 * n2);
            double u = Math.min(uA, uB);
            double mU = (n1 * n2) / 2;
            double omegaU = Math.sqrt((n1*n2*(n1 + n2 + 1.0))/12.0);

            t = (u - mU) / omegaU;
        }
        return "" + t;
    }

    private static String compare(String field, OptimizationJob job1, OptimizationJob job2) {
//		TODO do some statistical test
        int numRuns1 = job1.getNumRuns();
        int numRuns2 = job2.getNumRuns();
        double avg1 = Mathematics.mean(job1.getDoubleDataColumn(field));
        double avg2 = Mathematics.mean(job2.getDoubleDataColumn(field));

        if (avg1 < avg2) {
            return "-1";
        } else if (avg1 > avg2) {
            return "1";
        } else {
            return "0";
        }
    }

    /**
     * Return a list of field names which occur in all jobs.
     *
     * @param jobList
     * @return
     */
    private static List<String> getCommonFields(List<OptimizationJob> jobList) {
        List<String> lSoFar = null, tmpL = new LinkedList<>();
        for (OptimizationJob j : jobList) {
            if (lSoFar == null) {
                lSoFar = new LinkedList<>();
                for (String f : j.getFieldHeaders()) {
                    lSoFar.add(f);
                }
            } else {
                for (String f : lSoFar) {
                    if (j.getFieldIndex(f) >= 0) {
                        tmpL.add(f);
                    }
                }
                lSoFar = tmpL;
                tmpL = new LinkedList<>();
            }
        }
        return lSoFar;
    }

}
