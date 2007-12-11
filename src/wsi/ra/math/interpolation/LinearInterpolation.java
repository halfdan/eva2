///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: LinearInterpolation.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Original author: Charles S. Stanton
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:23 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package wsi.ra.math.interpolation;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

//import cern.jet.stat.*;

/**
 * Defines the routines for the spline interpolation of data.
 */
public class LinearInterpolation
{
	AbstractDataSet abstractDataSet = null;
	private double[] x, y;
	//vectors of x,y
	private double sumX = 0;
	private double sumY = 0;
	private double sumXY = 0;
	private double sumXsquared = 0;
	private double sumYsquared = 0;
	private double Sxx, Sxy, Syy, n;
	private double a = 0, b = 0;
	//coefficients of regression
	private int dataLength;
	private double[][] residual;
	// residual[0][i] = x[i], residual[1][i]= residual
	private double maxAbsoluteResidual = 0.0;
	private double SSR = 0.0;
	//regression sum of squares
	private double SSE = 0.0;
	//error sum of squares
	private double minX = Double.POSITIVE_INFINITY;
	private double maxX = Double.NEGATIVE_INFINITY;
	private double minY = Double.POSITIVE_INFINITY;
	private double maxY = Double.NEGATIVE_INFINITY;

	//MISC
	String xName, yName;
	double aCILower, aCIUpper, bCILower, bCIUpper; //confidence interval
	double t, bSE, aSE;
	double MSE, F;
	static double[] t025 =
		{
			Double.NaN,
			12.706,
			4.303,
			3.182,
			2.776,
			2.571,
			2.447,
			2.365,
			2.306,
			2.262,
			2.228,
			2.201,
			2.179,
			2.160,
			2.145,
			2.131,
			2.120,
			2.110,
			2.101,
			2.093,
			2.086,
			2.080,
			2.075,
			2.069,
			2.064,
			2.060,
			2.056,
			2.052,
			2.048,
			2.045,
			1.960 };

	/*------------------------------------------------------------------------*
	 * constructor
	 *------------------------------------------------------------------------*/
	/**
	 * Initializes this class.
	 */
	public LinearInterpolation() throws InterpolationException
	{
		this.abstractDataSet = null;
	}

	/**
	 * Constructor for regression calculator.
	 *
	 * @param  x  is the array of x data
	 * @param  y  is the array of y data
	 */
	public LinearInterpolation(double[] x, double[] y)
	{
		this.x = x;
		this.y = y;
		if (x.length != y.length)
		{
			System.out.println("x, y vectors must be of same length");
		}
		else
		{
			dataLength = x.length;
			doStatistics();
		}

	}

	/**
	 * Initializes this class and calculates the second derivative of the spline.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 */
	public LinearInterpolation(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.setAbstractDataSet(abstractDataSet);
	}

	public void setAbstractDataSet(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.abstractDataSet = abstractDataSet;
		x = abstractDataSet.getXData();
		y = abstractDataSet.getYData();
		if (x.length != y.length)
		{
			System.out.println("x, y vectors must be of same length");
		}
		else
		{
			dataLength = x.length;
			doStatistics();
		}
	}

	/**
	 * Find the p value for a given value of F.
	 * Requires the COLT high performance library:
	 * http://hoschek.home.cern.ch/hoschek/colt/
	 *
	 * @param  fValue  the value for the CDF
	 * @return         The P value
	 */
	//	public double getP(double fValue) {
	//		double answer;
	//		double y1;
	//		double y2;
	//		//nu1 = 1;
	//		//x2 =1
	//		double nu2 = n - 2;
	//		y1 = nu2 / (nu2 + fValue);
	//		y2 = 0.0;
	//		answer = Gamma.incompleteBeta(nu2 / 2.0, 1 / 2.0, y1)
	//				 - Gamma.incompleteBeta(nu2 / 2.0, 1 / 2.0, y2);
	//		return answer;
	//	}

	/*
	 *  Here are the accessor methods
	 *
	 */
	/**
	 *  Gets the intercept of the regression line.
	 *
	 * @return    The intercept.
	 */
	public double getIntercept()
	{
		return a;
	}

	/**
	 *  Gets the Slope of the regression line.
	 *
	 * @return    The slope.
	 */
	public double getSlope()
	{
		return b;
	}

