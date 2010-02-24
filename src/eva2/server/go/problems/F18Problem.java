package eva2.server.go.problems;

public class F18Problem extends AbstractProblemDouble implements
InterfaceMultimodalProblem {
	int dim = 10;
	double alpha = 1.;

	public F18Problem() {
		dim=10;
	}

	public F18Problem(F18Problem other) {
		dim=other.dim;
	}

	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double[] res = new double[1];
		double sum = 0;
		for (int i=0; i<getProblemDimension(); i++) {
			sum += Math.pow(Math.sin(5*Math.PI*x[i]), alpha);
		}
		res[0] = 1.-sum/getProblemDimension();
		return res;
	}

	public double getRangeLowerBound(int n) {
		return 0.;
	}
	
	public double getRangeUpperBound(int n) {
		return 1.;
	}
	
	public int getProblemDimension() {
		return dim;
	}

	public void setProblemDimension(int newDim) {
		dim = newDim;
	}

	public Object clone() {
		return new F18Problem(this);
	}

	public String getName() {
		return "F18-Problem";
	}

	public String globalInfo() {
		return "N-Function from Shir&Baeck, PPSN 2006.";
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
}
