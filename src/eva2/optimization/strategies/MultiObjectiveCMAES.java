package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.operator.archiving.ArchivingNSGAIISMeasure;
import eva2.optimization.operator.mutation.MutateESCovarianceMatrixAdaptionPlus;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.io.Serializable;
import java.util.HashMap;

/**
 * ToDo: Document
 */
@Description("A multi-objective CMA-ES variant after Igel, Hansen and Roth 2007 (EC 15(1),1-28).")
public class MultiObjectiveCMAES extends AbstractOptimizer implements Serializable {

    /**
     * Generated serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * @author mkron
     */
    class CounterClass {

        public CounterClass(int i) {
            value = i;
        }

        public int value;
        public boolean seen = false;
    }

    private int lambda = 1;
    private int lambdaMO = 1;

    public MultiObjectiveCMAES() {
        population = new Population(lambdaMO);
    }

    public MultiObjectiveCMAES(MultiObjectiveCMAES a) {
        optimizationProblem = (AbstractOptimizationProblem) a.optimizationProblem.clone();
        setPopulation((Population) a.population.clone());
        lambda = a.lambda;
    }

    @Override
    public MultiObjectiveCMAES clone() {
        return new MultiObjectiveCMAES(this);
    }

    public void hideHideable() {
        GenericObjectEditor
                .setHideProperty(this.getClass(), "population", true);
    }
    /*
     * (non-Javadoc)
     * 
     * @see eva2.optimization.strategies.InterfaceOptimizer#getAllSolutions()
     */
    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population pop = getPopulation();
        return new SolutionSet(pop, pop);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eva2.optimization.strategies.InterfaceOptimizer#getName()
     */
    @Override
    public String getName() {
        return "(1+" + lambda + ") MO-CMA-ES";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eva2.optimization.strategies.InterfaceOptimizer#getStringRepresentation()
     */
    @Override
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("(1+" + lambda + ") MO-CMA-ES:\nOptimization Problem: ");
        strB.append(this.optimizationProblem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.population.getStringRepresentation());
        return strB.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eva2.optimization.strategies.InterfaceOptimizer#init()
     */
    @Override
    public void initialize() {
        // initializeByPopulation(population, true);
        this.population.setTargetSize(lambdaMO);
        this.optimizationProblem.initializePopulation(this.population);
        // children = new Population(population.size());
        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eva2.optimization.strategies.InterfaceOptimizer#initializeByPopulation(eva2.server
     * .go.populations.Population, boolean)
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        setPopulation(pop);
        if (reset) {
            optimizationProblem.initializePopulation(population);
            optimizationProblem.evaluate(population);

        }
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eva2.optimization.strategies.InterfaceOptimizer#optimize()
     */
    @Override
    public void optimize() {

        HashMap<Long, CounterClass> SuccessCounterMap = new HashMap<>();

        // Eltern markieren und f�r die Z�hlung vorbereiten
        for (int j = 0; j < lambdaMO && j < population.size(); j++) {
            population.getEAIndividual(j).putData("Parent",
                    population.getEAIndividual(j));
            SuccessCounterMap.put(population.getEAIndividual(j).getIndyID(),
                    new CounterClass(0));
        }

        // Kinder erzeugen
        Population children = new Population(lambdaMO * lambda);
        children.setGeneration(population.getGeneration());

        for (int j = 0; j < children.getTargetSize(); j++) {
            AbstractEAIndividual parent = population.getEAIndividual(j
                    % lambdaMO);
            AbstractEAIndividual indy = (AbstractEAIndividual) parent.clone();
            indy.mutate();
            indy.putData("Parent", parent);
            children.add(indy);
        }
        evaluatePopulation(children);

        population.addPopulation(children);
        // Ranking
        ArchivingNSGAII dummyArchive = new ArchivingNSGAIISMeasure();
        Population[] store = dummyArchive
                .getNonDominatedSortedFronts(population);
        store = dummyArchive.getNonDominatedSortedFronts(population);
        dummyArchive.calculateCrowdingDistance(store);

        // Vergleichen und den Successcounter hochz�hlen wenn wir besser als
        // unser Elter sind
        for (int j = 0; j < population.size(); j++) {
            AbstractEAIndividual parent = (AbstractEAIndividual) population
                    .getEAIndividual(j).getData("Parent");
            if (population.getEAIndividual(j) != parent) { // Eltern nicht mit
                // sich selber
                // vergleichen
                int parentParetoLevel = ((Integer) parent
                        .getData("ParetoLevel")).intValue();
                double parentSMeasure = ((Double) parent.getData("HyperCube"))
                        .doubleValue();
                int childParetoLevel = ((Integer) population.getEAIndividual(
                        j).getData("ParetoLevel")).intValue();
                double childSMeasure = ((Double) population
                        .getEAIndividual(j).getData("HyperCube")).doubleValue();
                if (childParetoLevel < parentParetoLevel
                        || ((childParetoLevel == parentParetoLevel) && childSMeasure > parentSMeasure)) {
                    SuccessCounterMap.get(parent.getIndyID()).value++;
                }
            } else { // Debug

                SuccessCounterMap.get(parent.getIndyID()).seen = true;
            }
        }

        // Selection
        population.clear();
        for (int i = 0; i < store.length; i++) {
            if (population.size() + store[i].size() <= lambdaMO) { // Die
                // Front
                // passt
                // noch
                // komplett
                population.addPopulation(store[i]);

            } else { // die besten aus der aktuellen Front heraussuchen bis voll
                while (store[i].size() > 0 && population.size() < lambdaMO) {
                    AbstractEAIndividual indy = store[i].getEAIndividual(0);
                    double bestMeasure = ((Double) indy.getData("HyperCube"))
                            .doubleValue(); // TODO mal noch effizient machen
                    // (sortieren und die besten n
                    // herausholen)
                    for (int j = 1; j < store[i].size(); j++) {
                        if (bestMeasure < ((Double) store[i].getEAIndividual(j)
                                .getData("HyperCube")).doubleValue()) {
                            bestMeasure = ((Double) store[i].getEAIndividual(j)
                                    .getData("HyperCube")).doubleValue();
                            indy = store[i].getEAIndividual(j);
                        }
                    }
                    population.add(indy);
                    store[i].removeMember(indy);
                }
            }

        }

        // Strategieparemeter updaten
        for (int j = 0; j < population.size(); j++) {

            AbstractEAIndividual indy = population.getEAIndividual(j);
            if (indy.getMutationOperator() instanceof MutateESCovarianceMatrixAdaptionPlus) { // Das
                // geht
                // nur
                // wenn
                // wir
                // auch
                // die
                // richtige
                // Mutation
                // haben
                AbstractEAIndividual parent = (AbstractEAIndividual) indy
                        .getData("Parent");
                MutateESCovarianceMatrixAdaptionPlus muta = (MutateESCovarianceMatrixAdaptionPlus) indy
                        .getMutationOperator();
                double rate = ((double) SuccessCounterMap.get(parent
                        .getIndyID()).value)
                        / ((double) lambda);

                if (indy != parent) {
                    muta.updateCovariance();
                }
                muta.updateStepSize(rate);
            }
        }

        for (int j = 0; j < children.size(); j++) {
            children.getEAIndividual(j).putData("Parent", null);
        }

        population.incrFunctionCallsBy(children.size());
        population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eva2.optimization.strategies.InterfaceOptimizer#setPopulation(eva2.server
     * .go.populations.Population)
     */
    @Override
    public void setPopulation(Population pop) {
        population = pop;
        population.setNotifyEvalInterval(1);
        lambdaMO = population.getTargetSize();

    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int mLambda) {
        lambda = mLambda;
    }

    /*
     * public int getLambdaMo() { return lambdaMO; }
     * 
     * public void setLambdaMo(int mLambda) { lambdaMO = mLambda; }
     */
}
