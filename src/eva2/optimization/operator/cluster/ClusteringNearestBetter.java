package eva2.optimization.operator.cluster;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.AbstractEAIndividualComparator;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.paramcontrol.ParamAdaption;
import eva2.optimization.operator.paramcontrol.ParameterControlManager;
import eva2.optimization.population.Population;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Hierarchical clustering after Preuss et al., "Counteracting Genetic Drift and Disruptive Recombination
 * in (mu+,lambda)-EA on Multimodal Fitness Landscapes", GECCO '05.
 * <p/>
 * A tree is produced by assigning each individual the closest individual with better fitness.
 * Connections with a distance above a certain threshold are cut. After that, each interconnected subtree forms a cluster.
 * In the paper, the threshold is deduced as 2*d_p for d_p: the mean distance in the population.
 *
 * @author mkron
 */
public class ClusteringNearestBetter implements InterfaceClustering, Serializable {
    private static final long serialVersionUID = 1L;
    private InterfaceDistanceMetric metric = new PhenotypeMetric();
    private double absoluteDistThreshold = 0.5;
    private boolean thresholdMultipleOfMeanDist = true;
    private double meanDistFactor = 2.; // recommended setting
    private double currentMeanDistance = -1.;
    private int minimumGroupSize = 3;
    private boolean testConvergingSpeciesOnBestOnly = true; // if two species are tested for convergence, only the best indies may be compared regarding the distance threshold
    protected ParameterControlManager paramControl = new ParameterControlManager();

    private int[] uplink;
    private double[] uplinkDist;
    private AbstractEAIndividualComparator comparator = new AbstractEAIndividualComparator();
    private Vector<Integer>[] children;
    private static final String initializedForKey = "initializedClustNearestBetterOnHash";
    private static final String initializedRefData = "initializedClustNearestBetterData";

    private static boolean TRACE = false;

    public ClusteringNearestBetter() {
    }

    public ClusteringNearestBetter(ClusteringNearestBetter o) {
        this.metric = o.metric;
        this.absoluteDistThreshold = o.absoluteDistThreshold;
        this.thresholdMultipleOfMeanDist = o.thresholdMultipleOfMeanDist;
        this.meanDistFactor = o.meanDistFactor;
        this.currentMeanDistance = o.currentMeanDistance;
        this.minimumGroupSize = o.minimumGroupSize;
        this.comparator = (AbstractEAIndividualComparator) o.comparator.clone();
        this.testConvergingSpeciesOnBestOnly = o.testConvergingSpeciesOnBestOnly;
    }

    /**
     * Set the mean distance factor in the adaptive case or the absolute distance
     * threshold in the non-adaptive case.
     *
     * @param adaptive
     * @param thresholdOrFactor
     */
    public ClusteringNearestBetter(boolean adaptive, double thresholdOrFactor) {
        setAdaptiveThreshold(adaptive);
        if (adaptive) {
            setMeanDistFactor(thresholdOrFactor);
        } else {
            setDistThreshold(thresholdOrFactor);
        }
    }

    public void hideHideable() {
        setAdaptiveThreshold(isAdaptiveThreshold());
    }

    public ParameterControlManager getParamControl() {
        return paramControl;
    }

    public ParamAdaption[] getParameterControl() {
        return paramControl.getSingleAdapters();
    }

