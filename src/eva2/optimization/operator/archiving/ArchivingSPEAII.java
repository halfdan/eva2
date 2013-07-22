package eva2.optimization.operator.archiving;


import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.ObjectiveSpaceMetric;
import eva2.optimization.population.Population;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;

/**
 * The strength Pareto EA in it's second version, which is based on
 * dominance counts.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 31.03.2004
 * Time: 15:01:06
 * To change this template use File | Settings | File Templates.
 */
public class ArchivingSPEAII extends AbstractArchiving implements java.io.Serializable {

    private InterfaceDistanceMetric m_Metric = new ObjectiveSpaceMetric();
    private boolean soutDebug = false;

    public ArchivingSPEAII() {
    }

    public ArchivingSPEAII(ArchivingSPEAII a) {
        this.m_Metric = (InterfaceDistanceMetric) a.m_Metric.clone();
    }

    @Override
    public Object clone() {
        return (Object) new ArchivingSPEAII(this);
    }

    /**
     * This method allows you to merge two populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    @Override
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }

        // First merge the current population and the archive
        Population tmpPop = new Population();
        tmpPop.addPopulation((Population) pop.getClone());
        tmpPop.addPopulation((Population) pop.getArchive().getClone());
        tmpPop.removeRedundantIndiesUsingFitness();
//        double[][] d = this.showMay(tmpPop);

        double[] RawFitness = this.calculateRawFitness(tmpPop);
        double[] kthDistance = this.calculateKthDistance(tmpPop, Math.max(2, (int) Math.sqrt(tmpPop.size())));
//        // set these values to the individuals
//        for (int i = 0; i < tmpPop.size(); i++) {
//            ((AbstractEAIndividual)tmpPop.get(i)).SetData("RavFitness", new Double(RawFitness[i]));
//            ((AbstractEAIndividual)tmpPop.get(i)).SetData("kthDistance", new Double(kthDistance[i]));
//        }

        // Now init the new archive
        Population archive = new Population();
        archive.setTargetSize(pop.getArchive().getTargetSize());

//        archive = this.m_Selection.selectFrom(tmpPop, archive.getPopulationSize());

        // first i'll add all non-dominated
        for (int i = 0; i < RawFitness.length; i++) {
            if (RawFitness[i] < 1.0) {
                // this one is non-dominated, let's add it
                archive.add(tmpPop.get(i));
            }
        }

        // if there is some place left let's add some more
        int currentLevel = 0;
        while (!archive.targetSizeReached()) {
            currentLevel++;
            for (int i = 0; i < RawFitness.length; i++) {
                if ((RawFitness[i] >= currentLevel) && (RawFitness[i] < currentLevel + 1)) {
                    archive.add(tmpPop.get(i));
                }
            }
        }

        // Here i want to remove surplus individuals *pff*
        // So the basic idea is to search on the highes level of
        // RawFitness for the lowest kthDistance()!!!
        int ICurSma;
        double curSmall, highestLevel;
        while (archive.targetSizeExceeded()) {
            highestLevel = 0;
            RawFitness = this.calculateRawFitness(archive);
            for (int i = 0; i < RawFitness.length; i++) {
                if (RawFitness[i] > highestLevel) {
                    highestLevel = RawFitness[i];
                }
            }
            kthDistance = this.calculateKthDistance(archive, Math.max(2, (int) Math.sqrt(archive.size())));
            ICurSma = -1;
            curSmall = Double.MAX_VALUE;
            for (int i = 0; i < kthDistance.length; i++) {
                if ((highestLevel == (RawFitness[i]) && (curSmall > kthDistance[i]))) {
                    curSmall = kthDistance[i];
                    ICurSma = i;
                }
            }
            archive.remove(ICurSma);
        }

        pop.SetArchive(archive);

    }

    private double[][] showMay(Population pop) {
        Population tmp = new Population();
        tmp.addPopulation(pop);
        if (pop.getArchive() != null) {
            tmp.addPopulation(pop.getArchive());
        }

        double[][] fitness = new double[tmp.size()][];
        for (int i = 0; i < tmp.size(); i++) {
            fitness[i] = ((AbstractEAIndividual) tmp.get(i)).getFitness();
        }
        double[] minY, maxY;
        minY = fitness[0];
        maxY = fitness[0];
        for (int i = 1; i < fitness.length; i++) {
            if (minY[0] > fitness[i][0]) {
                minY = fitness[i];
            }
            if (maxY[1] > fitness[i][1]) {
                maxY = fitness[i];
            }
        }
        double[][] result = new double[2][];
        result[0] = minY;
        result[1] = maxY;
        //System.out.println("Borders: ("+ (Math.round((100*minY[0]))/100.0)+"/"+ (Math.round((100*minY[1]))/100.0)+") ("+ (Math.round((100*maxY[0]))/100.0)+"/"+ (Math.round((100*maxY[1]))/100.0)+")");
        return result;
    }

    /**
     * This method will calculate the raw fitness
     *
     * @param pop The population to evaluate
     * @return The raw fitness for all individuals
     */
    private double[] calculateRawFitness(Population pop) {
        double[] result = new double[pop.size()];
        int[] SPEAStrength;
        AbstractEAIndividual tmpIndy;

        SPEAStrength = new int[pop.size()];
        // first calculate the SPEAStrength
        for (int i = 0; i < pop.size(); i++) {
            result[i] = 0;
            SPEAStrength[i] = 0;
            tmpIndy = (AbstractEAIndividual) pop.get(i);
            for (int j = 0; j < pop.size(); j++) {
                if ((i != j) && (!this.isEqualTo(tmpIndy, (AbstractEAIndividual) pop.get(j))) && (tmpIndy.isDominating((AbstractEAIndividual) pop.get(j)))) {
                    SPEAStrength[i]++;
                }
            }
        }
        if (this.soutDebug) {
            for (int i = 0; i < SPEAStrength.length; i++) {
                System.out.println("SPEAStrength " + i + ": " + SPEAStrength[i]);
            }
        }

        // now calculate the SPEAFitness
        for (int i = 0; i < pop.size(); i++) {
            for (int j = 0; j < pop.size(); j++) {
                if ((i != j) && (!this.isEqualTo((AbstractEAIndividual) pop.get(i), (AbstractEAIndividual) pop.get(j))) && (((AbstractEAIndividual) pop.get(i)).isDominating(((AbstractEAIndividual) pop.get(j))))) {
                    result[j] += SPEAStrength[i];
                    if (this.soutDebug) {
                        if (i == 14) {
                            double[] f1, f2;
                            f1 = ((AbstractEAIndividual) pop.get(i)).getFitness();
                            f2 = ((AbstractEAIndividual) pop.get(j)).getFitness();
                            for (int n = 0; n < f1.length; n++) {
                                System.out.println("" + Math.abs(f1[n] - f2[n]));
                            }
                        }
                        System.out.println("Adding: " + SPEAStrength[i] + " to " + j + " because " + i + " is dominating!");
                    }
                }
            }
        }
        if (this.soutDebug) {
            for (int i = 0; i < result.length; i++) {
                System.out.println("Result " + i + ": " + result[i]);
            }
            this.m_Plot = new Plot("Debug SPEAII", "Y1", "Y2", true);
            this.m_Plot.setUnconnectedPoint(0, 0, 11);
            this.m_Plot.setUnconnectedPoint(1.2, 2.0, 11);
            GraphPointSet mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
            double[][] trueFitness;
            trueFitness = new double[pop.size()][];
            for (int i = 0; i < pop.size(); i++) {
                trueFitness[i] = ((AbstractEAIndividual) pop.get(i)).getFitness();
                System.out.println("Fitness: (" + trueFitness[i][0] + "/" + trueFitness[i][1] + ")");
            }
            DPoint myPoint;
            Chart2DDPointIconText tmp;
            mySet.setConnectedMode(false);
            for (int i = 0; i < trueFitness.length; i++) {

                myPoint = new DPoint(trueFitness[i][0], trueFitness[i][1]);
                tmp = new Chart2DDPointIconText("" + SPEAStrength[i] + "/" + result[i]);
                tmp.setIcon(new Chart2DDPointIconCircle());
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }
        }
        return result;
    }

