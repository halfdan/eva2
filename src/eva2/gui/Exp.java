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
			throw new IllegalArgumentException(
					"Can not calculate log on values smaller than or equal 0 --> target = "
							+ target);
		}
		return Math.log(target);
	}
}