    public void setParameterControl(ParamAdaption[] paramControl) {
        this.paramControl.setSingleAdapters(paramControl);
    }

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    @Override
    public Object clone() {
        return new ClusteringNearestBetter(this);
    }

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
    public int[] associateLoners(Population loners, Population[] species, Population referenceSet) {
//    	Pair<Integer,Double>[][] closestPerSpecList = new Pair[loners.size()][species.length];
        int[] res = new int[loners.size()];
        getRefData(referenceSet, loners);
        for (int l = 0; l < loners.size(); l++) { // for each loner: search closest better indy for each species.
            int nearestBetterSpeciesID = -1;
            double nearestBetterDist = -1;

            for (int spI = 0; spI < species.length; spI++) { // loop species
                boolean lonerIndyIsBest = (comparator.compare(loners.getEAIndividual(l), species[spI].getBestEAIndividual()) <= 0);
                if (lonerIndyIsBest) { // if the loner is the best, check the distance to the best indy within the species
                    double curDist = metric.distance(loners.getEAIndividual(l), species[spI].getBestEAIndividual());
                    //Population.getClosestFarthestIndy(loners.getEAIndividual(l), species[spI], metric, true).tail();
                    if (nearestBetterDist < 0 || (curDist < nearestBetterDist)) {
//						System.out.println("Loner is better " + loners.getEAIndividual(l) + " than best " + species[spI].getBestEAIndividual() + ", dist is "+curDist);
                        nearestBetterSpeciesID = spI;
                        nearestBetterDist = curDist;
                    }
                } else {
                    for (int i = 0; i < species[spI].size(); i++) { //loop indies in species
                        double curDist = metric.distance(loners.getEAIndividual(l), species[spI].getEAIndividual(i));
                        boolean specIndyIsBetter = (comparator.compare(species[spI].getEAIndividual(i), loners.getEAIndividual(l)) < 0);
                        if (specIndyIsBetter && (nearestBetterDist < 0 || (curDist < nearestBetterDist))) {
                            // if the found indy species is better than the loner, it is a possible cluster.
                            // store the closest possible cluster.
                            //    					nearestBetterIndyID = i;
                            nearestBetterSpeciesID = spI;
                            nearestBetterDist = curDist;
                        }
                    }
                }
//    			if (comparator.compare(species[spI].getEAIndividual(closestID), loners.getEAIndividual(l))<0) {
//
//    				if (closestClustDist<0 || (closestDist < closestClustDist)) {
//    					closestClustDist = closestDist;
//    					closestClustID = spI;
//    				}
//    			}
            } // end loop species
            if (nearestBetterDist < currentDistThreshold()) {
//    			System.out.println("dist is " + nearestBetterDist + ", assigning spec " + nearestBetterSpeciesID);
                res[l] = nearestBetterSpeciesID;
            } else {
                res[l] = -1;
            }
        } // end for all loners
        return res;
    }

//	public boolean belongsToSpecies(AbstractEAIndividual indy,
//			Population species, Population pop) {
//		// this sucks since every time the full clustering must be performed...
//		return false;
////		if (thresholdMultipleOfMeanDist) currentMeanDistance = pop.getPopulationMeasures(metric)[0];
////		ArrayList<AbstractEAIndividual> sorted = pop.getSorted(comparator);
////		for (int i=sorted.size()-1; i>=1; i--) {  // start with worst indies
////			if (sorted.get(i).getIndyID()==indy.getIndyID()) { // found the desired indy.
////				int uplink=-1; double uplinkDist = -1;
////				for (int j=i-1; j>=0; j--) { // search nearest better indy
////					double curDist = metric.distance(sorted.get(i), sorted.get(j));
////					if (uplinkDist<0 || (curDist < uplinkDist)) {
////						uplink = j;
////						uplinkDist = curDist;
////					}					
////				}
////				// if it belongs to species spec and the distance is below threshold, be happy and return true
////				if (uplink==-1) { // it is the best individual?
////					return false;
////				}
////				if (uplinkDist > currentDistThreshold()) return false;
////				else {
////					return (species.isMemberByID(pop.getEAIndividual(uplink)));
////				}
////			}
////		}
////		// size <= 1?
////		return false;
//	}

    /**
     * Perform one clustering step to measure the mean distance to the
     * nearest better individual (only if used).
     */
    @Override
    public String initClustering(Population pop) {
        if (this.isAdaptiveThreshold()) {
            ArrayList<AbstractEAIndividual> sorted = pop.getSorted(comparator);
            if (uplink == null || (uplink.length != pop.size())) {
                uplink = new int[pop.size()];
            } // parent index of all indys
            if (uplinkDist == null || (uplinkDist.length != pop.size())) {
                uplinkDist = new double[pop.size()];
            } // parent distance for all indys
            if (children == null || (children.length != pop.size())) {
                children = new Vector[pop.size()];
            } // list of children for all indies
            else if (children.length == pop.size()) {
                for (int i = 0; i < pop.size(); i++) {
                    children[i] = null;
                }
            }
            currentMeanDistance = createClusterTreeFromSortedPop(sorted);
            if (TRACE) {
                pop.putData(initializedForKey, pop.hashCode());
            }
            pop.putData(initializedRefData, currentMeanDistance);
            return initializedRefData;
        } else {
            return null;
        }
    }

