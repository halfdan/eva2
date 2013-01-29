package eva2.tools.math;
///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: StatisticUtils.java,v $
//  Purpose:  Interface definition for calling external programs from JOELib.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2004/02/28 17:19:28 $
//            $Author: ulmerh $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////
import eva2.server.go.problems.AbstractProblemDouble;
import eva2.tools.math.Jama.Matrix;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Statistic utils.
 */
public class StatisticUtils
{
	/** The natural logarithm of 2. */
	public static double log2 = Math.log(2);

	/** The small deviation allowed in double comparisons */
	public static double SMALL = 1e-6;

	/**
	 * Returns the correlation coefficient of two double vectors.
	 *
	 * @param y1 double vector 1
	 * @param y2 double vector 2
	 * @param n the length of two double vectors
	 * @return the correlation coefficient
	 */
	public final static double correlation(double y1[],double y2[]) {

		int i;
		double av1 = 0.0, av2 = 0.0, y11 = 0.0, y22 = 0.0, y12 = 0.0, c;
		int n=y1.length;
		if (n!=y2.length) {
                throw new RuntimeException("Error, mismatching vectors for correlation calculation in StatisticUtils.correlation(double[], double[])");
            }

		if (n <= 1) {
			return 1.0;
		}
		for (i = 0; i < n; i++) {
			av1 += y1[i];
			av2 += y2[i];
		}
		av1 /= (double) n;
		av2 /= (double) n;
		for (i = 0; i < n; i++) {
			y11 += (y1[i] - av1) * (y1[i] - av1);
			y22 += (y2[i] - av2) * (y2[i] - av2);
			y12 += (y1[i] - av1) * (y2[i] - av2);
		}
		if (y11 * y22 == 0.0) {
			c=1.0;
		} else {
			c = y12 / Math.sqrt(Math.abs(y11 * y22));
		}

		return c;
	}

	/**
	 * Computes differential shannon entropy
	 *
	 * @return DSE=SE(AB)-0.5*[SE(A)+SE(B)]
	 */
	public static double differentialShannon(int counts1[],int counts2[], int n, int countsSum1, int countsSum2)
	{
		double seA=0.0;
		double seB=0.0;
		double seAB=0.0;
		double c=0.0;
		int AB;
		int allSum = countsSum1+countsSum2;
		for(int i=0;i<n;i++)
		{
			AB = counts1[i] + counts2[i];
			seA -= xlogx(((double)counts1[i])/((double)countsSum1));
			seB -= xlogx(((double)counts2[i])/((double)countsSum2));
			seAB -= xlogx(((double)AB)/((double)allSum));
		}

		c= seAB - 0.5*(seA+seB);

		return c;
	}

	/**
	 * Computes entropy for an array of integers.
	 *
	 * @param counts array of counts
	 * @return - a log2 a - b log2 b - c log2 c + (a+b+c) log2 (a+b+c)
	 * when given array [a b c]
	 */
	public static double info(int counts[]) {

		int total = 0;
		int c;
		double x = 0;
		for (int j = 0; j < counts.length; j++) {
			x -= xlogx(counts[j]);
			total += counts[j];
		}
		return x + xlogx(total);
	}

	/**
	 * Computes shannon entropy for an array of integers.
	 *
	 * @param counts array of counts
	 * @return - a log2 a - b log2 b - c log2 c
	 * when given array [a b c]
	 */
	public static double shannon(int counts[], int countsSum) {

		double x = 0;
		for (int j = 0; j < counts.length; j++) {
			x -= xlogx( ((double)counts[j])/ ((double)countsSum));
		}
		return x;
	}


	/**
	 * Returns the logarithm of a for base 2.
	 *
	 * @param a a double
	 */
	public static final double log2(double a) {

		return Math.log(a) / log2;
	}

	/**
	 * Returns index of maximum element in a given
	 * array of doubles. First maximum is returned.
	 *
	 * @param doubles the array of doubles
	 * @return the index of the maximum element
	 */
	public static int maxIndex(double [] doubles) {

		double maximum = 0;
		int maxIndex = 0;

		for (int i = 0; i < doubles.length; i++) {
			if ((i == 0) || (doubles[i] > maximum)) {
				maxIndex = i;
				maximum = doubles[i];
			}
		}

		return maxIndex;
	}

