package eva2.server.go.problems.inference.metabolic;

import eva2.server.go.problems.inference.metabolic.odes.CKMMiSystem;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created at 2007-02-04
 *
 * @author Andreas Dr&auml;ger
 */
public class CKMMiProblem extends AbstractValineCurveFittingProblem {

	/**
	 * Generated serial id.
	 */
	private static final long serialVersionUID = 3459101339325025847L;

	public CKMMiProblem() {
		super(new CKMMiSystem());
	}

	public CKMMiProblem(CKMMiProblem ckmmiProblem) {
		super(ckmmiProblem);
	}

	public String getName() {
		return "CKMMiProblem";
	}
	
	protected double[][] getParameterRanges() {
		int i;
		double[][] range = new double[m_ProblemDimension][2];
		for (i = 0; i < 9; i++) { // double[] kcat = new double[9]; // ->
			// p[0..9]
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		for (i = 9; i < 12; i++) { // double[] ki = new double[3]; // ->
			// p[9..11]
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		for (i = 12; i < 28; i++) { // double[] km = new double[16]; // ->
			// p[12..27]
			range[i][0] = 1E-8;
			range[i][1] = 2000;
		}
		for (i = 28; i < 31; i++) { // double[] vm = new double[3]; // ->
			// p[28..30]
			range[i][0] = 0;
			range[i][1] = 2000;
		}
		for (i = 31; i < 34; i++) { // double[] kmm = new double[3]; // ->
			// p[31..33]
			range[i][0] = 1E-8;
			range[i][1] = 2000;
		}
		for (i = 34; i < 37; i++) { // double[] kia = new double[3]; // ->
			// p[34..36]
			range[i][0] = 0;
			range[i][1] = 1E+8;
		}
		for (i = 37; i < 40; i++) { // double[] kib = new double[3]; // ->
			// p[37..39]
			range[i][0] = 0;
			range[i][1] = 1E+8;
		}
		range[40][0] = 1E-8; // double acCoA = 0; // -> p[40]
		range[40][1] = 2000;
		return range;
	}

	@Override
	public Object clone() {
		return new CKMMiProblem(this);
	}

	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "Parameter optimization problem for the valine and leucine biosynthesis"
				+ " in C. glutamicum, where 7 reactions are modeled using convenience kinetics"
				+ " and 3 reactions with Michaelis Menten kinetics. Only two reactions"
				+ " are considered reversible.";
	}

}
