package javaeva.server.go.operators.cluster;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.operators.distancemetric.InterfaceDistanceMetric;
import javaeva.server.go.operators.distancemetric.PhenotypeMetricDoubleData;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.tools.RandomNumberGenerator;
import javaeva.gui.Plot;
import javaeva.gui.GraphPointSet;
import javaeva.gui.Chart2DDPointIconText;
import javaeva.gui.Chart2DDPointIconCircle;

import java.util.ArrayList;

import wsi.ra.chart2d.DPoint;

/** The k-mean clustering algorithms. I guess it is not a hierachical
 * clustering method.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.09.2004
 * Time: 13:49:44
 * To change this template use File | Settings | File Templates.
 */
public class ClusteringKMeans implements InterfaceClustering, java.io.Serializable {

    public int                         m_K                 = 5;
    public double[][]                  m_C                 = null;
    public boolean                     m_UseSearchSpace    = false;
    public boolean                     m_ReuseC            = false;
    public boolean                     m_Debug             = false;

    public ClusteringKMeans() {

    }

    public ClusteringKMeans(ClusteringKMeans a) {
        this.m_Debug            = a.m_Debug;
        this.m_K                = a.m_K;
        this.m_UseSearchSpace   = a.m_UseSearchSpace;
        if (a.m_C != null) {
            this.m_C = new double[a.m_C.length][a.m_C[0].length];
            for (int i = 0; i < this.m_C.length; i++) {
                System.arraycopy(a.m_C[i], 0, this.m_C[i], 0, this.m_C[i].length);
            }
        }
    }

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone() {
        return (Object) new ClusteringKMeans(this);
    }

