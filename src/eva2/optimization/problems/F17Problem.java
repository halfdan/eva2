package eva2.optimization.problems;

import eva2.optimization.operator.postprocess.SolutionHistogram;
/**
 * Bohachevsky function, numerous optima on an oval hyperparabola with similar attractor sizes 
 * but decreasing fitness towards the bounds. Described e.g. in Shir&Bäck, PPSN 2006, 
 * "Niche radius adaption in the CMA-ES Niching Algorithm".
 * f_B(\vec{x})=\sum_{i=1}^{n-1} x_i^2+2(x_{i+1}^2)+0.7-0.3 cos (3 \pi x_i)-0.4 cos (4 \pi x_{i+1})
 */
public class F17Problem extends AbstractProblemDouble implements
		InterfaceMultimodalProblem, InterfaceInterestingHistogram{
	int dim = 10;
	
	public F17Problem() {
		setDefaultRange(10.);
		dim=10;
	}
	
	public F17Problem(int dimension) {
		this();
		setProblemDimension(dimension);
	}
	
	public F17Problem(F17Problem other) {
		super(other);
		dim=other.dim;
	}

    @Override
	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double[] res = new double[1];
		double sum = 0;
		for (int i=0; i<getProblemDimension()-1; i++) {
			sum += x[i]*x[i]+2.*(x[i+1]*x[i+1]);
			sum += 0.7 - 0.3*Math.cos(3*Math.PI*x[i]) - 0.4*Math.cos(4*Math.PI*x[i+1]);
		}
		res[0] = sum;
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
		return new F17Problem(this);
	}

    @Override
	public String getName() {
		return "F17-Problem";
	}
	
	public static String globalInfo() {
		return "Bohachevsky function, numerous optima on an oval hyperparabola with similar attractor sizes but decreasing fitness towards the bounds.";
	}

    @Override
	public SolutionHistogram getHistogram() {
		if (getProblemDimension()<15) {
                return new SolutionHistogram(-0.5, 7.5, 16);
            }
		else {
                return new SolutionHistogram(-0.5, 15.5, 16);
            }
	}
}
