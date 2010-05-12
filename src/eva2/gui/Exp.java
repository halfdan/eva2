package eva2.gui;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import eva2.tools.chart2d.DFunction;

/**
 *
 */
public class Exp extends DFunction {
	private double minValue = 1e-10; // think of a minimal value we want to show in case invalid (<=0) values are requested
	
	public void setMinValue(double v) {
		if (v>0) minValue = v;
		else System.err.println("Error, minimal value for Exp must be positive!");
	}
	/*
	 * (non-Javadoc)
	 * @see eva2.tools.chart2d.DFunction#isDefinedAt(double)
	 */
	public boolean isDefinedAt(double source) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.tools.chart2d.DFunction#isInvertibleAt(double)
	 */
	public boolean isInvertibleAt(double image) {
		return image > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.tools.chart2d.DFunction#getImageOf(double)
	 */
	public double getImageOf(double source) {
		return Math.exp(source);
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.tools.chart2d.DFunction#getSourceOf(double)
	 */
	public double getSourceOf(double target) {
		if (target <= 0) {
			return Math.log(minValue); // think of a minimal value we want to show in case invalid values are requested
//			throw new IllegalArgumentException(
//					"Can not calculate log on values smaller than or equal 0 --> target = "
//							+ target);
		}
		return Math.log(target);
	}
	
	public void updateMinValue(double y) {
		if (y<minValue && (y>0)) minValue=y;
	}
}
