package eva2.optimization.operators.terminators;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.go.PopulationInterface;
import eva2.optimization.populations.InterfaceSolutionSet;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import java.io.Serializable;

/**
 * Abstract class giving the framework for a terminator that is based on
 * a population measure converging for a given time (number of evaluations or 
 * generations).
 * The class detects changes of a population measure over time and may signal convergence
 * if the measure m(P) behaved in a certain way for a given time. Convergence may
 * be signaled 
 * - if the measure reached absolute values below convThresh (absolute value),
 * - if the measure remained within m(P)+/-convThresh (absolute change),
 * - if the measure remained above m(P)-convThresh (absolute change and regard improvement only),
 * - if the measure remained within m(P)*[1-convThresh, 1+convThresh] (relative change),
 * - if the measure remained above m(P)*(1-convThresh) (relative change and regard improvement only).
 * 
 * @author mkron
 *
 */
public abstract class PopulationMeasureTerminator implements InterfaceTerminator,
Serializable {
	public enum ChangeTypeEnum {relativeChange, absoluteChange, absoluteValue};
	public enum DirectionTypeEnum {decrease, bidirectional};
	public enum StagnationTypeEnum {fitnessCallBased, generationBased};
	
	protected static boolean TRACE = false;
	private double convThresh = 0.01; //, convThreshLower=0.02;
	private double oldMeasure = -1;
	private int stagTime = 1000;
	private int oldPopFitCalls = 1000;
	private int oldPopGens = 1000;
	private boolean firstTime = true;
	private StagnationTypeEnum stagnationMeasure = StagnationTypeEnum.fitnessCallBased;
//	private SelectedTag convCondition = new SelectedTag("Relative change", "Absolute change", "Absolute value");
	private ChangeTypeEnum changeType = ChangeTypeEnum.relativeChange;
	private DirectionTypeEnum condDirection = DirectionTypeEnum.decrease;
//	private SelectedTag condImprovementOrChange = new SelectedTag("Decrease", "Improvement and Deterioration");
	protected String msg="Not terminated.";

	public PopulationMeasureTerminator() {}
	
	public PopulationMeasureTerminator(double convergenceThreshold, int stagnationTime, StagnationTypeEnum stagType, ChangeTypeEnum changeType, DirectionTypeEnum dirType) {
		this.convThresh = convergenceThreshold;
		this.stagTime = stagnationTime;
		this.stagnationMeasure = stagType;
//		this.convergenceCondition.setSelectedTag(bAbsolute ? 1 : 0);
		this.changeType = changeType;
		this.condDirection =  dirType;
//		this.condImprovementOrChange.setSelectedTag(bImprovement ? 0 : 1);
	}

	public PopulationMeasureTerminator(PopulationMeasureTerminator o) {
		convThresh = o.convThresh;
		stagTime = o.stagTime;
		oldPopFitCalls = o.oldPopFitCalls;
		oldPopGens = o.oldPopGens;
		firstTime = o.firstTime;
//		oldFit = o.oldFit.clone();
//		oldNorm = o.oldNorm;
		msg = o.msg;
		this.stagnationMeasure = o.stagnationMeasure;
//		this.convergenceCondition.setSelectedTag(o.convergenceCondition.getSelectedTagID());
		this.changeType = o.changeType;
		this.condDirection = o.condDirection;
//		this.condImprovementOrChange.setSelectedTag(o.condImprovementOrChange.getSelectedTagID());
	}
	
//	public void hideHideable() {
//		setConvergenceCondition(getConvergenceCondition());
//	}
	
//	public PopulationMeasureTerminator() {
//		pMetric = new PhenotypeMetric();
//	}
//
//	public PopulationMeasureTerminator(double thresh, int stagnPeriod, boolean bFitCallBased, boolean bAbsolute) {
//		pMetric = new PhenotypeMetric();
//		convThresh = thresh;
//		this.m_stagTime = stagnPeriod;
//		if (bFitCallBased) stagnationMeasure.setSelectedTag("Fitness calls");
//		else stagnationMeasure.setSelectedTag("Generations");
//		if (bAbsolute) convergenceCondition.setSelectedTag("Absolute");
//		else convergenceCondition.setSelectedTag("Relative");
//	}
//
//	public PopulationMeasureTerminator(PopulationMeasureTerminator other) {
//		pMetric = new PhenotypeMetric();
//		convThresh = other.convThresh;
//		this.m_stagTime = other.m_stagTime;
//		stagnationMeasure.setSelectedTag(other.getStagnationMeasure().getSelectedTagID());
//		convergenceCondition.setSelectedTag(other.getConvergenceCondition().getSelectedTagID());
//	}
	
	public static String globalInfo() {
		return "Stop if a convergence criterion has been met.";
	}

    @Override
	public void init(InterfaceOptimizationProblem prob) {
		firstTime = true;
		msg = "Not terminated.";
//		oldFit = null;
//		oldNorm=-1;
		oldPopFitCalls=-1;
		oldPopGens=-1;
	}

    @Override
	public boolean isTerminated(InterfaceSolutionSet solSet) {
		return isTerminated(solSet.getCurrentPopulation());
	}

    @Override
	public boolean isTerminated(PopulationInterface pop) {
		if (!firstTime && isStillConverged(pop)) {
			if (TRACE) {
                        System.out.println("Converged at " + pop.getGeneration() + "/" + pop.getFunctionCalls() + ", measure " + calcPopulationMeasure(pop));
                    }
			if (stagnationTimeHasPassed(pop)) {
				// population hasnt changed much for max time, criterion is met
				msg = getTerminationMessage();
				return true;
			} else {
				// population hasnt changed much for i<max time, keep running
				return false;
			}
		} else {
			// first call at all - or population improved more than "allowed" to terminate
			oldMeasure = calcInitialMeasure(pop);
			saveState(pop);
			return false;
		}
	}

	/**
	 * Calculate the initial measure (on the initial population).
	 * @return
	 */
	protected abstract double calcInitialMeasure(PopulationInterface pop);
		
    @Override
	public String lastTerminationMessage() {
		return msg;
	}

	/**
	 * Build a standard termination message based on the configuration.
	 * @return
	 */
	protected String getTerminationMessage() {
		StringBuffer sb = new StringBuffer(getMeasureName());
//		if (convergenceCondition.isSelectedString("Relative")) sb.append(" converged relatively ");
		switch (changeType) {
		case absoluteChange: sb.append(" changed absolutely "); break;
		case absoluteValue: sb.append(" reached absolute values "); break;
		case relativeChange: sb.append(" changed relatively "); break;
		}
		if (doCheckImprovement()) {
			sb.append("less than ");
			sb.append(convThresh);
		} else {
			sb.append("within +/-");
//			sb.append(convThreshLower);
//			sb.append("/");
			sb.append(convThresh);
		}
		sb.append(" for ");
		sb.append(stagTime);
		if (stagnationMeasure == StagnationTypeEnum.generationBased) {
                sb.append(" generations.");
            }
		else {
                sb.append(" function calls.");
            }
		return sb.toString();
	}

	/**
	 * Give a String description of the name of the population measure.
	 * @return
	 */
	protected abstract String getMeasureName();

	/**
	 * Save the population state if a change has been detected.
	 * When overriding, make sure to call the superclass method.
	 * 
	 * @param pop
	 */
	protected void saveState(PopulationInterface pop) {
//		oldFit = pop.getBestFitness().clone();
		oldMeasure = calcPopulationMeasure(pop);
		oldPopFitCalls = pop.getFunctionCalls();
		oldPopGens = pop.getGeneration();
		firstTime = false;
	}

	/**
	 * Calculate the population measure on which the termination
	 * criterion is based.
	 *
	 * @param pop
	 * @return
	 */
	protected abstract double calcPopulationMeasure(PopulationInterface pop);

	/**
	 * Return true if the population measure did not exceed the
	 * threshold for convergence since the last saved state.
	 *
	 * @param curFit
	 * @return
	 */
	protected boolean isStillConverged(PopulationInterface pop) {
		double measure = calcPopulationMeasure(pop);
		double allowedLower=Double.NEGATIVE_INFINITY, allowedUpper=Double.POSITIVE_INFINITY;
		boolean ret;
		switch (changeType) {
		case absoluteChange:
			allowedLower=oldMeasure-convThresh;
			if (!doCheckImprovement()) {
                allowedUpper=oldMeasure+convThresh;
            }
			break;
		case absoluteValue:
			allowedUpper=convThresh;
//			if (!doCheckImprovement()) allowedUpper = convThreshUpper;
			break;
		case relativeChange:
			double delta = oldMeasure*convThresh;
			allowedLower = oldMeasure-delta;
			if (!doCheckImprovement()) {
                allowedUpper = oldMeasure+delta;
            }
			break;
		}
		ret = (measure <= allowedUpper) && (measure >= allowedLower);
		// Old Version:
//		if (isRelativeConvergence()) {
//			double delta = oldMeasure*convThresh;
//			if (doCheckImprovement()) ret = (measure >= (oldMeasure - delta));
//			else ret = ((measure >= (oldMeasure-delta)) && (measure <= (oldMeasure+delta))); // check for rel. change which must be within +/- thresh
//		} else { // check absolute values
//			if (doCheckImprovement()) ret = (measure < oldMeasure+convThresh); // absolute improvement below fixed number
//			else ret = ((measure < oldMeasure+convThresh) && (measure > oldMeasure-convThresh)); // absolute change within fixed range
//		}
		if (TRACE) {
                System.out.println("isStillConverged returns " + ret + ", measure " + measure + ", old measure " + BeanInspector.toString(oldMeasure) + ", bounds: [" + allowedLower + " , " + allowedUpper + "]");
            }
		return ret;
	}

	public boolean doCheckImprovement() {
		return (condDirection==DirectionTypeEnum.decrease);
//		return condImprovementOrChange.isSelectedString("Improvement");
	}

	public boolean isRelativeConvergence() {
		return changeType==ChangeTypeEnum.relativeChange;
	}
	
	/**
	 * Return true if the defined stagnation time (function calls or generations) has passed
	 * since the last noteable change. 
	 *  
	 * @param pop
	 * @return
	 */
	private boolean stagnationTimeHasPassed(PopulationInterface pop) {
		if (stagnationMeasure==StagnationTypeEnum.fitnessCallBased) { // by fitness calls
//			System.out.println("stagnationTimeHasPassed returns " + ((pop.getFunctionCalls() - popFitCalls) >= m_stagTime) + " after " + (pop.getFunctionCalls() - popFitCalls));
			return (pop.getFunctionCalls() - oldPopFitCalls) >= stagTime;
		} else {// by generation
//			System.out.println("stagnationTimeHasPassed returns " + ((pop.getFunctionCalls() - popGens) >= m_stagTime) + " after " + (pop.getFunctionCalls() - popGens));
			return (pop.getGeneration() - oldPopGens) >= stagTime;
		}
	}

	public void setConvergenceThreshold(double x) {
		convThresh = x;
	}
	public double getConvergenceThreshold() {
		return convThresh;
	}
	public String convergenceThresholdTipText() {
		return "Ratio of improvement/change or absolute value of improvement/change to determine convergence.";
	}

//	public void setConvergenceThresholdLower(double x) {
//		convThreshLower = x;
//	}
//	public double getConvergenceThresholdLower() {
//		return convThreshLower;
//	}
//	public String convergenceThresholdUpperTipText() {
//		return "Lower threshold value in case of detecting absolute change, meaning the bounds [measure-convThresh,measure+convThresh] must be kept to assume convergence.";
//	}
	
	public void setStagnationTime(int k) {
		stagTime = k;
	}
	public int getStagnationTime() {
		return stagTime;
	}
	public String stagnationTimeTipText() {
		return "Terminate if the population has not improved/changed for this time";
	}

	public StagnationTypeEnum getStagnationMeasure() {
		return stagnationMeasure;
	}
	public void setStagnationMeasure(StagnationTypeEnum stagnationTimeIn) {
		this.stagnationMeasure = stagnationTimeIn;
	}
	public String stagnationMeasureTipText() {
		return "Stagnation time is measured in fitness calls or generations";
	}

	public ChangeTypeEnum getConvergenceCondition() {
		return changeType;
	}
	public void setConvergenceCondition(ChangeTypeEnum convergenceCondition) {
		this.changeType = convergenceCondition;
	}
	public String convergenceConditionTipText() {
		return "Select absolute or relative convergence condition";
	}
	
	public DirectionTypeEnum getCheckType() {
		return condDirection;
	}
	public void setCheckType(DirectionTypeEnum dt) {
		this.condDirection = dt;
	}
	public String checkTypeTipText() {
		return "Detect improvement only (decreasing measure) or change in both directions (decrease and increase)";
	}
}