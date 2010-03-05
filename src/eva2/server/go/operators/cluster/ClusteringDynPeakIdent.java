package eva2.server.go.operators.cluster;

import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.populations.Population;
import eva2.tools.Pair;

/**
 * Clustering using the DPI mechanism (dynamic peak identification).
 * Collect a number of peaks, which are the fittest individuals
 * which are not dominated by other individuals within a certain distance.
 * The remaining individuals are assigned to a peak if they have a distance
 * smaller than rho to that peak. 
 * The number of expected peaks (clusters) must be predefined. 
 * Note that the returned number of clusters may be smaller than q.
 * 
 * @author mkron
 *
 */
public class ClusteringDynPeakIdent implements InterfaceClustering, java.io.Serializable {
	private static final boolean TRACE=true;
	private int numNiches;
	private double nicheRadius;
	private boolean strictNicheRadius=true; // if false, all individuals are clustered to the closest niche, otherwise some remain unclustered (those which are further than the nicheRadius from any peak)
	InterfaceDistanceMetric metric = new PhenotypeMetric();

	public ClusteringDynPeakIdent(int numNs, double nicheRad) {
		this.numNiches = numNs;
		this.nicheRadius = nicheRad;
	}

	public ClusteringDynPeakIdent(ClusteringDynPeakIdent o) {
		this(o.numNiches, o.nicheRadius);
		metric = o.metric;
		this.strictNicheRadius = o.strictNicheRadius;
	}

	public Object clone() {
		return new ClusteringDynPeakIdent(this);
	}
	
	public int[] associateLoners(Population loners, Population[] species,
			Population referenceSet) {
		Population bests = new Population(species.length);
		for (int i=0; i<species.length; i++) bests.add(species[i].getBestEAIndividual());
		return assignLeaders(loners, bests);
	}

	/**
	 * Assign a set of "lone" individuals to a set of leaders. Returns a vector
	 * of ints which indicate for every loner the index of the associated leader.
	 * An index may be -1 if no leader is closer than the niche threshold AND strictNicheRadius
	 * is true. 
	 * 
	 * @param loners
	 * @param bests
	 * @return
	 */
	protected int[] assignLeaders(Population loners, Population bests) {
		int[] assoc = new int[loners.size()];
		for (int i=0; i<loners.size(); i++) {
			// check distances to best indies per species
			Pair<Integer, Double> closestInfo = Population.getClosestFarthestIndy(loners.getEAIndividual(i), bests, metric, true);
			// we have now the info about the closest best individual. If its closer than the threshold, we can assign the loner to that index
			if (!strictNicheRadius || (closestInfo.tail()<nicheRadius)) {
				assoc[i]=closestInfo.head();
			} else assoc[i]=-1;
		}
		return assoc;
	}

	public Population[] cluster(Population pop, Population referenceSet) {
		Population sorted = pop.getSortedBestFirst();
		Population peaks = performDynPeakIdent(metric, sorted, numNiches, nicheRadius);
		Population[] clusters = new Population[peaks.size()+1];
		for (int i=0; i<clusters.length; i++) clusters[i]=new Population();
		
		Population rest = pop.cloneShallowInds();
		rest.filter(peaks);
		int[] assoc = assignLeaders(rest, peaks);

		for (int i=0; i<assoc.length; i++) {
			if (assoc[i]>=0) { // it can be assigned to a peak
				clusters[assoc[i]+1].add(rest.getEAIndividual(i));
			} else { // its a loner
				clusters[0].add(rest.getEAIndividual(i));
			}
		}
		return clusters;
	}

	public String initClustering(Population pop) {
		return null;
	}

	public boolean mergingSpecies(Population species1, Population species2,
			Population referenceSet) {
		// in our case just return true if the leaders are close enough
		return (metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual())<nicheRadius);
	}

	
	/**
	 * The DPI mechanism. Collect a number of peaks, which are the fittest individuals
	 * which are not dominated by other individuals within distance rho.
	 * Note that the returned set may be smaller than q.
	 * 
	 * @param pop
	 * @param q the number of peaks to be identified
	 * @param rho the niche radius
	 * @return the dynamic peak set
	 */
	public static Population performDynPeakIdent(InterfaceDistanceMetric metric, Population sortedPop, int q, double rho) {
		int i=0;
		Population peaks = new Population(q);
		while (i<sortedPop.size() && (peaks.size() < q)) {
			if ((peaks.size()==0) || (!peaks.isWithinPopDist((AbstractEAIndividual)sortedPop.get(i), rho, metric))) {
				peaks.add(sortedPop.get(i));
				System.out.println("Added peak " + sortedPop.get(i));
			}
			i++;
		}
		if (TRACE) {
			System.out.println("Found " + peaks.size() + " peaks, ");
			for (int k=0; k<peaks.size(); k++) System.out.println("  " + peaks.getEAIndividual(k));
			System.out.println("Measures: " + BeanInspector.toString(peaks.getPopulationMeasures()));
		}
		return peaks;
	}
}
