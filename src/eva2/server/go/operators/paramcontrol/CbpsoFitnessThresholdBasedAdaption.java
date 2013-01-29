package eva2.server.go.operators.paramcontrol;

import eva2.server.go.populations.Population;
import eva2.server.go.strategies.CBNPSO;
import java.io.Serializable;

/**
 * We want to be better than a fitness threshold. We expect a change of a certain value increases
 * the chance to be better than a fitness threshold.
 * 
 * @author mkron
 *
 */
public class CbpsoFitnessThresholdBasedAdaption implements ParamAdaption, GenericParamAdaption, Serializable {
	private double initialVal=10000, lowerBnd = 1000, upperBnd=15000;
	private double currentVal=initialVal;
	
	private double incFact=1.2;
	private double decFact=1./incFact;
	private int adptIntervalGenerations = 10;
	private int lastAdaption = -1;
	private String paramName = "sigmaAdaptionPeriod";
	private double minInterestingRatio = 0.5;
	private double maxInterestingRatio = 0.8;
	
	public CbpsoFitnessThresholdBasedAdaption() {}
	
	public CbpsoFitnessThresholdBasedAdaption(int initialV, int minV, int maxV, double incFact, int generationInterval, double minIntRatio, double maxIntRatio) {
		initialVal = initialV;
		lowerBnd = minV;
		upperBnd = maxV;
		currentVal = initialVal;
		this.incFact = incFact;
		decFact = 1./incFact;
		adptIntervalGenerations = generationInterval;
		lastAdaption = -1;
		minInterestingRatio = minIntRatio;
		maxInterestingRatio = maxIntRatio;
	}
	
	public CbpsoFitnessThresholdBasedAdaption(CbpsoFitnessThresholdBasedAdaption o) {
		initialVal = o.initialVal;
		lowerBnd = o.lowerBnd;
		upperBnd = o.upperBnd;
		currentVal = o.currentVal;
		incFact = o.incFact;
		decFact = o.decFact;
		paramName = o.paramName;
		adptIntervalGenerations = o.adptIntervalGenerations;
		lastAdaption = o.lastAdaption;
		minInterestingRatio = o.minInterestingRatio;
		maxInterestingRatio = o.maxInterestingRatio;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
    @Override
	public Object clone() {
		return new CbpsoFitnessThresholdBasedAdaption(this);
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.operators.paramcontrol.ParamAdaption#calcValue(java.lang.Object, eva2.server.go.populations.Population, int, int)
	 */
    @Override
	public Object calcValue(Object obj, Population pop, int iteration,
			int maxIteration) {
		if (obj instanceof CBNPSO) {
			CBNPSO cbpso = (CBNPSO)obj;
			checkForAdaption(cbpso, pop, iteration, maxIteration);
		} else {
			System.err.println("Invalid object!");
		}
//		System.out.println("current val is " + currentVal);
		return currentVal;
	}

	private void checkForAdaption(CBNPSO cbpso, Population pop,
			int iteration, int maxIteration) {
		// How to find out if we are in need of improvement?
		double oldVal = currentVal;
		
		if (isPossibleAdaptionTime(pop)) {
			double interestingRatio = cbpso.getInterestingSolutionRatio();
			if (interestingRatio >= 0 && (interestingRatio < minInterestingRatio)) { // performance too bad
				// reduce period --> increase frequency
				adaptValue(decFact, pop);
			} else if (interestingRatio > maxInterestingRatio) { // performance is too good
				// increase period --> decrease frequency
				adaptValue(incFact, pop);
			}
			if (oldVal!=currentVal) {
//				System.out.println("++ Changed period at it." + iteration 
//						+ ", ratio " + interestingRatio + " from " + oldVal + " to " + currentVal);
				setShift(cbpso, pop, oldVal, currentVal);
			}
		}
	}

	private void setShift(CBNPSO cbpso, Population pop, double oldVal, double currentVal2) {
		double k = pop.getFunctionCalls()/currentVal;
		int diff = (int) (pop.getFunctionCalls()-(((int)k)*currentVal));
		
		cbpso.setSigmaAdaptionShift(diff);

	}

	private void adaptValue(double fact, Population pop) {
		currentVal *= fact;
		currentVal = Math.max(lowerBnd, Math.min(upperBnd, currentVal));
		lastAdaption = pop.getGeneration();
		adptIntervalGenerations = (int)(currentVal/pop.getTargetSize());
	}

	private boolean isPossibleAdaptionTime(Population pop) {
		if (lastAdaption+adptIntervalGenerations>pop.getGeneration()) return false;
		else return true;
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.operators.paramcontrol.ParamAdaption#finish(java.lang.Object, eva2.server.go.populations.Population)
	 */
    @Override
	public void finish(Object obj, Population pop) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.operators.paramcontrol.ParamAdaption#getControlledParam()
	 */
    @Override
	public String getControlledParam() {
		return paramName;
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.operators.paramcontrol.ParamAdaption#init(java.lang.Object, eva2.server.go.populations.Population, java.lang.Object[])
	 */
    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {
		currentVal=initialVal;
		lastAdaption=0;
		adptIntervalGenerations = (int)(currentVal/pop.getTargetSize());
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.operators.paramcontrol.GenericParamAdaption#setControlledParam(java.lang.String)
	 */
    @Override
	public void setControlledParam(String prm) {
		paramName = prm;
	}

	/**
	 * 
	 * @param initialVal
	 */
	public void setInitialVal(double initialVal) {
		this.initialVal = initialVal;
	}

	/**
	 * 
	 * @return
	 */
	public double getInitialVal() {
		return initialVal;
	}
	
}