    private boolean isEqualTo(AbstractEAIndividual a1, AbstractEAIndividual a2) {
        double[] f1, f2;
        f1 = a1.getFitness();
        f2 = a2.getFitness();
        for (int i = 0; i < f1.length; i++) {
            if (Math.abs(f1[i] - f2[i]) > 0.00000001) {
                return false;
            }
        }
        return false;
    }

    /**
     * This method will calculate the distance to the k-th neighbour.
     *
     * @param pop The population to compute.
     * @param k   The index k.
     * @return The distance for each individual.
     */
    public double[] calculateKthDistance(Population pop, int k) {
        double[] result = new double[pop.size()];
        double[][] distMatrix = new double[pop.size()][pop.size()];

        // first let's compute the complete distance matrix
        for (int i = 0; i < pop.size(); i++) {
            distMatrix[i][i] = 0.0;
            for (int j = i + 1; j < pop.size(); j++) {
                distMatrix[i][j] = this.m_Metric.distance((AbstractEAIndividual) pop.get(i), (AbstractEAIndividual) pop.get(j));
                distMatrix[j][i] = distMatrix[i][j];
            }
        }
        // now let's search for the k-th distance
        // for each individual
        int current;
        double currentSmallest;
        for (int i = 0; i < result.length; i++) {
            // for the k-th distance
            for (int j = 0; j < k; j++) {
                // search for the smallest distance and set it to max
                current = -1;
                currentSmallest = Double.MAX_VALUE;
                for (int n = 0; n < result.length; n++) {
                    if ((i != n) && (currentSmallest > distMatrix[i][n])) {
                        current = n;
                        currentSmallest = distMatrix[i][n];
                    }
                }
                if (current >= 0) {
                    result[i] = distMatrix[i][current];
                } else {
                    System.out.println("Error no smallest found in calculateKthDistance().");
                }
                distMatrix[i][current] = Double.MAX_VALUE;
            }
        }
//        double[] result = new double[pop.size()];
//        double  tmpD;
//
//        for (int i = 0; i < result.length; i++) {
//            result[i] = Double.POSITIVE_INFINITY;
//            for (int j = 0; j < result.length; j++) {
//                if (i != j) {
//                    tmpD = this.m_Metric.distance((AbstractEAIndividual)pop.get(i), (AbstractEAIndividual)pop.get(j));
//                    if (tmpD < result[i]) result[i] = tmpD;
//                }
//            }
//        }
        return result;
    }