    /** This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be asociated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     * @param pop       The population of individuals that is to be clustered.
     * @return Population[]
     */
    public Population[] cluster(Population pop) {
        double[][] data     = this.extractClusterDataFrom(pop);
        if (!(this.m_ReuseC) || (this.m_C == null)) {
            this.m_C            = new double[this.m_K][];
            // now choose random initial Cs
            for (int i = 0; i < this.m_C.length; i++) {
                this.m_C[i] = data[RandomNumberGenerator.randomInt(0, data.length-1)];
                //this.m_C[i] = data[i];     // This works!!
                // we won't check for double instances assuming that double instances
                // will be ironed out during clustering and to prevent infinite loops
                // in case there are too many double instances or too few instances
            }
        }

        // now let's do some clustering
        boolean     finished = false;
        double[][]  newC;
        int[]       numbOfAssigned;
        int[]       assignment = new int[data.length];
        int         assign;
        while (!finished) {
            // first assign the data to the closes C
            for (int i = 0; i < data.length; i++) {
                // check which C is closest
                assign = 0;
                for (int j = 1; j < this.m_C.length; j++) {
                    if (this.distance(this.m_C[assign], data[i]) > this.distance(this.m_C[j], data[i]))
                        assign = j;
                }
                assignment[i] = assign;
            }

            // now calcuate the mean of each cluster and calculate new C
            newC            = new double[this.m_K][data[0].length];
            numbOfAssigned  = new int[this.m_K];
            for (int i = 0; i < newC.length; i++) {
                numbOfAssigned[i] = 1;
                for (int j = 0; j < newC[i].length; j++) newC[i][j] = this.m_C[i][j];
            }
            for (int i = 0; i < assignment.length; i++) {
                numbOfAssigned[assignment[i]]++;
                for (int j = 0; j < newC[assignment[i]].length; j++) {
                    newC[assignment[i]][j] += data[i][j];
                }
            }
            for (int i = 0; i < newC.length; i++) {
                for (int j = 0; j < newC[i].length; j++) {
                    if (numbOfAssigned[i] > 1) newC[i][j] = newC[i][j]/(double)numbOfAssigned[i];
                    //else System.out.println("Someone was not assigned any data!? "+ i +" "+numbOfAssigned[i] + ": Data.size()="+data.length);
                }
            }
            if (this.m_Debug) {
                // let's see how they arrive here
                Plot        plot;
                double[]    tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                plot = new javaeva.gui.Plot("Debugging K-Means Clustering", "Y1", "Y2", tmpD, tmpD);

                for (int i = 0; i < pop.size(); i++) {
                    double[] x = ((InterfaceDataTypeDouble)pop.get(i)).getDoubleData();
                    plot.setUnconnectedPoint(x[0], x[1], 1);
                }
                // now add the c
                GraphPointSet           mySet;
                DPoint                  myPoint;
                Chart2DDPointIconText   tmp;
                for (int i = 0; i < this.m_C.length; i++) {
                    mySet = new GraphPointSet(10+i, plot.getFunctionArea());
                    mySet.setConnectedMode(true);
                    myPoint = new DPoint(this.m_C[i][0], this.m_C[i][1]);
                    tmp = new Chart2DDPointIconText("Old: "+i);
                    tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                    myPoint = new DPoint(newC[i][0], newC[i][1]);
                    tmp = new Chart2DDPointIconText("New: "+i);
                    tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            if (this.m_Debug) {
                // let's see how they arrive here
                Plot        plot;
                double[]    tmpD = new double[2];
                tmpD[0] = 0;
                tmpD[1] = 0;
                plot = new javaeva.gui.Plot("Debugging Cluster", "Y1", "Y2", tmpD, tmpD);
                GraphPointSet           mySet;
                DPoint                  myPoint;
                Chart2DDPointIconText   tmp;
                for (int i = 0; i < data.length; i++) {
                    mySet = new GraphPointSet(10+1, plot.getFunctionArea());
                    mySet.setConnectedMode(false);
                    double[] x  = data[i];
                    myPoint = new DPoint(x[0], x[1]);
                    tmp = new Chart2DDPointIconText(""+assignment[i]);
                    if (assignment[i] % 2 == 0) tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            // finally let's check whether or not the C changed and if i can terminate k_Means
            finished = true;
            for (int i = 0; i < this.m_C.length; i++) {
                if (this.distance(this.m_C[i], newC[i]) > 0.0001) finished = false;
                this.m_C[i] = newC[i];
            }
        } // gosh now i'm done

        // finally lets build the new populations
        Population[] result = this.cluster(pop, this.m_C);
//        Population[] result = new Population[this.m_K];
//        for (int i = 0; i < assignment.length; i++)
//            result[assignment[i]].add(pop.get(i));
        if (this.m_Debug) {
            // let's see how they arrive here
            Plot        plot;
            double[]    tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            plot = new javaeva.gui.Plot("Debugging Clustering Separation", "Y1", "Y2", tmpD, tmpD);
            GraphPointSet           mySet;
            DPoint                  myPoint;
            Chart2DDPointIconText   tmp;
            for (int i = 0; i < result.length; i++) {
                mySet = new GraphPointSet(10+1, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                for (int j = 0; j < result[i].size(); j++) {
                    double[] x  = ((InterfaceDataTypeDouble)result[i].get(j)).getDoubleData();
                    myPoint = new DPoint(x[0], x[1]);
                    tmp = new Chart2DDPointIconText(""+i);
                    if (i % 2 == 0) tmp.setIcon(new Chart2DDPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
        }

        return result;
    }

    /** This mehtod allows you to cluster a population using m_C
     * @param pop   The population
     * @param c     The centroids
     * @return The clusters as populaitons
     */
    public Population[] cluster(Population pop, double[][] c) {
        Population[]    result  = new Population[c.length];
        double[][]      data    = this.extractClusterDataFrom(pop);
        int             clusterAssigned;

        for (int i = 0; i < result.length; i++) result[i] = new Population();
        // let's assign the elements of the population to a c
        for (int i = 0; i < data.length; i++) {
            // find the closest c
            clusterAssigned = 0;
            for (int j = 1; j < c.length; j++) {
                if (this.distance(data[i], c[clusterAssigned]) > this.distance(data[i], c[j]))
                    clusterAssigned = j;
            }
            result[clusterAssigned].add(pop.get(i));
        }

        return result;
    }

    /** This method calculates the distance between two double values
     * @param d1
     * @param d2
     * @return The scalar distances between d1 and d2
     */
    private double distance(double[] d1, double[] d2) {
        double result = 0;

        for (int i = 0; i < d1.length; i++) {
            result += Math.pow(d1[i] - d2[i], 2);
        }
        result = Math.sqrt(result);
        return result;
    }

    /** This method extracts the double data to cluster from the
     * population
     * @param pop   The population
     * @return The double[][] data to cluster
     */
    private double[][] extractClusterDataFrom(Population pop) {
        double[][] data = new double[pop.size()][];

        // let's fetch the raw data either the double
        // phenotype or the coordinates in objective space
        // @todo: i case of repair i would need to set the phenotype!
        if (this.m_UseSearchSpace && (pop.get(0) instanceof InterfaceDataTypeDouble)) {
            for (int i = 0; i < pop.size(); i++) {
                data[i] = ((InterfaceDataTypeDouble)pop.get(i)).getDoubleData();
            }
        } else {
            for (int i = 0; i < pop.size(); i++) {
                data[i] = ((AbstractEAIndividual)pop.get(i)).getFitness();
            }
        }
        return data;
    }

    /** This method allows you to decied if two species converge.
     * @param species1  The first species.
     * @param species2  The second species.
     * @return True if species converge, else False.
     */
    public boolean convergingSpecies(Population species1, Population species2) {
        // @todo i could use the BIC metric from X-means to calculate this
        return false;
    }

    /** This method decides if a unclustered individual belongs to an already established species.
     * @param indy          A unclustered individual.
     * @param species       A species.
     * @return True or False.
     */
    public boolean belongsToSpecies(AbstractEAIndividual indy, Population species) {
        // @todo perhaps the same as in convergingSpecies
        return false;
    }

    /** This method allows you to recieve the c centroids
     * @return The centroids
     */
    public double[][] getC() {
        return this.m_C;
    }

    public void resetC() {
        this.m_C = null;
    }

    public static void main(String[] args) {
        ClusteringKMeans ckm = new ClusteringKMeans();
        ckm.setUseSearchSpace(true);
        ckm.m_Debug = true;
        Population pop = new Population();
        F1Problem f1 = new F1Problem();
        f1.setProblemDimension(2);
        f1.setEAIndividual(new ESIndividualDoubleData());
        f1.initPopulation(pop);
        ckm.cluster(pop);

    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Oldy but goldy: K-Means clustering.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "K-Means";
    }

    /** This method allows you to set/get the number of
     * clusters tofind
     * @return The current number of clusters to find.
     */
    public int getK() {
        return this.m_K;
    }
    public void setK(int m){
        if (m < 1) m = 1;
        this.m_K = m;
    }
    public String kTipText() {
        return "Choose the number of clusters to find.";
    }

    /** This method allows you to choose between using geno-
     * or phenotypic distance.
     * @return The distance type to use.
     */
    public boolean getUseSearchSpace() {
        return this.m_UseSearchSpace;
    }
    public void setUseSearchSpace(boolean m){
        this.m_UseSearchSpace = m;
    }
    public String useSearchSpaceTipText() {
        return "Toggel between search/objective space distance.";
    }

    /** This method allows you to toggle reuse of c.
     * @return The distance type to use.
     */
    public boolean getReuseC() {
        return this.m_ReuseC;
    }
    public void setReuseC(boolean m){
        this.m_ReuseC = m;
    }
    public String reuseCTipText() {
        return "Toggel reuse of previously found cluster centroids.";
    }
}