	/**
	 *  Gets the residuals of the regression.
	 *
	 * @return    The residuals.
	 */
	public double[][] getResiduals()
	{
		return residual;
	}

	/**
	 *  Gets the x data for the regression.
	 *
	 * @return    The array of x values.
	 */
	public double[] getDataX()
	{
		return x;
	}

	/**
	 *  Gets the y data for the regression.
	 *
	 * @return    The array of y values.
	 */
	public double[] getDataY()
	{
		return y;
	}

	/**
	 *  Gets the minimum of the x values.
	 *
	 * @return    The minimum.
	 */
	public double getMinX()
	{
		return minX;
	}

	/**
	 *  Gets the maximum of the x values.
	 *
	 * @return    The maximum.
	 */
	public double getMaxX()
	{
		return maxX;
	}

	/**
	 *  Gets the minimum of the y values.
	 *
	 * @return    The minumum.
	 */
	public double getMinY()
	{
		return minY;
	}

	/**
	 *  Gets the maximum of the y values.
	 *
	 * @return    The maximum.
	 */
	public double getMaxY()
	{
		return maxY;
	}

	/**
	 *  Gets the maximum absolute residual.
	 *
	 * @return    The maximum.
	 */
	public double getMaxAbsoluteResidual()
	{
		return maxAbsoluteResidual;
	}

	/**
	 *  Gets the sum of the square x deviations from mean of x.
	 *
	 * @return    The Sxx value
	 */
	public double getSxx()
	{
		return Sxx;
	}

	/**
	 *  Gets the sum of the square y deviations from mean of y.
	 *
	 * @return    The Syy value
	 */
	public double getSyy()
	{
		return Syy;
	}

	/**
	 *  Gets  SSR = Sxy * Sxy / Sxx;
	 *
	 * @return    The SSR value
	 */
	public double getSSR()
	{
		return SSR;
	}

	/**
	 *  Gets SSE = Syy - SSR.
	 *
	 * @return    The SSE value
	 */
	public double getSSE()
	{
		return SSE;
	}

	/**
	 *  Gets the mean square error MSE.
	 *
	 * @return    The MSE value
	 */
	public double getMSE()
	{
		return SSE / (n - 2);
	}

	/**
	 *  Gets the mean XBar of x.
	 *
	 * @return    The XBar value
	 */
	public double getXBar()
	{
		return sumX / n;
	}

	/**
	 *  Gets the mean YBar of y.
	 *
	 * @return    The YBar value
	 */
	public double getYBar()
	{
		return sumY / n;
	}

	/**
	 *  Gets the sample size.
	 *
	 * @return    The sample size.
	 */
	public int getDataLength()
	{
		return x.length;
	}

	/**
	 *  Gets the Pearson R statistic of the regression.
	 *
	 * @return    The PearsonR value
	 */
	public double getPearsonR()
	{
		return Sxy / Math.sqrt(Sxx * Syy);
	}

	/**
	 *  Gets the sum of the x squared values.
	 *
	 * @return    The sum of the x squared values.
	 */
	public double getSumXSquared()
	{
		return sumXsquared;
	}

	/**
	 * reset data to 0
	 */
	public void reset()
	{
		x = new double[0];
		y = new double[0];
		dataLength = 0;
		n = 0.0;
		residual = new double[0][0];

		sumX = 0;
		sumXsquared = 0;
		sumY = 0;
		sumYsquared = 0;
		sumXY = 0;

	}

	/**
	 *  Adds a new point to the regression (for interactive use).
	 *
	 * @param  xValue  The new x value
	 * @param  yValue  The new y value
	 */
	public void addPoint(double xValue, double yValue)
	{
		dataLength++;
		double[] xNew = new double[dataLength];
		double[] yNew = new double[dataLength];
		System.arraycopy(x, 0, xNew, 0, dataLength - 1);
		System.arraycopy(y, 0, yNew, 0, dataLength - 1);
		xNew[dataLength - 1] = xValue;
		yNew[dataLength - 1] = yValue;
		x = xNew;
		y = yNew;
		updateStatistics(xValue, yValue);
	}

