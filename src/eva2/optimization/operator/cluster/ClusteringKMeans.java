package eva2.optimization.operator.cluster;

import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.population.Population;
import eva2.optimization.problems.F1Problem;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;

import java.util.Arrays;

/**
 * The k-mean clustering algorithms. I guess it is not a hierachical
 * clustering method.
 */
public class ClusteringKMeans implements InterfaceClustering, java.io.Serializable {

    private int k = 5;
    private double[][] c = null;
    private double mergeDist = 0.001;
    private boolean useSearchSpace = true;
    private boolean reuseC = false;
    private boolean debug = false;
    private int minClustSize = 1;
    InterfaceDistanceMetric metric = new EuclideanMetric();
    AbstractEAIndividual tmpIndy = null;

    public ClusteringKMeans() {

    }

    public ClusteringKMeans(ClusteringKMeans a) {
        this.debug = a.debug;
        this.k = a.k;
        this.useSearchSpace = a.useSearchSpace;
        this.metric = a.metric;
        this.minClustSize = a.minClustSize;
        this.mergeDist = a.mergeDist;
        if (a.c != null) {
            this.c = new double[a.c.length][a.c[0].length];
            for (int i = 0; i < this.c.length; i++) {
                System.arraycopy(a.c[i], 0, this.c[i], 0, this.c[i].length);
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
        return (Object) new ClusteringKMeans(this);
    }

    /**
     * This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be asociated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     *
     * @param pop The population of individuals that is to be clustered.
     * @return Population[]
     */
    @Override
    public Population[] cluster(Population pop, Population referencePop) {
        if (pop.size() < k) {
            // in this case, there arent enough indies to do anything, so we just return them as "unclustered"
            Population[] res = new Population[1];
            res[0] = pop.cloneShallowInds();
            return res;
        }
        tmpIndy = (AbstractEAIndividual) pop.getEAIndividual(0).clone();
//        double[][] data     = this.extractClusterDataFrom(pop);
        if (!(this.reuseC) || (this.c == null)) {
            this.c = new double[this.k][];
            // now choose random initial Cs
            Population initialSeeds = pop.getRandNIndividuals(this.k);
            for (int i = 0; i < this.k; i++) {
                if (useSearchSpace) {
                    this.c[i] = initialSeeds.getEAIndividual(i).getDoublePosition().clone();
                } else {
                    this.c[i] = initialSeeds.getEAIndividual(i).getFitness().clone();
                }

//                this.c[i] = data[RNG.randomInt(0, data.length-1)];
                //this.c[i] = data[i];     // This works!!
                // we won't check for double instances assuming that double instances
                // will be ironed out during clustering and to prevent infinite loops
                // in case there are too many double instances or too few instances
            }
        }

        // now let's do some clustering
        boolean finished = false;
        double[][] newC;
        int[] numbOfAssigned;
        int[] assignment = new int[pop.size()];
        int assign;
        while (!finished) {
            // first assign the data to the closes C
            for (int i = 0; i < pop.size(); i++) {
                // check which C is closest
                assign = 0;
                for (int j = 1; j < this.c.length; j++) {
                    if (this.distance(pop.getEAIndividual(i), this.c[assign]) > this.distance(pop.getEAIndividual(i), this.c[j])) {
                        assign = j;
                    }
                }
                assignment[i] = assign;
            }

            // now calcuate the mean of each cluster and calculate new C
            newC = new double[this.k][c[0].length];
            numbOfAssigned = new int[this.k];
            for (int i = 0; i < newC.length; i++) {
                numbOfAssigned[i] = 1;
                for (int j = 0; j < newC[i].length; j++) {
                    newC[i][j] = this.c[i][j];
                }
            }
            for (int i = 0; i < assignment.length; i++) {
                numbOfAssigned[assignment[i]]++;
                for (int j = 0; j < newC[assignment[i]].length; j++) {
                    if (useSearchSpace) {
                        newC[assignment[i]][j] += pop.getEAIndividual(i).getDoublePosition()[j];
                    } else {
                        newC[assignment[i]][j] += pop.getEAIndividual(i).getFitness(j);
                    }
                }
            }
            for (int i = 0; i < newC.length; i++) {
                for (int j = 0; j < newC[i].length; j++) {
                    if (numbOfAssigned[i] > 1) {
                        newC[i][j] /= (double) numbOfAssigned[i];
                    }
                    //else System.out.println("Someone was not assigned any data!? "+ i +" "+numbOfAssigned[i] + ": Data.size()="+data.length);
                }
            }
            if (this.debug) {
                // let's see how they arrive here
                Plot plot;
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                plot = new Plot("Debugging K-Means Clustering", "Y1", "Y2", tmpD, tmpD);

                for (int i = 0; i < pop.size(); i++) {
                    double[] x = ((InterfaceDataTypeDouble) pop.get(i)).getDoubleData();
                    plot.setUnconnectedPoint(x[0], x[1], 1);
                }
                // now add the c
                GraphPointSet mySet;
                DPoint myPoint;
                Chart2DDPointIconText tmp;
                for (int i = 0; i < this.c.length; i++) {
                    mySet = new GraphPointSet(10 + i, plot.getFunctionArea());
                    mySet.setConnectedMode(true);
                    myPoint = new DPoint(this.c[i][0], this.c[i][1]);
                    tmp = new Chart2DDPointIconText("Old: " + i);
                    tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                    myPoint = new DPoint(newC[i][0], newC[i][1]);
                    tmp = new Chart2DDPointIconText("New: " + i);
                    tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            if (this.debug) {
                // let's see how they arrive here
                Plot plot;
                double[] tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                plot = new Plot("Debugging Cluster", "Y1", "Y2", tmpD, tmpD);
                GraphPointSet mySet;
                DPoint myPoint;
                Chart2DDPointIconText tmp;
                for (int i = 0; i < pop.size(); i++) {
                    mySet = new GraphPointSet(10 + 1, plot.getFunctionArea());
                    mySet.setConnectedMode(false);
                    double[] x = pop.getEAIndividual(i).getDoublePosition();
                    myPoint = new DPoint(x[0], x[1]);
                    tmp = new Chart2DDPointIconText("" + assignment[i]);
                    if (assignment[i] % 2 == 0) {
                        tmp.setIcon(new Chart2DDPointIconCircle());
                    }
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            // finally let's check whether or not the C changed and if i can terminate k_Means
            finished = true;
            for (int i = 0; i < this.c.length; i++) {
                if (EuclideanMetric.euclideanDistance(this.c[i], newC[i]) > 0.0001) {
                    finished = false;
                }
                this.c[i] = newC[i];
            }
        } // gosh now i'm done

        // finally lets build the new populations
        Population[] result = this.cluster(pop, this.c);
//        Population[] result = new Population[this.k];
//        for (int i = 0; i < assignment.length; i++)
//            result[assignment[i]].add(pop.get(i));
        if (this.debug) {
            // let's see how they arrive here
            Plot plot;
            double[] tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            plot = new Plot("Debugging Clustering Separation", "Y1", "Y2", tmpD, tmpD);
            GraphPointSet mySet;
            DPoint myPoint;
            Chart2DDPointIconText tmp;
            for (int i = 0; i < result.length; i++) {
                mySet = new GraphPointSet(10 + 1, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                for (int j = 0; j < result[i].size(); j++) {
                    double[] x = ((InterfaceDataTypeDouble) result[i].get(j)).getDoubleData();
                    myPoint = new DPoint(x[0], x[1]);
                    tmp = new Chart2DDPointIconText("" + i);
                    if (i % 2 == 0) {
                        tmp.setIcon(new Chart2DDPointIconCircle());
                    }
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
        }

        // now expand to the expected format (unclustered indies at pop of index 0)
        int largeEnough = 0;
        // count clusters that are large enough
        for (int i = 0; i < result.length; i++) {
            if (result[i].size() >= getMinClustSize()) {
                largeEnough++;
            }
        }
        Population[] resExpanded = new Population[largeEnough + 1];
        resExpanded[0] = pop.cloneWithoutInds();
        int lastIndex = 1;
        for (int i = 0; i < result.length; i++) {
            if (result[i].size() >= getMinClustSize()) {
                resExpanded[lastIndex] = result[i];
                lastIndex++;
            } else {
                resExpanded[0].addPopulation(result[i]);
            }
        }
        tmpIndy = null;
        return resExpanded;
    }

    /**
     * This method allows you to cluster a population using c. The minimal cluster
     * size is _not_ regarded here.
     *
     * @param pop The population
     * @param c   The centroids
     * @return The clusters as populations
     */
    public Population[] cluster(Population pop, double[][] c) {
        if (tmpIndy == null) {
            tmpIndy = (AbstractEAIndividual) pop.getEAIndividual(0).clone();
        } // nec. only because the method is public...
        Population[] result = new Population[c.length];
//        double[][]      data    = this.extractClusterDataFrom(pop);
        int clusterAssigned;

        try {
            for (int i = 0; i < result.length; i++) {
                result[i] = pop.getClass().newInstance();
                result[i].setSameParams(pop);
            }
        } catch (Exception e) {
            System.err.println("problems instantiating " + pop.getClass().getName() + " for clustering!");
            e.printStackTrace();
        }
        // let's assign the elements of the population to a c
        for (int i = 0; i < pop.size(); i++) {
            // find the closest c
            clusterAssigned = 0;
            for (int j = 1; j < c.length; j++) {
                if (this.distance(pop.getEAIndividual(i), c[clusterAssigned]) > this.distance(pop.getEAIndividual(i), c[j])) {
                    clusterAssigned = j;
                }
            }
            result[clusterAssigned].add(pop.get(i));
        }
        return result;
    }

    /**
     * This method calculates the distance between two double values
     *
     * @param d1
     * @param d2
     * @return The scalar distances between d1 and d2
     */
    private double distance(AbstractEAIndividual indy, double[] p) {
        if (useSearchSpace) {
            ((InterfaceDataTypeDouble) tmpIndy).setDoubleGenotype(p);
        } else {
            tmpIndy.setFitness(p);
        }

        return metric.distance(indy, tmpIndy);
    }

    /**
     * This method extracts the double data to cluster from the
     * population
     *
     * @param pop The population
     * @return The double[][] data to cluster
     */
    private double[][] extractClusterDataFrom(Population pop) {
        double[][] data = new double[pop.size()][];

        // let's fetch the raw data either the double
        // phenotype or the coordinates in objective space
        // @todo: i case of repair i would need to set the phenotype!
        if (this.useSearchSpace && (pop.get(0) instanceof InterfaceDataTypeDouble)) {
            for (int i = 0; i < pop.size(); i++) {
                data[i] = ((InterfaceDataTypeDouble) pop.get(i)).getDoubleData();
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                data[i] = ((AbstractEAIndividual) pop.get(i)).getFitness();
            }
        }
        return data;
    }

    /**
     * This method allows you to decied if two species converge.
     *
     * @param species1 The first species.
     * @param species2 The second species.
     * @return True if species converge, else False.
     */
    @Override
    public boolean mergingSpecies(Population species1, Population species2, Population referencePop) {
        // TODO i could use the BIC metric from X-means to calculate this
        if (metric.distance(species1.getBestEAIndividual(), species2.getBestEAIndividual()) < mergeDist) {
            return true;
        } else {
            return false;
        }
    }

//    /** This method decides if a unclustered individual belongs to an already established species.
//     * @param indy          A unclustered individual.
//     * @param species       A species.
//     * @return True or False.
//     */
//    public boolean belongsToSpecies(AbstractEAIndividual indy, Population species, Population pop) {
//        // @todo perhaps the same as in convergingSpecies
//        return false;
//    }

    @Override
    public int[] associateLoners(Population loners, Population[] species, Population referencePop) {
//		tmpIndy = (AbstractEAIndividual)loners.getEAIndividual(0).clone();
        int[] res = new int[loners.size()];
        System.err.println("Warning, associateLoners not implemented for " + this.getClass());
        Arrays.fill(res, -1);
        return res;
    }

    /**
     * This method allows you to recieve the c centroids
     *
     * @return The centroids
     */
    public double[][] getC() {
        return this.c;
    }

    public void resetC() {
        this.c = null;
    }

    public static void main(String[] args) {
        ClusteringKMeans ckm = new ClusteringKMeans();
        ckm.setUseSearchSpace(true);
        ckm.debug = true;
        Population pop = new Population();
        F1Problem f1 = new F1Problem();
        f1.setProblemDimension(2);
        f1.setEAIndividual(new ESIndividualDoubleData());
        f1.initializePopulation(pop);
        ckm.cluster(pop, (Population) null);

    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Oldy but goldy: K-Means clustering.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "K-Means";
    }

    /**
     * This method allows you to set/get the number of
     * clusters tofind
     *
     * @return The current number of clusters to find.
     */
    public int getK() {
        return this.k;
    }

    public void setK(int m) {
        if (m < 1) {
            m = 1;
        }
        this.k = m;
    }

    public String kTipText() {
        return "Choose the number of clusters to find.";
    }

    /**
     * This method allows you to choose between using geno-
     * or phenotypic distance.
     *
     * @return The distance type to use.
     */
    public boolean getUseSearchSpace() {
        return this.useSearchSpace;
    }

    public void setUseSearchSpace(boolean m) {
        this.useSearchSpace = m;
    }

    public String useSearchSpaceTipText() {
        return "Toggel between search/objective space distance.";
    }

    /**
     * This method allows you to toggle reuse of c.
     *
     * @return The distance type to use.
     */
    public boolean getReuseC() {
        return this.reuseC;
    }

    public void setReuseC(boolean m) {
        this.reuseC = m;
    }

    public String reuseCTipText() {
        return "Toggel reuse of previously found cluster centroids.";
    }

    @Override
    public String initClustering(Population pop) {
        return null;
    }

    public void setMinClustSize(int minClustSize) {
        this.minClustSize = minClustSize;
    }

    public int getMinClustSize() {
        return minClustSize;
    }

    public String minClustSizeTipText() {
        return "Require a cluster to be at least of this size. Smaller ones are assigned to the unclustered set.";
    }
}
