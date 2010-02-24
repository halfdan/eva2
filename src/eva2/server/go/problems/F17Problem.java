package eva2.server.go.problems;

public class F17Problem extends AbstractProblemDouble implements
		InterfaceMultimodalProblem {
	int dim = 10;
	
	public F17Problem() {
		dim=10;
	}
	
	public F17Problem(F17Problem other) {
		dim=other.dim;
	}

	public double[] eval(double[] x) {
		double[] res = new double[1];
		double sum = 0;
		for (int i=0; i<getProblemDimension()-1; i++) {
			sum += x[i]*x[i]+2.*(x[i+1]*x[i+1]);
			sum += 0.7 - 0.3*Math.cos(3*Math.PI*x[i]) - 0.4*Math.cos(4*Math.PI*x[i+1]);
		}
		res[0] = sum;
		return res;
	}

	public int getProblemDimension() {
		return dim;
	}
	
	public void setProblemDimension(int newDim) {
		dim = newDim;
	}

	public Object clone() {
		return new F17Problem(this);
	}

	public String getName() {
		return "F17-Problem";
	}
	
	public String globalInfo() {
		return "Bohachevsky function, numerous optima on an oval hyperparabola with similar attractor sizes but decreasing fitness towards the bounds.";
	}
}
