package eva2.server.go.problems.inference.metabolic.odes;

/**
 * This class describes the valine/leucine biosynthesis in Corynebacterium
 * gluthamicum where three reactions are modeled using traditional reversible
 * Michaelis Menten kinetics and the remaining reactions are modeled using
 * reversible generalized mass action kinetics, where inhibition is modeled
 * using 1/(1 + ki*[Inhibitor]). Created at 2007-02-04.
 *
 * @author Andreas Dr&auml;ger
 */
public class GMMrSystem extends AbstractValineSystem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = 7461506193192163451L;

	/**
	 *
	 */
	public GMMrSystem() {
	}

	/**
	 * @param params
	 */
	public GMMrSystem(double[] params) {
		this.p = params;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {
		/*
		 * double[] a = null, // <- params[0..6] 7 b = null, // <- params[7..13]
		 * 7 vm = null, // <- params[14..17] 4 km = null, // <- params[18..21] 4
		 * kia = null, // <- params[22..24] 3 kib = null, // <- params[25..27] 3
		 * ki = null; // <- params[28..30] 3 //
		 */

		return linearCombinationOfVelocities(new double[] {
		// v_1
				(p[0] * Math.pow(getPyr_2(t), 2) - p[7] * Y[2])
						/ (1 + p[28] * Y[3]),
				// v_2
				(p[1] * Y[2] * (.04 - getNADP_2(t)) - p[8] * Y[0]
						* getNADP_2(t))
						/ (1 + p[29] * Y[3]),
				// v_3
				((p[14] * Y[0]) / p[18] - (p[15] * Y[5]) / p[19])
						/ (1 + p[22] * Y[3] + (Y[0] / p[18] + Y[5] / p[19])
								* (1 + p[25] * Y[3])),
				// v_4
				p[2] * Y[5] * getGlut_2(t) - p[9] * Y[3] * getAKG_2(t),
				// v_5
				p[3] * Y[5] * getAla_2(t) - p[10] * Y[3] * getPyr_2(t),
				// v_6
				(p[16] * Y[3])
						/ (p[20] + p[20] * p[23] * Y[4] + Y[3] + p[26] * Y[4]
								* Y[3]),
				// v_7
				(p[4] * Y[5] - p[11] * Y[1]) / (1 + p[30] * Y[4]),
				// v_8
				p[5] * Y[1] * getNAD_2(t) - p[12] * Y[6] * (.8 - getNAD_2(t)),
				// v_9
				p[6] * Y[6] * getGlut_2(t) - p[13] * Y[4] * getAKG_2(t),
				// v_10
				(p[17] * Y[4])
						/ (p[21] + p[21] * p[24] * Y[3] + Y[4] + p[27] * Y[3]
								* Y[4]) });
	}

	public void getValue(double t, double[] Y, double[] resultVector) {
		resultVector = getValue(t, Y);
	}

	@Override
	public int getNumberOfParameters() {
		return 31;
	}

}