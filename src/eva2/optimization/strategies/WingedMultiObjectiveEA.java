package eva2.optimization.strategies;

import eva2.gui.PropertyDoubleArray;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.moso.MOSOWeightedFitness;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.FM0Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * The winged MOEA was a nice idea, which didn't really work out. Here a
 * standard MOEA is assisted by n additional local searchers, each optimizing
 * just one objective. The idea was that these local optimizers would span the
 * search space and would allow the MOEA to converge faster. But in the end the
 * performance of this algorithm strongly depends on the optimization problem.
 */
@Description("This is Evolutionary Multi-Criteria Optimization Algorithm hybridized with Local Searchers to span the Pareto-Front.")
public class WingedMultiObjectiveEA extends AbstractOptimizer implements java.io.Serializable {

    private InterfaceOptimizer multiObjectiveEA = new MultiObjectiveEA();
    private InterfaceOptimizer singleObjectiveEA = new GeneticAlgorithm();
    private InterfaceOptimizer[] singleObjectiveOptimizers;
    private int migrationRate = 5;
    private int outputDimension = 2;

    public WingedMultiObjectiveEA() {
    }

    public WingedMultiObjectiveEA(WingedMultiObjectiveEA a) {
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.multiObjectiveEA = (InterfaceOptimizer) a.multiObjectiveEA.clone();
        this.singleObjectiveEA = (InterfaceOptimizer) a.singleObjectiveEA.clone();
        if (a.singleObjectiveOptimizers != null) {
            this.singleObjectiveOptimizers = new InterfaceOptimizer[a.singleObjectiveOptimizers.length];
            for (int i = 0; i < this.singleObjectiveOptimizers.length; i++) {
                this.singleObjectiveOptimizers[i] = (InterfaceOptimizer) a.singleObjectiveOptimizers[i].clone();
            }
        }
        this.migrationRate = a.migrationRate;
        this.population = (Population) a.population.clone();
    }

    @Override
    public Object clone() {
        return new WingedMultiObjectiveEA(this);
    }

