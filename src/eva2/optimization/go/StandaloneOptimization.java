package eva2.optimization.go;

import eva2.gui.BeanInspector;
import eva2.gui.JParaPanel;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.GAIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.crossover.CrossoverGAGINPoint;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESFixedStepSize;
import eva2.optimization.operator.mutation.MutateESLocal;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.problems.F1Problem;
import eva2.optimization.strategies.EvolutionStrategies;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@eva2.util.annotation.Description(value = "This is a simple example framework for Evolutionary Algorithms.")
public class StandaloneOptimization implements InterfaceStandaloneOptimization, InterfacePopulationChangedEventListener, java.io.Serializable {

    // Interface GUI Stuff
    transient private JFrame mainFrame;
    transient private JPanel mainPanel;
    transient private JPanel buttonPanel;
    transient private JButton runButton, stopButton, continueButton, showSolutionButton;
    transient private JComponent optionsPanel, parameterPanel1, parameterPanel2;
    transient private JComponent statusPanel;
    transient private JLabel statusField;
    transient private JProgressBar progressBar;
    transient private SwingWorker worker;
    transient private boolean show = false;
    //    transient private InterfaceTest   test = new Test1();
    // Opt. Algorithms and Parameters
    //transient private InterfaceOptimizer              optimizer         = new EvolutionaryMultiObjectiveOptimization();
    //transient private InterfaceOptimizationProblem    problem           = new TF1Problem();
    //transient private int                             functionCalls     = 1000;
    private OptimizationParameters optimizationParameters;
    transient private int multiRuns = 1;
    transient private int recentFunctionCalls;
    transient private int currentExperiment = 0;
    transient private int currentRun;
    transient private int currentProgress;
    transient private String experimentName;
    transient private String outputPath = "";
    transient private String outputFileName = "none";
    // these parameters are for the continue option
    transient private Population backupPopulation;
    transient private boolean continueFlag;
    // Plot Panel stuff
    transient private Plot plot;
    transient private ArrayList performedRuns = new ArrayList();
    transient private ArrayList<Double[]> tmpData;
    transient private BufferedWriter outputFile;
    // Test
    transient private List list;

    /**
     * Create a new EALectureGUI.
     */
    public StandaloneOptimization() {
        this.optimizationParameters = OptimizationParameters.getInstance();
        this.experimentName = this.optimizationParameters.getOptimizer().getName() + "-" + this.performedRuns.size();
        this.optimizationParameters.addPopulationChangedEventListener(this);
        RNG.setRandomSeed(optimizationParameters.getSeed());
    }

    /**
     * This method allows you to get the current GO parameters
     */
    public OptimizationParameters getGOParameters() {
        return this.optimizationParameters;
    }

