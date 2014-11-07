package eva2.optimization.operator.cluster;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.population.Population;
import eva2.tools.Pair;
import eva2.util.annotation.Description;

import java.util.ArrayList;


/**
 * The DBSCAN method. As far as I recall this is an hierarchical
 * clustering method like the single-link method.
 */
@Description("A density-based clustering algorithm (DBSCAN).")
public class ClusteringDensityBased implements InterfaceClusteringDistanceParam, InterfaceClusteringMetricBased, java.io.Serializable {

    private InterfaceDistanceMetric metric = new PhenotypeMetric();
    private double clusterDistance = 0.1;
    private int minimumGroupSize = 3;
    private boolean[][] connectionMatrix;
    private boolean[] clustered;
    private boolean testConvergingSpeciesOnBestOnly = true;

    public ClusteringDensityBased() {
    }

    /**
     * Directly set the minimum cluster distance sigma.
     *
     * @param sigma the minimum cluster distance
     */
    public ClusteringDensityBased(double sigma) {
        clusterDistance = sigma;
    }

    /**
     * Directly set the minimum cluster distance sigma and minimum group size.
     *
     * @param sigma the minimum cluster distance
     */
    public ClusteringDensityBased(double sigma, int minGSize) {
        clusterDistance = sigma;
        minimumGroupSize = minGSize;
    }

    /**
     * Directly set the minimum cluster distance sigma and minimum group size.
     *
     * @param sigma the minimum cluster distance
     */
    public ClusteringDensityBased(double sigma, int minGSize, InterfaceDistanceMetric metric) {
        clusterDistance = sigma;
        minimumGroupSize = minGSize;
        this.metric = metric;
    }

    public ClusteringDensityBased(ClusteringDensityBased a) {
        if (a.metric != null) {
            this.metric = (InterfaceDistanceMetric) a.metric.clone();
        }
        this.testConvergingSpeciesOnBestOnly = a.testConvergingSpeciesOnBestOnly;
        this.clusterDistance = a.clusterDistance;
        this.minimumGroupSize = a.minimumGroupSize;
        if (a.clustered != null) {
            this.clustered = new boolean[a.clustered.length];
            System.arraycopy(a.clustered, 0, this.clustered, 0, this.clustered.length);
        }
        if (a.connectionMatrix != null) {
            this.connectionMatrix = new boolean[a.connectionMatrix.length][a.connectionMatrix[0].length];
            for (int i = 0; i < this.connectionMatrix.length; i++) {
                System.arraycopy(a.connectionMatrix[i], 0, this.connectionMatrix[i], 0, this.connectionMatrix[i].length);
            }
        }
    }

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    @Override
    public Object clone() {
        return new ClusteringDensityBased(this);
    }

    @Override
    public Population[] cluster(Population pop, Population referencePop) {
        connectionMatrix = new boolean[pop.size()][pop.size()];
        clustered = new boolean[pop.size()];
        AbstractEAIndividual tmpIndy1, tmpIndy2;
        Population PopulationOfUnclustered, Cluster, template;
        ArrayList<Population> ClusteredPopulations = new ArrayList<>();

        template = (Population) pop.clone();
        template.clear();
        PopulationOfUnclustered = (Population) template.clone();
        ClusteredPopulations.add(PopulationOfUnclustered);

        // Build the connection Matrix
        for (int i = 0; i < pop.size(); i++) {
            tmpIndy1 = pop.get(i);
            connectionMatrix[i][i] = true;
            for (int j = i + 1; j < pop.size(); j++) {
                tmpIndy2 = pop.get(j);
                if (tmpIndy1 == null || (tmpIndy2 == null)) {
                    System.err.println("Warning: Individual should not be null (ClusteringDensityBased)!");
                }
                if ((tmpIndy1 != null) && (tmpIndy2 != null) && (this.metric.distance(tmpIndy1, tmpIndy2) < this.clusterDistance)) {
                    connectionMatrix[i][j] = true;
                    connectionMatrix[j][i] = true;
                } else {
                    connectionMatrix[i][j] = false;
                    connectionMatrix[j][i] = false;
                }
            }
        }

        for (int i = 0; i < clustered.length; i++) {
            clustered[i] = false;
        }

        // Now identify clusters within pop and add them to the result
        for (int i = 0; i < connectionMatrix.length; i++) {
            if (!clustered[i]) {
                Cluster = (Population) template.clone();
                this.addRowToPopulation(i, Cluster, pop);
                if (Cluster.size() >= this.minimumGroupSize) {
                    ClusteredPopulations.add(Cluster);
                } else {
                    PopulationOfUnclustered.addPopulation(Cluster);
                }
            }
        }

        Population[] result = new Population[ClusteredPopulations.size()];
        for (int i = 0; i < ClusteredPopulations.size(); i++) {
            result[i] = ClusteredPopulations.get(i);
        }
        return result;
    }

