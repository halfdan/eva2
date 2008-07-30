/**
 *
 */
package eva2.server.go.problems.inference.metabolic.odes;

/**
 * Created at 2007-02-03
 *
 * This class describes the valine/leucine biosynthesis in <it>Corynebacterium
 * glutamicum</it> acording to the convenience kinetics as proposed by
 * Liebermeister and Klipp. Three reaction velocities follow the traditional
 * Michaelis Menten scheme the others are given in the convenience kinetics. All
 * reactions are considered to be ehter reversible or irreversible according to
 * what is written in the KEGG database.
 *
 * @author Andreas Dr&auml;ger
 *
 */
public class CKMMiSystem extends AbstractValineSystem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = 8595111459805099502L;


	/**
	 *
	 */
	public CKMMiSystem() {
		this.p = null;
	}

	/**
	 * @param params
	 *            The parameters of this system.
	 */
	public CKMMiSystem(double[] params) {
		this.p = params;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {
		// double[] kcat = new double[9]; // -> p[0..9]
		// double[] ki = new double[3]; // -> p[9..11]
		// double[] km = new double[16]; // -> p[12..27]
		// double[] vm = new double[3]; // -> p[28..30]
		// double[] kmm = new double[3]; // -> p[31..33]
		// double[] kia = new double[3]; // -> p[34..36]
		// double[] kib = new double[3]; // -> p[37..39]
		// double acCoA = 0; // -> p[40]

		double pyr = getPyr_2(t), nadp = getNADP_2(t), nadph = 0.04 - nadp, glut = getGlut_2(t), ala = getAla_2(t);
		/*
		 * Y[0] DHIV Y[1] IPM Y[2] AcLac Y[3] Val Y[4] Leu Y[5] KIV Y[6] KIC
		 */

		return linearCombinationOfVelocities(new double[] {
		/* Korrigiert: */
		// v_1: AHAS: convenience (ge�ndert)
				(p[0] * p[9] * Math.pow(pyr / p[12], 2))
						/ ((1 + pyr / p[12] + Math.pow(pyr / p[12], 2)) * (p[9] + Y[3])),

				// v_2: AHAIR: convenience (ge�ndert)
				(p[1] * Y[2] / p[13] * nadph / p[14] * p[10] - p[2] * Y[0]
						/ p[15] * nadp / p[16] * p[10])
						/ ((1 + Y[2] / p[13] + nadph / p[14] + (Y[2] * nadph)
								/ (p[13] * p[14]) + Y[0] / p[15] + nadp / p[16] + (Y[0] * nadp)
								/ (p[15] * p[16])) * (p[10] + Y[3])),

				// v_3: DHAD: reversible Michaelis Menten with two inhibitions
				// (ge�ndert)
				(p[28] * Y[0])
						/ (p[31] + Y[0] + Y[3] * (p[31] * p[34] + p[37] * Y[0])),

				// v_4: BCAAT_ValB: convenience (ge�ndert)
				(p[3] * Y[5] / p[17] * glut / p[18])
						/ (1 + Y[5] / p[17] + glut / p[18] + (Y[5] * glut)
								/ (p[17] * p[18])),

				// v_5: BCAAT_ValC: convenience (ge�ndert)
				(p[4] * Y[5] / p[19] * ala / p[20])
						/ (1 + Y[5] / p[19] + ala / p[20] + (Y[5] * ala)
								/ (p[19] * p[20])),

				// v_6: Trans_Val: irreversible Michaelis Menten with two
				// inhibitions
				(p[29] * Y[3])
						/ (p[32] + Y[3] + p[32] * p[35] * Y[4] + p[38] * Y[3]
								* Y[4]),

				// v_7: IPMS: convenience (AcCoA/K_m = p40) (ge�ndert)
				(p[5] * Y[5] / p[21] * p[40] * p[11])
						/ ((1 + Y[5] / p[21] + p[40] + p[40] * Y[5] / p[21]) * (p[11] + Y[4])),

				// v_8: IPMDH: convenience (ge�ndert)
				(p[6] * Y[1] / p[22] * nad / p[23])
						/ (1 + Y[1] / p[22] + nad / p[23] + (Y[1] * nad)
								/ (p[22] * p[23])),

				// v_9: BCAAT_LeuB: convenience (ge�ndert)
				(p[7] * Y[6] / p[24] * glut / p[25] - p[8] * Y[4] / p[26] * akg
						/ p[27])
						/ (1 + Y[6] / p[24] + glut / p[25] + (Y[6] * glut)
								/ (p[24] * p[25]) + Y[4] / p[26] + akg / p[27] + (Y[4] * akg)
								/ (p[26] * p[27])),

				// v_10: Trans_Leu: irreversible Michaelis Menten with two
				// inhibitions
				(p[30] * Y[4])
						/ (p[33] + Y[4] + p[33] * p[36] * Y[3] + p[39] * Y[3]
								* Y[4])

		/*
		 * zuvor: // AHAS: convenience (p[0] * pyr)/(p[9] * (1 + pyr/p[12] +
		 * Math.pow(pyr/p[12], 2)) + Y[3] * (1 + pyr/p[12] + Math.pow(pyr/p[12],
		 * 2))), // AHAIR: convenience (p[1] * Y[2] * nadph - p[2] * Y[0] *
		 * nadp)/((1 + Y[2]/p[13] + nadph/p[14] + (Y[2]*nadph)/(p[13]*p[14]) +
		 * Y[0]/p[15] + nadp/p[16] + (Y[0] * nadp)/(p[15] * p[16])) * p[10] *
		 * Y[3]), // DHAD: reversible Michaelis Menten with two inhibitions
		 * (p[28]* Y[0])/(p[31] + Y[0] + Y[3] * (p[28] * p[34] + p[37] * Y[0])), //
		 * BCAAT_ValB: convenience (p[3] * Y[5] * glut)/(1 + Y[5]/p[17] +
		 * glut/p[18] + (Y[5] * glut)/(p[17] * p[18])), // BCAAT_ValC:
		 * convenience (p[4] * Y[5] * ala)/(1 + Y[5]/p[19] + ala/p[20] + (Y[5] *
		 * ala)/(p[19] * p[20])), // Trans_Val: irreversible Michaelis Menten
		 * with two inhibitions (p[29] * Y[3])/(p[32] + Y[3] + p[32] * p[35] *
		 * Y[4] + p[38] * Y[3] * Y[4]), // IPMS: convenience (p[5] * Y[5])/((1 +
		 * Y[5]/p[21] + p[40] + (p[40] * Y[5])/p[21]) * (p[11] + Y[4])), //
		 * IPMDH: convenience (p[6] * Y[1] * nad)/(1 + Y[1]/p[22] + nad/p[23] +
		 * (Y[1] * nad)/(p[22] * p[23])), // BCAAT_LeuB: convenience (p[7] *
		 * Y[6] * glut - p[8] * Y[4] * akg)/(1 + Y[6]/p[24] + glut/p[25] + (Y[6] *
		 * glut)/(p[24] * p[25]) + Y[4]/p[26] + akg/p[27] + (Y[4] * akg)/(p[26] *
		 * p[27])), // Trans_Leu: irreversible Michaelis Menten with two
		 * inhibitions (p[30] * Y[4])/(p[33] + Y[4] + p[33] * p[36] * Y[3] +
		 * p[39] * Y[3] * Y[4])
		 */
		});
	}


	public void getValue(double t, double[] Y, double[] resultVector) {
		double tmp[] = getValue(t, Y);
		System.arraycopy(tmp, 0, resultVector, 0, tmp.length);
	}

	@Override
	public int getNumberOfParameters() {
		return 41;
	}

}