	/**
	 * Returns index of maximum element in a given
	 * array of integers. First maximum is returned.
	 *
	 * @param ints the array of integers
	 * @return the index of the maximum element
	 */
	public static int maxIndex(int [] ints) {

		int maximum = 0;
		int maxIndex = 0;

		for (int i = 0; i < ints.length; i++) {
			if ((i == 0) || (ints[i] > maximum)) {
				maxIndex = i;
				maximum = ints[i];
			}
		}

		return maxIndex;
	}

	/**
	 * Returns index of minimum element in a given
	 * array of integers. First minimum is returned.
	 *
	 * @param ints the array of integers
	 * @return the index of the minimum element
	 */
	public static int minIndex(int [] ints) {

		int minimum = 0;
		int minIndex = 0;

		for (int i = 0; i < ints.length; i++) {
			if ((i == 0) || (ints[i] < minimum)) {
				minIndex = i;
				minimum = ints[i];
			}
		}

		return minIndex;
	}

	/**
	 * Returns index of minimum element in a given
	 * array of doubles. First minimum is returned.
	 *
	 * @param doubles the array of doubles
	 * @return the index of the minimum element
	 */
	public static int minIndex(double [] doubles) {

		double minimum = 0;
		int minIndex = 0;

		for (int i = 0; i < doubles.length; i++) {
			if ((i == 0) || (doubles[i] < minimum)) {
				minIndex = i;
				minimum = doubles[i];
			}
		}

		return minIndex;
	}

	/**
	 * Computes the variance for an array of doubles.
	 *
	 * @param vector the array
	 * @param finiteSet if true, the vector is interpreted as complete data set, otherwise a set of samples of a larger set.
	 * @return the variance
	 */
	public static double variance(double[] vector, boolean finiteSet) {

		double sum = 0, sumSquared = 0;
		int n=vector.length;
		if (n <= 1) {
			return 0;
		}
		for (int i = 0; i < n; i++) {
			sum += vector[i];
			sumSquared += (vector[i] * vector[i]);
		}
		double denom;
		if (finiteSet) {
                denom=n;
            }
		else {
                denom=(n-1);
            }
		double result = (sumSquared - (sum * sum / (double) n)) / denom;

		// We don't like negative variance
		if (result < 0) {
			return 0;
		} else {
			return result;
		}
	}

	/**
	 * Returns c*log2(c) for a given integer value c.
	 *
	 * @param c an integer value
	 * @return c*log2(c) (but is careful to return 0 if c is 0)
	 */
	public static final double xlogx(int c) {

		if (c == 0) {
			return 0.0;
		}
		return c * StatisticUtils.log2((double) c);
	}

	/**
	 * Returns c*log2(c) for a given value c.
	 *
	 * @param c an integer value
	 * @return c*log2(c) (but is careful to return 0 if c is 0)
	 */
	public static final double xlogx(double c) {

		if (c == 0) {
			return 0.0;
		}
		return c * StatisticUtils.log2( c);
	}

	/**
	 * Tests if a is equal to b.
	 *
	 * @param a a double
	 * @param b a double
	 */
	public static final boolean eq(double a, double b){

		return (a - b < SMALL) && (b - a < SMALL);
	}

	/**
	 * Tests if a is smaller or equal to b.
	 *
	 * @param a a double
	 * @param b a double
	 */
	public static final boolean smOrEq(double a,double b) {

		return (a-b < SMALL);
	}

	/**
	 * Tests if a is greater or equal to b.
	 *
	 * @param a a double
	 * @param b a double
	 */
	public static final boolean grOrEq(double a,double b) {

		return (b-a < SMALL);
	}

	/**
	 * Tests if a is smaller than b.
	 *
	 * @param a a double
	 * @param b a double
	 */
	public static final boolean sm(double a,double b) {

		return (b-a > SMALL);
	}

	/**
	 * Tests if a is greater than b.
	 *
	 * @param a a double
	 * @param b a double
	 */
	public static final boolean gr(double a,double b) {

		return (a-b > SMALL);
	}

	/**
	 * returns root mean square error.
	 */
	public static final double rmsError(double array1[], double array2[])
	{
		if ((array1 == null) || (array2 == null)) { return -1.0; }

		double errorValueRMS = 0;
		for (int i=0; i<array1.length; i++)
		{
			// add squared error value
			errorValueRMS += (array1[i] - array2[i]) * (array1[i] - array2[i]);
		}
		// calculate mean and root of the sum of the squared error values
		errorValueRMS = Math.sqrt(errorValueRMS / (double) array1.length);

		return errorValueRMS;
	}

