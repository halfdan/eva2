package eva2.optimization.operator.moso;

import eva2.gui.PropertyDoubleArray;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This method minimizes the Lp metric to a given target fitness values, for (p<1) this equals the Tchebycheff metric.")
public class MOSOLpMetric implements InterfaceMOSOConverter, java.io.Serializable {

    private PropertyDoubleArray reference = null;
    private int p = 2;

    public MOSOLpMetric() {
        double[] tmpD = new double[2];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = 0.0;
        }
        this.reference = new PropertyDoubleArray(tmpD);
    }

    public MOSOLpMetric(MOSOLpMetric b) {
        this.p = b.p;
        if (b.reference != null) {
            this.reference = (PropertyDoubleArray) b.reference.clone();
        }
    }

    @Override
    public Object clone() {
        return new MOSOLpMetric(this);
    }

    /**
     * This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     *
     * @param pop The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
            this.convertSingleIndividual((AbstractEAIndividual) pop.get(i));
        }
    }

    /**
     * This method processes a single individual
     *
     * @param indy The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[] resultFit = new double[1];
        double[] tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        if (p >= 1) {
            // standard Lp Metric
            resultFit[0] = 0;
            for (int i = 0; (i < this.reference.getNumRows()) && (i < tmpFit.length); i++) {
                resultFit[0] += Math.pow(Math.abs(tmpFit[i] - this.reference.getValue(i, 0)), this.p);
            }
            resultFit[0] = Math.pow(resultFit[0], 1 / ((double) this.p));
        } else {
            // Tchebycheff metric
            resultFit[0] = Double.NEGATIVE_INFINITY;
            for (int i = 0; (i < this.reference.getNumRows()) && (i < tmpFit.length); i++) {
                resultFit[0] += Math.max(Math.abs(tmpFit[i] - this.reference.getValue(i, 0)), resultFit[0]);
            }
        }

        indy.setFitness(resultFit);
    }

    /**
     * This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     *
     * @param dim Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        double[] newWeights = new double[dim];

        for (int i = 0; i < newWeights.length; i++) {
            newWeights[i] = 0.0;
        }
        for (int i = 0; (i < this.reference.getNumRows()) && (i < newWeights.length); i++) {
            newWeights[i] = this.reference.getValue(i, 0);
        }

        this.reference.setDoubleArray(newWeights);
    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        String result = "Lp Metric\n";
        result += " P           = " + this.p + "\n";
        result += " Ref.Fitness = (";
        for (int i = 0; i < reference.getNumRows(); i++) {
            result += reference.getValue(i, 0);
            if (i < (reference.getNumRows() - 1)) {
                result += "; ";
            }
        }
        result += ")\n";
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Lp Metric";
    }

    /**
     * This method allows you to choose the reference for the Lp Metric
     *
     * @param reference The reference vector.
     */
    public void setReference(PropertyDoubleArray reference) {
        this.reference = reference;
    }

    public PropertyDoubleArray getReference() {
        return this.reference;
    }

    public String referenceTipText() {
        return "Choose the reference for the fitness values.";
    }

    /**
     * This method allows you to choose the p for the Lp Metric
     *
     * @param p
     */
    public void setP(int p) {
        this.p = Math.max(1, p);
    }

    public int getP() {
        return this.p;
    }

    public String pTipText() {
        return "Choose the exponent p for the Lp metric, if (p<1) this results in the Tchebycheff metric.";
    }

}