    /**
     * This method will calculate the SPEA strength and the raw fitness for all
     * individuals
     */
    public double[] calculateSPEA(Population pop) {
        int[] SPEAStrength, SPEAFitness;
        double[] SPEAResult;
        double[][] trueFitness;
        AbstractEAIndividual tmpIndy;

        SPEAStrength = new int[pop.size()];
        SPEAFitness = new int[pop.size()];
        SPEAResult = new double[pop.size()];
        trueFitness = new double[pop.size()][];

        // first calculate the SPEAStrength
        for (int i = 0; i < pop.size(); i++) {
            tmpIndy = (AbstractEAIndividual) pop.get(i);
            trueFitness[i] = tmpIndy.getFitness();
            for (int j = i + 1; j < pop.size(); j++) {
                if (tmpIndy.isDominating((AbstractEAIndividual) pop.get(j))) {
                    SPEAStrength[i]++;
                } else {
                    if (((AbstractEAIndividual) pop.get(j)).isDominating(tmpIndy)) {
                        SPEAStrength[j]++;
                    }
                }
            }
        }

        // now calculate the SPEAFitness
        for (int i = 0; i < pop.size(); i++) {
            for (int j = 0; j < pop.size(); j++) {
                if (i != j) {
                    if (((AbstractEAIndividual) pop.get(i)).isDominating(((AbstractEAIndividual) pop.get(j)))) {
                        SPEAFitness[j] += SPEAStrength[i];
                    }
                }
            }
        }

        // now i'll calculate the distance to the k'th neighbour
        // first let's calculate the complete distance matrix
        double[][] distMatrix = new double[pop.size()][pop.size()];
        int k = (int) Math.sqrt(pop.size());
        for (int i = 0; i < pop.size(); i++) {
            distMatrix[i][i] = 0.0;
            for (int j = i + 1; j < pop.size(); j++) {
                distMatrix[i][j] = this.m_Metric.distance((AbstractEAIndividual) pop.get(i), (AbstractEAIndividual) pop.get(j));
                distMatrix[j][i] = distMatrix[i][j];
            }
        }
        // now find the k'th distance *grml grml* what a mess!
        double[] tmpD = new double[pop.size()];
        double[] D = new double[pop.size()];
        for (int i = 0; i < tmpD.length; i++) {
            tmpD[i] = Double.POSITIVE_INFINITY;
            D[i] = Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < tmpD.length; j++) {
                for (int n = 0; n < tmpD.length; n++) {
                    if ((distMatrix[j][n] > D[j]) && (distMatrix[j][n] < tmpD[j])) {
                        tmpD[j] = distMatrix[j][n];
                    }
                }
            }
            for (int j = 0; j < tmpD.length; j++) {
                D[j] = tmpD[j];
                tmpD[j] = Double.POSITIVE_INFINITY;
            }
        }

        // now set the SPEAFitness
        for (int i = 0; i < SPEAResult.length; i++) {
            if (1 / (2 + D[i]) >= 1) {
                System.out.println("d " + 1 / (2 + D[i]));
            }
            SPEAResult[i] = SPEAFitness[i] + (1 / (2 + D[i]));
            ((AbstractEAIndividual) pop.get(i)).putData("RawFit", new Double(SPEAFitness[i]));
            ((AbstractEAIndividual) pop.get(i)).putData("SPEAFit", new Double(SPEAResult[i]));
        }

        // Puh!
        return SPEAResult;
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
        return "Strength Pareto EA revision 2.0. The variable k to calculate the k-th distance is given by max(2, sqrt(archive.size())).";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "SPEA II";
    }
}
