package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.BKnapsackProblem;
import eva2.tools.Pair;
import eva2.tools.math.SpecialFunction;
import eva2.util.annotation.Description;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

@Description("Basic implementation of the Linkage Tree Genetic Algorithm based on the works by Dirk Thierens.")
public class LTGA extends AbstractOptimizer implements java.io.Serializable, InterfacePopulationChangedEventListener {

    private static final Logger LOGGER = Logger.getLogger(LTGA.class.getName());
    private int probDim = 8;
    private int fitCrit = -1;
    private int popSize = 50;
    private AbstractOptimizationProblem optimizationProblem = new BKnapsackProblem();
    private AbstractEAIndividual template = null;
    private int generationCycle = 500;
    private boolean elitism = true;

    public LTGA() {

    }

    public LTGA(LTGA l) {
        this.probDim = l.probDim;
        this.popSize = l.popSize;
        this.population = (Population) l.population.clone();
        this.template = (AbstractEAIndividual) template.clone();
    }

    @Override
    public Object clone() {
        return new LTGA(this);
    }

    @Override
    public String getName() {
        return "Linkage Tree Genetic Algorithm";
    }

    private void defaultInit() {
        if (population == null) {
            this.population = new Population(this.popSize);
        } else {
            this.population.setTargetPopSize(this.popSize);
        }
        this.template = this.optimizationProblem.getIndividualTemplate();
        if (!(template instanceof InterfaceDataTypeBinary)) {
            LOGGER.log(Level.WARNING, "Requiring binary data!");
        } else {
            Object dim = BeanInspector.callIfAvailable(optimizationProblem,
                    "getProblemDimension", null);
            if (dim == null) {
                LOGGER.log(Level.WARNING, "Couldn't get problem dimension!");
            }
            probDim = (Integer) dim;
            ((InterfaceDataTypeBinary) this.template).setBinaryGenotype(new BitSet(probDim));
        }
        this.population.addPopulationChangedEventListener(this);
        this.population.setNotifyEvalInterval(this.generationCycle);
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

    @Override
    public void initialize() {
        this.defaultInit();
        this.optimizationProblem.initializePopulation(this.population);
        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    private void evaluatePopulation(Population pop) {
        for (int i = 0; i < pop.size(); i++) {
            evaluate(pop.getEAIndividual(i));
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
        this.optimizationProblem.evaluate(indy);
        // increment the number of evaluations
        this.population.incrFunctionCalls();
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        if (reset) {
            this.initialize();
        } else {
            defaultInit();
            this.population = pop;
        }
    }

    private Stack<Set<Integer>> buildLinkageTree() {
        // the final tree
        Stack<Set<Integer>> linkageTree = new Stack<>();
        // the stack to cluster here clusters can be removed
        Stack<Set<Integer>> workingTree = new Stack<>();
        // add the problem variables to the stacks
        for (int i = 0; i < this.probDim; i++) {
            Set<Integer> s1 = new HashSet<>();
            Set<Integer> s2 = new HashSet<>();
            s1.add(i);
            s2.add(i);
            linkageTree.add(s1);
            workingTree.add(s2);
        }
//		double[] probMass = calculateProbabilityMassFunction();
        // until there is only one cluster left
        while (workingTree.size() > 1) {
            Pair<Set<Integer>, Set<Integer>> toCluster = findNearestClusters(workingTree);
            // add all elements from the second cluster to the first one
            toCluster.head.addAll(toCluster.tail);
            // remove the second cluster from the working set
            workingTree.remove(toCluster.tail);
            // add the combined cluster to the linkage tree
            linkageTree.add(toCluster.head);
        }
        return linkageTree;
    }

    private Pair<Set<Integer>, Set<Integer>> findNearestClusters(Stack<Set<Integer>> stack) {
        Set<Integer> bestI = new HashSet<>();
        Set<Integer> bestJ = new HashSet<>();
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < stack.size(); i++) {
            Set<Integer> s1 = stack.get(i);
            for (int j = i + 1; j < stack.size(); j++) {
                Set<Integer> s2 = stack.get(j);
                double currDist = calculateDistance(s1, s2);
                // better cluster found
                if (currDist < bestScore) {
                    bestI = s1;
                    bestJ = s2;
                    bestScore = currDist;
                }
            }
        }
        // return the best pair
        return new Pair<>(bestI, bestJ);
    }

    private double calculateDistance(Set<Integer> s1, Set<Integer> s2) {
        double entropy1 = calculateEntropy(s1);
        double entropy2 = calculateEntropy(s2);
        Set<Integer> combined = new HashSet<>();
        combined.addAll(s1);
        combined.addAll(s2);
        double entropy3 = calculateEntropy(combined);
        return 2 - ((entropy1 + entropy2) / (entropy3));
    }

    private double calculateEntropy(Set<Integer> s) {
        double entropy = 0.0;
        // for possible states {0,1} do
        for (int i = 0; i <= 1; i++) {
            int count = 0;
            // for every individual
            for (int k = 0; k < this.popSize; k++) {
                BitSet b = getBinaryData(this.population.getEAIndividual(k));
                boolean addCount = true;
                // for every specified Bit
                for (Integer value : s) {
                    // is the bit not set correctly
                    if (b.get(value) != (i == 1)) {
                        addCount = false;
                        break;
                    }
                }
                if (addCount) {
                    count++;
                }
                addCount = true;
            }
            entropy += ((double) count) * SpecialFunction.logb((double) count, 2.0);
            count = 0;
        }
        return entropy;
    }

    @Override
    public void optimize() {
        this.optimizationProblem.evaluatePopulationStart(this.population);
        Stack<Set<Integer>> linkageTree = this.buildLinkageTree();
        Population newPop = new Population(this.popSize);
        if (elitism) {
            Population firstIndies = this.population.getBestNIndividuals(2, fitCrit);
            Population firstNewIndies = buildNewIndies(firstIndies, linkageTree);
            newPop.addAll(firstNewIndies);
        }
        for (int i = 0; i < (this.popSize / 2); i++) {
            if (this.elitism && i == 0) {
                continue;
            }
            Population indies = this.population.getRandNIndividuals(2);
            Population newIndies = this.buildNewIndies(indies, linkageTree);
            newPop.addAll(newIndies);
        }
        this.population.clear();
        this.population.addAll(newPop);
        this.optimizationProblem.evaluatePopulationEnd(this.population);
    }

    private Population buildNewIndies(Population indies,
                                      Stack<Set<Integer>> linkageTree) {
        if (indies.size() != 2) {
            return indies;
        }
        AbstractEAIndividual indy1 = indies.getEAIndividual(0);
        AbstractEAIndividual indy2 = indies.getEAIndividual(1);
        for (Set<Integer> mask : linkageTree) {
            BitSet gen1 = getBinaryData(indy1);
            BitSet gen2 = getBinaryData(indy2);
            BitSet newGene1 = (BitSet) gen1.clone();
            BitSet newGene2 = (BitSet) gen2.clone();
            boolean same = true;
            for (Integer exchange : mask) {
                if (newGene1.get(exchange) != newGene2.get(exchange)) {
                    same = false;
                }
                newGene1.set(exchange, gen2.get(exchange));
                newGene2.set(exchange, gen1.get(exchange));
            }
            if (!same) {
                AbstractEAIndividual newIndy1 = (AbstractEAIndividual) this.template.clone();
                AbstractEAIndividual newIndy2 = (AbstractEAIndividual) this.template.clone();
                ((InterfaceDataTypeBinary) newIndy1).setBinaryGenotype(newGene1);
                ((InterfaceDataTypeBinary) newIndy2).setBinaryGenotype(newGene2);
                evaluate(newIndy1);
                evaluate(newIndy2);
                if (Math.min(newIndy1.getFitness(0), newIndy2.getFitness(0)) < Math.min(indy1.getFitness(0), indy2.getFitness(0))) {
                    indy1 = newIndy1;
                    indy2 = newIndy2;
                }
            }
        }
        Population result = new Population(2);
        result.add(indy1);
        result.add(indy2);
        return result;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(this.population);
    }

    public boolean getElitism() {
        return this.elitism;
    }

    public void setElitism(boolean b) {
        this.elitism = b;
    }

    public String elitismTipText() {
        return "use elitism?";
    }

    @Override
    public String getStringRepresentation() {
        return "Linkage Tree GA";
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.FUN_CALL_INTERVAL_REACHED) == 0) {
            // set funcalls to real value
            this.population.setFunctionCalls(((Population) source).getFunctionCalls());
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    public static void main(String[] args) {
        LTGA ltga = new LTGA();
        ltga.initialize();
        ltga.optimize();
        System.out.println(ltga.popSize);
        Population p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
        ltga.optimize();
        p = ltga.getPopulation();
        System.out.println(p.getFunctionCalls() + "\t" + p.size());
        System.out.println(p.getBestEAIndividual().getStringRepresentation());
    }
}