    @Override
    public Population[] cluster(Population pop, Population referenceSet) {
        if (pop.isEmpty()) {
            return new Population[]{pop.cloneWithoutInds()};
        }
        ArrayList<AbstractEAIndividual> sorted = pop.getSorted(comparator);
        if (uplink == null || (uplink.length != pop.size())) {
            uplink = new int[pop.size()];
        } // parent index of all indys
        if (uplinkDist == null || (uplinkDist.length != pop.size())) {
            uplinkDist = new double[pop.size()];
        } // parent distance for all indys
        if (children == null || (children.length != pop.size())) {
            children = new Vector[pop.size()];
        } // list of children for all indies
        else if (children.length == pop.size()) {
            for (int i = 0; i < pop.size(); i++) {
                children[i] = null;
            }
        }

        if (TRACE) {
            System.out.println("Current pop measures: " + BeanInspector.toString(pop.getPopulationMeasures(metric)[0]));
            System.out.println("Current threshold: " + currentDistThreshold());
        }
        if (isAdaptiveThreshold()) { // test if there was a valid initialization step
            if (!getRefData(referenceSet, pop)) {
                currentMeanDistance = createClusterTreeFromSortedPop(sorted);
            } else {
                createClusterTreeFromSortedPop(sorted);
            }
        } else {
            createClusterTreeFromSortedPop(sorted);
        }

        // now go through indies starting with best.
        // Add all children which are closer than threshold and recursively their children to a cluster.
        // Mark them as clustered and start with the next best unclustered.
        int current = 0; // top indy is first
        boolean[] clustered = new boolean[pop.size()];
        LinkedList<Population> allClusters = new LinkedList<Population>();
        while (current < sorted.size()) {
            Population currentClust = pop.cloneWithoutInds();
            currentClust.add(sorted.get(current));
            clustered[current] = true;
            addChildren(current, clustered, sorted, currentClust);
            // currentClust now recursively contains all children - the cluster is complete
            // now jump to the next best unclustered indy
            allClusters.add(currentClust);
            while (current < sorted.size() && (clustered[current])) current++;
        }

        ArrayList<Population> finalClusts = new ArrayList<Population>(allClusters.size());
        finalClusts.add(pop.cloneWithoutInds());
        for (Population clust : allClusters) {
            if (clust.size() < minimumGroupSize) { // add to loner population
                finalClusts.get(0).addPopulation(clust);
            } else { // add to cluster list
                finalClusts.add(clust);
            }
        }
        Population[] finalArr = new Population[finalClusts.size()];
        return finalClusts.toArray(finalArr);
    }

    /**
     * Get the reference data from a population instance that should have been initialized.
     * If the reference set is null, the backup is treated as reference set.
     *
     * @param referenceSet
     * @param backup
     */
    private boolean getRefData(Population referenceSet, Population backup) {
        if (referenceSet == null) {
            referenceSet = backup;
        }
        Double refDat = (Double) referenceSet.getData(initializedRefData);
        if (refDat != null) {
            if (TRACE) { // check hash
                Integer hash = (Integer) referenceSet.getData(initializedForKey);
                if ((hash == null) || (hash != referenceSet.hashCode())) {
                    System.err.println("Warning, missing initialization before clustering for ClusteringNearestBetter!");
                    return false;
                }
            }
            currentMeanDistance = refDat.doubleValue();
            return true;
        } else {
            System.err.println("Warning, missing reference data - forgot reference set initialization? " + this.getClass());
            return false;
        }
    }

    private double createClusterTreeFromSortedPop(ArrayList<AbstractEAIndividual> sorted) {
        double edgeLengthSum = 0;
        int edgeCnt = 0;
        for (int i = sorted.size() - 1; i >= 1; i--) {  // start with worst indies
            // search for closest indy which is better
            uplink[i] = -1;
            uplinkDist[i] = -1;
            for (int j = i - 1; j >= 0; j--) { // look at all which are better
                // if the j-th indy is closer, reset the index
                double curDist = metric.distance(sorted.get(i), sorted.get(j));
                if (uplinkDist[i] < 0 || (curDist < uplinkDist[i])) {
                    uplink[i] = j;
                    uplinkDist[i] = curDist;
                }
            }
            // the closest best for indy i is now known. connect them in the graph.
            if (children[uplink[i]] == null) {
                children[uplink[i]] = new Vector<Integer>();
            }
            children[uplink[i]].add(i);
            edgeLengthSum += uplinkDist[i];
            edgeCnt++;
        }
//		currentMeanDistance = pop.getPopulationMeasures(metric)[0];
        return edgeLengthSum / ((double) edgeCnt); // the average edge length
    }