    /**
     * This method will generate a Plot Frame and a main Editing Frame
     */
    public void initFrame() {
        this.progressBar = new JProgressBar();
        // init the main frame
        this.mainFrame = new JFrame();
        this.mainFrame.setTitle("Genetic Optimizing");
        this.mainFrame.setSize(500, 400);
        this.mainFrame.setLocation(530, 50);
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        // build the main panel
        this.mainPanel = new JPanel();
        this.mainFrame.getContentPane().add(this.mainPanel);
        this.mainPanel.setLayout(new BorderLayout());
        // build the button panel
        this.buttonPanel = new JPanel();
        this.runButton = new JButton("Run");
        this.runButton.addActionListener(this.runListener);
        this.runButton.setEnabled(true);
        this.runButton.setToolTipText("Run the optimization process with the current parameter settings.");
        this.stopButton = new JButton("Stop");
        this.stopButton.addActionListener(this.stopListener);
        this.stopButton.setEnabled(false);
        this.stopButton.setToolTipText("Stop the runnig the optimization process.");
        this.continueButton = new JButton("Continue");
        this.continueButton.addActionListener(this.continueListener);
        this.continueButton.setEnabled(false);
        this.continueButton.setToolTipText("Resume the previous optimization (check termination criteria and multiruns = 1!).");
        this.showSolutionButton = new JButton("Show Solution");
        this.showSolutionButton.addActionListener(this.showSolListener);
        this.showSolutionButton.setEnabled(true);
        this.showSolutionButton.setToolTipText("Show the current best solution.");
        this.buttonPanel.add(this.runButton);
        this.buttonPanel.add(this.continueButton);
        this.buttonPanel.add(this.stopButton);
        this.buttonPanel.add(this.showSolutionButton);
        this.mainPanel.add(this.buttonPanel, BorderLayout.NORTH);

        // build the Options Panel
        JParaPanel paraPanel = new JParaPanel(this, "MyGUI");
        Class object = null, editor = null;
        String tmp = "eva2.optimization.go.Tools.InterfaceTest";
        try {
            object = Class.forName(tmp);
        } catch (java.lang.ClassNotFoundException e) {
            System.out.println("No Class found for " + tmp);
        }
        tmp = "eva2.gui.editor.GenericObjectEditor";
        try {
            editor = Class.forName(tmp);
        } catch (java.lang.ClassNotFoundException e) {
            System.out.println("No Class found for " + tmp);
        }
        if ((object != null) && (editor != null)) {
            paraPanel.registerEditor(object, editor);
        }
        this.parameterPanel1 = (paraPanel.makePanel());
        this.optionsPanel = new JTabbedPane();
        JParaPanel paraPanel2 = new JParaPanel(this.optimizationParameters, "MyGUI");
        this.parameterPanel2 = (paraPanel2.makePanel());
        ((JTabbedPane) this.optionsPanel).addTab("Optimization Parameters", this.parameterPanel2);
        ((JTabbedPane) this.optionsPanel).addTab("Statistics", this.parameterPanel1);
        this.mainPanel.add(this.optionsPanel, BorderLayout.CENTER);

        // build the Status Panel
        this.statusPanel = new JPanel();
        this.statusPanel.setLayout(new BorderLayout());
        this.statusField = new JLabel("Click Run to begin...");
        this.progressBar = new JProgressBar();
        this.statusPanel.add(this.statusField, BorderLayout.NORTH);
        this.statusPanel.add(this.progressBar, BorderLayout.SOUTH);
        this.mainPanel.add(this.statusPanel, BorderLayout.SOUTH);
        // The plot frame
        double[] tmpD = new double[2];
        tmpD[0] = 1;
        tmpD[1] = 1;
        this.plot = new Plot("EA Lecture Plot", "Function calls", "Fitness", true);
        // validate and show
        this.mainFrame.validate();
        this.mainFrame.setVisible(true);
    }