	private void doStatistics()
	{
		//Find sum of squares for x,y and sum of xy
		for (int i = 0; i < dataLength; i++)
		{
			minX = Math.min(minX, x[i]);
			maxX = Math.max(maxX, x[i]);
			minY = Math.min(minY, y[i]);
			maxY = Math.max(maxY, y[i]);
			sumX += x[i];
			sumY += y[i];
			sumXsquared += x[i] * x[i];
			sumYsquared += y[i] * y[i];
			sumXY += x[i] * y[i];
		}
		//Caculate regression coefficients
		n = (double) dataLength;
		Sxx = sumXsquared - sumX * sumX / n;
		Syy = sumYsquared - sumY * sumY / n;
		Sxy = sumXY - sumX * sumY / n;
		b = Sxy / Sxx;
		a = (sumY - b * sumX) / n;
		SSR = Sxy * Sxy / Sxx;
		SSE = Syy - SSR;
		calculateResiduals();
	}

	private void calculateResiduals()
	{
		residual = new double[2][dataLength];
		maxAbsoluteResidual = 0.0;
		for (int i = 0; i < dataLength; i++)
		{
			residual[0][i] = x[i];
			residual[1][i] = y[i] - (a + b * x[i]);
			maxAbsoluteResidual =
				Math.max(maxAbsoluteResidual, Math.abs(y[i] - (a + b * x[i])));
		}
	}

	//update statistics for a single additional data point
	private void updateStatistics(double xValue, double yValue)
	{
		//Find sum of squares for x,y and sum of xy
		n++;
		sumX += xValue;
		sumY += yValue;
		sumXsquared += xValue * xValue;
		sumYsquared += yValue * yValue;
		sumXY += xValue * yValue;
		//Caculate regression coefficients
		n = (double) dataLength;
		Sxx = sumXsquared - sumX * sumX / n;
		Syy = sumYsquared - sumY * sumY / n;
		Sxy = sumXY - sumX * sumY / n;
		b = Sxy / Sxx;
		a = (sumY - b * sumX) / n;
		SSR = Sxy * Sxy / Sxx;
		SSE = Syy - SSR;
		calculateResiduals();
	}

	/**
	 * regression line y = a + bx.
	 * 
	 * @param x
	 * @return double
	 * @throws InterpolationException
	 */
	public double getY(double x) throws InterpolationException
	{
		return a + b * x;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(1000);

		sb.append("Regression Statistics for " + yName + " = a + b*" + xName);
		sb.append("");
		sb.append("Sample Statistics");
		int n = this.getDataLength();
		sb.append("Sample size     n = " + n);
		sb.append("Mean of " + yName + "   Y bar = " + this.getYBar());
		sb.append("s_Y");
		sb.append("= " + Math.sqrt(this.getSyy() / ((float) (n - 1))));
		sb.append("Pearson correlation R = " + this.getPearsonR());
		sb.append("");
		sb.append("Coefficient Estimates");
		a = this.getIntercept();
		b = this.getSlope();
		sb.append("a = " + a);
		sb.append("b = " + b);
		sb.append("");

		sb.append("95%  Confidence Intervals");

		if (n > 32)
		{
			t = t025[30];
		}
		else if (n > 2)
		{
			t = t025[n - 2];
		}
		else
		{
			t = Double.NaN;
		}
		MSE = this.getMSE();
		if (n > 2)
		{
			bSE = Math.sqrt(MSE / this.getSxx());
		}
		else
		{
			bSE = Double.NaN;
		}
		aSE = bSE * Math.sqrt(this.getSumXSquared() / n);
		aCILower = a - t * aSE;
		aCIUpper = a + t * aSE;
		sb.append("a   :    (" + aCILower + ", " + aCIUpper + ")");
		bCILower = b - t * bSE;
		bCIUpper = b + t * bSE;
		sb.append("b    :    (" + bCILower + ", " + bCIUpper + ")");
		sb.append("");
		sb.append("Analysis of Variance");
		sb.append("Source      Degrees Freedom   Sum of Squares");
		sb.append("");
		SSR = this.getSSR();
		//allow one degree of freedom for mean
		sb.append(
			"model                            1                                 "
				+ SSR);
		sb.append(
			"error                         "
				+ (n - 2)
				+ "                         "
				+ this.getSSE());
		sb.append(
			"total(corrected)              "
				+ (n - 1)
				+ "                            "
				+ this.getSyy());
		sb.append("");
		sb.append("MSE =" + MSE);
		F = SSR / MSE;
		sb.append("F = " + F + "    ");
		//sb.append("p = " + this.getP(F));

		return sb.toString();
	}
}
/****************************************************************************
 * END OF FILE
 ****************************************************************************/