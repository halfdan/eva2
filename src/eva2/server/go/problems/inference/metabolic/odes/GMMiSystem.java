package eva2.server.go.problems.inference.metabolic.odes;

// Created at 2007-01-18

/**
 * This class implements a system of ordinary differential equations describing
 * the valine and leucin biosynthesis in <it>Corynebacterium gluthamicum</i>.
 *
 * @author Andreas Dr&auml;ger
 */
public class GMMiSystem extends AbstractValineSystem {

	/**
	 * Generated version id.
	 */
	private static final long serialVersionUID = 6119930633376953563L;


	/**
	 * <p>
	 * A vector of parameters it needed to initialize this class. The parameters
	 * are given as:
	 * </p>
	 * <ul>
	 * <li>params[0-6] = k, Rate constant for the forward reaction</li>
	 * <li>params[7-8] = l, Rate constant for the backward reaction</li>
	 * <li>params[9-11] = ki, Rate constant for inhibition of uncertain
	 * mechanism</li>
	 * <li>params[12-14] = vm, Maximal reaction velocity</li>
	 * <li>params[15-17] = kia, Rate constant for inhibition of the enzyme</li>
	 * <li>params[18-20] = kib, Rate constant for inhibition of the enzyme
	 * substrate complex</li>
	 * <li>params[21-23] = km, the Michaelis-Menten constant</li>
	 * </ul>
	 *
	 * @param params
	 */
	public GMMiSystem(double[] params) {
		if (params != null)
			if (params.length != 24)
				throw new Error(
						"This ODE system needs exactly 24 parameters. Given are "
								+ params.length);
		this.p = params;
	}

	/**
	 *
	 *
	 */
	public GMMiSystem() {
		this.p = null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {
		double[] v = new double[] {
		// v_1
				(p[0] * Math.pow(getPyr_2(t), 2)) / (1 + p[9] * Y[3]),
				// v_2
				(p[1] * Y[2] * (0.04 - getNADP_2(t)) - p[7] * Y[0]
						* getNADP_2(t))
						/ (1 + p[10] * Y[3]),
				// v_3
				// ((p[12] * Y[0])/p[21])/(1 + p[15] * Y[3] + (Y[0] + p[18] *
				// Y[3] *
				// Y[0])/p[21]),
				(p[12] * Y[0])
						/ (p[21] + Y[0] + p[21] * p[15] * Y[3] + p[18] * Y[0]
								* Y[3]),
				// v_4
				p[2] * Y[5] * getGlut_2(t),
				// v_5
				p[3] * Y[5] * getAla_2(t),
				// v_6
				// (p[13]/p[22] * Y[3])/(1 + p[16] * Y[4] + (Y[3] + p[19] * Y[4]
				// *
				// Y[3])/p[22]),
				(p[13] * Y[3])
						/ (p[22] + Y[3] + p[22] * p[16] * Y[4] + p[19] * Y[3]
								* Y[4]),
				// v_7
				(p[4] * Y[5]) / (1 + p[11] * Y[4]),
				// v_8
				p[5] * Y[1] * getNAD_2(t),
				// v_9
				p[6] * Y[6] * getGlut_2(t) - p[8] * Y[4] * getAKG_2(t),
				// v_10
				// ((p[14] * Y[4])/p[23])/(1 + p[17] * Y[3] + (Y[4] + p[20] *
				// Y[3] *
				// Y[4])/p[23])
				(p[14] * Y[4])
						/ (p[23] + Y[4] + p[17] * p[23] * Y[3] + p[20] * Y[3]
								* Y[4]) };

		/*
		 * if ((Double.isNaN(v[0]) || Double.isInfinite(v[0])) && (t < -1.784)) {
		 * System.out.println(t+". Nenner: "+(1 + p[9] * Y[3])+"\tY[3] =
		 * "+Y[3]+"\tparam9 = "+p[9]); }//
		 */
		return linearCombinationOfVelocities(v);
	}

	public void getValue(double t, double[] Y, double[] resultVector) {
		double tmp[] = getValue(t, Y);
		System.arraycopy(tmp, 0, resultVector, 0, tmp.length);
	}

	@Override
	public int getNumberOfParameters() {
		return 24;
	}

}
