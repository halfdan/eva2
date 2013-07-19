
package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.03.2004
 * Time: 13:44:55
 * To change this template use File | Settings | File Templates.
 */
public class MOSOUtilityFunction implements InterfaceMOSOConverter, java.io.Serializable {

    private int                 m_OutputDimension = 2;

    public MOSOUtilityFunction () {
    }
    public MOSOUtilityFunction(MOSOUtilityFunction b) {
        System.out.println("Warning no source!");
        this.m_OutputDimension  = b.m_OutputDimension;
    }
    @Override
    public Object clone() {
        return (Object) new MOSOUtilityFunction(this);
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
        double[]    resultFit = new double[1];
        double[]    tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        resultFit[0] = 0;

        /**********************************************************************************************
         * limit editing to this area
         */

        resultFit[0] = tmpFit[1];
        System.out.println("Editied");

        /**********************************************************************************************
         * and don't forget to set the reduced fitness to the individual
         */
        indy.setFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem.
     * @param dim       Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        this.m_OutputDimension = dim;
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
        return "Utility Function";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method allows you to progamm an individual utility function.";
    }

//    /** This method allows you to edit the source for the MOSOUtilityFunction
//     * and therefore alter the utility function itself!
//     * @param newSource     The new source code for the ultility function
//     */
//    public void setSource (Source newSource) {
//        m_Source = newSource;
//    }
//    public Source getSource() {
//        return m_Source;
//    }
//    public String sourceTipText() {
//        return "Edit the source code for the utility function, but limit editing to the convertSingleIndividual() method.";
//    }
}
