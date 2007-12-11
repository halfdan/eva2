package javaeva.server.go.operators.cluster;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.operators.distancemetric.InterfaceDistanceMetric;
import javaeva.server.go.operators.distancemetric.PhenotypeMetricDoubleData;
import javaeva.server.go.populations.Population;

import java.util.ArrayList;

/** The DBSCAN method. As far as I recall this is an hierachical
 * clustering method like the single-link method.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.04.2003
 * Time: 15:17:53
 * To change this template use Options | File Templates.
 */
public class ClusteringDensityBased implements InterfaceClustering, java.io.Serializable {

    private InterfaceDistanceMetric     m_Metric            = new PhenotypeMetricDoubleData();
    private double                      m_ClusterDistance   = 0.1;
    private int                         m_MinimumGroupSize  = 3;
    private boolean[][]                 ConnectionMatrix;
    private boolean[]                   Clustered;
    private boolean                     m_TestConvergingSpeciesOnBestOnly = true;

    public ClusteringDensityBased() {

    }

    public ClusteringDensityBased(ClusteringDensityBased a) {
        if (a.m_Metric != null) this.m_Metric           = (InterfaceDistanceMetric)a.m_Metric.clone();
        this.m_TestConvergingSpeciesOnBestOnly  = a.m_TestConvergingSpeciesOnBestOnly;
        this.m_ClusterDistance                  = a.m_ClusterDistance;
        this.m_MinimumGroupSize                 = a.m_MinimumGroupSize;
        if (a.Clustered != null) {
            this.Clustered = new boolean[a.Clustered.length];
            for (int i = 0; i < this.Clustered.length; i++) {
                if (a.Clustered[i]) this.Clustered[i] = true;
                else this.Clustered[i] = false;
            }
        }
        if (a.ConnectionMatrix != null) {
            this.ConnectionMatrix = new boolean[a.ConnectionMatrix.length][a.ConnectionMatrix[0].length];
            for (int i = 0; i < this.ConnectionMatrix.length; i++) {
                for (int j = 0; j < this.ConnectionMatrix[i].length; j++) {
                    if (a.ConnectionMatrix[i][j]) this.ConnectionMatrix[i][j] = true;
                    else this.ConnectionMatrix[i][j] = false;
                }
            }
        }
    }

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone() {
        return (Object) new ClusteringDensityBased(this);
    }

    /** This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be asociated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     * @param pop       The population of individuals that is to be clustered.
     * @return Population[]
     */
    public Population[] cluster(Population pop) {
        ConnectionMatrix    = new boolean[pop.size()][pop.size()];
        Clustered           = new boolean[pop.size()];
        AbstractEAIndividual   tmpIndy1, tmpIndy2;
        Population              PopulationOfUnclustered = new Population(), Cluster, template;
        ArrayList<Population>               ClusteredPopulations = new ArrayList<Population>();

        template = (Population)pop.clone();
        template.clear();
        PopulationOfUnclustered = (Population)template.clone();
        ClusteredPopulations.add(PopulationOfUnclustered);

        // Build the connection Matrix
        for (int i = 0; i < pop.size(); i++) {
            tmpIndy1 = (AbstractEAIndividual)pop.get(i);
            ConnectionMatrix[i][i] = true;
            for (int j = i; j < pop.size(); j++) {
                tmpIndy2 = (AbstractEAIndividual)pop.get(j);
                if (this.m_Metric.distance(tmpIndy1, tmpIndy2) < this.m_ClusterDistance) {
                    ConnectionMatrix[i][j] = true;
                    ConnectionMatrix[j][i] = true;
                } else {
                    ConnectionMatrix[i][j] = false;
                    ConnectionMatrix[j][i] = false;
                }
            }
        }

        for (int i = 0; i < Clustered.length; i++) Clustered[i] = false;

        // Now identify clusters within pop and add them to the result
        for (int i = 0; i < ConnectionMatrix.length; i++) {
            if (!Clustered[i]) {
                Cluster                 = (Population)template.clone();
                this.addRowToPopulation(i, Cluster, pop);
                if (Cluster.size() >= this.m_MinimumGroupSize) ClusteredPopulations.add(Cluster);
                else PopulationOfUnclustered.addPopulation(Cluster);
            }
        }

        Population[] result = new Population[ClusteredPopulations.size()];
        for (int i = 0; i < ClusteredPopulations.size(); i++) result[i] = ClusteredPopulations.get(i);
        return result;
    }

