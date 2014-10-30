package eva2.optimization.modules;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceNotifyOnInformers;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.operator.postprocess.InterfacePostProcessParams;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import eva2.util.annotation.Parameter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {
    protected static final Logger LOGGER = Logger.getLogger(AbstractOptimizationParameters.class.getName());

    @Parameter(name = "Random Seed", description = "Random number seed, set to zero to use current system time.")
    protected long randomSeed = (long) 0.0;

    /**
     * The optimizer to be executed.
     */
    @Parameter(name = "Optimizer", description = "Choose an optimization strategy.")
    protected InterfaceOptimizer optimizer;

    /**
     * The optimization problem to be optimized.
     * When changed it is automatically applied to the
     * selected optimizer.
     */
    @Parameter(name = "Problem", description = "Choose the problem that is to optimize and the EA individual parameters.")
    protected InterfaceOptimizationProblem problem;

    /**
     * The termination criteria that terminated an
     * optimization run.
     */
    @Parameter(name = "Terminator", description = "Choose a termination criterion.")
    protected InterfaceTerminator terminator;

    /**
     * Post processing parameters.
     * This can be enabled in the UI and will perform additional
     * optimization e.g. with Hill Climbing.
     */
    @Parameter(name = "Post Processing", description = "Parameters for the post processing step.")
    protected InterfacePostProcessParams postProcessParams = new PostProcessParams(false);

    transient protected InterfacePopulationChangedEventListener populationChangedEventListener;
    transient private List<InterfaceNotifyOnInformers> toInformAboutInformers = null;

    protected AbstractOptimizationParameters() {
    }

    protected AbstractOptimizationParameters(AbstractOptimizationParameters optimizationParameters) {
        this();
        this.optimizer = optimizationParameters.optimizer;
        this.problem = optimizationParameters.problem;
        this.terminator = optimizationParameters.terminator;
        this.optimizer.setProblem(this.problem);
        this.randomSeed = optimizationParameters.randomSeed;
        this.postProcessParams = optimizationParameters.postProcessParams;
    }

    public AbstractOptimizationParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
        this();
        optimizer = opt;
        problem = prob;
        terminator = term;
        postProcessParams = new PostProcessParams(false);
        opt.setProblem(prob);
    }

    /**
     * Apply the given GO parameter settings to this instance. This maintains the listeners etc.
     *
     * @param parameters
     */
    public void setSameParams(AbstractOptimizationParameters parameters) {
        setOptimizer(parameters.optimizer);
        setProblem(parameters.problem);
        setTerminator(parameters.terminator);
        this.optimizer.setProblem(this.problem);
        setRandomSeed(parameters.randomSeed);
        setPostProcessParams(parameters.postProcessParams);
    }

    /**
     * Add a listener to the current optimizer.
     *
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.populationChangedEventListener = ea;
        if (this.optimizer != null) {
            this.optimizer.addPopulationChangedEventListener(this.populationChangedEventListener);
        }
    }

    public boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener == ea) {
            populationChangedEventListener = null;
            if (this.optimizer != null) {
                this.optimizer.removePopulationChangedEventListener(ea);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    public void saveInstance(String fileName) {
        try {
            FileOutputStream fileStream = new FileOutputStream(fileName);
            Serializer.storeObject(fileStream, this);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }
    }

    @Override
    public void saveInstance() {
        String fileName = this.getClass().getSimpleName() + ".ser";
        saveInstance(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder(getName());
        sBuilder.append("\n");
        sBuilder.append("seed=");
        sBuilder.append(randomSeed);
        sBuilder.append("\nProblem: ");
        sBuilder.append(BeanInspector.toString(problem));
        sBuilder.append("\nOptimizer: ");
        sBuilder.append(BeanInspector.toString(optimizer));
        sBuilder.append("\nTerminator: ");
        sBuilder.append(BeanInspector.toString(terminator));
        sBuilder.append("\n");
        return sBuilder.toString();
    }

    @Override
    public void addInformableInstance(InterfaceNotifyOnInformers o) {
        if (toInformAboutInformers == null) {
            toInformAboutInformers = new LinkedList<>();
        }
        if (!toInformAboutInformers.contains(o)) {
            toInformAboutInformers.add(o);
        }
        o.setInformers(getInformerList());
    }

    @Override
    public boolean removeInformableInstance(InterfaceNotifyOnInformers o) {
        if (toInformAboutInformers == null) {
            return false;
        } else {
            return toInformAboutInformers.remove(o);
        }
    }

    private void fireNotifyOnInformers() {
        if (toInformAboutInformers != null) {
            for (InterfaceNotifyOnInformers listener : toInformAboutInformers) {
                listener.setInformers(getInformerList());
            }
        }
    }

    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        this.optimizer = optimizer;
        this.optimizer.setProblem(this.problem);
        if (this.populationChangedEventListener != null) {
            this.optimizer.addPopulationChangedEventListener(this.populationChangedEventListener);
        }
        fireNotifyOnInformers();
    }

    private List<InterfaceAdditionalPopulationInformer> getInformerList() {
        LinkedList<InterfaceAdditionalPopulationInformer> ret = new LinkedList<>();
        if (problem instanceof InterfaceAdditionalPopulationInformer) {
            ret.add(problem);
        }
        if (optimizer instanceof InterfaceAdditionalPopulationInformer) {
            ret.add((InterfaceAdditionalPopulationInformer) optimizer);
        }
        return ret;
    }

    @Override
    public InterfaceOptimizer getOptimizer() {
        return this.optimizer;
    }

    @Override
    public String getName() {
        return "Optimization parameters";
    }

    /**
     * This method will set the problem that is to be optimized.
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.problem = problem;
        this.optimizer.setProblem(this.problem);
        fireNotifyOnInformers();
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.problem;
    }

    /**
     * This methods allow you to set and get the Seed for the Random Number Generator.
     *
     * @param x Long seed.
     */
    @Override
    public void setRandomSeed(long x) {
        randomSeed = x;
    }

    /**
     * Returns the current seed for the random number generator.
     *
     * @return The current seed for the random number generator.
     */
    @Override
    public long getRandomSeed() {
        return randomSeed;
    }

    /**
     * This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     *
     * @param term The new terminator
     */
    @Override
    public void setTerminator(InterfaceTerminator term) {
        this.terminator = term;
    }

    @Override
    public InterfaceTerminator getTerminator() {
        return this.terminator;
    }

    @Override
    public InterfacePostProcessParams getPostProcessParams() {
        return postProcessParams;
    }

    @Override
    public void setPostProcessParams(InterfacePostProcessParams ppp) {
        postProcessParams = ppp;
    }

    @Override
    public void setDoPostProcessing(boolean doPP) {
        postProcessParams.setDoPostProcessing(doPP);
    }
}
