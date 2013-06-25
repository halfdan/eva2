package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.populations.InterfaceSolutionSet;
import eva2.optimization.populations.Population;
import eva2.optimization.populations.SolutionSet;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.BKnapsackProblem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.tools.math.SpecialFunction;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLTGA implements InterfaceOptimizer, java.io.Serializable, InterfacePopulationChangedEventListener {

    private static final Logger LOGGER = Logger.getLogger(MLTGA.class.getName());
    transient private InterfacePopulationChangedEventListener m_Listener = null;
    private String m_Identifier = "LTGA";
    private int probDim = 8;
    private int fitCrit = -1;
    private int popSize = 50;
    private Population population = new Population();
    private AbstractOptimizationProblem problem = new BKnapsackProblem();
    private AbstractEAIndividual template = null;
    private int generationCycle = 500;
    private boolean elitism = true;

    public MLTGA() {
    }

    public MLTGA(MLTGA l) {
        this.m_Listener = l.m_Listener;
        this.m_Identifier = l.m_Identifier;
        this.probDim = l.probDim;
        this.popSize = l.popSize;
        this.population = (Population) l.population.clone();
        this.problem = (AbstractOptimizationProblem) l.problem.clone();
        this.template = (AbstractEAIndividual) template.clone();
    }

    @Override
    public Object clone() {
        return new MLTGA(this);
    }

    @Override
    public String getName() {
        return "Mutating Linkage Tree Genetic Algorithm";
    }

    public static String globalInfo() {
        return "Modified implementation of the Linkage Tree Genetic Algorithm.";
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;

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

    private void defaultInit() {
        if (population == null) {
            this.population = new Population(this.popSize);
        } else {
            this.population.setTargetPopSize(this.popSize);
        }
        this.template = this.problem.getIndividualTemplate();
        if (!(template instanceof InterfaceDataTypeBinary)) {
            LOGGER.log(Level.WARNING, "Requiring binary data!");
        } else {
            Object dim = BeanInspector.callIfAvailable(problem,
                    "getProblemDimension", null);
            if (dim == null) {
                LOGGER.log(Level.WARNING, "Couldn't get problem dimension!");
            }
            probDim = (Integer) dim;
            ((InterfaceDataTypeBinary) this.template).SetBinaryGenotype(new BitSet(probDim));
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
    public void init() {
        defaultInit();
        this.problem.initializePopulation(this.population);
        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
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
        this.problem.evaluate(indy);
        // increment the number of evaluations
        this.population.incrFunctionCalls();
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

    private Stack<Set<Integer>> buildLinkageTree() {
        // the final tree
        Stack<Set<Integer>> linkageTree = new Stack<Set<Integer>>();
        // the stack to cluster here clusters can be removed
        Stack<Set<Integer>> workingTree = new Stack<Set<Integer>>();
        // add the problem variables to the stacks
        for (int i = 0; i < this.probDim; i++) {
            Set<Integer> s1 = new HashSet<Integer>();
            Set<Integer> s2 = new HashSet<Integer>();
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
        Set<Integer> bestI = new HashSet<Integer>();
        Set<Integer> bestJ = new HashSet<Integer>();
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
        return new Pair<Set<Integer>, Set<Integer>>(bestI, bestJ);
    }

    private double calculateDistance(Set<Integer> s1, Set<Integer> s2) {
        double entropy1 = calculateEntropy(s1);
        double entropy2 = calculateEntropy(s2);
        Set<Integer> combined = new HashSet<Integer>();
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
        this.problem.evaluatePopulationStart(this.population);
        Stack<Set<Integer>> linkageTree = buildLinkageTree();
        Population newPop = new Population(this.popSize);
        if (elitism) {
            AbstractEAIndividual firstIndy = this.population.getBestEAIndividual();
            AbstractEAIndividual firstNewIndy = buildNewIndy(firstIndy, linkageTree);
            newPop.add(firstNewIndy);
        }
        for (int i = 0; i < this.popSize; i++) {
            if (this.elitism && i == 0) {
                continue;
            }
            Population indies = this.population.getRandNIndividuals(1);
            AbstractEAIndividual newIndy = buildNewIndy(indies.getEAIndividual(0), linkageTree);
            newPop.add(newIndy);
        }
        this.population.clear();
        this.population.addAll(newPop);
        this.problem.evaluatePopulationEnd(this.population);
    }

    private AbstractEAIndividual buildNewIndy(AbstractEAIndividual indy,
            Stack<Set<Integer>> linkageTree) {
        for (Set<Integer> mask : linkageTree) {
            BitSet gen = getBinaryData(indy);
            BitSet newGene = (BitSet) gen.clone();
            for (Integer flipID : mask) {
                newGene.flip(flipID);
            }
            AbstractEAIndividual newIndy = (AbstractEAIndividual) this.template.clone();
            ((InterfaceDataTypeBinary) newIndy).SetBinaryGenotype(newGene);
            evaluate(newIndy);
            if (newIndy.getFitness(0) < indy.getFitness(0)) {
                indy = newIndy;
            }
        }
        return indy;
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
    public InterfaceOptimizationProblem getProblem() {
        return this.problem;
    }

    @Override
    public String getStringRepresentation() {
        return "Linkage Tree GA";
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.funCallIntervalReached) == 0) {
            // set funcalls to real value
            this.population.setFunctionCalls(((Population) source).getFunctionCalls());
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    }

    public static void main(String[] args) {
        MLTGA ltga = new MLTGA();
        ltga.init();
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
