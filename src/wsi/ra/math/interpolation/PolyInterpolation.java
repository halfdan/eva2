///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: PolyInterpolation.java,v $
//  Purpose:  Some interpolation stuff.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision: 1.1 $
//            $Date: 2003/07/22 19:25:30 $
//            $Author: wegnerj $
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
///////////////////////////////////////////////////////////////////////////////

package wsi.ra.math.interpolation;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

/**
 * Defines the routines for the interpolation of data.
 */
public class PolyInterpolation
{
	AbstractDataSet abstractDataSet = null;
	boolean sloppy = true;
	double[] polynomialCoefficients = null;

	/*------------------------------------------------------------------------*
	 * constructor
	 *------------------------------------------------------------------------*/
	/**
	 * Initializes this class.
	 */
	public PolyInterpolation() throws InterpolationException
	{
		this.abstractDataSet = null;
		sloppy = true;
		polynomialCoefficients = null;
	}

	/**
	 * Initializes this class and calculates the coefficients of the polynom.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 */
	public PolyInterpolation(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.abstractDataSet = abstractDataSet;
		sloppy = true;
		this.polynomialCoefficients = calculatePolynomialCoefficients();
	}

	/**
	 * Initializes this class and calculates the coefficients of the polynom.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 * @param sloppy if <code>true</code> Neville's algorithm which is used in the
	 *     <code>polynomialInterpolation</code>-routines does only print a
	 *    warning message on the screen and does not throw an
	 *    <code>Exception</code> if two x values are identical.
	 */
	public PolyInterpolation(AbstractDataSet abstractDataSet, boolean sloppy)
		throws InterpolationException
	{
		this.abstractDataSet = abstractDataSet;
		this.sloppy = sloppy;
		this.polynomialCoefficients = calculatePolynomialCoefficients();
	}

	/**
	 * Sets the new <code>AbstractDataSet</code> and calculates the coefficients
	 * of the polynom.
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 */
	public void setAbstractDataSet(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		this.abstractDataSet = abstractDataSet;
		this.polynomialCoefficients = calculatePolynomialCoefficients();
	}

	/**
	 * Uses the polynom with the calculated coefficients to calculate the
	 * <code>y</code> value. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 5, pages 173-176.</a><br>
	 * The Neville's algorithm which is used in the <code>polynomialInterpolation</code>-
	 * routines returns also the error of this interpolated point.
	 *
	 * @param x the x value
	 * @return the interpolated y value
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getYandDerivatives(double, int)
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 */

	public double getY(double x)
	{
		int n = polynomialCoefficients.length - 1;
		double y = polynomialCoefficients[n];
		for (int j = n - 1; j >= 0; j--)
			y = y * x + polynomialCoefficients[j];

		return y;
	}

	/**
	 * Uses the polynom with the calculated coefficients to calculate the
	 * <code>y</code> value and the derivatives at the point <code>x</code>,
	 * <code>y</code>. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 5, pages 173-176.</a><br>
	 * The Neville's algorithm which is used in the <code>polynomialInterpolation</code>-
	 * routines returns also the error of this interpolated point.
	 *
	 * @param x the x value
	 * @param ndDerivateNumber the number of the calculated derivatives
	 * @return the interpolated y value at ...[0], the 1st derivativ value at
	 *         ...[1], the 2nd derivativ at ...[2] and so on ...
	 * @see #getY(double)
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 */
	public double[] getYandDerivatives(double x, int ndDerivateNumber)
		throws InterpolationException
	{
		if (ndDerivateNumber < 0)
			throw new InterpolationException("Negative derivative numbers make no sense.");
		else if (ndDerivateNumber == 0)
		{
			double[] pd = new double[1];
			pd[0] = getY(x);
			return pd;
		}

		int nnd, j, i;
		int nc = polynomialCoefficients.length - 1;
		double[] pd = new double[ndDerivateNumber + 1];
		double cnst = 1.0;

		pd[0] = polynomialCoefficients[nc];
		for (j = 1; j <= ndDerivateNumber; j++)
			pd[j] = 0.0;
		for (i = nc - 1; i >= 0; i--)
		{
			nnd = (ndDerivateNumber < (nc - i) ? ndDerivateNumber : nc - i);
			for (j = nnd; j >= 1; j--)
				pd[j] = pd[j] * x + pd[j - 1];
			pd[0] = pd[0] * x + polynomialCoefficients[i];
		}
		for (i = 2; i <= ndDerivateNumber; i++)
		{
			cnst *= i;
			pd[i] *= cnst;
		}

		return pd;
	}

	/**
	 * Neville's interpolation algorithm. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @param x the x value
	 * @return the interpolated y value and the interpolation error
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 */
	public PolynomialInterpolationResult polynomialInterpolation(double x)
		throws InterpolationException
	{
		if (abstractDataSet == null)
			throw new InterpolationException(
				"No data." + " The AbstractDataSet was not defined.");
		return polynomialInterpolation(
			abstractDataSet.getXData(),
			abstractDataSet.getYData(),
			x);
	}

	/**
	 * Neville's interpolation algorithm. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 * @param x the x value
	 * @return the interpolated y value and the interpolation error
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 */
	public PolynomialInterpolationResult polynomialInterpolation(
		AbstractDataSet abstractDataSet,
		double x)
		throws InterpolationException
	{
		if (abstractDataSet == null)
			throw new InterpolationException(
				"No data." + " The AbstractDataSet was not defined.");
		return polynomialInterpolation(
			abstractDataSet.getXData(),
			abstractDataSet.getYData(),
			x);
	}

