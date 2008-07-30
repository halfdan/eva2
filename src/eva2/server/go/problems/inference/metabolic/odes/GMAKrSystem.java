package eva2.server.go.problems.inference.metabolic.odes;

/**
 * This class describes the valine/leucine biosynthesis in Corynebacterium
 * glutamicum. All 10 reactions are assumed to be reversible and modeled
 * following the generalized mass action kinetic, where inhibition is modeled
 * using the function 1/(1 + ki*[Inhibitor]).
 *
 * @date 2007-02-04
 * @author Andreas Dr&auml;ger
 */
public class GMAKrSystem extends AbstractValineSystem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = -5668558229555497614L;
	transient double[] veloc = null;

	public GMAKrSystem() {
		super();
	}

	/**
	 * @param params
	 */
	public GMAKrSystem(double params[]) {
		super();
		this.p = params;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {
		// a <- params[0..9]
		// b <- params[10..17]
		// g <- params[18..23]
		double[] res = new double[7];
		getValue(t, Y, res);
		return res;
		// return linearCombinationOfVelocities(new double[] {
		// (params[0] * Math.pow(getPyr_2(t), 2) - params[10] * Y[2])/ (1 +
		// params[18] * Y[3]), // Math.exp((-params[18]) * Y[3]),
		// (params[1] * Y[2] * (.04 - getNADP_2(t)) - params[11] * Y[0] *
		// getNADP_2(t)) / (1 + params[19] * Y[3]), // * Math.exp((-params[19])
		// *
		// Y[3]),
		// (params[2] * Y[0] - params[12] * Y[5]) / (1 + params[20] * Y[3]), //
		// *
		// // Math.exp((-params[20])
		// // *
		// // Y[3]),
		// params[3] * getGlut_2(t) * Y[5] - params[13] * getAKG_2(t) * Y[3],
		// params[4] * getAla_2(t) * Y[5] - params[14] * getPyr_2(t) * Y[3],
		// (params[5] * Y[3]) / (1 + params[21] * Y[4]), // *
		// // Math.exp((-params[21])
		// // * Y[4]),
		// (params[6] * Y[5] - params[15] * Y[1]) / (1 + params[22] * Y[4]), //
		// *
		// // Math.exp((-params[22])
		// // *
		// // Y[4]),
		// params[7] * getNAD_2(t) * Y[1] - params[16] * Y[6] * (.8 -
		// getNAD_2(t)),
		// params[8] * getGlut_2(t) * Y[6] - params[17] * getAKG_2(t) * Y[4],
		// (params[9] * Y[4]) / (1 + params[23] * Y[3]) // *
		// // Math.exp((-params[23])
		// // * Y[3])
		// });
	}

	public void getValue(double t, double[] Y, double[] res) {
		if (veloc == null)
			veloc = new double[10];

		veloc[0] = (p[0] * Math.pow(getPyr_2(t), 2) - p[10] * Y[2])
				/ (1 + p[18] * Y[3]); // Math.exp((-params[18]) * Y[3]),
		veloc[1] = (p[1] * Y[2] * (.04 - getNADP_2(t)) - p[11] * Y[0]
				* getNADP_2(t))
				/ (1 + p[19] * Y[3]); // * Math.exp((-params[19]) * Y[3]),
		veloc[2] = (p[2] * Y[0] - p[12] * Y[5])
				/ (1 + p[20] * Y[3]); // *
		// Math.exp((-params[20])
		// *
		// Y[3]),
		veloc[3] = p[3] * getGlut_2(t) * Y[5] - p[13] * getAKG_2(t)
				* Y[3];
		veloc[4] = p[4] * getAla_2(t) * Y[5] - p[14] * getPyr_2(t)
				* Y[3];
		veloc[5] = (p[5] * Y[3]) / (1 + p[21] * Y[4]); // *
		// Math.exp((-params[21])
		// * Y[4]),
		veloc[6] = (p[6] * Y[5] - p[15] * Y[1])
				/ (1 + p[22] * Y[4]); // *
		// Math.exp((-params[22])
		// *
		// Y[4]),
		veloc[7] = p[7] * getNAD_2(t) * Y[1] - p[16] * Y[6]
				* (.8 - getNAD_2(t));
		veloc[8] = p[8] * getGlut_2(t) * Y[6] - p[17] * getAKG_2(t)
				* Y[4];
		veloc[9] = (p[9] * Y[4]) / (1 + p[23] * Y[3]); // *
		linearCombinationOfVelocities(veloc, res);
	}

	@Override
	public int getNumberOfParameters() {
		return 24;
	}

}
