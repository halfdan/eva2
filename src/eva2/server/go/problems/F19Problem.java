package eva2.server.go.problems;

import java.util.Arrays;
import java.util.Random;

import eva2.server.go.operators.postprocess.SolutionHistogram;
import eva2.tools.math.RNG;

/**
 * Fletcher-Powell function with up to 2^n optima from Shir&Baeck, PPSN 2006, 
 * after Bäck 1996. Alphas and Matrices A and B are randomly created with a fixed seed.
 * @author mkron
 *
 */
public class F19Problem extends AbstractProblemDouble implements
InterfaceMultimodalProblem, InterfaceInterestingHistogram {
	int dim = 10;
	transient private double[] alphas, As;
	transient private int[] A,B;
	private long randSeed = 23; 
	
	public F19Problem() {
		alphas=null;
		dim=10;
		setDefaultRange(Math.PI);
	}

	public F19Problem(F19Problem other) {
		dim=other.dim;
		alphas=null;
	}

	public F19Problem(int d) {
		this();
		setProblemDimension(d);
	}

	public void initProblem() {
		super.initProblem();
//		if (alphas==null) {
			// create static random data
			Random rand = new Random(randSeed);
			alphas = RNG.randomDoubleArray(rand, -Math.PI, Math.PI, dim);
			A = RNG.randomIntArray(rand, -100, 100, dim*dim);
			B = RNG.randomIntArray(rand, -100, 100, dim*dim);
			As = transform(alphas);
//		}
	}

	private double[] transform(double[] x) {
		double[] v = new double[dim];
		Arrays.fill(v, 0.);
		for (int i=0; i<dim; i++) {
			for (int j=0; j<dim; j++) {
				v[i] += get(A, i, j)*Math.sin(x[j])+get(B, i, j)*Math.cos(x[j]);
			}
		}
		return v;
	}
	
	/**
	 * Get a value in row i, col j, from matrix M (represented as vector).
	 * @param M
	 * @param i
	 * @param j
	 * @return
	 */
	private int get(int[] M, int i, int j) {
		return M[i*dim+j];
	}
	
	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double[] res = new double[1];
		double[] Bs = transform(x);
		
		double sum=0;
		for (int i=0; i<getProblemDimension(); i++) {
			sum += Math.pow(As[i]-Bs[i], 2);
		}
		res[0] = sum;
		return res;
	}

	public int getProblemDimension() {
		return dim;
	}

	public void setProblemDimension(int newDim) {
		dim = newDim;
		if (alphas!=null && (newDim>alphas.length)) { // only recreate if really necessary
			alphas=null;
			A=null;
			B=null;
		}
	}

	public Object clone() {
		return new F19Problem(this);
	}

	public String getName() {
		return "F19-Problem";
	}

	public String globalInfo() {
		return "Fletcher-Powell function with up to 2^n optima from Shir&Baeck, PPSN 2006, after Bäck 1996. Alphas and Matrices A and B are randomly created with a fixed seed.";
	}

	public SolutionHistogram getHistogram() {
		if (getProblemDimension()<15) return new SolutionHistogram(0, 8, 16);
		else return new SolutionHistogram(0, 40000, 16);
	}
}