	/**
	 * Returns the correlation coefficient r^2.
	 *
	 * Correlation ("Statistik", 7 Aufl., Hartung, 1989, Kapitel 9 und 10, S.546-608):
	 * a=yMess[i];
	 * b=yWahr[i];
	 * aa=a*a;
	 * bb=b*b;
	 * ab=a*b;
	 * numerator=sumAB-(sumA*sumB/n);
	 * denominator=sqrt[(sumAA-(sumA*sumA/n))*(sumBB-(sumB*sumB/n))];
	 * R=correlationcoefficient=numerator/denominator;
	 *
	 * @author Joerg K. Wegner
	 */
	public static double getCorrelationCoefficient(double array1[], double array2[]) {
		if ((array1 == null) || (array2 == null)) { return -2.0; }

		double sumA  = 0;
		double sumB  = 0;
		double sumAB = 0;
		double sumAA = 0;
		double sumBB = 0;

		for (int i=0; i<array1.length; i++)
		{
			double a = array1[i];
			double b = array2[i];

			sumA  += a;
			sumB  += b;
			sumAA += a*a;
			sumBB += b*b;
			sumAB += a*b;
		}

		double n = (double) array1.length;
		double numerator   = sumAB - (sumA*sumB/n);
		double denominator = Math.sqrt((sumAA - (sumA*sumA/n)) * (sumBB - (sumB*sumB/n)));
		double corrCoefficient = numerator / denominator;
		corrCoefficient *= corrCoefficient;

		return corrCoefficient;
	}

	/**
	 * Main method for testing this class.
	 */
	public static void main(String[] ops) {
		double[][] vs = {{ 356.873,-498.563, 130.923,-223.684, 494.296, 380.739, 481.353, 344.571, 501.105,-166.109},
				{ 199.116,-423.448, 487.898, 344.579, 140.709, 89.007,-259.310, 512.000, 152.069,-440.778},
				{ 439.128, 112.370,-422.820,-43.119,-297.609,-438.940, 488.914,-512.000,-407.847, 386.611},
				{ 395.772, 191.634,-511.999,-93.078,-282.853,-444.621, 491.291,-512.000,-407.620, 386.493}};
		
		vs[1]=Mathematics.rotate(vs[0], AbstractProblemDouble.initDefaultRotationMatrix(10, 10));
		
		for (int i=0; i<vs.length; i++) {
			for (int j=0; j<vs.length; j++) {
				System.out.print("\t" + correlation(vs[i], vs[j]));
			}
			System.out.println();
		}
//		vs[1]=Mathematics.rotate(vs[0], AbstractProblemDouble.initDefaultRotationMatrix(30, 10));
//		vs[2]=Mathematics.rotate(vs[0], AbstractProblemDouble.initDefaultRotationMatrix(50, 10));
//		vs[3]=Mathematics.rotate(vs[0], AbstractProblemDouble.initDefaultRotationMatrix(70, 10));
		
		vs = new double[][]{{0.1,0.9},
							{0.8,0.2},
//							{0.8,0.2},
							{0.0,1.0}};
		System.out.println("---");
		
//		for (int i=0; i<vs.length; i++) {
//			Mathematics.svAdd(512, vs[i], vs[i]);
//			Mathematics.normVect(vs[i], vs[i]);
//			System.out.println(BeanInspector.toString(vs[i]));
//		}
		
//		System.out.println("entropy cols: " + entropyOverColumns(vs));
//		System.out.println("entropy rows: " + entropyOverRows(vs));
	}
	
