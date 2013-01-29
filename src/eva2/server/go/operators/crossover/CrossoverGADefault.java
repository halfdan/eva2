package eva2.server.go.operators.crossover;


import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * This operator performs one-point crossover.
 *
 * Created by IntelliJ IDEA. User: streiche Date: 03.04.2003 Time: 10:34:17 To
 * change this template use Options | File Templates.
 */
public class CrossoverGADefault implements InterfaceCrossover,
    java.io.Serializable {
	private InterfaceOptimizationProblem	m_OptimizationProblem;

	public CrossoverGADefault() {

	}

	public CrossoverGADefault(CrossoverGADefault c) {
		this.m_OptimizationProblem = c.m_OptimizationProblem;
	}

	/**
	 * This method will enable you to clone a given mutation operator
	 *
	 * @return The clone
	 */
    @Override
	public Object clone() {
		return new CrossoverGADefault(this);
	}

	/**
	 * This method performs crossover on two individuals. If the individuals do
	 * not implement InterfaceGAIndividual, then nothing will happen.
	 *
	 * @param indy1
	 *          The first individual
	 * @param partners
	 *          The second individual
	 */
    @Override
	public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
	    Population partners) {
		AbstractEAIndividual[] result = null;
		result = new AbstractEAIndividual[partners.size() + 1];
		result[0] = (AbstractEAIndividual) (indy1).clone();
		for (int i = 0; i < partners.size(); i++) {
            result[i + 1] = (AbstractEAIndividual) ((AbstractEAIndividual) partners
                .get(i)).clone();
        }
		// for (int i = 0; i < result.length; i++) System.out.println("Before
		// Crossover: " +result[i].getSolutionRepresentationFor());
		if (partners.size() == 0) return result;
		if ((indy1 instanceof InterfaceGAIndividual)
		    && (partners.get(0) instanceof InterfaceGAIndividual)) {
			// Currently we will only handle two parents
			int crossoverpoint = RNG.randomInt(0,
			    ((InterfaceGAIndividual) indy1).getGenotypeLength() - 1);
			boolean tmpValue;
			BitSet[] tmpBitSets = new BitSet[2];
			tmpBitSets[0] = ((InterfaceGAIndividual) result[0]).getBGenotype();
			tmpBitSets[1] = ((InterfaceGAIndividual) result[1]).getBGenotype();
			for (int i = crossoverpoint; i < ((InterfaceGAIndividual) result[0])
			    .getGenotypeLength(); i++) {
				if (tmpBitSets[0].get(i))
					tmpValue = true;
				else tmpValue = false;
				if (tmpBitSets[1].get(i))
					tmpBitSets[0].set(i);
				else tmpBitSets[0].clear(i);
				if (tmpValue)
					tmpBitSets[1].set(i);
				else tmpBitSets[1].clear(i);
			}
			((InterfaceGAIndividual) result[0]).SetBGenotype(tmpBitSets[0]);
			((InterfaceGAIndividual) result[1]).SetBGenotype(tmpBitSets[1]);
		}
		// in case the crossover was successfull lets give the mutation operators a
		// chance to mate the strategy parameters
		for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1,
                partners);
        }
		// for (int i = 0; i < result.length; i++) System.out.println("After
		// Crossover: " +result[i].getSolutionRepresentationFor());
		return result;
	}

	/**
	 * This method allows you to evaluate weather two crossover operators are
	 * actually the same.
	 *
	 * @param crossover
	 *          The other crossover operator
	 */
    @Override
	public boolean equals(Object crossover) {
		if (crossover instanceof CrossoverGADefault)
			return true;
		else return false;
	}

	/**
	 * This method will allow the crossover operator to be initialized depending
	 * on the individual and the optimization problem. The optimization problem is
	 * to be stored since it is to be called during crossover to calculate the
	 * exogene parameters for the offsprings.
	 *
	 * @param individual
	 *          The individual that will be mutated.
	 * @param opt
	 *          The optimization problem.
	 */
    @Override
	public void init(AbstractEAIndividual individual,
	    InterfaceOptimizationProblem opt) {
		this.m_OptimizationProblem = opt;
	}

    @Override
	public String getStringRepresentation() {
		return this.getName();
	}

	/*****************************************************************************
	 * These are for GUI
	 */
	/**
	 * This method allows the CommonJavaObjectEditorPanel to read the name to the
	 * current object.
	 *
	 * @return The name.
	 */
	public String getName() {
		return "GA default crossover";
	}

	/**
	 * This method returns a global info string
	 *
	 * @return description
	 */
	public static String globalInfo() {
		return "This is a one-point crossover between two individuals.";
	}
}