    /**
     * This action listener, called by the "Run/Restart" button, will init the
     * problem and start the computation.
     */
    ActionListener runListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            worker = new SwingWorker() {
                @Override
                public Object construct() {
                    return doWork();
                }

                @Override
                public void finished() {
                    runButton.setEnabled(true);
                    continueButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    backupPopulation = (Population) optimizationParameters.getOptimizer().getPopulation().clone();
                }
            };
            worker.start();
            runButton.setEnabled(false);
            continueButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    };
    /**
     * This action listener, called by the "Cancel" button, interrupts the
     * worker thread which is running this.doWork(). Note that the doWork()
     * method handles InterruptedExceptions cleanly.
     */
    ActionListener stopListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            runButton.setEnabled(true);
            continueButton.setEnabled(true);
            stopButton.setEnabled(false);
            worker.interrupt();
            for (int i = 0; i < multiRuns; i++) {
                plot.clearGraph(1000 + i);
            }
        }
    };
    /**
     * This action listener, called by the "Cancel" button, interrupts the
     * worker thread which is running this.doWork(). Note that the doWork()
     * method handles InterruptedExceptions cleanly.
     */
    ActionListener continueListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            // todo something need to be done here...
            worker = new SwingWorker() {
                @Override
                public Object construct() {
                    return doWork();
                }

                @Override
                public void finished() {
                    runButton.setEnabled(true);
                    continueButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    backupPopulation = (Population) optimizationParameters.getOptimizer().getPopulation().clone();
                    continueFlag = false;
                }
            };
            // also mal ganz anders ich gehe davon aus, dass der Benutzer das Ding parametrisiert hat
            // setze einfach die Backup population ein...
            continueFlag = true;
            multiRuns = 1;      // multiruns machen bei continue einfach keinen Sinn...
            worker.start();
            runButton.setEnabled(false);
            continueButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    };
    /**
     * This action listener, called by the "show" button will show the currently
     * best solution in a frame.
     */
    ActionListener showSolListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            JFrame frame = new JFrame();
            frame.setTitle("The current best solution for " + optimizationParameters.getProblem().getName());
            frame.setSize(400, 300);
            frame.setLocation(450, 250);
            Population pop = optimizationParameters.getOptimizer().getPopulation();
            frame.getContentPane().add(optimizationParameters.getProblem().drawIndividual(pop.getGeneration(), pop.getFunctionCalls(), pop.getBestEAIndividual()));
            frame.validate();
            frame.setVisible(true);
        }
    };

    /**
     * This method gives the experimental settings and starts the work.
     */
    @Override
    public void startExperiment() {
        // This is for the CBN-TEST RUNS
        this.optimizationParameters.setOptimizer(new EvolutionStrategies());
        this.optimizationParameters.setProblem(new F1Problem());
        EvaluationTerminator terminator = new EvaluationTerminator();
        terminator.setFitnessCalls(50000);
        this.optimizationParameters.setTerminator(terminator);
        this.multiRuns = 10;
        int experimentType = 0;
        this.experimentName = "InferringGRN";
        this.outputFileName = "Result";
        this.outputPath = "results/";
        // These are some tmp Variables
        InterfaceDataTypeDouble tmpIndy = new ESIndividualDoubleData();
        InterfaceMutation tmpMut = new MutateESFixedStepSize();

        switch (experimentType) {
            case 0: {
                // use the Struture Skeletalizing with GA
                this.outputFileName = "Prim4_StructSkelGATESTIT";
                GeneticAlgorithm ga = new GeneticAlgorithm();
                SelectTournament tour = new SelectTournament();
                tour.setTournamentSize(10);
                ga.setParentSelection(tour);
                ga.setPartnerSelection(tour);
                this.optimizationParameters.setOptimizer(ga);
                this.optimizationParameters.getOptimizer().getPopulation().setTargetSize(100);
                F1Problem problem = new F1Problem();
                tmpIndy = new GAIndividualDoubleData();
                ((GAIndividualDoubleData) tmpIndy).setCrossoverOperator(new CrossoverGAGINPoint());
                ((GAIndividualDoubleData) tmpIndy).setCrossoverProbability(1.0);
                ((GAIndividualDoubleData) tmpIndy).setMutationProbability(1.0);
                ((F1Problem) problem).setEAIndividual(tmpIndy);
                //((FGRNInferringProblem)this.problem).setStructreSkelInterval(1);
                this.optimizationParameters.getOptimizer().setProblem(problem);
                this.optimizationParameters.getOptimizer().addPopulationChangedEventListener(this);
                this.doWork();
                break;
            }
            case 1: {
                // use the simple ES Local
                this.outputFileName = "X360_StandardES";
                EvolutionStrategies es = new EvolutionStrategies();
                this.optimizationParameters.setOptimizer(es);
                this.optimizationParameters.getOptimizer().getPopulation().setTargetSize(50);
                F1Problem problem = new F1Problem();
                tmpIndy = new ESIndividualDoubleData();
                ((AbstractEAIndividual) tmpIndy).setMutationOperator(new MutateESLocal());
                ((F1Problem) problem).setEAIndividual(tmpIndy);
                //((FGRNInferringProblem)this.problem).setUseHEigenMatrix(true);
                //((FGRNInferringProblem)this.problem).setUseOnlyPositiveNumbers(true);
                this.optimizationParameters.getOptimizer().setProblem(problem);
                this.optimizationParameters.getOptimizer().addPopulationChangedEventListener(this);
                this.doWork();
                break;
            }
        }
    }

    /**
     * This method represents the application code that we'd like to run on a
     * separate thread. It simulates slowly computing a value, in this case just
     * a string 'All Done'. It updates the progress bar every half second to
     * remind the user that we're still busy.
     */
    public Object doWork() {
        try {
            this.optimizationParameters.saveInstance();
            if (this.show) {
                this.statusField.setText("Optimizing...");
            }

            RNG.setRandomSeed(optimizationParameters.getSeed());
            // opening output file...
            if (!this.outputFileName.equalsIgnoreCase("none")) {
                String name = "";
                SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
                String startDate = formatter.format(new Date());
                name = this.outputPath + this.outputFileName + "_" + this.experimentName + "_" + startDate + ".dat";
                try {
                    this.outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
                } catch (FileNotFoundException e) {
                    System.out.println("Could not open output file! Filename: " + name);
                }
            } else {
                this.outputFile = null;
            }

            // init problem 
            this.optimizationParameters.getProblem().initializeProblem();
            this.optimizationParameters.getOptimizer().setProblem(this.optimizationParameters.getProblem());
            // int optimizer and population
            //this.optimizationParameters.getOptimizer().init();

            // init the log data
            ArrayList tmpMultiRun = new ArrayList();
            this.performedRuns.add(tmpMultiRun);

            // something to log file
            //if (outputFile != null) this.writeToFile(this.optimizationParameters.getOptimizer().getStringRepresentation());
            //this.writeToFile("Here i'll write something characterizing the algorithm.");

            for (int j = 0; j < this.multiRuns; j++) {
                this.optimizationParameters.getProblem().initializeProblem(); // in the loop as well, dynamic probs may need that (MK)
                this.tmpData = new ArrayList<>();
                this.currentRun = j;
                if (this.show) {
                    this.statusField.setText("Optimizing Run " + (j + 1) + " of " + this.multiRuns + " Multi Runs...");
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                // write header to file
                this.writeToFile(" FitnessCalls\t Best\t Mean\t Worst \t" + BeanInspector.toString(this.optimizationParameters.getProblem().getAdditionalDataHeader(), '\t', false, ""));
                if ((this.continueFlag) && (this.backupPopulation != null)) {
                    this.recentFunctionCalls += this.backupPopulation.getFunctionCalls();
                    this.optimizationParameters.getOptimizer().getProblem().initializeProblem();
                    this.optimizationParameters.getOptimizer().addPopulationChangedEventListener(null);
                    this.optimizationParameters.getOptimizer().setPopulation(this.backupPopulation);
                    this.optimizationParameters.getOptimizer().getProblem().evaluate(this.optimizationParameters.getOptimizer().getPopulation());
                    this.optimizationParameters.getOptimizer().getProblem().evaluate(this.optimizationParameters.getOptimizer().getPopulation().getArchive());
                    this.optimizationParameters.getOptimizer().initByPopulation(this.backupPopulation, false);
                    this.optimizationParameters.getOptimizer().getPopulation().setFunctionCalls(0);
                    this.optimizationParameters.addPopulationChangedEventListener(this);
                } else {
                    this.recentFunctionCalls = 0;
                    this.optimizationParameters.getOptimizer().init();
                }
                //while (this.optimizationParameters.getOptimizer().getPopulation().getFunctionCalls() < this.functionCalls) {
                while (!this.optimizationParameters.getTerminator().isTerminated(this.optimizationParameters.getOptimizer().getPopulation())) {
                    //System.out.println("Simulated Function calls "+ this.optimizer.getPopulation().getFunctionCalls());
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    optimizationParameters.getOptimizer().optimize();
                }
                System.gc();
                // @TODO if you want the final report include this
                //this.writeToFile(this.optimizationParameters.getProblem().getStringRepresentationForProblem(this.optimizationParameters.getOptimizer()));
                tmpMultiRun.add(this.tmpData);
            }
            if (this.show) {
                this.plot.setInfoString(this.currentExperiment, this.experimentName, 0.5f);
            }
            if (this.show) {
                this.draw();
            }
            this.experimentName = this.optimizationParameters.getOptimizer().getName() + "-" + this.performedRuns.size();
        } catch (InterruptedException e) {
            updateStatus(0);
            if (this.show) {
                this.statusField.setText("Interrupted...");
            }
            return "Interrupted";
        }
        if (this.outputFile != null) {
            try {
                this.outputFile.close();
            } catch (IOException e) {
                System.out.println("Failed to close output file!");
            }
        }
        if (this.show) {
            for (int i = 0; i < this.multiRuns; i++) {
                this.plot.clearGraph(1000 + i);
            }
        }
        updateStatus(0);
        if (this.show) {
            this.statusField.setText("Finished...");
        }
        return "All Done";
    }

    /**
     * This method refreshes the plot
     */
    private void draw() {
        ArrayList multiRuns, singleRun;
        Double[] tmpD;
        double[][] data;
//        Color       tmpColor;

        for (int i = this.performedRuns.size() - 1; i < this.performedRuns.size(); i++) {
            multiRuns = (ArrayList) this.performedRuns.get(i);
            // determine minimum run length
            int minRunLen = Integer.MAX_VALUE;
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList) multiRuns.get(j);
//                tmpD = (Double[])singleRun.get(singleRun.size()-1);
                minRunLen = Math.min(minRunLen, singleRun.size());
            }

            data = new double[minRunLen][3];
            // First run to determine mean
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList) multiRuns.get(j);
                for (int p = 0; p < data.length; p++) {
                    tmpD = (Double[]) singleRun.get(p);
                    data[p][0] = tmpD[0].doubleValue();
                    data[p][1] += tmpD[1].doubleValue() / multiRuns.size();
                }
            }
            // Second run to determine variance
            for (int j = 0; j < multiRuns.size(); j++) {
                singleRun = (ArrayList) multiRuns.get(j);
                for (int p = 0; p < data.length; p++) {
                    tmpD = (Double[]) singleRun.get(p);
                    data[p][2] += Math.pow(data[p][1] - tmpD[1].doubleValue(), 2) / multiRuns.size();
                }
            }
            // Now enter this stuff into the graph
            this.plot.clearGraph(this.currentExperiment);

            for (int j = 0; j < data.length; j++) {
                if (this.continueFlag) {
                    this.plot.setConnectedPoint(data[j][0] + this.recentFunctionCalls, data[j][1], this.currentExperiment);
                } else {
                    this.plot.setConnectedPoint(data[j][0], data[j][1], this.currentExperiment);
                }
            }
            this.currentExperiment++;
        }
    }

    /**
     * When the worker needs to update the GUI we do so by queuing a Runnable
     * for the event dispatching thread with SwingUtilities.invokeLater(). In
     * this case we're just changing the progress bars value.
     */
    void updateStatus(final int i) {
        if (this.progressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue(i);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }

    /**
     * This method will create a new instance of LectureGUI and will call show()
     * to create the necessary frames. This method will ignore all arguments.
     *
     * @param args
     */
    public static void main(String[] args) {
        StandaloneOptimization program = new StandaloneOptimization();
        RNG.setRandomSeed(1);
        program.initFrame();
        program.setShow(true);
    }

    @Override
    public void setShow(boolean t) {
        this.show = t;
    }

    /**
     * This method allows an optimizer to register a change in the optimizer.
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals(Population.NEXT_GENERATION_PERFORMED)) {
            Population population = ((InterfaceOptimizer) source).getPopulation();
            double x = 100 / this.multiRuns;
            if (this.optimizationParameters.getTerminator() instanceof EvaluationTerminator) {
                double y = x / (double) ((EvaluationTerminator) this.optimizationParameters.getTerminator()).getFitnessCalls();
                currentProgress = (int) (this.currentRun * x + population.getFunctionCalls() * y);
            } else {
                currentProgress = (int) (this.currentRun * x);
            }
            updateStatus(currentProgress);

            // data to be stored in file
            double tmpd = 0;
            StringBuffer tmpLine = new StringBuffer("");
            tmpLine.append(population.getFunctionCalls());
            tmpLine.append("\t");
            tmpLine.append(population.getBestEAIndividual().getFitness(0));
            tmpLine.append("\t");
            for (int i = 0; i < population.size(); i++) {
                tmpd += ((AbstractEAIndividual) population.get(i)).getFitness(0) / (double) population.size();
            }
            tmpLine.append("\t");
            tmpLine.append(tmpd);
            tmpLine.append("\t");
            tmpLine.append(population.getWorstEAIndividual().getFitness(0));
            //tmpLine.append("\t");
            //tmpLine.append(this.optimizationParameters.getProblem().getAdditionalDataValue(population));
            this.writeToFile(tmpLine.toString());

            Double[] tmpData = new Double[2];
            tmpData[0] = new Double(population.getFunctionCalls());
            // instead of adding simply the best fitness value i'll ask the problem what to show
            tmpData[1] = this.optimizationParameters.getProblem().getDoublePlotValue(population);
            if (this.plot != null) {
                if (this.continueFlag) {
                    this.plot.setConnectedPoint(tmpData[0].doubleValue() + this.recentFunctionCalls, tmpData[1].doubleValue(), 1000 + this.currentRun);
                } else {
                    this.plot.setConnectedPoint(tmpData[0].doubleValue(), tmpData[1].doubleValue(), 1000 + this.currentRun);
                }
            }
            this.tmpData.add(tmpData);
        }
    }

    /**
     * This method writes Data to file.
     *
     * @param line The line that is to be added to the file
     */
    private void writeToFile(String line) {
        String write = line + "\n";
        if (this.outputFile == null) {
            return;
        }
        try {
            this.outputFile.write(write, 0, write.length());
            this.outputFile.flush();
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the name to
     * the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "EA Lecture GUI";
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */

    /**
     * This method allows you to set the number of mulitruns that are to be
     * performed, necessary for stochastic optimizers to ensure reliable
     * results.
     *
     * @param multiruns The number of multiruns that are to be performed
     */
    public void setMultiRuns(int multiruns) {
        this.multiRuns = multiruns;
    }

    public int getMultiRuns() {
        return this.multiRuns;
    }

    public String multiRunsTipText() {
        return "Multiple runs may be necessary to produce reliable results for stochastic optimizing algorithms.";
    }

    /**
     * This method allows you to set the seed for the random number generator.
     *
     * @param seed The seed for the random number generator
     */
    // MK: These methods have nothing to do with the seed parameter from the optimizationParameters object which is actually used, so I comment them out
//    public void setSeed(long seed) {
//        RNG.setseed(seed);
//    }
//    public long getSeed() {
//        return RNG.getRandomSeed();
//    }
//    public String seedTipText() {
//        return "Choose the seed for the random number generator.";
//    }

    /**
     * This method sets the name of the current experiment as it will occur in
     * the plot legend.
     *
     * @param experimentName The experiment name
     */
    public void setExpName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExpName() {
        return this.experimentName;
    }

    public String expNameTipText() {
        return "Set the name of the experiment as it will occur in the legend.";
    }

    /**
     * This method will set the output filename
     *
     * @param name
     */
    public void setOutputFileName(String name) {
        this.outputFileName = name;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }

    public String outputFileNameTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
    }

    /**
     * This method will set the output filename
     *
     * @param name
     */
    public void setList(List name) {
        this.list = name;
    }

    public List getList() {
        return this.list;
    }

    public String listTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
    }
}
