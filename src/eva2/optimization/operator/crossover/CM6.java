package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.BinaryScatterSearch;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * Score driven Crossover-Method. It uses the same score as the BinaryScatterSearch.
 * Only the first individual of the given Population in the mate method is used. the rest is only for calculating the score
 * It only mates 2 Individuals, not more
 */
@Description("Score driven crossover method")
public class CM6 implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;

    public CM6() {

    }

    public CM6(CM6 c) {
        this.optimizationProblem = c.optimizationProblem;
    }

    @Override
    public Object clone() {
        return new CM6(this);
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
                                       Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[1];
        if (indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary) {
            BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            BitSet data2 = ((InterfaceDataTypeBinary) partners.getIndividual(0)).getBinaryData();
            for (int j = 0; j < data2.size(); j++) {
                if (data2.get(j)) {
                    data.set(j, true);
                }
            }
            partners.remove(0);
            for (int i = 0; i < data.size(); i++) {
                if (!(data.get(i) && RNG.flipCoin(Math.min(0.1 + BinaryScatterSearch.score(i, partners), 1)))) {
                    data.set(i, false);
                }
            }
            ((InterfaceDataTypeBinary) indy1).setBinaryGenotype(data);
        }
        result[0] = indy1;
        return result;
    }

    @Override
    public void init(AbstractEAIndividual individual,
                     InterfaceOptimizationProblem opt) {
        this.optimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return getName();
    }

    public String getName() {
        return "Combination Method 6";
    }
}
