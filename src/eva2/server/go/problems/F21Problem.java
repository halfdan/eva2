package eva2.server.go.problems;

import eva2.server.go.operators.postprocess.SolutionHistogram;
import java.util.Random;

/**
 * Langerman function: A non-separable function built from random peaks each of which is surrounded by circular ridges.
 * The number of optima is equal to the number of dimensions.
 * The positions and height values of the peaks are initialized randomly with a fixed seed for reproducibility.
 * 
 * @author mkron
 *
 */
public class F21Problem extends AbstractProblemDouble implements InterfaceMultimodalProblem, InterfaceInterestingHistogram{
	private double[] heights = null; // will receive random positions within the range
	private double[][] peaks = null; // will receive values in [0,1] as peak height values
	private static final int rndSeed=23;
	private int dim=2;
	
	public F21Problem() {
	}
	
	public F21Problem(F21Problem f21Problem) {
		this();
	}

    @Override
	public String getName() {
		return "Langerman-Function";
	}
	
	public static String globalInfo() {
		return "The Langerman function, with n peaks each of which surrounded by circular ridges.";
	}
	
	@Override
	public double getRangeLowerBound(int dim) {
		return 0;
	}

	@Override
	public double getRangeUpperBound(int dim) {
		return 10;
	}

	@Override
	public void initProblem() {
		super.initProblem();
		Random rnd = new Random(rndSeed);
		heights=new double[getProblemDimension()];
		peaks=new double[getProblemDimension()][];
		for (int i=0; i<getProblemDimension(); i++) {
			heights[i]=rnd.nextDouble();
			peaks[i]=new double[getProblemDimension()];
			for (int j=0; j<getProblemDimension(); j++) {
				peaks[i][j]=getRangeLowerBound(i)+rnd.nextDouble()*(getRangeUpperBound(i)-getRangeLowerBound(i));
			}
		}
//		System.out.println("peaks is " + BeanInspector.toString(peaks));
//		System.out.println("heights is " + BeanInspector.toString(heights));
	}

	@Override
	public double[] eval(double[] x) {
		double res[] = new double[1];
		double tmp, innerSum, sum=0;
		x = rotateMaybe(x);
		
		for (int i=0; i<x.length; i++) {
			innerSum=0;
			for (int j=0; j<x.length; j++) { 
				tmp=(x[j]-peaks[i][j]); 
				innerSum+=(tmp*tmp);
			}
			sum+=(heights[i]*
					Math.exp(-(1./Math.PI)*innerSum)*
					Math.cos(Math.PI*innerSum));
		}
		
		res[0] = 1-sum;
		return res;
	}

	@Override
	public int getProblemDimension() {
		return dim;
	}
	public void setProblemDimension(int d) {
		dim=d;
	}
	public String problemDimensionTipText() {
		return "The problem dimension and number of optima.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.AbstractOptimizationProblem#clone()
	 */
    @Override
	public Object clone() {
		return new F21Problem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.InterfaceInterestingHistogram#getHistogram()
	 */
    @Override
	public SolutionHistogram getHistogram() {
		return new SolutionHistogram(0, 0.5, 10);
	}

}
