///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: SplineInterpolation.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:42 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package eva2.tools.math.interpolation;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/**
 * Defines the routines for the spline interpolation of data.
 */
public class SplineInterpolation
{
	AbstractDataSet abstractDataSet = null;
	double[] secondDerivative = null;
	double[] xArray = null;
	double[] yArray = null;
	boolean ascendingData = true;

	/*------------------------------------------------------------------------*
	 * constructor
	 *------------------------------------------------------------------------*/
	/**
	 * Initializes this class.
	 */
	public SplineInterpolation() throws InterpolationException
	{
		this.abstractDataSet = null;
		this.secondDerivative = null;
	}

	/**
	 * Initializes this class and calculates the second derivative of the spline.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 */
	public SplineInterpolation(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.setAbstractDataSet(abstractDataSet);
	}

	/**
	 * Sets the new <code>AbstractDataSet</code> and calculates the second
	 * derivative of the spline.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 */
	public void setAbstractDataSet(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.abstractDataSet = abstractDataSet;
		double[] x = abstractDataSet.getXData();
		double[] y = abstractDataSet.getYData();
		boolean ascending = false;
		boolean descending = false;
		int n = x.length;

		xArray = new double[n];
		yArray = new double[n];
		xArray[n - 1] = x[0];
		yArray[n - 1] = y[0];
		for (int i = 0; i < n - 1; i++)
		{
			xArray[i] = x[n - i - 1];
			yArray[i] = y[n - i - 1];
			if (x[i] < x[i + 1])
			{
				//if(descending)throw new InterpolationException("The x values must be"+
				//          " in continous ascending/descending order.");
				ascending = true;
			}
			else
			{
				//if(ascending)throw new InterpolationException("The x values must be"+
				//          " in continous ascending/descending order.");
				descending = true;
			}
		}
		ascendingData = ascending;

		if (ascendingData)
		{
			xArray = null;
			yArray = null;
			xArray = abstractDataSet.getXData();
			yArray = abstractDataSet.getYData();
		}
		this.secondDerivative =
			spline(
				xArray,
				yArray,
				(yArray[1] - yArray[0]) / (xArray[1] - xArray[0]),
				(yArray[n - 1] - yArray[n - 2]) / (xArray[1] - xArray[n - 2]));
	}

	/**
	 * Uses the spline with the calculated second derivative values to calculate
	 * the <code>y</code> value. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 5, pages 173-176.</a><br>
	 */
	public double getY(double x) throws InterpolationException
	{
		return splineInterpolation(xArray, yArray, secondDerivative, x);
	}

	public double getDerivative(double x) throws InterpolationException
	{
		return splineInterpolatedDerivative(
			xArray,
			yArray,
			secondDerivative,
			x);
	}

	/**
	 * Calculates the second derivative of the data. It's important that the
	 * x<sub>i</sub> values of the function y<sub>i</sub>=f(x<sub>i</sub>) are
	 * in ascending order, as x<sub>0</sub>&lt;x<sub>1</sub>&lt;x<sub>2</sub>&lt;... .
	 * This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 113-116.</a><br>
	 */
	public double[] spline(double[] x, double[] y, double yp0, double ypn)
		throws InterpolationException
	{
		if (x[0] > x[1])
			throw new InterpolationException(
				"The x values must be" + " in ascending order.");
		int n = x.length;
		double[] y2 = new double[n];
		double[] u = new double[n - 1];
		int i, k;
		double p, qn, sig, un;

		if (yp0 > 0.99e30)
			y2[0] = u[0] = 0.0;
		else
		{
			y2[0] = -0.5;
			u[0] =
				(3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp0);
		}
		for (i = 2; i <= n - 1; i++)
		{
			sig = (x[i - 1] - x[i - 2]) / (x[i] - x[i - 2]);
			p = sig * y2[i - 2] + 2.0;
			y2[i - 1] = (sig - 1.0) / p;
			u[i - 1] =
				(y[i] - y[i - 1]) / (x[i] - x[i - 1])
					- (y[i - 1] - y[i - 2]) / (x[i - 1] - x[i - 2]);
			u[i - 1] =
				(6.0 * u[i - 1] / (x[i] - x[i - 2]) - sig * u[i - 2]) / p;
		}
		if (ypn > 0.99e30)
		{
			qn = un = 0.0;
		}
		else
		{
			qn = 0.5;
			un =
				(3.0 / (x[n - 1] - x[n - 2]))
					* (ypn - (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]));
		}
		y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
		for (k = n - 1; k >= 1; k--)
		{
			y2[k - 1] = y2[k - 1] * y2[k] + u[k - 1];
		}

		return y2;
	}

	/**
	 * Calculates the second derivative of the data. This algorithm
	 * was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 113-116.</a><br>
	 */
	public double splineInterpolation(
		double[] xa,
		double[] ya,
		double[] y2a,
		double x)
		throws InterpolationException
	{
		int n = xa.length;
		if (n != ya.length || n != y2a.length)
		{
			throw new InterpolationException("Arrays have different lengths.");
		}
		double y;
		int klo, khi, k;
		double h, b, a;

		klo = 0;
		khi = n - 1;
		while (khi - klo > 1)
		{
			k = (khi + klo) >> 1;
			if (xa[k] > x)
				khi = k;
			else
				klo = k;
		}
		h = xa[khi] - xa[klo];
		//System.out.println(""+x+" between "+xa[khi]+" "+xa[klo]);
		if (h == 0.0)
			throw new InterpolationException("Two identical x values. The values must be distinct.");
		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;
		y =
			a * ya[klo]
				+ b * ya[khi]
				+ ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi])
					* (h * h)
					/ 6.0;
		return y;
	}

	/**
	 * Calculates the second derivative of the data. This algorithm
	 * was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 113-116.</a><br>
	 */
	public double splineInterpolatedDerivative(
		double[] xa,
		double[] ya,
		double[] y2a,
		double x)
		throws InterpolationException
	{
		int n = xa.length;
		if (n != ya.length || n != y2a.length)
		{
			throw new InterpolationException("Arrays have different lengths.");
		}
		double dydx;
		int klo, khi, k;
		double h, b, a;

		klo = 0;
		khi = n - 1;
		while (khi - klo > 1)
		{
			k = (khi + klo) >> 1;
			if (xa[k] > x)
				khi = k;
			else
				klo = k;
		}
		h = xa[khi] - xa[klo];
		//System.out.println(""+x+" between "+xa[khi]+" "+xa[klo]);
		if (h == 0.0)
			throw new InterpolationException("Two identical x values. The values must be distinct.");
		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;
		dydx =
			(ya[khi] - ya[klo]) / h
				- ((3 * (a * a) - 1) * h * y2a[klo]) / 6.0
				+ ((3 * (b * b) - 1) * h * y2a[khi]) / 6.0;
		return dydx;
	}
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/