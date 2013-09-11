package eva2.optimization.problems;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ExternalRuntimeProblem extends AbstractOptimizationProblem
        implements Interface2DBorderProblem, InterfaceProblemDouble, InterfaceHasInitRange {

    protected AbstractEAIndividual m_OverallBest = null;
    protected int m_ProblemDimension = 10;
    //    protected boolean                   m_UseTestConstraint = false;
    protected String m_Command = "";
    protected String m_WorkingDir = "";
    //    protected double					m_upperBound		= 10;
//    protected double					m_lowerBound		= 0;
    PropertyDoubleArray m_Range = new PropertyDoubleArray(m_ProblemDimension, 2, -10, 10);
    PropertyDoubleArray m_initRange = new PropertyDoubleArray(m_ProblemDimension, 2, -10, 10);
    private String additionalArg = "";
    protected InterfaceMOSOConverter m_MosoConverter = new MOSONoConvert();

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
//				System.out.println("monitor-thread finished!");
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
        ((ESIndividualDoubleData) this.template).setDoubleDataLength(m_ProblemDimension);
        ((ESIndividualDoubleData) this.template).setDoubleRange(makeRange());
    }

    public ExternalRuntimeProblem(ExternalRuntimeProblem b) {
        //AbstractOptimizationProblem
        if (b.template != null) {
            this.template = (AbstractEAIndividual) ((AbstractEAIndividual) b.template).clone();
        }
        //ExternalRuntimeProblem
        if (b.m_OverallBest != null) {
            this.m_OverallBest = (AbstractEAIndividual) ((AbstractEAIndividual) b.m_OverallBest).clone();
        }
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_Command = b.m_Command;
        if (b.m_Range != null) {
            this.m_Range = (PropertyDoubleArray) b.m_Range.clone();
        } else {
            this.m_Range = null;
        }
        if (b.m_initRange != null) {
            this.m_initRange = (PropertyDoubleArray) b.m_initRange.clone();
        } else {
            this.m_initRange = null;
        }
        if (b.m_MosoConverter != null) {
            this.m_MosoConverter = (InterfaceMOSOConverter) b.m_MosoConverter.clone();
        } else {
            this.m_MosoConverter = null;
        }
        this.m_WorkingDir = b.m_WorkingDir;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new ExternalRuntimeProblem(this);
    }

    /**
     * This method inits the Problem to log multiruns
     */
    @Override
    public void initializeProblem() {
        this.m_OverallBest = null;
        File f = new File(m_Command);
        if (f.exists()) {
            m_Command = f.getAbsolutePath();
        } else {
            String sep = System.getProperty("file.separator");
            if (m_WorkingDir.endsWith(sep)) {
                f = new File(m_WorkingDir + m_Command);
            } else {
                f = new File(m_WorkingDir + sep + m_Command);
            }
            if (f.exists()) {
                m_Command = f.getAbsolutePath();
            } else {
                System.err.println("Warning, " + this.getClass() + " could not find command " + m_Command + " in " + m_WorkingDir);
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
        this.m_OverallBest = null;

        ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(this.m_ProblemDimension);
        ((InterfaceDataTypeDouble) this.template).setDoubleRange(makeRange());

        AbstractOptimizationProblem.defaultInitPopulation(population, template, this);
    }

    @Override
    public double[][] makeRange() {
        if (m_Range == null) {
            System.err.println("Warning, range not set in ExternalRuntimeProblem.makeRange!");
        }
        if (m_Range.getNumRows() != getProblemDimension()) {
            System.err.println("Warning, problem dimension and range dimension dont match in ExternalRuntimeProblem.makeRange!");
        }
        return m_Range.getDoubleArrayShallow().clone();
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
        if (range.getNumRows() < this.m_ProblemDimension) {
            System.err.println("Warning, expected range of dimension " + m_ProblemDimension + " in setRange!");
        }
        m_Range.setDoubleArray(range.getDoubleArrayShallow());
    }

    public PropertyDoubleArray getRange() {
        return m_Range;
    }

    public String rangeTipText() {
        return "The domain bounds for the problem";
    }

    @Override
    public double getRangeLowerBound(int dim) {
        return m_Range.getValue(dim, 0);
    }

    @Override
    public double getRangeUpperBound(int dim) {
        return m_Range.getValue(dim, 1);
    }


    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evaluatated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[] x;
//        double[]        fitness;

        x = getXVector(individual);

        double[] fit = eval(x);
        individual.setFitness(fit);

//        if (this.m_UseTestConstraint) {
//            if (x[0] < 1) individual.addConstraintViolation(1-x[0]);
//        }
        if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
            this.m_OverallBest = (AbstractEAIndividual) individual.clone();
        }
    }

    @Override
    public void evaluatePopulationEnd(Population population) {
        super.evaluatePopulationEnd(population);
        if (m_MosoConverter != null) {
            m_MosoConverter.convertMultiObjective2SingleObjective(population);
        }
    }

    protected double[] getXVector(AbstractEAIndividual individual) {
        double[] x;
        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        return x;
    }

    public static List<String> runProcess(String[] parameters, String workingDir) {
        List<String> params = new ArrayList<String>(parameters.length);
        for (String str : parameters) {
            params.add(str);
        }
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
        List<String> results = new ArrayList<String>();
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
                for (String str : parts) {
                    results.add(str);
                }
//				results.add(line); 
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
    public double[] eval(double[] x) {
        if (x == null) {
            throw new RuntimeException("Error, x=null value received in ExternalRuntimeProblem.eval");
        }
        ArrayList<Double> fitList = new ArrayList<Double>();

        List<String> parameters = new ArrayList<String>();
        parameters.add(this.m_Command);
        if (additionalArg != null && (additionalArg.length() > 0)) {
            parameters.add(additionalArg);
        }
        for (int i = 0; i < this.m_ProblemDimension; i++) {
            String p = prepareParameter(x, i);
            parameters.add(p);
        }

        List<String> res = runProcess(parameters, m_WorkingDir);
        try {
            for (String str : res) {
                fitList.add(new Double(str));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: " + m_Command + " delivered malformatted output for " + BeanInspector.toString(x));
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
        return new String("" + x[i]);
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("External Runtime Problem:\n");
        sb.append("Here the individual codes a vector of real number x is to be minimized on a user given external problem.\nParameters:\n");
        sb.append("Dimension   : ");
        sb.append(this.m_ProblemDimension);

        return sb.toString();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
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

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Use an external command as target function.";
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
        this.m_ProblemDimension = t;
        this.m_Range.adaptRowCount(t);
        this.m_initRange.adaptRowCount(t);
    }

    @Override
    public int getProblemDimension() {
        return this.m_ProblemDimension;
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
        this.m_Command = t;
    }

    public String getCommand() {
        return this.m_Command;
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
        this.m_WorkingDir = t;
    }

    public String getWorkingDirectory() {
        return this.m_WorkingDir;
    }

    public String workingDirectoryTipText() {
        return "The working directory";
    }

//    /** This method allows you to toggle the application of a simple test constraint.
//     * @param b     The mode for the test constraint
//     */
//    public void setUseTestConstraint(boolean b) {
//        this.m_UseTestConstraint = b;
//    }
//    public boolean getUseTestConstraint() {
//        return this.m_UseTestConstraint;
//    }
//    public String useTestConstraintTipText() {
//        return "Just a simple test constraint of x[0] >= 1.";
//    }

    public InterfaceMOSOConverter getMosoConverter() {
        return m_MosoConverter;
    }

    public void setMosoConverter(InterfaceMOSOConverter mMosoConverter) {
        m_MosoConverter = mMosoConverter;
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
        return eval(project2DPoint(point))[0];
    }

    @Override
    public double[] project2DPoint(double[] point) {
        return Mathematics.expandVector(point, getProblemDimension(), 0.);
    }

    @Override
    public double[][] get2DBorder() {
        return getRange().getDoubleArrayShallow();
    }

//	/**
//	 * @return the m_upperBound
//	 */
//	public double getRangeUpperBound() {
//		return m_upperBound;
//	}
//	/**
//	 * @param bound the m_upperBound to set
//	 */
//	public void setRangeUpperBound(double bound) {
//		m_upperBound = bound;
//	}
//	
//	public String rangeUpperBoundTipText() {
//		return "Upper bound of the search space in any dimension.";
//	}
//	/**
//	 * @return the m_lowerBound
//	 */
//	public double getRangeLowerBound() {
//		return m_lowerBound;
//	}
//	/**
//	 * @param bound the m_lowerBound to set
//	 */
//	public void setRangeLowerBound(double bound) {
//		m_lowerBound = bound;
//	}	
//	
//	public String rangeLowerBoundTipText() {
//		return "Lower bound of the search space in any dimension.";
//	}

    public String additionalArgumentTipText() {
        return "Optionally define an additional (first) argument for the command line command.";
    }

    public String getAdditionalArgument() {
        return additionalArg;
    }

    public void setAdditionalArgument(String additionalArg) {
        this.additionalArg = additionalArg;
    }

    //	@Override
    @Override
    public Object getInitRange() {
        if (m_initRange == null) {
            if (m_Range == null) {
                System.err.println("Warning, neither range nor initRange has been set in ExternalRuntimeProblem!");
            }
            return m_Range.getDoubleArrayShallow();
        } else {
            return m_initRange.getDoubleArrayShallow();
        }
    }

    public void setInitialRange(double[][] range) {
        PropertyDoubleArray pRange = new PropertyDoubleArray(range);
        this.setInitialRange(pRange);
    }

    public void setInitialRange(PropertyDoubleArray range) {
        if (range.getNumRows() < this.m_ProblemDimension) {
            System.err.println("Warning, expected range of dimension " + m_ProblemDimension + " in setInitRange!");
        }
        m_initRange = new PropertyDoubleArray(range);
    }

    public PropertyDoubleArray getInitialRange() {
        return m_initRange;
    }

    public String initialRangeTipText() {
        return "Initialization range for the problem";
    }

    public String[] customPropertyOrder() {
        return new String[]{"workingDirectory", "command", "additionalArgument", "problemDimension", "initialRange", "range"};
    }

}
