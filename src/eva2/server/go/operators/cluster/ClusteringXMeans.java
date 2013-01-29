package eva2.server.go.operators.cluster;

import eva2.gui.GraphPointSet;
import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;
import eva2.tools.math.RNG;
import java.util.Arrays;

/** The x-means clustering method should be able to determine a
 * suiteable value for k automatically, simply by evaluating all
 * alternatives.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.06.2005
 * Time: 14:48:35
 * To change this template use File | Settings | File Templates.
 */
public class ClusteringXMeans implements InterfaceClustering, java.io.Serializable {

    public int                         m_MaxK              = 5;
    public double[][]                  m_C;
    public boolean                     m_UseSearchSpace    = false;
    public boolean                     m_Debug             = false;

    public ClusteringXMeans() {

    }

    public ClusteringXMeans(ClusteringXMeans a) {
        this.m_Debug            = a.m_Debug;
        this.m_MaxK             = a.m_MaxK;
        this.m_UseSearchSpace   = a.m_UseSearchSpace;
    }

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    @Override
    public Object clone() {
        return (Object) new ClusteringXMeans(this);
    }

    /** This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be asociated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     * @param pop       The population of individuals that is to be clustered.
     * @return Population[]
     */
    @Override
    public Population[] cluster(Population pop, Population referencePop) {
        ClusteringKMeans    kmeans      = new ClusteringKMeans();
        Population[][]      tmpResults  = new Population[this.m_MaxK][];
        double[][][]        tmpC        = new double[this.m_MaxK][][];
        double[][]          data        = this.extractClusterDataFrom(pop);

        // the first result is the unclustered population
        tmpResults[0]       = new Population[1];
        tmpResults[0][0]    = pop;
        tmpC[0]             = new double[1][];
        tmpC[0][0]          = this.calculateMean(data);
        // the other solutions are kmeans results
        for (int i = 1; i < this.m_MaxK; i++) {
            kmeans.setUseSearchSpace(this.m_UseSearchSpace);
            kmeans.setK(i+1);
            tmpResults[i]   = kmeans.cluster(pop, (Population)null);
            tmpC[i]         = kmeans.getC();
        }


        double  bestBIC = Double.NEGATIVE_INFINITY, tmpBIC;
        int     index = 0;
        for (int i = 0; i < tmpResults.length; i++) {
            tmpBIC = this.calculateBIC(tmpResults[i], tmpC[i]);
            if (this.m_Debug) {
                Plot        plot;
                double[]    tmpD = new double[2], x;
                tmpD[0] = 0;
                tmpD[1] = 0;
                plot = new eva2.gui.Plot("K="+(i+1)+" reaches BIC = "+tmpBIC, "Y1", "Y2", tmpD, tmpD);
                GraphPointSet           mySet;
                DPoint                  myPoint;
                Chart2DDPointIconText   tmp;
                for (int k = 0; k < tmpResults[i].length; k++) {
                    mySet = new GraphPointSet(10+k, plot.getFunctionArea());
                    mySet.setConnectedMode(false);
                    // for each population
                    for (int l = 0; l < tmpResults[i][k].size(); l++) {
                        x  = ((InterfaceDataTypeDouble)tmpResults[i][k].get(l)).getDoubleData();
                        myPoint = new DPoint(x[0], x[1]);
                        tmp = new Chart2DDPointIconText(""+k);
                        if (k % 2 == 0) {
                            tmp.setIcon(new Chart2DDPointIconCircle());
                        }
                        myPoint.setIcon(tmp);
                        mySet.addDPoint(myPoint);
                    }
                }
                mySet = new GraphPointSet(9, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                for (int k = 0; k < tmpC[i].length; k++) {
                    myPoint = new DPoint(tmpC[i][k][0], tmpC[i][k][1]);
                    tmp = new Chart2DDPointIconText("C/"+k);
                    if (k % 2 == 0) {
                        tmp.setIcon(new Chart2DDPointIconCircle());
                    }
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            if (tmpBIC > bestBIC) {
                bestBIC = tmpBIC;
                index = i;
            }
        }
        System.out.println("XMeans results in "+ (index+1) +" clusters.");
        Population[] result = tmpResults[index];
        this.m_C = tmpC[index];

        return result;
    }

    /** This method should calculate the BIC
     *
     */
    private double calculateBIC(Population[] pop, double[][] C) {
        double      result = 0;
        double[][]  data;
        double[]    mean;
        double      RM, R = 0, M = 0, K, sigma;


        for (int i = 0; i < pop.length; i++) {
            R += pop[i].size();
        }
        K = pop.length;
        for (int i = 0;  i < pop.length; i++) {
            data    = this.extractClusterDataFrom(pop[i]);
            RM      = data.length;
            if (data.length > 0) {
                M       = data[0].length;
                mean    = this.calculateMean(data);
                sigma   = this.calculateSigma(data, mean);
                result += - (RM/2.0)*Math.log(2*Math.PI);
                result += - 0.5*RM*M*Math.log(sigma);
                result += - 0.5*(RM-K);
                result += RM*Math.log(RM);
                result += RM*Math.log(R);
            }
        }
        result += - ((K-1)+(M*K)+1)*Math.log(R);

        return result;
    }

    private double[] calculateMean(double[][] data) {
        double[] result = new double[data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                result[j] += data[i][j];
            }
        }
        for (int j = 0; j < result.length; j++) {
            result[j] /= ((double)data.length);
        }
        return result;
    }

    private double calculateSigma(double[][] data, double[] mean) {
        double result = 0;
        if (data.length == 1) {
            return 1.0;
        }

        for (int i = 0; i < data.length; i++) {
            result += Math.pow(this.distance(data[i], mean), 2);
        }
        result /= ((double)data.length);

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
    @Override
    public boolean mergingSpecies(Population species1, Population species2, Population referencePop) {
        // @todo i could use the BIC metric from X-means to calculate this
        return false;
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
		int[] res=new int[loners.size()];
		System.err.println("Warning, associateLoners not implemented for " + this.getClass());
		Arrays.fill(res, -1);
		return res;
	}
	
    /** This method allows you to recieve the c centroids
     * @return The centroids
     */
    public double[][] getC() {
        return this.m_C;
    }

    /** This mehtod allows you to cluster a population using m_C
     * @param pop   The population
     * @param c     The centroids
     * @return The clusters as populations
     */
    public Population[] cluster(Population pop, double[][] c) {
        Population[]    result  = new Population[c.length];
        double[][]      data    = this.extractClusterDataFrom(pop);
        int             clusterAssigned;

        for (int i = 0; i < result.length; i++) {
            result[i] = new Population();
        }
        // let's assign the elements of the population to a c
        for (int i = 0; i < data.length; i++) {
            // find the closest c
            clusterAssigned = 0;
            for (int j = 1; j < c.length; j++) {
                if (this.distance(data[i], c[clusterAssigned]) > this.distance(data[i], c[j])) {
                    clusterAssigned = j;
                }
            }
            result[clusterAssigned].add(pop.get(i));
        }

        return result;
    }    

    public static void main(String[] args) {
        ClusteringXMeans ckm = new ClusteringXMeans();
        ckm.setUseSearchSpace(true);
        ckm.m_Debug = true;
        Population pop = new Population();
        pop.setTargetSize(100);
        F1Problem f1 = new F1Problem();
        f1.setProblemDimension(2);
        f1.setEAIndividual(new ESIndividualDoubleData());
        if (true) {
            int         k = 3;
            double[]    x;
            f1.initPopulation(pop);
            for (int i = 0; i < pop.size(); i++) {
                x = ((InterfaceDataTypeDouble)pop.get(i)).getDoubleData();
                switch (i%k) {
                    case 0 : {
                        x[0] = 0 + RNG.gaussianDouble(1.2);
                        x[1] = -1 + RNG.gaussianDouble(1.5);
                        break;
                    }
                    case 1 : {
                        x[0] = 3 + RNG.gaussianDouble(1.8);
                        x[1] = 8 + RNG.gaussianDouble(0.9);
                        break;
                    }
                    case 2 : {
                        x[0] = -4 + RNG.gaussianDouble(1.2);
                        x[1] = -8 + RNG.gaussianDouble(1.2);
                        break;
                    }
                    case 3 : {
                        x[0] = 7 + RNG.gaussianDouble(1.1);
                        x[1] = -5 + RNG.gaussianDouble(1.0);
                        break;
                    }
                    default : {
                        x[0] = -2 + RNG.gaussianDouble(1.2);
                        x[1] = 5 + RNG.gaussianDouble(1.2);
                    }
                }
                if (i == 0) {
                    x[0] = -10;
                    x[1] = -10;
                }
                if (i == 1) {
                    x[0] = 10;
                    x[1] = 10;
                }
                ((InterfaceDataTypeDouble)pop.get(i)).SetDoubleGenotype(x);
            }
        } else {
            f1.initPopulation(pop);
        }
        ckm.cluster(pop, (Population)null);

    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
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
    public int getMaxK() {
        return this.m_MaxK;
    }
    public void setMaxK(int m){
        if (m < 1) {
            m = 1;
        }
        this.m_MaxK = m;
    }
    public String maxKTipText() {
        return "Choose the max number of clusters to find.";
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
        return "Toggle between search/objective space distance.";
    }

    @Override
	public String initClustering(Population pop) {
		return null;
	}
}