    /**
     * Add the next layer of children to the clustered population.
     *
     * @param current
     * @param clustered
     * @param sorted
     * @param currentClust
     */
    private void addChildren(int current, boolean[] clustered, ArrayList<AbstractEAIndividual> sorted, Population currentClust) {
        if (children[current] != null && (children[current].size() > 0)) {
            for (int i = 0; i < children[current].size(); i++) {
                if ((!clustered[children[current].get(i)]) && (uplinkDist[children[current].get(i)] < currentDistThreshold())) {
                    // the first child is not clustered yet and below distance threshold.
                    // so add it to the cluster, mark it, and proceed recursively.
                    currentClust.add(sorted.get(children[current].get(i)));
                    clustered[children[current].get(i)] = true;
                    if (TRACE) {
                        System.out.println("Assigned " + current);
                    }
                    addChildren(children[current].get(i), clustered, sorted, currentClust);
                } else {
                    if (TRACE) {
                        System.out.println("Not assigned " + current);
                    }
                }
            }
        } else {
            // nothing more to do
        }
    }

    private double currentDistThreshold() {
        if (thresholdMultipleOfMeanDist) {
            return meanDistFactor * currentMeanDistance;
        } else {
            return absoluteDistThreshold;
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
    public boolean mergingSpecies(Population species1, Population species2, Population referenceSet) {
        getRefData(referenceSet, species1);
        if (testConvergingSpeciesOnBestOnly) {
            if (this.metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual()) < this.currentDistThreshold()) {
                return true;
            } else {
                return false;
            }
        } else {
            Population tmpPop = new Population(species1.size() + species2.size());
            tmpPop.addPopulation(species1);
            tmpPop.addPopulation(species2);
            if (this.cluster(tmpPop, referenceSet).length <= 2) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static String globalInfo() {
        return "A tree is produced by assigning each individual the closest individual with better fitness. Connections with a distance above a certain threshold are cut. After that, each interconnected subtree forms a cluster.";
    }

    public String metricTipText() {
        return "The metric to use during clustering.";
    }

    public InterfaceDistanceMetric getMetric() {
        return metric;
    }

    public void setMetric(InterfaceDistanceMetric metric) {
        this.metric = metric;
    }

    public String distThresholdTipText() {
        return "In the non-adaptive case the absolute threshold below which clusters are connected.";
    }

    public double getDistThreshold() {
        return absoluteDistThreshold;
    }

    public void setDistThreshold(double distThreshold) {
        this.absoluteDistThreshold = distThreshold;
    }

    public String minimumGroupSizeTipText() {
        return "Minimum group size that makes an own cluster.";
    }

    public int getMinimumGroupSize() {
        return minimumGroupSize;
    }

    public void setMinimumGroupSize(int minimumGroupSize) {
        this.minimumGroupSize = minimumGroupSize;
    }

    public String comparatorTipText() {
        return "Define the comparator by which the population is sorted before clustering.";
    }

    public AbstractEAIndividualComparator getComparator() {
        return comparator;
    }
//	public void setComparator(AbstractEAIndividualComparator comparator) {
//		this.comparator = comparator;
//	}

    public String adaptiveThresholdTipText() {
        return "Activate adaptive threshold which is calculated from mean distance in the population and a constant factor.";
    }

    public boolean isAdaptiveThreshold() {
        return thresholdMultipleOfMeanDist;
    }

    public void setAdaptiveThreshold(boolean thresholdMultipleOfMeanDist) {
        this.thresholdMultipleOfMeanDist = thresholdMultipleOfMeanDist;
        GenericObjectEditor.setHideProperty(this.getClass(), "meanDistFactor", !thresholdMultipleOfMeanDist);
        GenericObjectEditor.setHideProperty(this.getClass(), "distThreshold", thresholdMultipleOfMeanDist);
    }

    public String meanDistFactorTipText() {
        return "Factor producing the distance threshold from population mean distance.";
    }

    public double getMeanDistFactor() {
        return meanDistFactor;
    }

    public void setMeanDistFactor(double meanDistFactor) {
        this.meanDistFactor = meanDistFactor;
    }

    public String testConvergingSpeciesOnBestOnlyTipText() {
        return "Only the best individuals may be compared when testing whether to merge two species.";
    }

    public boolean isTestConvergingSpeciesOnBestOnly() {
        return testConvergingSpeciesOnBestOnly;
    }

    public void SetTestConvergingSpeciesOnBestOnly(
            boolean testConvergingSpeciesOnBestOnly) {
        this.testConvergingSpeciesOnBestOnly = testConvergingSpeciesOnBestOnly;
    }

}
