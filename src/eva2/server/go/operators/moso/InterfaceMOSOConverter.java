package eva2.server.go.operators.moso;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** The MOSO converter work on the fitness values only and convert a double[] into
 * a single double values, thus allowing weight aggregation, goal programming and
 * so on. To allow logging the original double[] values are stored in the userData
 * using the key "MOFitness".
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.03.2004
 * Time: 10:44:14
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceMOSOConverter {

    /** This method makes a perfect clone
     * @return the clone
     */
    public Object clone();

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    public void convertMultiObjective2SingleObjective(Population pop);

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       output dimension
     */
    public void setOutputDimension(int dim);

//    /** This method allows the problem to set the names of the output variables
//     * @param dim       Output names
//     */
//    public void setOutputNames(String[] dim);

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    public void convertSingleIndividual(AbstractEAIndividual indy);

    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName();

    /** This method returns a description of the objective
     * @return A String
     */
    public String getStringRepresentation();
}