    @Override
    public void initialize() {
        if (this.optimizationProblem instanceof AbstractMultiObjectiveOptimizationProblem) {
            AbstractMultiObjectiveOptimizationProblem tmpProb = (AbstractMultiObjectiveOptimizationProblem) this.optimizationProblem;
            AbstractMultiObjectiveOptimizationProblem tmpP;
            MOSOWeightedFitness tmpWF;
            PropertyDoubleArray tmpDA;
            int dim = this.outputDimension;
            double[] weights;
            // dim = tmpProb.getOutputDimension();
            this.multiObjectiveEA.setProblem((InterfaceOptimizationProblem) this.optimizationProblem.clone());
            this.multiObjectiveEA.initialize();
            this.singleObjectiveOptimizers = new InterfaceOptimizer[dim];
            for (int i = 0; i < dim; i++) {
                tmpP = (AbstractMultiObjectiveOptimizationProblem) this.optimizationProblem.clone();
                weights = new double[dim];
                for (int j = 0; j < dim; j++) {
                    weights[j] = 0;
                }
                weights[i] = 1;
                tmpDA = new PropertyDoubleArray(weights);
                tmpWF = new MOSOWeightedFitness();
                tmpWF.setWeights(tmpDA);
                tmpP.setMOSOConverter(tmpWF);
                this.singleObjectiveOptimizers[i] = (InterfaceOptimizer) this.singleObjectiveEA.clone();
                this.singleObjectiveOptimizers[i].setProblem(tmpP);
                this.singleObjectiveOptimizers[i].initialize();
            }
        } else {
            this.singleObjectiveEA.setProblem(this.optimizationProblem);
            this.singleObjectiveEA.initialize();
        }
        this.communicate();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        if (this.optimizationProblem instanceof AbstractMultiObjectiveOptimizationProblem) {
            AbstractMultiObjectiveOptimizationProblem tmpProb = (AbstractMultiObjectiveOptimizationProblem) this.optimizationProblem;
            AbstractMultiObjectiveOptimizationProblem tmpP;
            MOSOWeightedFitness tmpWF;
            PropertyDoubleArray tmpDA;
            int dim = 2;
            double[] weights;
            // dim = tmpProb.getOutputDimension();
            this.multiObjectiveEA.setProblem((InterfaceOptimizationProblem) this.optimizationProblem.clone());
            this.multiObjectiveEA.initializeByPopulation(pop, reset);
            this.singleObjectiveOptimizers = new InterfaceOptimizer[dim];
            for (int i = 0; i < dim; i++) {
                tmpP = (AbstractMultiObjectiveOptimizationProblem) this.optimizationProblem.clone();
                weights = new double[dim];
                for (int j = 0; j < dim; j++) {
                    weights[j] = 0;
                }
                weights[i] = 1;
                tmpDA = new PropertyDoubleArray(weights);
                tmpWF = new MOSOWeightedFitness();
                tmpWF.setWeights(tmpDA);
                tmpP.setMOSOConverter(tmpWF);
                this.singleObjectiveOptimizers[i] = (InterfaceOptimizer) this.singleObjectiveEA.clone();
                this.singleObjectiveOptimizers[i].setProblem(tmpP);
                this.singleObjectiveOptimizers[i].initializeByPopulation(pop, reset);
            }
        } else {
            this.singleObjectiveEA.setProblem(this.optimizationProblem);
            this.singleObjectiveEA.initializeByPopulation(pop, reset);
        }
        this.communicate();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * The optimize method will compute a 'improved' and evaluated population
     */
    @Override
    public void optimize() {

        this.multiObjectiveEA.optimize();
        for (int i = 0; i < this.singleObjectiveOptimizers.length; i++) {
            this.singleObjectiveOptimizers[i].optimize();
        }
        this.population.incrGeneration();
        if ((this.population.getGeneration() % this.migrationRate) == 0) {
            this.communicate();
        }

        System.gc();

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will manage comunication between the islands
     */
    private void communicate() {
        int oldFunctionCalls;
        this.population.clear();
        this.population.setFunctionCalls(0);
        Population pop;
        // first collect all the data
        pop = (Population) this.multiObjectiveEA.getPopulation().clone();
        this.population.addPopulation(pop);
        this.population.incrFunctionCallsBy(pop.getFunctionCalls());
        for (int i = 0; i < this.singleObjectiveOptimizers.length; i++) {
            pop = (Population) this.singleObjectiveOptimizers[i].getPopulation().clone();
            this.population.addPopulation(pop);
            this.population.incrFunctionCallsBy(pop.getFunctionCalls());
        }
        oldFunctionCalls = this.population.getFunctionCalls();
        this.optimizationProblem.evaluate(this.population);
        this.population.setFunctionCalls(oldFunctionCalls);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
//        double plotValue = (this.problem.getDoublePlotValue(this.population)).doubleValue();
        // now they are synchronized lets migrate
        this.migrate();
    }

    /**
     * This method implements the migration between the optimzers
     */
    private void migrate() {
        AbstractEAIndividual[] bestIndys = new AbstractEAIndividual[this.outputDimension];
        double tmpF1, tmpF2;
        // for each dimension find the best
        for (int i = 0; i < this.outputDimension; i++) {
            bestIndys[i] = (AbstractEAIndividual) ((AbstractEAIndividual) this.population.get(0)).clone();
            tmpF1 = bestIndys[i].getFitness(i);
            // for each individual find the best
            for (int j = 0; j < this.population.size(); j++) {
                if (((AbstractEAIndividual) this.population.get(j)).getFitness(i) < tmpF1) {
                    bestIndys[i] = (AbstractEAIndividual) ((AbstractEAIndividual) this.population.get(j)).clone();
                    tmpF1 = bestIndys[i].getFitness(i);
                }
            }
        }
        // now perform the migration
        AbstractEAIndividual tmpIndy;
        for (int i = 0; i < this.outputDimension; i++) {
            tmpIndy = (AbstractEAIndividual) bestIndys[i].clone();
            this.multiObjectiveEA.getProblem().evaluate(tmpIndy);
            this.multiObjectiveEA.getPopulation().set(i, tmpIndy);
            tmpIndy = (AbstractEAIndividual) bestIndys[i].clone();
            this.singleObjectiveOptimizers[i].getProblem().evaluate(tmpIndy);
            this.singleObjectiveOptimizers[i].getPopulation().set(0, bestIndys[i]);
        }
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "EMO:\n";
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "EMO-LS";
    }

    /**
     * Assuming that all optimizer will store their data in a population we will
     * allow access to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    public String populationTipText() {
        return "(Defunct)";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method allows you to set/get the optimizing technique to use.
     *
     * @return The current optimizing method
     */
    public InterfaceOptimizer getMOOptimizer() {
        return this.multiObjectiveEA;
    }

    public void setMOOptimizer(InterfaceOptimizer b) {
        this.multiObjectiveEA = b;
    }

    public String mOOptimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /**
     * This method allows you to set/get the optimizing technique to use.
     *
     * @return The current optimizing method
     */
    public InterfaceOptimizer getSOOptimizer() {
        return this.singleObjectiveEA;
    }

    public void setSOOptimizer(InterfaceOptimizer b) {
        this.singleObjectiveEA = b;
    }

    public String sOOptimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /**
     * This method allows you to set/get the archiving strategy to use.
     *
     * @return The current optimizing method
     */
    public int getMigrationRate() {
        return this.migrationRate;
    }

    public void setMigrationRate(int b) {
        this.migrationRate = b;
    }

    public String migrationRateTipText() {
        return "Choose a proper migration rate.";
    }
}