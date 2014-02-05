package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;

import java.util.BitSet;

/**
 * This crossover-Method tries to convert the first individual into the second. If a better Individual is found on the way, this individual is chosen.
 * If no better individual is found, one with the greatest distance from the both is chosen
 *
 * @author Alex
 */
public class CM7 implements InterfaceCrossover, java.io.Serializable, InterfaceEvaluatingCrossoverOperator {
    private InterfaceOptimizationProblem optimizationProblem;
    private int evaluations = 0;

    public CM7() {

    }

    public CM7(CM7 c) {
        this.optimizationProblem = c.optimizationProblem;
        this.evaluations = c.evaluations;
    }

    @Override
    public Object clone() {
        return new CM7(this);
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
                                       Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[1];
        if (partners.size() > 0) ;
        if (indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary) {
            BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            BitSet dataSave = (BitSet) data.clone();
            BitSet data2 = ((InterfaceDataTypeBinary) partners.getEAIndividual(0)).getBinaryData();
            double f1 = indy1.getFitness(0);
            double f2 = partners.getEAIndividual(0).getFitness(0);
            double min = Math.min(f1, f2);
            int different = 0;
            boolean foundBetter = false;
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) != data2.get(i)) {
                    different++;
                    data.flip(i);
                    ((InterfaceDataTypeBinary) indy1).setBinaryGenotype(data);
                    this.optimizationProblem.evaluate(indy1);
                    this.evaluations++;
                    if (indy1.getFitness(0) < min) {
                        foundBetter = true;
                        i = data.size();
                    }
                }
            }
            if (!foundBetter) {
                for (int i = 0; i < dataSave.size(); i++) {
                    if (dataSave.get(i) != data2.get(i) && different > 0) {
                        dataSave.flip(i);
                    }
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

    @Override
    public int getEvaluations() {
        return this.evaluations;
    }

    /**
     * **************************************************
     * GUI
     */

    public String getName() {
        return "Combination Method 7";
    }

    public static String globalInfo() {
        //TODO
        return "";
    }

    @Override
    public void resetEvaluations() {
        this.evaluations = 0;
    }

}
