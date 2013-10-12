package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.optimization.enums.BOAScoringMethods;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.BKnapsackProblem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.tools.math.BayNet;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.io.*;
import java.text.DateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic implementation of the Bayesian Optimization Algorithm
 * <p/>
 * Martin Pelikan, David E. Goldberg and Erick Cantu-Paz: 'BOA: The Bayesian
 * Optimization Algorithm' the works by Martin Pelikan and David E. Goldberg.
 * Genetic and Evolutionary Computation Conference (GECCO-99), pp. 525-532
 * (1999)
 *
 * @author seitz
 */
@Description(text = "Basic implementation of the Bayesian Optimization Algorithm based on the works by Martin Pelikan and David E. Goldberg.")
public class BOA implements InterfaceOptimizer, java.io.Serializable {
    private static final Logger LOGGER = Logger.getLogger(BOA.class.getName());
    transient private InterfacePopulationChangedEventListener m_Listener = null;
    private String m_Identifier = "BOA";
    private int probDim = 8;
    private int fitCrit = -1;
    private int PopSize = 50;
    private int numberOfParents = 3;
    private transient BayNet network = null;
    private Population population = new Population();
    private AbstractOptimizationProblem problem = new BKnapsackProblem();
    private AbstractEAIndividual template = null;
    private double learningSetRatio = 0.5;
    private double resampleRatio = 0.5;
    private double upperProbLimit = 0.9;
    private double lowerProbLimit = 0.1;
    private int count = 0;
    private String netFolder = "BOAOutput";
    private int[][] edgeRate = null;
    private BOAScoringMethods scoringMethod = BOAScoringMethods.BDM;
    private boolean printNetworks = false;
    private boolean printEdgeRate = false;
    private boolean printTimestamps = false;
    private boolean printMetrics = false;

    public BOA() {
    }

    public BOA(int numberOfParents, int popSize, BOAScoringMethods method,
               double learningSetRatio, double resampleRatio, String outputFolder,
               double upperProbLimit, double lowerProbLimit, boolean printNetworks,
               boolean printEdgeRate, boolean printMetrics, boolean printTimestamps) {
        this.numberOfParents = numberOfParents;
        this.PopSize = popSize;
        this.scoringMethod = method;
        this.learningSetRatio = learningSetRatio;
        this.resampleRatio = resampleRatio;
        this.netFolder = outputFolder;
        this.upperProbLimit = upperProbLimit;
        this.lowerProbLimit = lowerProbLimit;
        this.printEdgeRate = printEdgeRate;
        this.printNetworks = printNetworks;
        this.printMetrics = printMetrics;
        this.printTimestamps = printTimestamps;
    }

    public BOA(BOA b) {
        this.m_Listener = b.m_Listener;
        this.m_Identifier = b.m_Identifier;
        this.probDim = b.probDim;
        this.fitCrit = b.fitCrit;
        this.PopSize = b.PopSize;
        this.numberOfParents = b.numberOfParents;
        this.network = (BayNet) b.network.clone();
        this.population = (Population) b.population.clone();
        this.problem = (AbstractOptimizationProblem) b.problem.clone();
        this.template = (AbstractEAIndividual) b.template.clone();
        this.learningSetRatio = b.learningSetRatio;
        this.resampleRatio = b.resampleRatio;
        this.upperProbLimit = b.upperProbLimit;
        this.lowerProbLimit = b.lowerProbLimit;
        this.count = b.count;
        this.netFolder = b.netFolder;
        this.scoringMethod = b.scoringMethod;
        this.edgeRate = new int[b.edgeRate.length][b.edgeRate.length];
        for (int i = 0; i < this.edgeRate.length; i++) {
            for (int j = 0; j < this.edgeRate[i].length; j++) {
                this.edgeRate[i][j] = b.edgeRate[i][j];
            }
        }
        this.scoringMethod = b.scoringMethod;
//		this.printExtraOutput = b.printExtraOutput;
        this.printNetworks = b.printNetworks;
        this.printMetrics = b.printMetrics;
        this.printEdgeRate = b.printEdgeRate;
        this.printTimestamps = b.printTimestamps;
    }

    @Override
    public Object clone() {
        return new BOA(this);
    }

    @Override
    public String getName() {
        return "Bayesian Optimization Algorithm";
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }

