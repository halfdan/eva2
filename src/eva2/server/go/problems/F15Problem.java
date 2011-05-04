package eva2.server.go.problems;

import java.io.Serializable;

import eva2.server.go.operators.postprocess.SolutionHistogram;

/**
 * The Levy-function, from Levy, A., and Montalvo, A. (1985). Also described in 
 * "A Trust-Region Algorithm for Global Optimization", Bernardetta Addisy and Sven Leyfferz, 2004/2006.
 */

public class F15Problem extends AbstractProblemDouble implements Serializable, InterfaceInterestingHistogram {
	private int dim=10;
    
	public F15Problem() {
		super();
		super.SetDefaultAccuracy(0.00001);
		setDefaultRange(10);
	}
	
	public F15Problem(int d) {
		this();
		setProblemDimension(d);
	}
	
	public F15Problem(F15Problem o) {
		super(o);
		dim=o.getProblemDimension();
		setDefaultRange(o.getDefaultRange());
	}
	
	@Override
	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double t=0, s=0, sum=0;

		for (int i=0; i<x.length-1; i++) {
			s=(x[i]-1.);
			t=Math.sin(Math.PI*x[i+1]);
			sum += (s*s)*(1+10.*t*t);
		}
		
		s=Math.sin(Math.PI*x[0]);
		double[] y = new double[1];
		y[0] = 10.*s*s+sum+dim*(x[dim-1]-1)*(x[dim-1]-1);
		return y;
	}

	@Override
	public int getProblemDimension() {
		return dim;
	}

	public void setProblemDimension(int d) {
		dim=d;
	}
	
	@Override
	public Object clone() {
		return new F15Problem(this);
	}

	public SolutionHistogram getHistogram() {
		if (getProblemDimension()<15) return new SolutionHistogram(0, 2, 16);
		else if (getProblemDimension()<25) return new SolutionHistogram(0, 4, 16);
		else return new SolutionHistogram(0, 8, 16);
	}

	public String getName() {
		return "F15-Problem";
	}
	
	public static String globalInfo() {
		return "The Levy-function, from Levy, A., and Montalvo, A. (1985)";
	}
	
}
