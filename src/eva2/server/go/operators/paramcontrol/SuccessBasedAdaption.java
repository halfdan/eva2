package eva2.server.go.operators.paramcontrol;

import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.strategies.InterfaceOptimizer;
import java.io.Serializable;

public class SuccessBasedAdaption implements ParamAdaption, InterfaceHasUpperDoubleBound, 
GenericParamAdaption, Serializable {
	private double fitThreshold;
//	private double lastSuccRate=-1;
	private int fitCrit=0;
	private double targetRate = 0.75;
	private String paramStr = "unnamedParameter";
	private double lowerBnd=0.1;
	private double upperBnd=2;
	private double curValue;
	private double incFact = 1.05;
	
	public SuccessBasedAdaption() {}
	
	public SuccessBasedAdaption(double thresh, int crit, double rate) {
		setFitThreshold(thresh);
		fitCrit = crit;
		targetRate = rate;
	}
	
	public SuccessBasedAdaption(SuccessBasedAdaption successBasedAdaption) {
		// TODO Auto-generated constructor stub
	}

    @Override
	public Object clone() {
		return new SuccessBasedAdaption(this);
	}

    @Override
	public Object calcValue(Object obj, Population pop, int iteration,
			int maxIteration) {
		if (obj instanceof InterfaceOptimizer) {
			SolutionSet sols = (SolutionSet) ((InterfaceOptimizer)obj).getAllSolutions();
			double curSuccRate = getSuccessRate(sols.getSolutions());
			if (curSuccRate<targetRate) decrease(); // higher exploitation
			else increase(); // higher exploration
			System.out.println("Succ rate is " + curSuccRate + ", setting val " + curValue);
			return curValue;
		} else {
			System.err.println("Unknown object to be controlled by " + this.getClass().getName());
			return null;
		}
	}

	private void increase() {
		curValue = Math.min(upperBnd, incFact*curValue);
	}

	private void decrease() {
		curValue = Math.max(lowerBnd, (1./incFact)*curValue);
	}

	/**
	 * The success rate of the given population considering the 
	 * fitness threshold defined in this instance.
	 * 
	 * @param solutions
	 * @return
	 */
	private double getSuccessRate(Population solutions) {
		int numSucc = solutions.filterByFitness(getFitThreshold(), fitCrit).size();
		return ((double)numSucc)/solutions.size();
	}

    @Override
	public void finish(Object obj, Population pop) {}

    @Override
	public String getControlledParam() {
		return paramStr;
	}

    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {
		curValue = 0.5*(upperBnd+lowerBnd);
	}

    @Override
	public void setControlledParam(String prm) {
		paramStr = prm;
	}

    @Override
	public void SetUpperBnd(double u) {
		upperBnd = u;
	}

    @Override
	public double getUpperBnd() {
		return upperBnd;
	}

	public void setFitThreshold(double fitThreshold) {
		this.fitThreshold = fitThreshold;
	}

	public double getFitThreshold() {
		return fitThreshold;
	}

}
