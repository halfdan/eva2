package eva2.server.go.operators.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.AbstractEAIndividualComparator;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.populations.Population;

/**
 * Hierarchical clustering after Preuss et al., "Counteracting Genetic Drift and Disruptive Recombination 
 * in (mu+,lambda)-EA on Multimodal Fitness Landscapes", GECCO '05.
 * 
 * A tree is produced by assigning each individual the closest individual with better fitness.
 * Connections with a distance above a certain threshold are cut. After that, each interconnected subtree forms a cluster.
 * In the paper, the threshold is deduced as 2*d_p for d_p: the mean distance in the population.  
 * 
 * @author mkron
 *
 */
public class ClusteringHierarchical implements InterfaceClustering, Serializable {
	private static final long serialVersionUID = 1L;
	private InterfaceDistanceMetric     	metric            = new PhenotypeMetric();
    private double                      	absoluteDistThreshold = 0.5;
    private boolean 						thresholdMultipleOfMeanDist = false;
    private double 							meanDistFactor = 2.; // recommended setting
    private double							currentMeanDistance = -1.;
    private int                         	minimumGroupSize  = 3;
	private boolean 						testConvergingSpeciesOnBestOnly = true; // if two species are tested for convergence, only the best indies may be compared regarding the distance threshold 

	private int[]                 			uplink;
    private double[]						uplinkDist;
    private AbstractEAIndividualComparator comparator = new AbstractEAIndividualComparator();
    private Vector<Integer>[]				children;

	private static boolean TRACE = false;

	public ClusteringHierarchical() {
	}

    public ClusteringHierarchical(ClusteringHierarchical o) {
    	this.metric = o.metric;
    	this.absoluteDistThreshold = o.absoluteDistThreshold;
    	this.thresholdMultipleOfMeanDist = o.thresholdMultipleOfMeanDist;
    	this.meanDistFactor = o.meanDistFactor;
    	this.currentMeanDistance = o.currentMeanDistance;
    	this.minimumGroupSize = o.minimumGroupSize;
    	this.comparator = (AbstractEAIndividualComparator)o.comparator.clone();
    	this.testConvergingSpeciesOnBestOnly = o.testConvergingSpeciesOnBestOnly;
	}
    
    public void hideHideable() {
    	setAdaptiveThreshold(isAdaptiveThreshold());
    }

	/** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone() {
        return (Object) new ClusteringHierarchical(this);
    }
	
	public boolean belongsToSpecies(AbstractEAIndividual indy,
			Population species) {
		// TODO Auto-generated method stub
		return false;
	}

	public Population[] cluster(Population pop) {
		if (pop.isEmpty()) return new Population[]{pop.cloneWithoutInds()};
		ArrayList<AbstractEAIndividual> sorted = pop.getSorted(comparator);
		if (uplink==null || (uplink.length!=pop.size())) uplink = new int[pop.size()]; // parent index of all indys
		if (uplinkDist==null || (uplinkDist.length!=pop.size())) uplinkDist = new double[pop.size()]; // parent distance for all indys
		if (children==null || (children.length!=pop.size())) children = new Vector[pop.size()]; // list of children for all indies
		else if (children.length==pop.size()) for (int i=0; i<pop.size(); i++) children[i]=null;
 		
		if (thresholdMultipleOfMeanDist) currentMeanDistance = pop.getPopulationMeasures(metric)[0];
		if (TRACE) {
			System.out.println("Current pop measures: " + BeanInspector.toString(pop.getPopulationMeasures(metric)[0]));
			System.out.println("Current threshold: " + currentDistThreshold());
		}
		for (int i=sorted.size()-1; i>=1; i--) {  // start with worst indies
			// search for closest indy which is better
			uplink[i]=-1;
			uplinkDist[i] = -1;
			for (int j=i-1; j>=0; j--) { // look at all which are better
				// if the j-th indy is closer, reset the index
				double curDist = metric.distance(sorted.get(i), sorted.get(j));
				if (uplinkDist[i]<0 || (curDist < uplinkDist[i])) {
					uplink[i] = j;
					uplinkDist[i] = curDist;
				}
			}
			// the closest best for indy i is now known. connect them in the graph.
			if (children[uplink[i]]==null) children[uplink[i]]=new Vector<Integer>();
			children[uplink[i]].add(i);
		}
		
		// now go through indies starting with best. 
		// Add all children which are closer than threshold and recursively their children to a cluster.
		// Mark them as clustered and start with the next best unclustered.
		int current = 0; // top indy is first
		boolean[] clustered = new boolean[pop.size()];
		LinkedList<Population> allClusters = new LinkedList<Population>();
		while (current<sorted.size()) {
			Population currentClust = pop.cloneWithoutInds();
			currentClust.add(sorted.get(current));
			clustered[current]=true;
			addChildren(current, clustered, sorted, currentClust);
			// currentClust now recursively contains all children - the cluster is complete 
			// now jump to the next best unclustered indy
			allClusters.add(currentClust);
			while (current<sorted.size() && (clustered[current])) current++;
		}
		
		ArrayList<Population> finalClusts = new ArrayList<Population>(allClusters.size());
		finalClusts.add(pop.cloneWithoutInds()); 
		for (Population clust : allClusters) {
			if (clust.size()<minimumGroupSize) { // add to loner population
				finalClusts.get(0).addPopulation(clust);
			} else { // add to cluster list
				finalClusts.add(clust);
			}
		}
		Population[] finalArr = new Population[finalClusts.size()]; 
		return finalClusts.toArray(finalArr);
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
		if (children[current]!=null && (children[current].size()>0)) {
			for (int i=0; i<children[current].size(); i++) {
				if ((!clustered[children[current].get(i)]) && (uplinkDist[children[current].get(i)] < currentDistThreshold())) {
					// the first child is not clustered yet and below distance threshold.
					// so add it to the cluster, mark it, and proceed recursively.
					currentClust.add(sorted.get(children[current].get(i)));
					clustered[children[current].get(i)]=true;
					addChildren(children[current].get(i), clustered, sorted, currentClust);
				}
			}
		} else {
			// nothing more to do
		}
	}

	private double currentDistThreshold() {
		if (thresholdMultipleOfMeanDist) return meanDistFactor*currentMeanDistance;
		else return absoluteDistThreshold;
	}

    /** This method allows you to decide if two species converge.
     * @param species1  The first species.
     * @param species2  The second species.
     * @return True if species converge, else False.
     */
    public boolean mergingSpecies(Population species1, Population species2) {
        if (testConvergingSpeciesOnBestOnly) {
            if (this.metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual()) < this.currentDistThreshold()) return true;
            else return false;
        } else {
            Population tmpPop = new Population(species1.size()+species2.size());
            tmpPop.addPopulation(species1);
            tmpPop.addPopulation(species2);
            if (this.cluster(tmpPop).length <= 2) return true;
            else return false;
        }
    }
    
    public String globalInfo() { 
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
