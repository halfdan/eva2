package eva2.server.go.operators.postprocess;

import java.util.Arrays;

import eva2.gui.BeanInspector;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceInterestingHistogram;
import eva2.tools.math.Mathematics;

public class SolutionHistogram {
	private double lBound, uBound;
	private int numBins;
	private int[] histogram;
	private int arity; // number of summed-up histograms
	
	public SolutionHistogram(double lower, double upper, int nBins) {
		lBound=lower;
		uBound=upper;
		numBins=nBins;
		histogram = new int[numBins];
		arity = 0;
	}
	
	public SolutionHistogram cloneEmpty() {
		return new SolutionHistogram(getLowerBound(), getUpperBound(), getNumBins());
	}
	
	public boolean isEmtpy() {
		return arity==0;
	}
	
	private void setEntry(int i, int v) {
		histogram[i]=v;
	}
	
	public int getEntry(int i) {
		return histogram[i];
	}
	
	public double lowerBoundOfEntry(int i) {
		return lBound+(i*getStepSize());
	}
	public double upperBoundOfEntry(int i) {
		return lBound+((i+1)*getStepSize());
	}
	public double getLowerBound() {
		return lBound;
	}

	public double getUpperBound() {
		return uBound;
	}

	public int getNumBins() {
		return numBins;
	}

	public double getStepSize() {
		return (uBound-lBound)/numBins;
	}
	
	public boolean isCompatible(SolutionHistogram o) {
		if (lBound==o.getLowerBound() && (uBound==o.getUpperBound()) && (numBins==o.getNumBins())) return true;
		else return false;
	}
	
	/**
	 * Add another histogram to this one.
	 * @param o
	 */
	public void addHistogram(SolutionHistogram o) {
		if (o.isEmtpy()) System.err.println("Warning, adding empty histogram... (SolutionHistogram)");
		if (isCompatible(o)) {
			arity+=o.arity;
			for (int i=0; i<numBins; i++) histogram[i]+=o.histogram[i];
		}
	}
	
	/**
	 * Get the average of summed-up histograms.
	 * @return
	 */
	public double[] getAverage() {
		double[] avg = new double[numBins];
		for (int i=0; i<numBins; i++) avg[i]=((double)histogram[i])/arity;
		return avg;
	}
	
	public int sum() {
		return Mathematics.sum(histogram);
	}
	
    @Override
	public String toString() {
		return "Hist("+arity+"):"+lBound+"/"+uBound+","+BeanInspector.toString(histogram) + ",Sc:"+getScore()+(arity>1 ? (",Avg.Sc:"+(getScore()/arity)) : (""));
	}
	
	/**
	 * Fill a given histogram from a population. Old entries are overwritten.
	 * @param pop
	 * @param hist
	 */
    public static void createFitNormHistogram(Population pop, SolutionHistogram hist, int crit) {
    	hist.reset();
    	if (pop.size()>0) {
    		if (pop.getBestFitness()[crit]<hist.getLowerBound()) {
    			System.err.println("Warning, population contains solution with lower fitness than lower bound of the histogram!");
//    			System.err.println("Pop was " + pop.getStringRepresentation());
    			System.err.println("Histogramm was " + hist.toString());
    		}
    		for (int i=0; i<hist.getNumBins(); i++) {
    			hist.setEntry(i, PostProcess.filterFitnessIn(pop, hist.lowerBoundOfEntry(i), hist.upperBoundOfEntry(i), crit).size());
    		}
    	}
    	hist.setSingularHist();
    }
    
	public static SolutionHistogram defaultEmptyHistogram(AbstractOptimizationProblem prob) {
		if (prob instanceof InterfaceInterestingHistogram) {
			return ((InterfaceInterestingHistogram)prob).getHistogram();
		} else {
//			System.err.println("Unknown problem to make histogram for, returning default...");
			return new SolutionHistogram(0, 100, 10);
		}
	}
    
    /**
     * Notify that a single histogram has been created.
     */
    private void setSingularHist() {
		arity=1;
	}

	private void reset() {
		arity=0;
		Arrays.fill(histogram, 0);
	}
	
	/**
	 * Fill a given histogram from a population. Old entries are overwritten.
	 * This resets the arity.
	 * @param pop
	 * @param hist
	 */
	public void updateFrom(Population pop, int crit) {
    	SolutionHistogram.createFitNormHistogram(pop, this, crit);
    }
    
    /**
     * Create a fitness histogram of an evaluated population within the given interval and nBins number of bins.
     * Therefore a bin is of size (upperBound-lowerBound)/nBins, and bin 0 starts at lowerBound.
     * Returns an integer array with the number of individuals in each bin.  
     * 
     * @param pop	the population to scan.
     * @param lowerBound	lower bound of the fitness interval
     * @param upperBound	upper bound of the fitness interval
     * @param nBins	number of bins
     * @return	an integer array with the number of individuals in each bin
     * @see filterFitnessIn()
     */    
    public static SolutionHistogram createFitNormHistogram(Population pop, double lowerBound, double upperBound, int nBins, int crit) {
    	SolutionHistogram res = new SolutionHistogram(lowerBound, upperBound, nBins);
    	createFitNormHistogram(pop, res, crit);
    	return res;
    }    
    
//    public void updateFrom(Population pop, double accuracy) {
//    	
//    }

	public double getScore() {
		double sc = 0; 
		if (sum()>0) {
			for (int i=numBins-1; i>=0; i--) {
				sc += getScalingFactor(i)*((double)getEntry(i));
			}
			return sc;
		} else return 0;
	}

	private double getScalingFactor(int i) {
		return (numBins-i)/((double)numBins);
	}
}
