package eva2.optimization.problems;

import eva2.optimization.operators.postprocess.SolutionHistogram;
import java.io.Serializable;

/**
 * The multi-modal, multi-funnel Rana function, f_rana = sum_{i=0}^{n-2} (g(x_i, x_{i+1})
 * with g(x,y) = x sin(a)cos(b)+(y+1)cos(a)sin(b), a=sqrt(|-x+y+1|), b=sqrt(|x+y+1|).
 * 
 * For gnuplot: x *sin(sqrt(abs(-x+y+1)))*cos(sqrt(abs(x+y+1)))+(y+1)*cos(sqrt(abs(-x+y+1)))*sin(sqrt(abs(x+y+1)))
 *
 */
public class F20Problem extends AbstractProblemDouble implements Serializable, InterfaceInterestingHistogram{
	private int dim=10;
	private boolean shiftFit = false;
	
	public F20Problem() {
		setDefaultRange(512.);
	}
	
	public F20Problem(int dim, boolean rotate) {
		this();
		setProblemDimension(dim);
		setDoRotation(rotate);
	}
	
	public F20Problem(F20Problem o) {
		super(o);
		setDefaultRange(512);
		this.dim = o.dim;
		this.setShiftFit(o.isShiftFit());
	}

	@Override
	public double[] eval(double[] x) {
		x = rotateMaybe(x);
		double sum=getYOffset();
		
		for (int i=0; i<x.length-1; i++) {
			sum += g(x[i],x[i+1]);
		}
		return new double[] {sum};
	}

	private double getYOffset() {
		if (isShiftFit()) {
                return (getProblemDimension() - 1)*getDefaultRange();
            }
		else {
                return 0;
            }
	}
	
	private double g(double x, double y) {
		double a=beta(-x,y);
		double b=beta(x,y);
		return x*Math.sin(a)*Math.cos(b)+(y+1.)*Math.cos(a)*Math.sin(b);
	}

	private double beta(double x, double y) {
		return Math.sqrt(Math.abs(x+y+1));
	}
	
	@Override
	public int getProblemDimension() {
		return dim;
	}
	
	public void setProblemDimension(int newDim) {
		dim=newDim;
	}

	@Override
	public Object clone() {
		return new F20Problem(this);
	}

    @Override
	public String getName() {
		return "Rana"+(isDoRotation() ? "-rot" : "");
	}
	
	public static String globalInfo() {
		return "The Rana function is non-separable, highly multi-modal and multi-funnel." +
				" There are diagonal ridges across the search space and the optima are close to the bounds." +
				"The minimum fitness f(x*) is close to (n-1)*r for dimension n and default range r, by which " +
				"this implementation may be shifted to the positive domain.";  
	}

	public void setShiftFit(boolean shiftFit) {
		this.shiftFit = shiftFit;
	}

	public boolean isShiftFit() {
		return shiftFit;
	}

    @Override
	public SolutionHistogram getHistogram() {
		if (getProblemDimension()==10) {
			if (getYOffset()==0) {
                        return new SolutionHistogram(-5200, -3600, 16);
                    }
			else {
                        return new SolutionHistogram(0, 1600, 16);
                    }
		}
		if (getProblemDimension()==30) {
            if (getYOffset() == 0) {
                return new SolutionHistogram(-15000, -8600, 16);
            }
//			das passst wohl nicht für Multimodales... Also nochmal für 30D.
            else {
                return new SolutionHistogram(0, 6400, 16);
            }
        }
        if (getProblemDimension() <= 5) {
            double lower = getYOffset() - ((getProblemDimension() - 1) * getDefaultRange());
            return new SolutionHistogram(lower, lower + 160, 16);
        } else if (getProblemDimension() < 15) {
            return new SolutionHistogram(getYOffset() - 5000, getYOffset() - 3400, 16);
        } else {
            return new SolutionHistogram(getYOffset() - 15000, getYOffset() - 13400, 16);
        }
    }
}
