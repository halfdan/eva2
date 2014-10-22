package eva2.problems;

import eva2.gui.BeanInspector;
import eva2.gui.PropertyDoubleArray;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.moso.InterfaceMOSOConverter;
import eva2.optimization.operator.moso.MOSONoConvert;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use an external command as target function.
 */
@Description("Use an external command as target function.")
public class ExternalRuntimeProblem extends AbstractOptimizationProblem
        implements Interface2DBorderProblem, InterfaceProblemDouble, InterfaceHasInitRange {

    protected AbstractEAIndividual bestIndividuum = null;
    protected int problemDimension = 10;
    protected String command = "";
    protected String workingDir = "";
    PropertyDoubleArray range = new PropertyDoubleArray(problemDimension, 2, -10, 10);
    PropertyDoubleArray initializationRange = new PropertyDoubleArray(problemDimension, 2, -10, 10);
    private String additionalArg = "";
    protected InterfaceMOSOConverter mosoConverter = new MOSONoConvert();

    // Private Subclass to redirect Streams within an extra Thread to avoid dead
    // locks
    private static class MonitorInputStreamThread extends Thread {
        private Reader reader;
        private Writer writer;

        public MonitorInputStreamThread(InputStream in) {
            reader = new InputStreamReader(new BufferedInputStream(in));
            writer = new OutputStreamWriter(System.err);
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                int c;
                while ((c = reader.read()) != -1) {
                    writer.write(c);
                    writer.flush();
                }
            } catch (IOException ioe) {
                System.err.println("IOException in MonitorInputStreamThread/ExternalRuntimeProblem: " + ioe.getMessage());
                ioe.printStackTrace(System.err);
            } finally {
                try {
                    reader.close();
                    writer.close();
                } catch (IOException e) {
                    System.err.println("IOException in MonitorInputStreamThread/ExternalRuntimeProblem: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public ExternalRuntimeProblem() {
        this.template = new ESIndividualDoubleData();
        ((ESIndividualDoubleData) this.template).setDoubleDataLength(problemDimension);
        ((ESIndividualDoubleData) this.template).setDoubleRange(makeRange());
    }

    public ExternalRuntimeProblem(ExternalRuntimeProblem b) {
        //AbstractOptimizationProblem
        if (b.template != null) {
            this.template = (AbstractEAIndividual) b.template.clone();
        }
        //ExternalRuntimeProblem
        if (b.bestIndividuum != null) {
            this.bestIndividuum = (AbstractEAIndividual) b.bestIndividuum.clone();
        }
        this.problemDimension = b.problemDimension;
        this.command = b.command;
        if (b.range != null) {
            this.range = (PropertyDoubleArray) b.range.clone();
        } else {
            this.range = null;
        }
        if (b.initializationRange != null) {
            this.initializationRange = (PropertyDoubleArray) b.initializationRange.clone();
        } else {
            this.initializationRange = null;
        }
        if (b.mosoConverter != null) {
            this.mosoConverter = (InterfaceMOSOConverter) b.mosoConverter.clone();
        } else {
            this.mosoConverter = null;
        }
        this.workingDir = b.workingDir;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new ExternalRuntimeProblem(this);
    }

    /**
     * This method inits the Problem to log multiruns
     */
    @Override
    public void initializeProblem() {
        this.bestIndividuum = null;
        File f = new File(command);
        if (f.exists()) {
            command = f.getAbsolutePath();
        } else {
            String sep = System.getProperty("file.separator");
            if (workingDir.endsWith(sep)) {
                f = new File(workingDir + command);
            } else {
                f = new File(workingDir + sep + command);
            }
            if (f.exists()) {
                command = f.getAbsolutePath();
            } else {
                System.err.println("Warning, " + this.getClass() + " could not find command " + command + " in " + workingDir);
            }
        }
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    @Override
    public void initializePopulation(Population population) {
        this.bestIndividuum = null;

        ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(this.problemDimension);
        ((InterfaceDataTypeDouble) this.template).setDoubleRange(makeRange());

        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    @Override
    public double[][] makeRange() {
        if (range == null) {
            System.err.println("Warning, range not set in ExternalRuntimeProblem.makeRange!");
        }
        if (range.getNumRows() != getProblemDimension()) {
            System.err.println("Warning, problem dimension and range dimension dont match in ExternalRuntimeProblem.makeRange!");
        }
        return range.getDoubleArrayShallow().clone();
    }

    public void setRange(double[][] range) {
        PropertyDoubleArray pRange = new PropertyDoubleArray(range);
        this.setRange(pRange);
    }

    /**
     * Set the internal problem range to the given array.
     *
     * @param range
     */
    public void setRange(PropertyDoubleArray range) {
        if (range.getNumRows() < this.problemDimension) {
            System.err.println("Warning, expected range of dimension " + problemDimension + " in setRange!");
        }
        this.range.setDoubleArray(range.getDoubleArrayShallow());
    }

    public PropertyDoubleArray getRange() {
        return range;
    }

    public String rangeTipText() {
        return "The domain bounds for the problem";
    }

    @Override
    public double getRangeLowerBound(int dim) {
        return range.getValue(dim, 0);
    }

    @Override
    public double getRangeUpperBound(int dim) {
        return range.getValue(dim, 1);
    }


    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evaluatated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[] x;

        x = getXVector(individual);

        double[] fit = evaluate(x);
        individual.setFitness(fit);

        if ((this.bestIndividuum == null) || (this.bestIndividuum.getFitness(0) > individual.getFitness(0))) {
            this.bestIndividuum = (AbstractEAIndividual) individual.clone();
        }
    }

    @Override
    public void evaluatePopulationEnd(Population population) {
        super.evaluatePopulationEnd(population);
        if (mosoConverter != null) {
            mosoConverter.convertMultiObjective2SingleObjective(population);
        }
    }

    protected double[] getXVector(AbstractEAIndividual individual) {
        double[] x;
        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        return x;
    }

    public static List<String> runProcess(String[] parameters, String workingDir) {
        List<String> params = new ArrayList<>(parameters.length);
        Collections.addAll(params, parameters);
        return runProcess(params, workingDir);
    }

    /**
     * Parse the output values of a process by line and by whitespace characters and some others
     * returning a string list.
     *
     * @param parameters
     * @param workingDir
     * @return
     */
    public static List<String> runProcess(List<String> parameters, String workingDir) {
        String colSepRegExp = "[\\s;:|]";  // \s for whitespaces, double quoting necessary!
        Process process;
        ProcessBuilder pb;
        List<String> results = new ArrayList<>();
        try {
            pb = new ProcessBuilder(parameters);
            pb.directory(new File(workingDir));
            process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread thread = new MonitorInputStreamThread(process.getErrorStream());//grab the Error Stream
            thread.start();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(colSepRegExp);
                Collections.addAll(results, parts);
            }
            br.close();
        } catch (IOException e) {
            String msg = "IO Error when calling external command! Invalid command for ExternalRuntimeProblem?";
            System.err.println(msg);
            e.printStackTrace();
            throw new RuntimeException(msg);
        }
        return results;
    }

    @Override
    public double[] evaluate(double[] x) {
        if (x == null) {
            throw new RuntimeException("Error, x=null value received in ExternalRuntimeProblem.evaluate");
        }
        ArrayList<Double> fitList = new ArrayList<>();

        List<String> parameters = new ArrayList<>();
        parameters.add(this.command);
        if (additionalArg != null && (additionalArg.length() > 0)) {
            parameters.add(additionalArg);
        }
        for (int i = 0; i < this.problemDimension; i++) {
            String p = prepareParameter(x, i);
            parameters.add(p);
        }

        List<String> res = runProcess(parameters, workingDir);
        try {
            for (String str : res) {
                fitList.add(new Double(str));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: " + command + " delivered malformatted output for " + BeanInspector.toString(x));
            e.printStackTrace();
        }
        double[] fit = new double[fitList.size()];
        for (int i = 0; i < fit.length; i++) {
            fit[i] = fitList.get(i);
        }
        return fit;
    }

    /**
     * How to prepare a given parameter within a double array to present it
     * to the external program.
     *
     * @param x
     * @param i
     * @return
     */
    protected String prepareParameter(double[] x, int i) {
        return "" + x[i];
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("External Runtime Problem:\n");
        sb.append("Here the individual codes a vector of real number x is to be minimized on a user given external problem.\nParameters:\n");
        sb.append("Dimension   : ");
        sb.append(this.problemDimension);

        return sb.toString();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "External Runtime Problem";
    }

    public String[] getGOEPropertyUpdateLinks() {
        return new String[]{"problemDimension", "initialRange", "problemDimension", "range"};
    }

    /**
     * Length of the x vector that is to be optimized. Be sure to keep
     * the ranges fit in length.
     *
     * @param t Length of the x vector that is to be optimized
     */
    public void setProblemDimension(int t) {
        this.problemDimension = t;
        this.range.adaptRowCount(t);
        this.initializationRange.adaptRowCount(t);
    }

    @Override
    public int getProblemDimension() {
        return this.problemDimension;
    }

    public String problemDimensionTipText() {
        return "Domain dimension of the problem";
    }

    /**
     * Length of the x vector at is to be optimized
     *
     * @param t Length of the x vector at is to be optimized
     */
    public void setCommand(String t) {
        this.command = t;
    }

    public String getCommand() {
        return this.command;
    }

    public String commandTipText() {
        return "External command to be called for evaluation";
    }

    /**
     * Working dir of the external runtime
     *
     * @param t working directory
     */
    public void setWorkingDirectory(String t) {
        this.workingDir = t;
    }

    public String getWorkingDirectory() {
        return this.workingDir;
    }

    public String workingDirectoryTipText() {
        return "The working directory";
    }

    public InterfaceMOSOConverter getMosoConverter() {
        return mosoConverter;
    }

    public void setMosoConverter(InterfaceMOSOConverter mMosoConverter) {
        mosoConverter = mMosoConverter;
    }

    public String mosoConverterTipText() {
        return "Possible conversion of multi-objective fitness to single objective fitness.";
    }

    /**
     * This method allows you to choose the EA individual
     *
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.template = (AbstractEAIndividual) indy;
    }

    @Override
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble) this.template;
    }

    @Override
    public double functionValue(double[] point) {
        return evaluate(project2DPoint(point))[0];
    }

    @Override
    public double[] project2DPoint(double[] point) {
        return Mathematics.expandVector(point, getProblemDimension(), 0.);
    }

    @Override
    public double[][] get2DBorder() {
        return getRange().getDoubleArrayShallow();
    }

    public String additionalArgumentTipText() {
        return "Optionally define an additional (first) argument for the command line command.";
    }

    public String getAdditionalArgument() {
        return additionalArg;
    }

    public void setAdditionalArgument(String additionalArg) {
        this.additionalArg = additionalArg;
    }

    @Override
    public Object getInitializationRange() {
        if (initializationRange == null) {
            if (range == null) {
                System.err.println("Warning, neither range nor initRange has been set in ExternalRuntimeProblem!");
            }
            return range.getDoubleArrayShallow();
        } else {
            return initializationRange.getDoubleArrayShallow();
        }
    }

    public void setInitialRange(double[][] range) {
        PropertyDoubleArray pRange = new PropertyDoubleArray(range);
        this.setInitialRange(pRange);
    }

    public void setInitialRange(PropertyDoubleArray range) {
        if (range.getNumRows() < this.problemDimension) {
            System.err.println("Warning, expected range of dimension " + problemDimension + " in setInitRange!");
        }
        initializationRange = new PropertyDoubleArray(range);
    }

    public PropertyDoubleArray getInitialRange() {
        return initializationRange;
    }

    public String initialRangeTipText() {
        return "Initialization range for the problem";
    }

    public String[] customPropertyOrder() {
        return new String[]{"workingDirectory", "command", "additionalArgument", "problemDimension", "initialRange", "range"};
    }

}
