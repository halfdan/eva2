package eva2.problems;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.optimization.individuals.*;
import eva2.optimization.operator.postprocess.InterfacePostProcessParams;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.terminators.FitnessConvergenceTerminator;
import eva2.optimization.operator.terminators.PhenotypeConvergenceTerminator;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.ChangeTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.DirectionTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.StagnationTypeEnum;
import eva2.optimization.population.Population;
import eva2.optimization.stat.InterfaceTextListener;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.util.annotation.Description;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Interface problem class for Matlab(TM). Towards EvA2 this behaves like any other double valued
 * problem implementation. However internally, every evaluation "asks" a mediator instance for
 * the result which waits for Matlab to evaluate the x value. When Matlab is finished, the mediator
 * returns to the evaluate method and the optimization can continue.
 */
@Description("Interface problem class for optimization in Matlab, only usable from within Matlab")
public class MatlabProblem extends AbstractOptimizationProblem implements InterfaceHasInitRange, InterfaceTextListener, Serializable {
    private static final long serialVersionUID = 4913310869887420815L;
    transient OptimizerRunnable runnable = null;
    protected boolean allowSingleRunnable = true;
    private transient Population seedPopulation = null;
    protected int problemDimension = 10;
    transient PrintStream dos = null;
    private double range[][] = null;
    private static String defTestOut = "matlabproblem-debug.log";
    int verbosityLevel = 0;
    boolean outputAllStatsField = true;
    private MatlabEvalMediator handler = null;
    private MatlabProblemDataTypeEnum dataType = MatlabProblemDataTypeEnum.typeDouble;
    private double[][] initialRange = null; // the initial range for double-valued problems

    public static boolean hideFromGOE = true;

    public MatlabProblem(MatlabProblem o) {
        this.template = null;
        this.handler = o.handler;
        this.runnable = o.runnable;
        this.allowSingleRunnable = o.allowSingleRunnable;
//		this.jmInterface = new String(o.jmInterface);
        this.problemDimension = o.problemDimension;
//		this.res = new ResultArr();
//		if (o.res != null) if (o.res.get() != null) res.set(o.res.get());
        this.range = o.range;
        this.dataType = o.dataType;
        this.initialRange = o.initialRange;
//		this.mtCmd = o.mtCmd;
//		currArray = null;
    }

    @Override
    public Object clone() {
        return new MatlabProblem(this);
    }

    /**
     * Constructor of a real valued problem.
     * @param dim
     * @param range
     */
//	public MatlabProblem(int dim, double[][] range) {
//		initialize(dim, ProblemDataTypeEnum.typeDouble, range, null, defTestOut);
//	}

    /**
     * Constructor of a binary problem with given bit length.
     *
     * @param dim
     */
    public MatlabProblem(int dim) {
        init(dim, MatlabProblemDataTypeEnum.typeBinary, null, null, defTestOut);
    }

    /**
     * Constructor of a real valued problem with initialization range.
     * @param dim
     * @param range
     * @param initRange
     */
//	public MatlabProblem(int dim, double[][] range, double[][] initRange) {
//		initialize(dim, ProblemDataTypeEnum.typeDouble, range, initRange, defTestOut);
//	}

    /**
     * Constructor of real or integer valued problem, the range will be converted
     * to integer in the latter case.
     *
     * @param dim
     * @param datType
     * @param range
     */
    public MatlabProblem(int dim, MatlabProblemDataTypeEnum datType, double[][] range) {
        init(dim, datType, range, null, defTestOut);
    }

    /**
     * Constructor of real or integer valued problem with initialization range.
     * The ranges will be converted to integer in the latter case.
     *
     * @param dim
     * @param datType
     * @param range
     */
    public MatlabProblem(int dim, MatlabProblemDataTypeEnum datType, double[][] range, double[][] initRange) {
        init(dim, datType, range, initRange, defTestOut);
    }