	/**
	 * Neville's interpolation algorithm. This algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @param xa the array of x values
	 * @param ya the array of y values
	 * @param x the x value
	 * @return the interpolated y value and the interpolation error
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 */
	public PolynomialInterpolationResult polynomialInterpolation(
		double[] xa,
		double[] ya,
		double x)
		throws InterpolationException
	{
		if (xa == null || ya == null)
			throw new InterpolationException("No data.");
		int i, m, ns = 1;
		double den, dif, dift, ho, hp, w;
		double[] c = new double[xa.length + 1];
		double[] d = new double[xa.length + 1];
		PolynomialInterpolationResult result =
			new PolynomialInterpolationResult();

		dif = Math.abs(x - xa[1 - 1]);
		for (i = 1; i <= xa.length; i++)
		{
			if ((dift = Math.abs(x - xa[i - 1])) < dif)
			{
				ns = i;
				dif = dift;
			}
			c[i] = ya[i - 1];
			d[i] = ya[i - 1];
			//System.out.println("x"+xa[i-1]+" y"+ya[i-1]);
		}
		result.y = ya[ns - 1];
		//System.out.println("y="+result.y+" ns="+ns);
		ns--;
		for (m = 1; m < xa.length; m++)
		{
			for (i = 1; i <= xa.length - m; i++)
			{
				ho = xa[i - 1] - x;
				hp = xa[i + m - 1] - x;
				w = c[i + 1] - d[i];
				if ((den = ho - hp) == 0.0)
				{
					if (sloppy)
					{
						System.out.println(
							"Two identical x values. The values must be distinct.");
						den = 1.0;
					}
					else
						throw new InterpolationException("Two identical x values.");
				}
				den = w / den;
				d[i] = hp * den;
				c[i] = ho * den;
			}
			result.y
				+= (result.yError =
					(2 * ns < (xa.length - m) ? c[ns + 1] : d[ns--]));
		}
		return result;
	}

	/**
	 * Calculates the coefficients of a polynom of the grade <code>N-1</code>. This
	 * interpolation algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @return the array with the polynomial coefficients y = ...[0] +
	 *         ...[1]*x<SUP>2</SUP> + ...[2]*x<SUP>3</SUP> + ...
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 */
	public double[] calculatePolynomialCoefficients()
		throws InterpolationException
	{
		if (abstractDataSet == null)
			throw new InterpolationException(
				"No data." + " The AbstractDataSet was not defined.");
		return calculatePolynomialCoefficients(
			abstractDataSet.getXData(),
			abstractDataSet.getYData());
	}

	/**
	 * Calculates the coefficients of a polynom of the grade <code>N-1</code>. This
	 * interpolation algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @param abstractDataSet the <code>AbstractDataSet</code>
	 * @return the array with the polynomial coefficients y = ...[0] +
	 *         ...[1]*x<SUP>2</SUP> + ...[2]*x<SUP>3</SUP> + ...
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(double[], double[])
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 */
	public double[] calculatePolynomialCoefficients(AbstractDataSet abstractDataSet)
		throws InterpolationException
	{
		if (abstractDataSet == null)
			throw new InterpolationException(
				"No data." + " The AbstractDataSet was not defined.");
		return calculatePolynomialCoefficients(
			abstractDataSet.getXData(),
			abstractDataSet.getYData());
	}

	/**
	 * Calculates the coefficients of a polynom of the grade <code>N-1</code>. This
	 * interpolation algorithm was taken from:<br>
	 * <a href="http://www.ulib.org/webRoot/Books/Numerical_Recipes/" target="_top">
	 * William H. Press, Saul A. Teukolosky, William T. Vetterling, Brian P. Flannery,
	 * "Numerical Recipes in C - The Art of Scientific Computing", Second Edition,
	 * Cambridge University Press,
	 * ISBN 0-521-43108-5,
	 * chapter 3, pages 108-122.</a><br>
	 *
	 * @param x the array of x values
	 * @param y the array of y values
	 * @return the array with the polynomial coefficients y = ...[0] +
	 *         ...[1]*x<SUP>2</SUP> + ...[2]*x<SUP>3</SUP> + ...
	 * @see #calculatePolynomialCoefficients()
	 * @see #calculatePolynomialCoefficients(AbstractDataSet)
	 * @see #polynomialInterpolation(double)
	 * @see #polynomialInterpolation(AbstractDataSet, double)
	 * @see #polynomialInterpolation(double[], double[], double)
	 * @see #getY(double)
	 * @see #getYandDerivatives(double, int)
	 */
	public double[] calculatePolynomialCoefficients(double x[], double y[])
	{
		int k, j, i, n = x.length - 1;
		double phi, ff, b;
		double[] s = new double[n + 1];
		double[] cof = new double[n + 1];

		for (i = 0; i <= n; i++)
		{
			s[i] = cof[i] = 0.0;
		}
		s[n] = -x[0];

		for (i = 1; i <= n; i++)
		{
			for (j = n - i; j <= n - 1; j++)
			{
				s[j] -= x[i] * s[j + 1];
			}
			s[n] -= x[i];
		}

		for (j = 0; j < n; j++)
		{
			phi = n + 1;
			for (k = n; k >= 1; k--)
			{
				phi = k * s[k] + x[j] * phi;
			}
			ff = y[j] / phi;
			b = 1.0;
			for (k = n; k >= 0; k--)
			{
				cof[k] += b * ff;
				b = s[k] + x[j] * b;
			}
		}
		return cof;
	}
}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/