    /** This method adds all Connected and !Clustered Individuals form row index
     * to pop
     * @param index     The index of the row that is to be computed.
     * @param cluster   The Cluster to which the individuals are to be added
     * @param source    The source which is to be clustered.
     */
    private void addRowToPopulation(int index, Population cluster, Population source) {
        for (int i = 0; i < ConnectionMatrix[index].length; i++) {
            if ((!Clustered[i]) && (ConnectionMatrix[index][i])) {
                Clustered[i] = true;
                ConnectionMatrix[index][i] = false;
                ConnectionMatrix[i][index] = false;
                cluster.add(source.get(i));
                this.addRowToPopulation(i, cluster, source);
            }
        }
    }

    /** This method allows you to decied if two species converge.
     * @param species1  The first species.
     * @param species2  The second species.
     * @return True if species converge, else False.
     */
    public boolean convergingSpecies(Population species1, Population species2) {
        if (m_TestConvergingSpeciesOnBestOnly) {
            if (this.m_Metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual()) < this.m_ClusterDistance) return true;
            else return false;
        } else {
            Population tmpPop = new Population(species1.size()+species2.size());
            tmpPop.addPopulation(species1);
            tmpPop.addPopulation(species2);
            if (this.cluster(tmpPop).length <= 2) return true;
            else return false;
        }
    }

    /** This method decides if a unclustered individual belongs to an already established species.
     * @param indy          A unclustered individual.
     * @param species       A species.
     * @return True or False.
     */
    public boolean belongsToSpecies(AbstractEAIndividual indy, Population species) {
        if (this.m_TestConvergingSpeciesOnBestOnly) {
            if (this.m_Metric.distance(indy, species.getBestEAIndividual()) < this.m_ClusterDistance) return true;
            else return false;
        } else {
            Population tmpPop = (Population)species.clone();
            tmpPop.add(indy);
            if (this.cluster(tmpPop)[0].size() == 0) return true;
            else return false;
        }
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "A density-based Clustering Algorithm (DBSCAN).";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "DBSCAN";
    }

    /** This method allows you to set/get the distance metric used by the DBSCAN method.
     * @return The currently used distance metric.
     */
    public InterfaceDistanceMetric getMetric() {
        return this.m_Metric;
    }
    public void setMetric(InterfaceDistanceMetric m){
        this.m_Metric = m;
    }
    public String metricTipText() {
        return "Choose the distance metric to use.";
    }
    /** This method allows you to set/get the distance threshhold of the DBSCAN method.
     * @return The currently used distance threshhold.
     */
    public double getClusterDistance() {
        return this.m_ClusterDistance;
    }
    public void setClusterDistance(double m){
        if (m < 0) m = 0;
        this.m_ClusterDistance = m;
    }
    public String clusterDistanceTipText() {
        return "Set the distance threshhold for the DBSCAN method.";
    }
    /** This method allows you to set/get the minimal group size of the DBSCAN method.
     * @return The currently used minimal group size.
     */
    public int getMinimumGroupSize() {
        return this.m_MinimumGroupSize;
    }
    public void setMinimumGroupSize(int m){
        if (m < 1) m = 1;
        this.m_MinimumGroupSize = m;
    }
    public String minimumGroupSizeTipText() {
        return "Set the minimum group size for the DBSCAN method.";
    }


//    /** For debuggy only
//     * @param plot TopoPlot
//     */
//    public void draw(TopoPlot plot, Population pop) {
//        InterfaceDataTypeDouble tmpIndy1, tmpIndy2;
//        Population[] species = this.cluster(pop);
//        DPointSet   popRep = new DPointSet();
//
//        for (int i = 0; i < pop.size(); i++) {
//            popRep = new DPointSet();
//            tmpIndy1 = (InterfaceDataTypeDouble)pop.get(i);
//            popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//            plot.m_PlotArea.addDElement(popRep);
//            //System.out.println("Adding" + i + " : ("+tmpIndy1.getDoubleData()[0]+"/"+tmpIndy1.getDoubleData()[1]+")");
//            for (int j = i; j < pop.size(); j++) {
//                if (ConnectionMatrix[i][j]) {
//                    popRep = new DPointSet();
//                    popRep.setConnected(true);
//                    tmpIndy1 = (InterfaceDataTypeDouble)pop.get(i);
//                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//                    tmpIndy1 = (InterfaceDataTypeDouble)pop.get(j);
//                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//                    plot.m_PlotArea.addDElement(popRep);
//                }
//            }
//        }
//
//    }
}