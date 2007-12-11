package javaeva.server.go.operators.moso;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.archiving.ArchivingNSGAII;
import javaeva.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.06.2005
 * Time: 17:05:11
 * To change this template use File | Settings | File Templates.
 */
public class MOSOMOGARankBased implements InterfaceMOSOConverter, java.io.Serializable {

    public MOSOMOGARankBased() {
    }
    public MOSOMOGARankBased(MOSOMOGARankBased b) {
    }
    public Object clone() {
        return (Object) new MOSOMOGARankBased(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    public void convertMultiObjective2SingleObjective(Population pop) {
        int[] MOGARank = new int[pop.size()];
        for (int i = 0; i < MOGARank.length; i++) MOGARank[i] = 1;
        for (int i = 0; i < pop.size()-1; i++) {
            for (int j = 0; j < pop.size(); j++) {
                if (i != j) {
                    if (((AbstractEAIndividual)pop.get(j)).isDominatingDebConstraints((AbstractEAIndividual)pop.get(i))) {
                        MOGARank[i] += 1;
                    }
                }
            }
        }
        for (int i = 0; i < pop.size(); i++) {
            ((AbstractEAIndividual)pop.get(i)).SetData("MOGARank", new Integer(MOGARank[i]));
        }
        for (int i = 0; i < pop.size(); i++) {
             this.convertSingleIndividual((AbstractEAIndividual)pop.get(i));
        }
    }

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[]    resultFit = new double[1];
        double[]    tmpFit;

        tmpFit = indy.getFitness();
        indy.SetData("MOFitness", tmpFit);
        resultFit[0] = ((Integer)indy.getData("MOGARank")).doubleValue();
        indy.SetFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    public void setOutputDimension(int dim) {

    }

    /** This method returns a description of the objective
     * @return A String
     */
    public String getStringRepresentation() {
        return this.getName()+"\n";
    }


/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "MOGA Rank Based";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method calcuates the MOGA rank of each individual and uses the rank as fitness [Fonseca93Genetic].";
    }
}