    /**
     * This method adds all Connected and !clustered Individuals form row index
     * to pop
     *
     * @param index   The index of the row that is to be computed.
     * @param cluster The Cluster to which the individuals are to be added
     * @param source  The source which is to be clustered.
     */
    private void addRowToPopulation(int index, Population cluster, Population source) {
        for (int i = 0; i < connectionMatrix[index].length; i++) {
            if ((!clustered[i]) && (connectionMatrix[index][i])) {
                clustered[i] = true;
                connectionMatrix[index][i] = false;
                connectionMatrix[i][index] = false;
                cluster.add(source.get(i));
                this.addRowToPopulation(i, cluster, source);
            }
        }
    }

    /**
     * This method allows you to decide if two species converge.
     *
     * @param species1 The first species.
     * @param species2 The second species.
     * @return True if species converge, else False.
     */
    @Override
    public boolean mergingSpecies(Population species1, Population species2, Population referencePop) {
        if (testConvergingSpeciesOnBestOnly) {
            double specDist = this.metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual());
//        	System.out.println("Dist between species is " + specDist);
            return (specDist < this.clusterDistance);
        } else {
            Population tmpPop = new Population(species1.size() + species2.size());
            tmpPop.addPopulation(species1);
            tmpPop.addPopulation(species2);
            if (this.cluster(tmpPop, referencePop).length <= 2) {
                return true;
            } else {
                return false;
            }
        }
    }

//    /** This method decides if a unclustered individual belongs to an already established species.
//     * @param indy          A unclustered individual.
//     * @param species       A species.
//     * @return True or False.
//     */
//    public boolean belongsToSpecies(AbstractEAIndividual indy, Population species, Population pop) {
//        if (this.testConvergingSpeciesOnBestOnly) {
//            if (this.distanceMetric.distance(indy, species.getBestEAIndividual()) < this.clusterDistance) return true;
//            else return false;
//        } else {
//            Population tmpPop = (Population)species.clone();
//            tmpPop.add(indy);
//            if (this.cluster(tmpPop)[0].size() == 0) return true;
//            else return false;
//        }
//    }

    /**
     * Try to associate a set of loners with a given set of species. Return a list
     * of indices assigning loner i with species j for all loners. If no species can
     * be associated, -1 is returned as individual entry.
     * Note that the last cluster threshold is used which may have depended on the last
     * generation.
     *
     * @param loners
     * @param species
     * @return associative list matching loners to species.
     */
    @Override
    public int[] associateLoners(Population loners, Population[] species, Population referencePop) {
        int[] res = new int[loners.size()];
        for (int l = 0; l < loners.size(); l++) {
            double minDist = -1;
            res[l] = -1;
            for (int spI = 0; spI < species.length; spI++) {  // O(species.length^2)
                Pair<Integer, Double> iDist = Population.getClosestFarthestIndy(loners.getEAIndividual(l), species[spI], metric, true);
                if (iDist.tail() < clusterDistance) { // its close enough to be added
                    // set SP ID only if its the closest species which is still below cluster distance
                    if (minDist < 0 || (iDist.tail() < minDist)) {
                        res[l] = spI;
                    }
                }
            } // end for all species
        } // end for all loners
        return res;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "DBSCAN";
    }

    /**
     * This method allows you to set/get the distance metric used by the DBSCAN method.
     *
     * @return The currently used distance metric.
     */
    @Override
    public InterfaceDistanceMetric getMetric() {
        return this.metric;
    }

    @Override
    public void setMetric(InterfaceDistanceMetric m) {
        this.metric = m;
    }

    public String metricTipText() {
        return "Choose the distance metric to use.";
    }

    /**
     * This method allows you to set/get the minimal group size of the DBSCAN method.
     *
     * @return The currently used minimal group size.
     */
    public int getMinimumGroupSize() {
        return this.minimumGroupSize;
    }

    public void setMinimumGroupSize(int m) {
        if (m < 1) {
            m = 1;
        }
        this.minimumGroupSize = m;
    }

    public String minimumGroupSizeTipText() {
        return "Set the minimum group size for the DBSCAN method.";
    }

    @Override
    public String initClustering(Population pop) {
        return null;
    }

    @Override
    public double getClustDistParam() {
        return this.clusterDistance;
    }

    @Override
    public void setClustDistParam(double m) {
        if (m < 0) {
            m = 0;
        }
        this.clusterDistance = m;
    }

    public String clustDistTipText() {
        return "Set the distance threshhold for the DBSCAN method.";
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
//            plot.plotArea.addDElement(popRep);
//            //System.out.println("Adding" + i + " : ("+tmpIndy1.getDoubleData()[0]+"/"+tmpIndy1.getDoubleData()[1]+")");
//            for (int j = i; j < pop.size(); j++) {
//                if (connectionMatrix[i][j]) {
//                    popRep = new DPointSet();
//                    popRep.setConnected(true);
//                    tmpIndy1 = (InterfaceDataTypeDouble)pop.get(i);
//                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//                    tmpIndy1 = (InterfaceDataTypeDouble)pop.get(j);
//                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//                    plot.plotArea.addDElement(popRep);
//                }
//            }
//        }
//
//    }
}