    protected void initTemplate() {
        switch (dataType) {
            case typeDouble:
                if (template == null || !(template instanceof ESIndividualDoubleData)) {
                    template = new ESIndividualDoubleData();
                }
                if (getProblemDimension() > 0) { // avoid evil case setting dim to 0 during object initialize
                    ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(getProblemDimension());
                    ((InterfaceDataTypeDouble) this.template).setDoubleRange(range);
                }
                break;
            case typeBinary:
                ///// binary alternative
                if (template == null || !(template instanceof GAIndividualBinaryData)) {
                    template = new GAIndividualBinaryData(getProblemDimension());
                }
                break;
            case typeInteger:
                int[][] intRange = makeIntRange(range);
                if (template == null || !(template instanceof GIIndividualIntegerData)) {
                    template = new GIIndividualIntegerData(intRange);
                }
                break;
        }
    }

    private int[][] makeIntRange(double[][] r) {
        int[][] intRange = new int[r.length][r[0].length];
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[0].length; j++) {
                intRange[i][j] = (int) r[i][j];
            }
        }
        return intRange;
    }

    public void setMediator(MatlabEvalMediator h) {
        handler = h;
        handler.setMatlabProblem(this);
    }

    @Override
    public void initializeProblem() {
        init(this.problemDimension, dataType, range, initialRange, defTestOut);
    }

    public static FitnessConvergenceTerminator makeFitConvTerm(double thresh, int stagnPeriod) {
        FitnessConvergenceTerminator fct = new FitnessConvergenceTerminator(thresh, stagnPeriod, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease);
        return fct;
    }

    public static PhenotypeConvergenceTerminator makePhenConvTerm(double thresh, int stagnPeriod) {
        PhenotypeConvergenceTerminator pct = new PhenotypeConvergenceTerminator(thresh, stagnPeriod, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease);
        return pct;
    }

    /**
     * Make deep clones for the ranges, or there may be deadlocks in communicating with Matlab!
     *
     * @param dim
     * @param globalRange
     * @param initRange
     * @param outFile
     */
    private void init(int dim, MatlabProblemDataTypeEnum datType, double[][] globalRange, double[][] initRange, String outFile) {
        this.problemDimension = dim;
//		if ((rng != null) && (dim != rng.length)) throw new ArrayIndexOutOfBoundsException("Mismatching dimension and range!");
        if (globalRange != null) { // these may be Matlab objects, so I do it by foot, just to be sure not to clone them within Matlab instead of here
            this.range = new double[globalRange.length][globalRange[0].length];
            for (int i = 0; i < this.range.length; i++) {
                System.arraycopy(globalRange[i], 0, this.range[i], 0, this.range[0].length);
            }
        } else {
            this.range = null;
        }

        if (initRange != null) { // these may be Matlab objects, so I do it by foot, just to be sure not to clone them within Matlab instead of here
            this.initialRange = new double[initRange.length][initRange[0].length];
            for (int i = 0; i < this.initialRange.length; i++) {
                System.arraycopy(initRange[i], 0, this.initialRange[i], 0, this.initialRange[0].length);
            }
        } else {
            this.initialRange = null;
        }

        if (Arrays.deepEquals(initialRange, range)) {
            initialRange = null;
        }

        dataType = datType; // store the data type
        log("### Data type is " + dataType);

        initTemplate();
        log("Initial range is " + BeanInspector.toString(initialRange) + "\n");
    }

    /**
     * If swtch is true and no output file is open yet, open a new one which will be used for debug output.
     * if fname is null, the default filename will be used.
     * if swtch is false, close the output file and deactivate debug output.
     *
     * @param swtch
     * @param fname
     */
    public void setDebugOut(boolean swtch, String fname) {
        if (!swtch && (dos != null)) {
            dos.close();
            dos = null;
        } else if (swtch && (dos == null)) {
            try {
                if (fname == null || (fname.length() == 0)) {
                    fname = defTestOut;
                }
                dos = new PrintStream(new FileOutputStream(fname));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStatsOutput(int verboLevel) {
        if ((verboLevel >= 0) && (verboLevel <= 3)) {
            verbosityLevel = verboLevel;
        } else {
            System.err.println("Error, invalid verbosity level for statistics output!");
        }
    }

    public String jmiInterfaceNameTipText() {
        return "Name of the JEInterface instance in Matlab";
    }

    /**
     * @param problemDimension the problemDimension to set
     */
    public void setProblemDimension(int problemDimension) {
        this.problemDimension = problemDimension;
    }


    public int getProblemDimension() {
        return problemDimension;
    }

    public String problemDimensionTipText() {
        return "The dimension of the problem.";
    }

    public void log(String str) {
        if (dos != null) {
            dos.print(str);
            dos.flush();
        }
    }

    public void optimize(final int optType, String outputFilePrefix) {
        optimize(optType, outputFilePrefix, null, null);
    }

    public void clearSeedPopulation() {
        seedPopulation = null;
    }

    public void setSeedPopulation(double[][] seedData, double[][] seedDataFit) {
        if (seedData == null) {
            seedPopulation = null;
        } else {
            if ((seedData.length != seedDataFit.length)
                    || (seedData[0].length != getProblemDimension())) {
                System.err.println("Error, unable to set seed population due to mismatching dimensions");
                seedPopulation = null;
            } else {
                log("Setting seed population of size " + seedData.length + "\n");
                seedPopulation = new Population(seedData.length);
                for (int i = 0; i < seedData.length; i++) {
                    AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
                    setIndyGenotype(indy, seedData[i]);
                    indy.setFitness(seedDataFit[i]);
                    seedPopulation.add(indy);
                }
            }
        }
    }

    private void setIndyGenotype(AbstractEAIndividual indy, double[] ds) {
        switch (dataType) {
            case typeDouble:
                ((InterfaceDataTypeDouble) indy).setDoubleGenotype(ds);
                break;
            case typeBinary:
                ((InterfaceDataTypeBinary) indy).setBinaryGenotype(toBinary(ds));
                break;
            case typeInteger:
                ((InterfaceDataTypeInteger) indy).setIntGenotype(toInteger(ds));
                break;
        }
    }

    private int[] toInteger(double[] ds) {
        int[] a = new int[ds.length];
        for (int i = 0; i < ds.length; i++) {
            a[i] = (int) Math.round(ds[i]);
        }
        return a;
    }

    private BitSet toBinary(double[] ds) {
        BitSet bs = new BitSet(ds.length);
        for (int i = 0; i < ds.length; i++) {
            bs.set(i, (ds[i] > 0.5));
        }
        return bs;
    }

    /**
     * Start an optimization using the MatlabProblem. The optType references the standard
     * optimizer as in OptimizerFactory. An output file prefix is optional. In two arrays,
     * name-value mappings can be given as additional parameters to the optimizer.
     *
     * @param optType
     * @param outputFilePrefix
     * @param specParams
     * @param specValues
     * @see OptimizerFactory.getOptRunnable
     */
    public void optimize(final int optType, String outputFilePrefix, Object[] specParams, Object[] specValues) {
        if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
            System.err.println("Please wait for the current optimization to finish");
        } else {
            handler.setMatlabProblem(this);
            handler.setFinished(false);
            runnable = OptimizerFactory.getOptRunnable(optType, this, outputFilePrefix);
            log("in MP optimize B\n");
            log("Setting text listener, verbo " + verbosityLevel + "\n");
            runnable.setTextListener(this);
            runnable.setVerbosityLevel(verbosityLevel);
            if (verbosityLevel > 0) {
                runnable.setOutputTo(2);
            } // both file + window
            else {
                runnable.setOutputTo(1);
            } // only window
            runnable.setOutputFullStatsToText(outputAllStatsField);

            if (seedPopulation != null) {
                runnable.getGOParams().getOptimizer().setPopulation(seedPopulation);
                runnable.setDoRestart(true);
                log("Setting seed population of size " + seedPopulation.size() + ", target size " + seedPopulation.getTargetSize() + "\n");
                log(BeanInspector.toString(seedPopulation.getStringRepresentation()) + "\n");
                log("Restart of optimization targetted.\n");
            }

            if ((specParams != null) && (specParams.length > 0)) {
                if ((specValues == null) || (specValues.length != specParams.length)) {
                    System.err.println("mismatching value list for parameter arguments: " + specValues);
                } else {
                    log("setting specific parameters...\n");
                    InterfaceOptimizer opt = runnable.getGOParams().getOptimizer();
                    for (int i = 0; i < specParams.length; i++) { // loop over settings
                        log("try setting " + specParams[i] + " to " + specValues[i]);
                        String paramName = null;
                        try {
                            paramName = (String) specParams[i];
                        } catch (ClassCastException e) {
                            paramName = "" + specParams[i];
                            if (!(specParams[i] instanceof Character)) {
                                System.err.println("Error, parameter " + specParams[i] + " could not be cast to String, trying " + paramName);
                            }
                        }
                        Object specVal = null; // avoid giving chars to the converter method here - the ascii value would be assigned instead of the string
                        if (specValues[i] instanceof Character) {
                            specVal = "" + specValues[i];
                        } else {
                            specVal = specValues[i];
                        }
                        if ((paramName == null) || (!BeanInspector.setMem(opt, paramName, specVal))) {
                            log("... Fail!\n");
                            System.err.println("Unable to set parameter " + paramName + ", skipping...");
                        } else {
                            log("... Ok.\n");
                        }
                    }
                    log(BeanInspector.toString(BeanInspector.getMemberDescriptions(opt, true)));
                }
            }
            new Thread(new WaitForEvARunnable(runnable, this)).start();
        }
    }

    public void setOutputAllStatFields(boolean showAll) {
        outputAllStatsField = showAll;
    }

    public void startPostProcess(InterfacePostProcessParams ppp) {
        if (ppp.isDoPostProcessing()) {
            if (allowSingleRunnable && (runnable != null) && (!runnable.isFinished())) {
                System.err.println("Please wait for the current optimization to finish");
            } else {
                handler.setFinished(false);
                log("\nstarting post process thread... " + BeanInspector.toString(ppp));
                runnable.setDoRestart(true);
                runnable.setDoPostProcessOnly(true);
                runnable.setPostProcessingParams(ppp);
                new Thread(new WaitForEvARunnable(runnable, this)).start();
            }
        } else {
            System.err.println("Nothing to be done.");
        }
    }

    /**
     * Request post processing of the last optimization results with given parameters
     * and export the result solution set to matlab.
     *
     * @param steps post processing steps with hill climber
     * @param sigma sigma parameter for clustering
     * @param nBest maximum number of solutions to retrieve
     */
    public void requestPostProcessing(int steps, double sigma, int nBest) {
        PostProcessParams ppp = new PostProcessParams(steps, sigma, nBest);
        startPostProcess(ppp);
    }

    /**
     * Request post processing of the last optimization results with given parameters
     * and export the result solution set to matlab.
     * This variant retrieves all solutions found in this way.
     *
     * @param steps post processing steps with hill climber
     * @param sigma sigma parameter for clustering
     */
    public void requestPostProcessing(int steps, double sigma) {
        requestPostProcessing(steps, sigma, -1);
    }

    /**
     * Try and stop the current optimization as well as any post processing
     * currently running.
     */
    public void stopOptimize() {
        log(">>>>>>>>>> Stop event!\n");
        if (runnable != null) {
            runnable.stopOpt();
        }
        PostProcess.stopAllPP();
    }

    public String getInfoString() {
        if (runnable == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        sb.append(runnable.terminatedBecause());
        return sb.toString();
    }

    public int getFunctionCalls() {
        if (runnable == null) {
            return 0;
        }
        return runnable.getGOParams().getOptimizer().getPopulation().getFunctionCalls();
    }

    void exportResultPopulationToMatlab(Population pop) {
        if ((pop != null) && (pop.size() > 0)) {
            switch (dataType) {
                case typeDouble:
                    double[][] rsolSet = new double[pop.size()][];
                    for (int i = 0; i < pop.size(); i++) {
                        rsolSet[i] = ((InterfaceDataTypeDouble) pop.getEAIndividual(i)).getDoubleData();
                    }
                    handler.setSolutionSet(rsolSet);
                    break;
                case typeBinary:
                    BitSet[] bsolSet = new BitSet[pop.size()];
                    for (int i = 0; i < pop.size(); i++) {
                        bsolSet[i] = ((InterfaceDataTypeBinary) pop.getEAIndividual(i)).getBinaryData();
                    }
                    handler.setSolutionSet(bsolSet);
                    break;
                case typeInteger:
                    int[][] isolSet = new int[pop.size()][];
                    for (int i = 0; i < pop.size(); i++) {
                        isolSet[i] = ((InterfaceDataTypeInteger) pop.getEAIndividual(i)).getIntegerData();
                    }
                    handler.setSolutionSet(isolSet);
                    break;
            }
        } else {
            switch (dataType) {
                case typeDouble:
                    handler.setSolutionSet((double[][]) null);
                    break;
                case typeBinary:
                    handler.setSolutionSet((BitSet[]) null);
                    break;
                case typeInteger:
                    handler.setSolutionSet((int[][]) null);
                    break;
            }
        }
    }

    public void exportResultToMatlab(OptimizerRunnable runnable) {
        handler.setSolution(getIntermediateResult());
    }

    /**
     * To be called by the executing thread to inform that the thread is finished.
     * We
     */
    void notifyFinished() {
        handler.setFinished(true);
    }

    public Object getIntermediateResult() {
        if (runnable == null) {
            System.err.println("Warning, runnable is null in MatlabProblem!");
            return null;
        } else {
            switch (dataType) {
                case typeDouble:
                    return runnable.getDoubleSolution();
                case typeBinary:
                    return runnable.getBinarySolution();
                case typeInteger:
                    return runnable.getIntegerSolution();
                default:
                    System.err.println("Warning, incompatible data type in MatlabProblem!");
                    return null;
            }
        }
    }

    /**
     * Return the number of function calls performed so far.
     *
     * @return
     */
    public int getProgress() {
        if (runnable == null) {
            return 0;
        } else {
            return runnable.getProgress();
        }
    }

    @Override
    public void print(String str) {
        if (verbosityLevel > 0) {
            // matlab displays sysout output in the command window, so we simply use this channel
            System.out.print(str);
        }
        log(str);
    }

    @Override
    public void println(String str) {
        print(str);
        print("\n");
    }
    //

    @Override
    public void evaluate(AbstractEAIndividual indy) {
        log("evaluating " + AbstractEAIndividual.getDefaultStringRepresentation(indy) + "\n");
        double[] res = handler.requestEval(this, AbstractEAIndividual.getIndyData(indy));
        log("evaluated to " + BeanInspector.toString(res) + "\n");
        log("Free mem is " + Runtime.getRuntime().freeMemory() + ", time is " + System.currentTimeMillis() + "\n");
        indy.setFitness(res);
    }

    @Override
    public void initializePopulation(Population population) {
        initTemplate();
        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("A general Matlab problem");
        sb.append(this.getName());
        //sb.append("\n");
        return sb.toString();
    }

    @Override
    public Object getInitializationRange() {
        log("retrieving initial range..., first entry: " + ((initialRange == null) ? "null" : BeanInspector.toString(initialRange[0])));
        return initialRange;
    }

    @Override
    public String getName() {
        return "MatlabProblem";
    }
}
