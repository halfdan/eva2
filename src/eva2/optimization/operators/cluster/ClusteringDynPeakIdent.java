package eva2.optimization.operators.cluster;

import eva2.gui.BeanInspector;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.AbstractEAIndividualComparator;
import eva2.optimization.individuals.IndividualDistanceComparator;
import eva2.optimization.operators.distancemetric.EuclideanMetric;
import eva2.optimization.operators.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operators.distancemetric.PhenotypeMetric;
import eva2.optimization.populations.Population;
import eva2.tools.Pair;
import java.util.ArrayList;

/**
 * Clustering using the DPI mechanism (dynamic peak identification).
 * Collect a number of peaks, which are the fittest individuals
 * which are not dominated by other individuals within a certain distance.
 * The remaining individuals are assigned to the closest peak up to a maximum 
 * niche count. If more individuals would be assigned to one peak, the best ones are
 * chosen and remaining ones are assumed unclustered.
 * For the strict radius case, individuals are only assigned to a peak if they 
 * have a distance smaller than the niche radius to that peak. 
 * The number of expected peaks (clusters) must be predefined. 
 * Note that the returned number of clusters may be smaller than q.
 * 
 * @author mkron
 *
 */
public class ClusteringDynPeakIdent implements InterfaceClustering, java.io.Serializable {
	private static final boolean TRACE=false;
	private int numNiches;
	private double nicheRadius;
	private int maxNicheCount; // maximum number of individuals per peak
	private boolean strictNicheRadius=true; // if false, all individuals are clustered to the closest niche, otherwise some remain unclustered (those which are further than the nicheRadius from any peak)

	InterfaceDistanceMetric metric = new PhenotypeMetric();

	/**
	 * Uses the alternative metric if it is non null. In case it is null, the last metric is used or, if
	 * none was set, the default PhenotypeMetric() is used.
	 * 
	 * @param numNs
	 * @param numIndiesPerPeak
	 * @param nicheRad
	 * @param strictRad
	 * @param alternativeMetric
	 */
	public ClusteringDynPeakIdent(int numNs, int numIndiesPerPeak, double nicheRad, boolean strictRad, InterfaceDistanceMetric alternativeMetric) {
		this.numNiches = numNs;
		this.maxNicheCount = numIndiesPerPeak;
		this.nicheRadius = nicheRad;
		this.strictNicheRadius = strictRad;
		if (metric==null && (alternativeMetric==null)) {
                metric=new PhenotypeMetric();
            }
		else if (alternativeMetric!=null) {
                metric=alternativeMetric;
            }
	}

	public ClusteringDynPeakIdent(ClusteringDynPeakIdent o) {
		this(o.numNiches, o.maxNicheCount, o.nicheRadius, o.strictNicheRadius, o.metric);
	}

    @Override
	public Object clone() {
		return new ClusteringDynPeakIdent(this);
	}
	
