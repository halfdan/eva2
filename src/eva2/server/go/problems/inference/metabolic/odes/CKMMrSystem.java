package eva2.server.go.problems.inference.metabolic.odes;

/**
 * This class describes the valine/leucine biosynthesis in <it>Corynebacterium
 * glutamicum</it> acording to the convenience kinetics as proposed by
 * Liebermeister and Klipp. Three reaction velocities follow the traditional
 * Michaelis Menten scheme the others are given in the convenience kinetics.
 * However, in this class all reactions are assumed to be reversible.
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date 2007-02-05
 */
public class CKMMrSystem extends AbstractValineSystem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = 726678613269764327L;
	transient protected double velocities[] = null;

	/**
	 * @param x
	 */
	public CKMMrSystem(double[] x) {
		this.p = x;
	}

	public CKMMrSystem() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eva2.server.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {

		double[] res = new double[7];
		getValue(t, Y, res);
		return res;

	}

	public void getValue(double t, double[] Y, double[] res) {
		double pyr = getPyr_2(t), nadp = getNADP_2(t), nadph = 0.04 - nadp, glut = getGlut_2(t), akg = getAKG_2(t), ala = getAla_2(t), nad = getNAD_2(t), nadh = 0.8 - nad;

		if (velocities == null)
			velocities = new double[10];

		// v_1
		velocities[0] = ((p[0] * p[44] * Math.pow(pyr, 2)) / Math.pow(p[14], 2) - (p[7]
				* p[44] * Y[2])
				/ p[15])
				/ ((1 + pyr / p[14] + Math.pow(pyr / p[14], 2) + Y[2] / p[15]) * (p[44] + Y[3]));
		// v_2
		velocities[1] = ((p[1] * p[45] * Y[2] * nadph) / (p[16] * p[17]) - (p[8]
				* p[45] * Y[0] * nadp)
				/ (p[18] * p[19]))
				/ ((1 + Y[2] / p[16] + nadph / p[17] + (Y[2] * nadph)
						/ (p[16] * p[17]) + Y[0] / p[18] + nadp / p[19] + (Y[0] * nadp)
						/ (p[18] * p[19])) * (p[45] + Y[3]));
		// v_3
		velocities[2] = ((p[47] * Y[0]) / p[20] - (p[48] * Y[5]) / p[21])
				/ (1 + p[51] * Y[3] + (Y[0] / p[20] + Y[5] / p[21])
						* (1 + p[54] * Y[3]));
		// v_4
		velocities[3] = ((p[2] * Y[5] * glut) / (p[22] * p[23]) - (p[9] * Y[3] * akg)
				/ (p[24] * p[25]))
				/ (1 + Y[5] / p[22] + glut / p[23] + (Y[5] * glut)
						/ (p[22] * p[23]) + Y[3] / p[24] + akg / p[25] + (Y[3] * akg)
						/ (p[24] * p[25]));
		// v_5
		velocities[4] = ((p[3] * Y[5] * ala) / (p[26] * p[27]) - (p[10] * Y[3] * pyr)
				/ (p[28] * p[29]))
				/ (1 + Y[5] / p[26] + ala / p[27] + (Y[5] * ala)
						/ (p[26] * p[27]) + Y[3] / p[28] + pyr / p[29] + (Y[3] * pyr)
						/ (p[28] * p[29]));
		// v_6
		velocities[5] = (p[49] * Y[3])
				/ (p[30] + p[30] * p[52] * Y[4] + Y[3] + p[55] * Y[3] * Y[4]);
		// v_7
		velocities[6] = ((p[4] * p[46] * Y[5] * p[57]) / (p[31] * p[32]) - (p[11]
				* p[46] * Y[1] * p[58])
				/ (p[33] * p[34]))
				/ ((1 + Y[5] / p[31] + p[57] / p[32] + (Y[5] * p[57])
						/ (p[31] * p[32]) + Y[1] / p[33] + p[58] / p[34] + (Y[1] * p[58])
						/ (p[33] * p[34])) * (p[46] + Y[4]));
		// v_8
		velocities[7] = ((p[5] * Y[1] * nad) / (p[35] * p[36]) - (p[12] * Y[6] * nadh)
				/ (p[37] * p[38]))
				/ (1 + Y[1] / p[35] + nad / p[36] + (Y[1] * nad)
						/ (p[35] * p[36]) + Y[6] / p[37] + nadh / p[38] + (Y[6] * nadh)
						/ (p[37] * p[38]));
		// v_9
		velocities[8] = ((p[6] * Y[6] * glut) / (p[39] * p[40]) - (p[13] * Y[4] * akg)
				/ (p[41] * p[42]))
				/ (1 + Y[6] / p[39] + glut / p[40] + (Y[6] * glut)
						/ (p[39] * p[40]) + Y[4] / p[41] + akg / p[42] + (Y[4] * akg)
						/ (p[41] * p[42]));
		// v_10
		velocities[9] = (p[50] * Y[4])
				/ (p[43] + p[43] * p[53] * Y[3] + Y[4] + p[56] * Y[3] * Y[4]);

		linearCombinationOfVelocities(velocities, res);
	}

	@Override
	public int getNumberOfParameters() {
		return 59;
	}
}