    /**
     * Computes the entropy of the given array.
     *
     * @param array an array of double data
     * @return the entropy
     */
    public static double entropy(double[] array) {
        double returnValue = 0;
        double sum = 0;

        for (int i = 0; i < array.length; i++) {
            returnValue -= lnFunc(array[i]);
            sum += array[i];
        }

        if (StatisticUtils.eq(sum, 0)) {
            return 0;
        } else {
            return (returnValue + lnFunc(sum)) / (sum * log2);
        }
    }

    
    // these came from ContingencyTables.java in the wsi package (mkron)
//    /**
//     * Computes conditional entropy of the rows given
//     * the columns.
//     *
//     * @param matrix the contingency table
//     * @return the conditional entropy of the rows given the columns
//     */
//    public static double entropyConditionedOnColumns(double[][] matrix) {
//        double ret = 0;
//        double colSum;
//        double total = 0;
//
//        for (int j = 0; j < matrix[0].length; j++) {
//            colSum = 0;
//
//            for (int i = 0; i < matrix.length; i++) {
//                ret = ret + lnFunc(matrix[i][j]);
//                colSum += matrix[i][j];
//            }
//
//            ret = ret - lnFunc(colSum);
//            total += colSum;
//        }
//
//        if (StatisticUtils.eq(total, 0)) {
//            return 0;
//        }
//
//        return -ret / (total * log2);
//    }

//    /**
//     * Computes conditional entropy of the columns given
//     * the rows.
//     *
//     * @param matrix the contingency table
//     * @return the conditional entropy of the columns given the rows
//     */
//    public static double entropyConditionedOnRows(double[][] matrix) {
//        double returnValue = 0;
//        double sumForRow;
//        double total = 0;
//
//        for (int i = 0; i < matrix.length; i++) {
//            sumForRow = 0;
//
//            for (int j = 0; j < matrix[0].length; j++) {
//                returnValue = returnValue + lnFunc(matrix[i][j]);
//                sumForRow += matrix[i][j];
//            }
//            returnValue = returnValue - lnFunc(sumForRow);
//            total += sumForRow;
//        }
//
//        if (StatisticUtils.eq(total, 0)) {
//            return 0;
//        }
//
//        return -returnValue / (total * log2);
//    }

//    /**
//     * Computes the columns' entropy for the given contingency table.
//     *
//     * @param matrix the contingency table
//     * @return the columns' entropy
//     */
//    public static double entropyOverColumns(double[][] matrix)
//    {
//        double returnValue = 0;
//        double sumForColumn;
//        double total = 0;
//
//        for (int j = 0; j < matrix[0].length; j++)
//        {
//            sumForColumn = 0;
//
//            for (int i = 0; i < matrix.length; i++)
//            {
//                sumForColumn += matrix[i][j];
//            }
//
//            returnValue = returnValue - lnFunc(sumForColumn);
//            total += sumForColumn;
//        }
//
//        if (StatisticUtils.eq(total, 0))
//        {
//            return 0;
//        }
//
//        return (returnValue + lnFunc(total)) / (total * log2);
//    }

//    /**
//     * Computes the rows' entropy for the given contingency table.
//     *
//     * @param matrix the contingency table
//     * @return the rows' entropy
//     */
//    public static double entropyOverRows(double[][] matrix)
//    {
//        double returnValue = 0;
//        double sumForRow;
//        double total = 0;
//
//        for (int i = 0; i < matrix.length; i++)
//        {
//            sumForRow = 0;
//
//            for (int j = 0; j < matrix[0].length; j++)
//            {
//                sumForRow += matrix[i][j];
//            }
//
//            returnValue = returnValue - lnFunc(sumForRow);
//            total += sumForRow;
//        }
//
//        if (StatisticUtils.eq(total, 0))
//        {
//            return 0;
//        }
//
//        return (returnValue + lnFunc(total)) / (total * log2);
//    }
    
    private static double lnFunc(double num) {
        // hard coded for efficiency reasons
        if (num < 1e-7) {
            return 0;
        } else {
            return num * Math.log(num);
        }
    }
    
//	// The following methods got mysteriously lost maybe during cvs-svn refactoring.
//	// For the time being I add method thunks which give a warning when called. (mkron)
//	public static double quadratic_entropy(double[] ds) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}
//
//	public static double mutual_information(double[] ds, double[] ds2, int nbins) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}
//
//	public static double quadratic_mutinf(double[] feature, double[] labels) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}
//
//	public static double quadratic_mutinf(double[] feature, double[] labels, int[] classes) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}
//
//	public static double SUquadratic(double[] feature, double[] labels) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}
//
//	public static double SUquadratic(double[] feature, double[] labels, int[] classes) {
//		// TODO Auto-generated method stub
//		System.err.println("warning, not implemented!");
//		return 0;
//	}