    @Override
	public int[] associateLoners(Population loners, Population[] species,
			Population referenceSet) {
		Population bests = new Population(species.length);
		for (int i=0; i<species.length; i++) {
            bests.add(species[i].getBestEAIndividual());
        }
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
			} else {
                        assoc[i]=-1;
                    }
		}
		return assoc;
	}

    @Override
	public Population[] cluster(Population pop, Population referenceSet) {
//		boolean TRACE_METH=false;
//		if (TRACE_METH) System.out.println("A1 " + System.currentTimeMillis());
		AbstractEAIndividualComparator eaComparator = new AbstractEAIndividualComparator(-1);
		Population sorted = pop.getSortedBestFirst(eaComparator);
//		if (TRACE_METH) System.out.println("A2 " + System.currentTimeMillis());
		Population peaks = performDynPeakIdent(metric, sorted, numNiches, nicheRadius);
//		System.out.println("peak measures: " + BeanInspector.toString(peaks.getPopulationMeasures()));
//		if (TRACE_METH) System.out.println("A3 " + System.currentTimeMillis());
		Population[] clusters = new Population[peaks.size()+1];
		for (int i=0; i<clusters.length; i++) {
			clusters[i]=referenceSet.cloneWithoutInds();
			if (i>0) {
                        clusters[i].add(peaks.getEAIndividual(i-1));
                    } // add peaks to clusters!
		}
//		if (TRACE_METH) System.out.println("A4 " + System.currentTimeMillis());
		Population rest = pop.filter(peaks);
//		if (TRACE_METH) System.out.println("A4a " + System.currentTimeMillis());
		if (pop.getRedundancyCount()>0) {
			// happens e.g. on the bounds of the domain
			System.err.println("warning, found redundant indies: " + pop.getRedundancyCount());
			rest.removeRedundantIndies();
		}
		if ((rest.size()+peaks.size())+pop.getRedundancyCount()!=pop.size()) {
			System.err.println("Warning, inconsistent filtering in ClusteringDynPeakIdent! Redundant: " + pop.getRedundancyCount() );
		}
		int[] assoc = assignLeaders(rest, peaks);
//		if (TRACE_METH) System.out.println("A5 " + System.currentTimeMillis());

		for (int i=0; i<assoc.length; i++) {
			if (assoc[i]>=0) { // it can be assigned to a peak
				clusters[assoc[i]+1].add(rest.getEAIndividual(i));
			} else { // its a loner
				clusters[0].add(rest.getEAIndividual(i));
			}
		}
//		if (TRACE_METH) System.out.println("A6 " + System.currentTimeMillis());
		int cnt = clusters[0].size();
		for (int i=1; i<clusters.length; i++) {
			cnt += clusters[i].size();
			if (clusters[i].size()==0) {
				System.err.println("Warning!!!");
			}
		}
		if (cnt!=(pop.size()-pop.getRedundancyCount())) {
			System.err.println("Another warning!!  " + cnt + " vs. " + pop.size());
		}
		if (maxNicheCount>0) { // check for too large species
			for (int i=1; i<clusters.length; i++) {
				if (clusters[i].size()>maxNicheCount) {
//					Population overhead = clusters[i].getSortedNIndividuals(clusters[i].size()-maxNicheCount, false);
					ArrayList<AbstractEAIndividual> overhd = clusters[i].getSorted(new IndividualDistanceComparator(peaks.getEAIndividual(i-1), new EuclideanMetric(), true));
					Population overhead = new Population();
					overhead.addAll(overhead.toTail(clusters[i].size()-maxNicheCount, overhd)); // add only the front maxNicheCount individuals
					clusters[i].removeMembers(overhead, true);
					clusters[0].addPopulation(overhead);
				}
			}
		}		
//		if (TRACE_METH) System.out.println("-- " + System.currentTimeMillis());
		return clusters;
	}

    @Override
	public String initClustering(Population pop) {
		return null;
	}

    @Override
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
		if (TRACE) {
                System.out.print("Adding peaks: ");
            }
		while (i<sortedPop.size() && (peaks.size() < q)) {
			if ((peaks.size()==0) || (!peaks.isWithinPopDist((AbstractEAIndividual)sortedPop.get(i), rho, metric))) {
				peaks.add(sortedPop.get(i));
				if (TRACE) {
                                System.out.print(" " + sortedPop.getEAIndividual(i).getFitness(0));
                            }
			}
			i++;
		}
		if (TRACE) {
			System.out.println("\nFound " + peaks.size() + " peaks, ");
//			for (int k=0; k<peaks.size(); k++) System.out.println("  " + peaks.getEAIndividual(k));
			System.out.println("Measures: " + BeanInspector.toString(peaks.getPopulationMeasures(metric)));
		}
		return peaks;
	}
	
	public boolean isStrictNicheRadius() {
		return strictNicheRadius;
	}
	public void setStrictNicheRadius(boolean strictNicheRadius) {
		this.strictNicheRadius = strictNicheRadius;
	}
	public String strictNicheRadiusTipText() {
		return "If false, any individual will be assigned its closest peak; if true, it must be within the niche radius to be assigend and remains unclustered otherwise.";
	}
	
	public void setNicheRadius(double r) {
		nicheRadius = r;
	}
	public double getNicheRadius() {
		return nicheRadius;
	}
	public String nicheRadiusTipText() {
		return "Distance below which two individuals are assumed to belong to the same niche";
	}
}
