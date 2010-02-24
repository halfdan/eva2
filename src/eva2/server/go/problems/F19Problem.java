package eva2.server.go.problems;

import java.util.Arrays;

import wsi.ra.math.RNG;

public class F19Problem extends AbstractProblemDouble implements
InterfaceMultimodalProblem {
	int dim = 10;
	transient private double[] alphas, As;
	transient private int[] A,B;
	
	public F19Problem() {
		alphas=null;
		dim=10;
		setDefaultRange(Math.PI);
	}

	public F19Problem(F19Problem other) {
		dim=other.dim;
		alphas=null;
	}

	public void initProblem() {
		super.initProblem();
		if (alphas==null) {
			// create static random data
			alphas = RNG.randomVector(dim, -Math.PI, Math.PI);
			A = RNG.randomVector(dim*dim, -100, 100);
			B = RNG.randomVector(dim*dim, -100, 100);
			As = transform(alphas);
		}
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
		return "Fletcher-Powell function with up to 2^n optima from Shir&Baeck, PPSN 2006, after Bäck 1996. Alphas and Matrices A and B are randomly created but kept when the dimension is decreased.";
	}
}

