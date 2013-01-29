package eva2.server.go.operators.moso;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 11:02:21
 * To change this template use File | Settings | File Templates.
 */
public class MOSONoConvert implements InterfaceMOSOConverter, java.io.Serializable {

    public MOSONoConvert() {
    }
    public MOSONoConvert(MOSONoConvert b) {
    }
    @Override
    public Object clone() {
        return (Object) new MOSONoConvert(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
             this.convertSingleIndividual((AbstractEAIndividual)pop.get(i));
        }
    }

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[]    tmpFit;

        tmpFit          = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
//        resultFit[0]    = tmpFit[RNG.randomInt(0, tmpFit.length)];
//        indy.SetFitness(resultFit);
    }    

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension
     */
    @Override
    public void setOutputDimension(int dim) {
        // nothing to do here
    }

    /** This method returns a description of the objective
     * @return A String
     */
    @Override
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
    @Override
    public String getName() {
        return "No Convert";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method leaves everything the same.";
    }

}