	/**
	 * Random Latin Hypercube Sampling within a given double range.
	 * 
	 * @see #rlh(int, int, double, double, boolean)
	 * 
	 * @param samples
	 * @param range
	 * @param edges 
	 * @return
	 */
	public static Matrix rlh( int samples, double[][] range, boolean edges) {
		Matrix rlhM = rlh(samples, range.length, 0., 1., edges);
		for( int j=0; j<samples; ++j ) {
			for( int i=0; i<range.length;++i ) {
					// carsten hatte nen bug im RLH - zählweise der indices beginnt bei 0, nicht bei 1
				rlhM.set(j, i, range[i][0]+rlhM.get(j,i)*(range[i][1]-range[i][0]));
			}
		}
		return rlhM;
	}
	
	/**
	 * Random Latin Hypercube Sampling, from "Engineering Design via Surrogate Modelling", p.17.
	 * The returned matrix is of dimension Matrix(samples,dim). If edges is true, the boundary values
	 * are included. Initial version by C. Henneges.
	 * 
	 * @param samples
	 * @param dim
	 * @param lb
	 * @param ub
	 * @param edges
	 * @return
	 */
	public static Matrix rlh( int samples, int dim, double lb, double ub, boolean edges) {
		//    		System.out.println( "Latin Hypercube sampling" );
		//    		++rlhsamplings;

		// perform Latin-Hypercube-Sampling to obtain a stratified sample of
		// function values over the optimization domain

		// Pre-allocate memory
		Matrix X = new Matrix( samples,dim );

		ArrayList<Integer> indices = new ArrayList<Integer>( samples );
		for( int i=0; i<samples; ++i ) { indices.add( i ); }

		// for i = 1:k
		for( int i=0; i<dim; ++i ) {

			// X(:,i) = randperm(n)';
			Collections.shuffle( indices );
			for( int j=0; j<indices.size(); ++j ) {
				X.set( j,i,indices.get(j) );
			}
			// end
		}

		// if edges==1
		if( edges ) {
			// X = (X-1)/(n-1);
			for( int i=0; i<X.getRowDimension(); ++i ) {
				for( int j=0; j<X.getColumnDimension(); ++j ) {
					double v = X.get(i,j);
					v = (v)/(samples-1);
					X.set( i,j,v );
				}
			}
			// else
		} else {
			// X = (X-0.5)/n;
			for( int i=0; i<X.getRowDimension(); ++i ) {
				for( int j=0; j<X.getColumnDimension(); ++j ) {
					double v = X.get(i,j);
					v = (v + 0.5)/samples;
					X.set( i,j,v );
				}
			}
		}
		// end

		// ------

		// Transform and Scale random values to [lb,ub]
		for( int i=0; i<X.getRowDimension(); ++i ) {
			for( int j=0; j<X.getColumnDimension(); ++j ) {
				double v = X.get(i,j);
				v = (ub-lb)*v + lb;
				X.set( i,j,v );
			}
		}

		return X;
	}

	/**
	 * Returns a list of point matrices which form a latin hypercube sampling of the given space.
	 * Each entry is a Matrix(dim,1).
	 * 
	 * @param samples
	 * @param dim
	 * @param lb
	 * @param ub
	 * @param edges
	 * @return
	 */
	public static ArrayList<Matrix> rlhPoints(int samples, int dim, double lb, double ub, boolean edges) 
	{
		ArrayList<Matrix> samplePoints = new ArrayList<Matrix>(samples);
		Matrix p = rlh( samples,dim,lb,ub,edges );

		for( int i=0; i<samples;++i ) {

			Matrix x = new Matrix(dim,1);
			for( int j=0; j<dim; ++j ) {
				x.set( j,0,p.get( i,j ) );
			}
			samplePoints.add( x );

		}
		return samplePoints;
	}
	
	/**
	 * Returns a list of point matrices which form a latin hypercube sampling of the given space.
	 * @param samples
	 * @param dim
	 * @param lb
	 * @param ub
	 * @param edges
	 * @return
	 */
	public static ArrayList<Matrix> rlhPoints(int samples, double[][] range, boolean edges) 
	{
		ArrayList<Matrix> rlhM = rlhPoints(samples, range.length, 0, 1, edges);
		for( int i=0; i<rlhM.size();++i ) {
			Matrix p = rlhM.get(i);
			for( int j=0; j<range.length; ++j ) {
					// carsten hatte nen bug im RLH - zählweise der indices beginnt bei 0, nicht bei 1
				p.set(j, 0, range[j][0]+p.get(j,0)*(range[j][1]-range[j][0]));
			}
		}
		return rlhM;
	}
}
