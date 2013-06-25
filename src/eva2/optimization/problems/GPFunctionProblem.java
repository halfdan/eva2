package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.codings.gp.GPArea;
import eva2.optimization.individuals.codings.gp.InterfaceProgram;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.EVAERROR;

/**
 * A helper problem class which takes a GP program as argument and serves as target function
 * for evaluating the GP function in optimization. A scaling option is implemented to flatten the
 * function by scaling it logarithmically. This avoids ugly functions in GP. To activate scaling,
 * set the scStart and scLimit parameters to positive values. Function values y with |y|>scStart
 * will be rescaled to values below scLimit.
 * 
 * @author mkron
 *
 */
public class GPFunctionProblem extends AbstractProblemDouble implements InterfaceProgramProblem {
	InterfaceProgram gpProblem = null;
	GPArea gpArea = new GPArea();
	double[] pos = null;
	int dim = 2;
	double scalingStart=10.;
	double scalingLimit=20.;
	
	public static boolean hideFromGOE = true;
	
	/**
	 * Initialize a default GP function problem in 2D with scaling.
	 * @param gpProb
	 * @param area
	 */
	public GPFunctionProblem(InterfaceProgram gpProb, GPArea area) {
		this(gpProb, area, 0., 0.);
	}
	
	/**
	 * By default, a 2-D problem is initialized with given scaling bounds. Set the scaling limit to 0 to
	 * deactivate scaling.
	 * 
	 * @param gpProb
	 * @param area
	 * @param scStart
	 * @param scLim
	 */
	public GPFunctionProblem(InterfaceProgram gpProb, GPArea area, double scStart, double scLim) {
		this(gpProb, area, 2, scStart, scLim);
	}
	
	/**
	 * A GP function problem is initialized with given scaling bounds. Set the scaling limit to 0 to
	 * deactivate scaling.
	 * 
	 * @param gpProb
	 * @param area
	 * @param scStart
	 * @param scLim
	 */
	public GPFunctionProblem(InterfaceProgram gpProb, GPArea area, int pDim, double scStart, double scLim) {
		dim = pDim;
		((ESIndividualDoubleData) template).setDoubleDataLength(dim);
		gpProblem = gpProb;
		gpArea = area;
		scalingStart=scStart;
		scalingLimit=scLim;
	}
	
	public GPFunctionProblem(GPFunctionProblem functionProblem) {
		dim = functionProblem.dim;
		if (functionProblem.pos != null) {
			pos = new double[dim];
			System.arraycopy(functionProblem.pos, 0, pos, 0, dim);
		}
		gpArea = (GPArea) functionProblem.gpArea.clone();
		gpProblem = functionProblem.gpProblem;
	}

	@Override
	public Object clone() {
		return new GPFunctionProblem(this);
	}

	@Override
	public double[] eval(double[] x) {
		if (x.length != dim) {
			EVAERROR.errorMsgOnce("mismatching dimension of GPFunctionProblem! Setting to " + x.length);
			setProblemDimension(x.length);
		}
		x = rotateMaybe(x);
		pos = x;
		Double res = (Double) gpProblem.evaluate(this);
		double[] fit = new double[1];
		fit[0] = scaleFit(res.doubleValue());
		return fit;
	}

	/**
	 * Set the problem dimension. Make sure that the associated GP tree is still valid
	 * and does not explicitely use more dimensions than defined here, for instance.
	 * @param newDim
	 */
	public void setProblemDimension(int newDim) {
		dim = newDim;
		((ESIndividualDoubleData) template).setDoubleDataLength(dim);
	}

	/**
	 * Scale the allover fitness value.
	 * 
	 * @param doubleValue
	 * @return
	 */
	public double scaleFit(double v) {
		if (scalingLimit==0.) {
                return v;
            }
		else {
			double aV = Math.abs(v);
			if (aV > scalingStart) {
				double logVal=Math.log(aV)/Math.log(scalingStart);
				double tmp=1./(logVal);
				return (scalingLimit - tmp)*Math.signum(v);
			} else {
                        return v;
                    }
		}
	}

//	public static void main(String[] args) {
//		for (double x=1.; x<100000000; x*=10.) {
//			System.out.println("x: " + -x + " sc: " + scaleFit(-x));
//		}
//	}
	
	@Override
	public int getProblemDimension() {
		return dim;
	}

    @Override
	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "GP find a function problem";
	}

    @Override
	public Object getSensorValue(String sensor) {
		return PSymbolicRegression.getSensorValue(sensor, pos, null);
	}

    @Override
	public void setActuatorValue(String actuator, Object parameter) {
		// nothing to do here
	}

    @Override
	public GPArea getArea() {
		return gpArea;
	}

}
