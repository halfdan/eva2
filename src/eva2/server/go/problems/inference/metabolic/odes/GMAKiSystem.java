/**
 *
 */
package eva2.server.go.problems.inference.metabolic.odes;

// Created ad 2007-01-13

/**
 * In this class the valine/leucine biosynthesis in Corynebacterium glutamicum
 * is modeled to be in many reactions irreversible according to what is written
 * in the KEGG database. Inhibition is modeled as negative exponential function.
 *
 * @author Andreas Dr&auml;ger
 *
 */
public class GMAKiSystem extends AbstractValineSystem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = -5893476466596099997L;

	/**
	 *
	 * @param params
	 */
	public GMAKiSystem(double params[]) {
		this.p = params;
	}

	public GMAKiSystem() {
		this.p = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks.Des.DESystem#getValue(double,
	 *      double[])
	 */
	public double[] getValue(double t, double[] Y) {
		return linearCombinationOfVelocities(new double[] {
				p[0] * Math.pow(getPyr_2(t), 2)
						* Math.exp((-p[12]) * Y[3]),
				(p[1] * Y[2] * (0.04 - getNADP_2(t)) - p[10] * Y[0]
						* getNADP_2(t))
						* Math.exp((-p[13]) * Y[3]),
				p[2] * Y[0] * Math.exp((-p[14]) * Y[3]),
				p[3] * getGlut_2(t) * Y[5],
				p[4] * getAla_2(t) * Y[5],
				p[5] * Y[3] * Math.exp((-p[15]) * Y[4]),
				p[6] * Y[5] * Math.exp((-p[16]) * Y[4]),
				p[7] * getNAD_2(t) * Y[1],
				p[8] * getGlut_2(t) * Y[6] - p[11] * getAKG_2(t)
						* Y[4],
				p[9] * Y[4] * Math.exp((-p[17]) * Y[3]) });
	}

	public void getValue(double t, double[] Y, double[] resultVector) {
		double tmp[] = getValue(t, Y);
		System.arraycopy(tmp, 0, resultVector, 0, tmp.length);
	}

	@Override
	public int getNumberOfParameters() {
		return 18;
	}

}
