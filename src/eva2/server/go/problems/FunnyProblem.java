package eva2.server.go.problems;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

/**
 * This is just an example function with no real use except that it has a
 * nice plot to it. It doesnt make much sense for optimization, so Ill
 * hide it for now.
 * If you wonder what this function actually represents, you may want to
 * try to display a cut with x[2]=1; x[3]=1.
 *
 * @author mkron
 *
 */
public class FunnyProblem extends AbstractProblemDoubleOffset {
	int iterations = 100;
//	public static final boolean hideFromGOE = true;
	
	public FunnyProblem() {}
	
    public FunnyProblem(FunnyProblem o) {
		iterations = o.iterations;
	}

	public Object clone() {
        return (Object) new FunnyProblem(this);
    }

    /** This method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
    	double[] c = new double[2];
    	
//    	c[0]= (x[0])*getXOffSet();
//    	c[1]= (x[1])*Math.sin((Math.PI/2)*getYOffSet());
    	
		c[0] = x[0]*x[2];
		c[1] = x[1] * sin( PI / 2.0 * x[3]);
		
//    	c[0]= (x[0]+(x[2]/10))*getXOffSet();
//    	c[1]= (x[1]+(x[3]/10))*Math.sin(Math.PI/2*getYOffSet());
//    	c[0]= (x[0]*(1-x[2]/10));
//    	c[1]= (x[1]*(1-x[3]/10));
        double[] result = new double[1];
        result[0] = flatten(evalRec(x, c, iterations));
        return result;
    }
    
    private double[] evalRec(double[] x, double[] c, int n) {
        if (n==0) return x;
        else return evalRec(addComplex(squareComplex(x),c), c, n-1);
    }
    
    private double[] squareComplex(double[] x) {
    	double[] result = new double[2];
    	result[0] = (x[0]*x[0])-(x[1]*x[1]);
    	result[1] = (2*x[0]*x[1]);
    	return result;
    }
    
    private double[] addComplex(double[] x, double[] y) {
    	double[] result = new double[2];
    	result[0] = x[0] + y[0];
    	result[1] = x[1] + y[1];
    	return result;
    }
    
    private double flatten(double[] x) {
    	
    	double len = Math.sqrt((x[0]*x[0])+(x[1]*x[1]));
    	double ang = Math.atan2(x[1],x[0]);
    	if (Double.isNaN(len) || (len > 1000.)) len = 1000.;
    	return len;
//    	return 0.5+0.5*t*(2*b*a*u(x/a));
//    	return 1/(2*Math.PI*ang);
//    	return +Math.abs(x[0])+Math.abs(x[1]);
    }
    
    public int getProblemDimension() {
    	return 4;
    }
    
    public String getName() {
    	return "FunnyProblem";
    }
    
    public double getRangeLowerBound(int dim) {
    	if (dim == 0) return -2.5;
    	else return -1.5;
    }

    public double getRangeUpperBound(int dim) {
    	return 1.5;
    }

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
}