    private void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            LOGGER.log(Level.INFO, "creating directory: " + directoryName);
            theDir.mkdir();
        }
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener == ea) {
            m_Listener = null;
            return true;
        } else {
            return false;
        }
    }

    private static BitSet getBinaryData(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceGAIndividual) {
            return ((InterfaceGAIndividual) indy).getBGenotype();
        } else if (indy instanceof InterfaceDataTypeBinary) {
            return ((InterfaceDataTypeBinary) indy).getBinaryData();
        } else {
            throw new RuntimeException(
                    "Unable to get binary representation for "
                            + indy.getClass());
        }
    }

    /**
     * evaluate the given Individual and increments the counter. if the
     * individual is null, only the counter is incremented
     *
     * @param indy the individual you want to evaluate
     */
    private void evaluate(AbstractEAIndividual indy) {
        // evaluate the given individual if it is not null
        if (indy == null) {
            LOGGER.log(Level.WARNING, "tried to evaluate null");
            return;
        }
        this.problem.evaluate(indy);
        // increment the number of evaluations
        this.population.incrFunctionCalls();
    }

    /**
     * the default initialization
     */
    private void defaultInit() {
        this.count = 0;
//		if (printExtraOutput) {
        if (printTimestamps) {
            printTimeStamp();
        }
//		}
        if (population == null) {
            this.population = new Population(this.PopSize);
        } else {
            this.population.setTargetPopSize(this.PopSize);
        }
        this.template = this.problem.getIndividualTemplate();
        if (!(template instanceof InterfaceDataTypeBinary)) {
            LOGGER.log(Level.WARNING, "Requiring binary data!");
        } else {
            Object dim = BeanInspector.callIfAvailable(problem,
                    "getProblemDimension", null);
            if (dim == null) {
                LOGGER.log(Level.WARNING, "Coudn't get problem dimension!");
            }
            probDim = (Integer) dim;
            ((InterfaceDataTypeBinary) this.template)
                    .setBinaryGenotype(new BitSet(probDim));
        }
        this.network = new BayNet(this.probDim, upperProbLimit, lowerProbLimit);
        this.network.setScoringMethod(this.scoringMethod);
        this.edgeRate = new int[this.probDim][this.probDim];
    }

    @Override
    public void init() {
        defaultInit();
        this.problem.initializePopulation(this.population);
        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    private void evaluatePopulation(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
            evaluate(pop.getEAIndividual(i));
        }
    }

    @Override
    public void initByPopulation(Population pop, boolean reset) {
        if (reset) {
            init();
        } else {
            defaultInit();
            this.population = pop;
        }
    }

    private void generateGreedy(Population pop) {
        this.network = new BayNet(this.probDim, upperProbLimit, lowerProbLimit);
        this.network.setScoringMethod(this.scoringMethod);
        boolean improvement = true;
        this.network.initScoreArray(pop);
        double score = this.network.getNewScore(pop, -1);
        double score1 = score;
        List<Pair<Integer, Integer>> bestNetworks = new LinkedList<Pair<Integer, Integer>>();
        while (improvement) {
            improvement = false;
            for (int i = 0; i < this.probDim; i++) {
                for (int j = 0; j < this.probDim; j++) {
                    // if we are allowed to add the edge
                    if ((!this.network.hasEdge(i, j))
                            && (i != j)
                            && (this.network.getNode(j).getNumberOfParents() < this.numberOfParents)) {
                        // add the edge
                        this.network.addEdge(i, j);
                        // if it is still acyclic
                        if (this.network.isACyclic(i, j)) {
                            // calculate the new score
                            double tmpScore = this.network.getNewScore(pop, j);
                            // if we have a score larger or equal to the current score
                            if (tmpScore >= score && tmpScore != score1) {
                                // if the score is equal
                                if (tmpScore == score) {
                                    // add the edge to the list of possible new edges
                                    bestNetworks
                                            .add(new Pair<Integer, Integer>(i,
                                                    j));
                                    // if we have a better score
                                } else {
                                    // delete the current possible edges
                                    bestNetworks.clear();
                                    // add the edge to the list fo possible new edges
                                    bestNetworks
                                            .add(new Pair<Integer, Integer>(i,
                                                    j));
                                    // adapt the score
                                    score = tmpScore;
                                    // we could improve the network
                                    improvement = true;
                                }
                            }
                        }
                        // remove the edge from the network and try the next one
                        this.network.removeEdge(i, j);
                    }
                }
            }
            // if we found at least one edge that could improve the network
            if (bestNetworks.size() > 0) {
                // get one edge randomly from the list of possible edges
                int val = RNG.randomInt(bestNetworks.size());
                Pair<Integer, Integer> pair = bestNetworks.get(val);
                // add it to the network
                this.network.addEdge(pair.getHead(), pair.getTail());
                // adapt the array that allowes the fast calculation of the scores
                this.network.updateScoreArray(pop, pair.getTail());
            }
            // adapt the score
            score = this.network.getNewScore(pop, -1);
            score1 = score;
            bestNetworks.clear();
        }
        score = this.network.getScore(pop);
    }

    /**
     * Generate a Bayesian network with the individuals of the population as a
     * reference Point
     *
     * @param pop the individuals the network is based on
     */
    private void constructNetwork(Population pop) {
        generateGreedy(pop);
    }

    /**
     * generate new individuals based on the bayesian network
     *
     * @return the new individuals
     */
    private Population generateNewIndys(int sampleSetSize) {
        Population pop = new Population(sampleSetSize);
        LOGGER.log(Level.CONFIG, "Resampling " + sampleSetSize + " indies...");
        while (pop.size() < sampleSetSize) {
            AbstractEAIndividual indy = (AbstractEAIndividual) this.template
                    .clone();
            BitSet data = this.network.sample(getBinaryData(indy));
            ((InterfaceDataTypeBinary) indy).setBinaryGenotype(data);
            evaluate(indy);
            pop.add(indy);
        }
        return pop;
    }

    /**
     * Calculate a plausible number of individuals to be resampled per
     * iteration.
     *
     * @return
     */
    private int calcResampleSetSize() {
        int result = (int) Math.min(PopSize,
                Math.max(1.0, ((double) PopSize) * resampleRatio));
        return result;
    }

    /**
     * Calculate a plausible number of individuals from which the BayNet is
     * learned. In principle this can be independent of the resampling set size.
     *
     * @return
     */
    private int calcLearningSetSize() {
        return (int) Math.min(PopSize,
                Math.max(1.0, ((double) PopSize) * learningSetRatio));
    }

    /**
     * remove the individuals in pop from the population
     *
     * @param pop
     */
    public void remove(Population pop) {
        for (Object indy : pop) {
            this.population.remove(indy);
        }
    }

    private void printEdgeRate() {
        String filename = this.netFolder + "/edgeRate.m";
        Writer w = null;
        PrintWriter out = null;
        String message = "edgeRate" + this.scoringMethod + " = [";
        createDirectoryIfNeeded(this.netFolder);
        for (int i = 0; i < this.edgeRate.length; i++) {
            for (int j = 0; j < this.edgeRate.length; j++) {
                message += (((double) edgeRate[i][j]) / (this.count + 1));
                if (j != this.edgeRate.length - 1) {
                    message += ",";
                }
            }
            if (i != this.edgeRate.length - 1) {
                message += ";";
            }
        }
        message += "];";
        try {
            w = new FileWriter(filename);
            out = new PrintWriter(w);
            out.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printNetworkToFile(String i) {
        String filename = this.netFolder + "/network_" + i + ".graphml";
        Writer w = null;
        PrintWriter out = null;
        String message = this.network.generateYFilesCode();
        createDirectoryIfNeeded(this.netFolder);
        try {
            w = new FileWriter(filename);
            out = new PrintWriter(w);
            out.write(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printTimeStamp() {
        String fileName = this.netFolder + "/timestamps.txt";
        Date d = new Date();
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        String message = this.count + "\t" + df.format(d) + "\n";
        createDirectoryIfNeeded(this.netFolder);
        boolean exists = (new File(fileName)).exists();
        if (exists) {
            try {
                // Create file
                FileWriter fstream = new FileWriter((fileName), true);
                BufferedWriter out = new BufferedWriter(fstream);

                out.write(message);
                out.newLine();
                // Close the output stream
                out.close();
            } catch (Exception e) {// Catch exception if any
                LOGGER.log(Level.WARNING, "Error: ", e);
            }

        } else {
            try {
                // Create file
                FileWriter fstream = new FileWriter((fileName), false);
                BufferedWriter out = new BufferedWriter(fstream);
                out.newLine();
                out.write(message);
                out.newLine();
                // Close the output stream
                out.close();
            } catch (Exception e) {// Catch exception if any
                LOGGER.log(Level.WARNING, "Error: ", e);
            }
        }
    }

    private void printMetrics(Population pop) {
        this.network.setScoringMethod(BOAScoringMethods.BDM);
        double bdmMetric = this.network.getScore(pop);
        this.network.setScoringMethod(BOAScoringMethods.K2);
        double k2Metric = this.network.getScore(pop);
        this.network.setScoringMethod(BOAScoringMethods.BIC);
        double bicMetric = this.network.getScore(pop);
        this.network.setScoringMethod(this.scoringMethod);
        String fileName = this.netFolder + "/" + "metrics.csv";
        createDirectoryIfNeeded(this.netFolder);
        boolean exists = (new File(fileName)).exists();
        if (exists) {
            try {
                // Create file
                FileWriter fstream = new FileWriter((fileName), true);
                BufferedWriter out = new BufferedWriter(fstream);

                out.write("" + bdmMetric + "," + k2Metric + "," + bicMetric);
                out.newLine();
                // Close the output stream
                out.close();
            } catch (Exception e) {// Catch exception if any
                LOGGER.log(Level.WARNING, "Error: ", e);
            }

        } else {
            try {
                // Create file
                FileWriter fstream = new FileWriter((fileName), false);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("BDMMetric,  " + "K2Metric, " + "BIC");
                out.newLine();
                out.write("" + bdmMetric + "," + k2Metric + "," + bicMetric);
                out.newLine();
                // Close the output stream
                out.close();
            } catch (Exception e) {// Catch exception if any
                LOGGER.log(Level.WARNING, "Error: ", e);
            }
        }
    }

    @Override
    public void optimize() {
        this.problem.evaluatePopulationStart(this.population);
        // get the best individuals from the population
        Population best = this.population.getBestNIndividuals(
                calcLearningSetSize(), this.fitCrit);
        // generate the network with these individuals
        constructNetwork(best);
//		if(this.printExtraOutput && this.printEdgeRate){
        if (this.printEdgeRate) {
            this.edgeRate = this.network.adaptEdgeRate(this.edgeRate);
        }
        // sample new individuals from the network
        Population newlyGenerated = generateNewIndys(calcResampleSetSize());
        // remove the worst individuals from the population
        Population toRemove = this.population.getWorstNIndividuals(
                calcResampleSetSize(), this.fitCrit);
        remove(toRemove);
        // add the newly generated Individuals to the population
        this.population.addAll(newlyGenerated);
        this.count++;
        // we are done with one generation
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        this.problem.evaluatePopulationEnd(this.population);
        // print output if desired
//		if (this.printExtraOutput) {
        if (printNetworks) {
            printNetworkToFile("" + this.count);
        }
        if (printEdgeRate) {
            printEdgeRate();
        }
        if (printMetrics) {
            printMetrics(best);
        }
        if (printTimestamps) {
            printTimeStamp();
        }
//		}
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.m_Listener != null) {
            this.m_Listener.registerPopulationStateChanged(this, name);
        }
    }

    @Override
    public Population getPopulation() {
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(this.population);
    }

    @Override
    public void setIdentifier(String name) {
        this.m_Identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.m_Identifier;
    }

    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.problem = (AbstractOptimizationProblem) problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.problem;
    }

    @Override
    public String getStringRepresentation() {
        return "Bayesian Network";
    }

    // -------------------------------
    // -------------GUI---------------
    // -------------------------------
    public int getNumberOfParents() {
        return this.numberOfParents;
    }

    public void setNumberOfParents(int i) {
        this.numberOfParents = i;
    }

    public String numberOfParentsTipText() {
        return "The maximum number of parents a node in the Bayesian Network can have";
    }

    public String replaceNetworkTipText() {
        return "if set, the network will be completely replaced. If not, it will be tried to improve the last network, if that is not possible, it will be replaced";
    }

    public BOAScoringMethods getNetworkGenerationMethod() {
        return this.scoringMethod;
    }

    public void setNetworkGenerationMethod(BOAScoringMethods n) {
        this.scoringMethod = n;
    }

    public String networkGenerationMethodTipText() {
        return "The Method with which the Bayesian Network will be gererated";
    }

    public int getPopulationSize() {
        return PopSize;
    }

    public void setPopulationSize(int popSize) {
        PopSize = popSize;
    }

    public String populationSizeTipText() {
        return "Define the pool size used by BOA";
    }

    public double getResamplingRatio() {
        return resampleRatio;
    }

    public void setResamplingRatio(double resampleRat) {
        this.resampleRatio = resampleRat;
    }

    public String resamplingRatioTipText() {
        return "Ratio of individuals to be resampled from the Bayesian network per iteration";
    }

    public double getLearningRatio() {
        return learningSetRatio;
    }

    public void setLearningRatio(double rat) {
        this.learningSetRatio = rat;
    }

    public String learningRatioTipText() {
        return "Ratio of individuals to be used to learn the Bayesian network";
    }

    public double getProbLimitHigh() {
        return upperProbLimit;
    }

    public void setProbLimitHigh(double upperProbLimit) {
        this.upperProbLimit = upperProbLimit;
    }

    public String probLimitHighTipText() {
        return "the upper limit of the probability to set one Bit to 1";
    }

    public double getProbLimitLow() {
        return lowerProbLimit;
    }

    public void setProbLimitLow(double lowerProbLimit) {
        this.lowerProbLimit = lowerProbLimit;
    }

    public String probLimitLowTipText() {
        return "the lower limit of the probability to set one Bit to 1";
    }

    public String[] customPropertyOrder() {
        return new String[]{"learningRatio", "resamplingRatio"};
    }

    public boolean isPrintNetworks() {
        return this.printNetworks;
    }

    public void setPrintNetworks(boolean b) {
        this.printNetworks = b;
    }

    public String printNetworksTipText() {
        return "Print the underlying networks of each generation";
    }

    public boolean isPrintEdgeRate() {
        return this.printEdgeRate;
    }

    public void setPrintEdgeRate(boolean b) {
        this.printEdgeRate = b;
    }

    public String printEdgeRateTipText() {
        return "Print the rate with which each edge is used in the optimization run";
    }

    public boolean isPrintMetrics() {
        return this.printMetrics;
    }

    public void setPrintMetrics(boolean b) {
        this.printMetrics = b;
    }

    public String printMetricsTipText() {
        return "Print the values of all the metrics for every network";
    }

    public boolean isPrintTimestamps() {
        return this.printTimestamps;
    }

    public void setPrintTimestamps(boolean b) {
        this.printTimestamps = b;
    }

    public String printTimestampsTipText() {
        return "Print the time starting time and a timestamp after each generation";
    }

    public static void main(String[] args) {
        Population pop = new Population();
        GAIndividualBinaryData indy1 = new GAIndividualBinaryData();
        indy1.setBinaryDataLength(8);
        GAIndividualBinaryData indy2 = (GAIndividualBinaryData) indy1.clone();
        GAIndividualBinaryData indy3 = (GAIndividualBinaryData) indy1.clone();
        GAIndividualBinaryData indy4 = (GAIndividualBinaryData) indy1.clone();
        GAIndividualBinaryData indy5 = (GAIndividualBinaryData) indy1.clone();
        BitSet data1 = indy1.getBinaryData();
        BitSet data2 = indy2.getBinaryData();
        BitSet data3 = indy3.getBinaryData();
        BitSet data4 = indy4.getBinaryData();
        BitSet data5 = indy5.getBinaryData();
        BitSet data6 = indy5.getBinaryData();
        BitSet data7 = indy5.getBinaryData();
        BitSet data8 = indy5.getBinaryData();
        BitSet data9 = indy5.getBinaryData();
        BitSet data10 = indy5.getBinaryData();
        BitSet data11 = indy5.getBinaryData();
        BitSet data12 = indy5.getBinaryData();
        BitSet data13 = indy5.getBinaryData();
        BitSet data14 = indy5.getBinaryData();
        BitSet data15 = indy5.getBinaryData();
        BitSet data16 = indy5.getBinaryData();

        data1.set(0, true);
        data1.set(1, false);
        data1.set(2, true);
        data1.set(3, false);
        data1.set(4, true);
        data1.set(5, true);
        data1.set(6, false);
        data1.set(7, false);

        data5.set(0, true);
        data5.set(1, false);
        data5.set(2, true);
        data5.set(3, false);
        data5.set(4, false);
        data5.set(5, true);
        data5.set(6, true);
        data5.set(7, true);
        data6.set(0, true);
        data6.set(1, false);
        data6.set(2, true);
        data6.set(3, false);
        data6.set(4, true);
        data6.set(5, true);
        data6.set(6, false);
        data6.set(7, false);
        data7.set(0, true);
        data7.set(1, false);
        data7.set(2, true);
        data7.set(3, false);
        data7.set(4, true);
        data7.set(5, true);
        data7.set(6, false);
        data7.set(7, false);

        data2.set(0, true);
        data2.set(1, false);
        data2.set(2, true);
        data2.set(3, false);
        data2.set(4, true);
        data2.set(5, true);
        data2.set(6, false);
        data2.set(7, false);
        data8.set(0, true);
        data8.set(1, false);
        data8.set(2, true);
        data8.set(3, false);
        data8.set(4, true);
        data8.set(5, true);
        data8.set(6, false);
        data8.set(7, false);
        data9.set(0, true);
        data9.set(1, false);
        data9.set(2, true);
        data9.set(3, false);
        data9.set(4, true);
        data9.set(5, true);
        data9.set(6, false);
        data9.set(7, false);
        data10.set(0, true);
        data10.set(1, false);
        data10.set(2, true);
        data10.set(3, false);
        data10.set(4, true);
        data10.set(5, true);
        data10.set(6, false);
        data10.set(7, false);

        data3.set(0, true);
        data3.set(1, false);
        data3.set(2, true);
        data3.set(3, false);
        data3.set(4, false);
        data3.set(5, false);
        data3.set(6, true);
        data3.set(7, true);
        data11.set(0, true);
        data11.set(1, false);
        data11.set(2, true);
        data11.set(3, false);
        data11.set(4, false);
        data11.set(5, false);
        data11.set(6, true);
        data11.set(7, true);
        data12.set(0, true);
        data12.set(1, false);
        data12.set(2, true);
        data12.set(3, false);
        data12.set(4, false);
        data12.set(5, false);
        data12.set(6, true);
        data12.set(7, true);
        data13.set(0, true);
        data13.set(1, false);
        data13.set(2, true);
        data13.set(3, false);
        data13.set(4, false);
        data13.set(5, false);
        data13.set(6, true);
        data13.set(7, true);

        data4.set(0, true);
        data4.set(1, false);
        data4.set(2, true);
        data4.set(3, false);
        data4.set(4, false);
        data4.set(5, false);
        data4.set(6, true);
        data4.set(7, true);
        data14.set(0, true);
        data14.set(1, false);
        data14.set(2, true);
        data14.set(3, false);
        data14.set(4, false);
        data14.set(5, false);
        data14.set(6, true);
        data14.set(7, true);
        data15.set(0, true);
        data15.set(1, false);
        data15.set(2, true);
        data15.set(3, false);
        data15.set(4, false);
        data15.set(5, false);
        data15.set(6, true);
        data15.set(7, true);
        data16.set(0, true);
        data16.set(1, false);
        data16.set(2, true);
        data16.set(3, false);
        data16.set(4, false);
        data16.set(5, false);
        data16.set(6, true);
        data16.set(7, true);

        // data5.set(0, true);
        // data5.set(1, true);
        // data5.set(2, true);
        indy1.setBinaryGenotype(data1);
        indy2.setBinaryGenotype(data2);
        indy3.setBinaryGenotype(data3);
        indy4.setBinaryGenotype(data4);
        indy5.setBinaryGenotype(data5);
        pop.add(indy1);
        pop.add(indy2);
        pop.add(indy3);
        pop.add(indy4);
        pop.add(indy5);
        BOA b = new BOA();
        b.init();
        b.optimize();
        b.optimize();
        b.optimize();
        b.optimize();
        b.optimize();
        // b.generateGreedy(pop);
        // System.out.println(pop.getStringRepresentation());
        // b.print();
        // b.printNetworkToFile("test");
    }
}