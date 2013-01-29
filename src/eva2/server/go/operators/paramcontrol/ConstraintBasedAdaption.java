package eva2.server.go.operators.paramcontrol;

import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.constraint.AbstractConstraint;
import eva2.server.go.populations.Population;
import eva2.tools.math.Mathematics;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Simple penalty factor adaption for contraints with fitness based penalty after Bean and Hadj-Alouane 1992/1997.
 * If the best individual was always feasible for k generations, the penalty factor is decreased,
 * if it was always infeasible, the penalty factor is increased. For other cases, the penalty remains the same.
 * This is plausible for the typical case that the optimum lies near the constraint boundary, however it makes
 * the fitness function change dynamically based only on the positions of last best indidivuals. 
 * 
 * The authors advise to select betaInc != 1./betaDec to avoid cycling. 
 * @author mkron
 *
 */
public class ConstraintBasedAdaption implements ParamAdaption, Serializable {
	private double betaInc=1.5;
	private double betaDec=0.7;
	private double initialPenalty = 1.;
	private double minPenalty=0.01; 
	private double maxPenalty=100.;
	private double currentFactor = 1.;
	private int genGap=5;
	
	LinkedList<Boolean> lastBestSatisfactionState = new LinkedList<Boolean>();
	
//	private double endAct=0;
//	private double deltaInertness = 0.1;
	private static boolean TRACE=false;
	
	private static String target = "penaltyFactor";
	
	public ConstraintBasedAdaption() {};
			
	public ConstraintBasedAdaption(
			ConstraintBasedAdaption o) {
		betaInc = o.betaInc;
		betaDec = o.betaDec;
		genGap = o.genGap;
//		endAct = o.endAct;
//		deltaInertness = o.deltaInertness;
	}

    @Override
	public Object clone() {
		return new ConstraintBasedAdaption(this);
	}

    @Override
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		boolean changed = false;
		if (!(obj instanceof AbstractConstraint)) System.err.println(this.getClass().getSimpleName() + " cant control " + obj.getClass().getSimpleName() + " ! ");
		else {
			if (TRACE) System.out.println("calc value at it " + iteration + " of " + maxIteration);
			if (lastBestSatisfactionState.size()==genGap) lastBestSatisfactionState.poll();
			boolean bestIsFeasible = ((AbstractConstraint)obj).isSatisfied(((InterfaceDataTypeDouble)pop.getBestEAIndividual()).getDoubleDataWithoutUpdate());
			if (!lastBestSatisfactionState.offer(bestIsFeasible)) System.err.println("Error, could not push best indy state!"); 
		
			changed=maybeAdaptFactor((AbstractConstraint)obj);
		}
		double curPen = initialPenalty * currentFactor;
		if (curPen<minPenalty) {
			currentFactor = minPenalty/initialPenalty;
			curPen=minPenalty;
		} else if (curPen > maxPenalty) {
			currentFactor = maxPenalty/initialPenalty;
			curPen=maxPenalty;
		}
		if (TRACE && changed) System.out.println("NEW penalty: " + curPen);
		return curPen;
	}

	private boolean maybeAdaptFactor(AbstractConstraint constr) {
		boolean changed = false;
		if (lastBestSatisfactionState.size() >= genGap) {
			boolean allValid=true;
			boolean allInvalid=true;
			for (Boolean isFeasible : lastBestSatisfactionState) {
				if (isFeasible) allInvalid=false;
				else allValid=false;
			}
			if (allValid) {
				currentFactor*=betaDec;
				changed=true;
				if (TRACE) System.out.println("all valid, new fact is " + currentFactor + " times " + initialPenalty);
			} else if (allInvalid) {
				changed=true;
				currentFactor*=betaInc;
				if (TRACE) System.out.println("all invalid, new fact is " + currentFactor + " times " + initialPenalty);
			}
		} else if (TRACE) System.out.println("not yet looking at " + genGap + " individuals...");
		return changed;
	}

    @Override
	public String getControlledParam() {
		return target;
	}

	public double getBetaInc() {
		return betaInc;
	}
	public void setBetaInc(double d) {
		this.betaInc = d;
	}
	public String betaIncTipText() {
		return "The increase factor for the penalty.";
	}
	
	public double getBetaDec() {
		return betaDec;
	}
	public void setBetaDec(double d) {
		this.betaDec = d;
	}
	public String betaDecTipText() {
		return "The decrease factor for the penalty.";
	}
	
	public int getGenGap() {
		return genGap;
	}
	public void setGenGap(int v) {
		this.genGap = v;
	}
	public String genGapTipText() {
		return "The number of generations regarded.";
	}

	public static String globalInfo() {
		return "Adapt a constraint's penalty factor (esp. fitness based) if the population contained only valid or only invalid individuals for some generations."; 
	}
	
    @Override
	public void finish(Object obj, Population pop) {
		lastBestSatisfactionState.clear();
		((AbstractConstraint) obj).setPenaltyFactor(initialPenalty);
	}

    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {
		initialPenalty = ((AbstractConstraint) obj).getPenaltyFactor();
		if (minPenalty > maxPenalty) {
			System.err.println("Error in " + this.getClass().getSimpleName() + ", inconsistent penalty factor restrictions!");
		}
		initialPenalty = Mathematics.projectValue(initialPenalty, minPenalty, maxPenalty);
		lastBestSatisfactionState.clear();
		currentFactor=1.;
	}

	public double getMinPenalty() {
		return minPenalty;
	}

	public void setMinPenalty(double minPenalty) {
		this.minPenalty = minPenalty;
	}
	
	public String minPenaltyTipText() {
		return "The minimum penalty factor.";
	}

	public double getMaxPenalty() {
		return maxPenalty;
	}

	public void setMaxPenalty(double maxPenalty) {
		this.maxPenalty = maxPenalty;
	}
	
	public String maxPenaltyTipText() {
		return "The maximum penalty factor.";
	}
	
	public String getName() {
		return "Adaptive penalty factor"; 
	}

}
