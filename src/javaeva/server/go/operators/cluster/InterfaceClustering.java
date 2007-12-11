package javaeva.server.go.operators.cluster;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.populations.Population;

import java.util.ArrayList;
//import javaeva.server.oa.go.GraphicUserInterface.Plot.TopoPlot;

/** This the interface to clustering algorithms, but since there
 * is no true concept on how to calculate a possbily problem
 * specific distance between two individuals, this is still to
 * be considered as under construction.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.04.2003
 * Time: 15:13:36
 * To change this template use Options | File Templates.
 */
public interface InterfaceClustering {

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /** This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be asociated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     * @param pop       The population of individuals that is to be clustered.
     * @return Population[]
     */
    public Population[] cluster(Population pop);

    /** This method allows you to decied if two species converge.
     * @param species1  The first species.
     * @param species2  The second species.
     * @return True if species converge, else False.
     */
    public boolean convergingSpecies(Population species1, Population species2);

    /** This method decides if an unclustered individual belongs to an already established species.
     * @param indy          A unclustered individual.
     * @param species       A species.
     * @return True or False.
     */
    public boolean belongsToSpecies(AbstractEAIndividual indy, Population species);

//        /** For debuggy only
//     * @param plot TopoPlot
//     */
//    public void draw(TopoPlot plot, Population pop);
}
