package eva2.server.go.problems;

public class F16Problem extends AbstractProblemDouble implements InterfaceMultimodalProblem, Interface2DBorderProblem {
	int dim = 10;
	
	public F16Problem() {
		dim=10;
	}
	
	public F16Problem(F16Problem other) {
		dim = other.dim;
	}
	
	@Override
	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double[] res = new double[1];
		double sum = 0;
		
		for (int i=0; i<getProblemDimension(); i++) {
			sum += Math.sin(10*Math.log(x[i]));
		}
		
		res[0] = 1.-((1./getProblemDimension())*sum);
		return res;
	}

	@Override
	public int getProblemDimension() {
		return dim;
	}
	
	public void setProblemDimension(int newDim) {
		dim = newDim;
	}

	@Override
	public Object clone() {
		return new F16Problem(this);
	}
	
	public double getRangeLowerBound(int n) {
		return 0.25;
	}
	public double getRangeUpperBound(int n) {
		return 10.;
	}

	public String getName() {
		return "Vincent function";
	}
	
	public static String globalInfo() {
		return "Multiple optima with increasing densitiy near the lower bounds, therefore decreasing attractor size.";
	}
}
