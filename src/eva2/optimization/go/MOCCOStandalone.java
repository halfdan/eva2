package eva2.optimization.go;

import eva2.gui.PropertyDoubleArray;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.mocco.*;
import eva2.optimization.mocco.paretofrontviewer.MOCCOViewer;
import eva2.optimization.operator.moso.InterfaceMOSOConverter;
import eva2.optimization.operator.moso.MOSOWeightedFitness;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.problems.*;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.IslandModelEA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 *
 */
public class MOCCOStandalone implements InterfaceStandaloneOptimization, InterfacePopulationChangedEventListener, Serializable {

    public volatile MOCCOState state;
    private SwingWorker worker;
    private volatile boolean stillWorking = false;
    public int iteration = -1;
    private JFrame mainFrame;
    //public ParetoFrontView      n_ParetoFrontView;
    private boolean debug = false;
    public MOCCOViewer view;
    public JPanel mainPanel, parameterPanel, controlPanel, buttonPanel;
    private JLabel currentState;
    private JProgressBar progressBar;

    public MOCCOStandalone() {
        this.state = new MOCCOState();
    }

    /**
     * This method will initialize the main MOCCO GUI
     * frame
     */
    public void initMOCCOFrame() {

        this.state.isVisible = true;
        this.mainFrame = new JFrame();
        this.mainFrame.setTitle("MOCCO - Interactive Multi-Objective Optimization");
        this.mainFrame.setSize(1200, 750);
        this.mainFrame.setLocation(50, 50);
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        // initialize basic panel structure
        this.mainPanel = new JPanel();
        this.parameterPanel = new JPanel();
        this.parameterPanel.setPreferredSize(new Dimension(500, 300));
        this.parameterPanel.setMinimumSize(new Dimension(500, 300));
        this.controlPanel = new JPanel();
        this.view = new MOCCOViewer(this);
        this.mainFrame.getContentPane().add(this.mainPanel);
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(this.parameterPanel, BorderLayout.WEST);
        this.mainPanel.add(this.view, BorderLayout.CENTER);
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new BorderLayout());
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridLayout(2, 1));
        this.currentState = new JLabel("Problem Initialization");
        tmpP.add(this.currentState);
        this.progressBar = new JProgressBar();
        tmpP.add(this.progressBar);
        this.buttonPanel.add(tmpP, BorderLayout.CENTER);
        this.controlPanel.setMinimumSize(new Dimension(400, 0));
        this.controlPanel.setPreferredSize(new Dimension(400, 0));
        this.buttonPanel.add(this.controlPanel, BorderLayout.EAST);
        this.mainPanel.add(this.buttonPanel, BorderLayout.SOUTH);
        this.mainFrame.validate();
        // everything is prepared let's start the main loop
        this.MOCCOOptimization();
    }

    public void MOCCOOptimization() {
        boolean cont = true;
        InterfaceProcessElement tmpP;
        while (cont) {
            this.iteration++;
            while (stillWorking) {
                try {
                    Thread.sleep(1000);
                } catch (java.lang.InterruptedException e) {
                }
            }
            if (this.state.originalProblem == null) {
                this.state.originalProblem = new TF1Problem();
                tmpP = new MOCCOProblemInitialization(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) {
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException e) {
                    }
                }
                this.state.currentProblem = (InterfaceOptimizationProblem) this.state.originalProblem.clone();
                this.view.problemChanged(true);
                this.parameterPanel.removeAll();
                tmpP = new MOCCOInitialPopulationSize(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) {
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException e) {
                    }
                }
                this.state.initialPopulationSize = Math.max(1, this.state.initialPopulationSize);
                Population pop = new Population();
                pop.setTargetSize(this.state.initialPopulationSize);
                this.state.currentProblem = (InterfaceOptimizationProblem) this.state.originalProblem.clone();
                this.state.currentProblem.initializePopulation(pop);
                this.state.currentProblem.evaluate(pop);
                this.state.addPopulation2History(pop);
                this.view.problemChanged(true);
            }
            ((InterfaceMultiObjectiveDeNovoProblem) this.state.currentProblem).deactivateRepresentationEdit();
            this.updateStatus("Analysis/Redefinition", 33);
            tmpP = new MOCCOProblemRedefinition(this);
            tmpP.initProcessElementParametrization();
            while (!tmpP.isFinished()) {
                try {
                    Thread.sleep(1000);
                } catch (java.lang.InterruptedException e) {
                }
            }
            this.state.makeFitnessCache(true);
            this.state.currentProblem.initializeProblem();
            this.state.makeBackup();
            this.view.problemChanged(true);
            if (this.state.currentProblem.isMultiObjective()) {
                this.updateStatus("MO Strategy Selection", 50);
                tmpP = new MOCCOChooseMOStrategy(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) {
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException e) {
                    }
                }
                switch (((MOCCOChooseMOStrategy) tmpP).getMOStrategy()) {
                    case MOCCOChooseMOStrategy.STRATEGY_MOEA: {
                        this.updateStatus("MOEA Parameterization", 75);
                        tmpP = new MOCCOParameterizeMO(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_STEP: {
                        this.updateStatus("Reference Solution...", 75);
                        tmpP = new MOCCOChooseReferenceSolution(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        AbstractEAIndividual reference = ((MOCCOChooseReferenceSolution) tmpP).getReferenceSolution();
                        this.updateStatus("STEP Parameterization...", 90);
                        tmpP = new MOCCOParameterizeSTEP(this);
                        ((MOCCOParameterizeSTEP) tmpP).setReferenceSolution(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_REFP: {
                        this.updateStatus("Reference Point...", 75);
                        tmpP = new MOCCOChooseReferencePoint(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        double[] reference = ((MOCCOChooseReferencePoint) tmpP).getReferencePoint();
                        this.updateStatus("Reference Point Parameterization...", 90);
                        tmpP = new MOCCOParameterizeRefPoint(this);
                        ((MOCCOParameterizeRefPoint) tmpP).setReferencePoint(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_TBCH: {
                        this.updateStatus("Reference Point...", 75);
                        tmpP = new MOCCOChooseReferencePoint(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        double[] reference = ((MOCCOChooseReferencePoint) tmpP).getReferencePoint();
                        this.updateStatus("Tchebycheff Method Parameterization...", 90);
                        tmpP = new MOCCOParameterizeTchebycheff(this);
                        ((MOCCOParameterizeTchebycheff) tmpP).setReferencePoint(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_GDF: {
                        this.updateStatus("Reference Solution...", 75);
                        tmpP = new MOCCOChooseReferenceSolution(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        AbstractEAIndividual reference = ((MOCCOChooseReferenceSolution) tmpP).getReferenceSolution();
                        this.updateStatus("Geoffrion-Dyer-Feinberg Method Parameterization...", 90);
                        tmpP = new MOCCOParameterizeGDF(this);
                        ((MOCCOParameterizeGDF) tmpP).setReferenceSolution(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                    default: {
                        tmpP = new MOCCOParameterizeMO(this);
                        while (!tmpP.isFinished()) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                            }
                        }
                        break;
                    }
                }
            } else {
                this.updateStatus("SO-Optimizer Parameterization", 66);
                tmpP = new MOCCOParameterizeSO(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) {
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException e) {
                    }
                }
            }
            // now optimize
            this.updateStatus("Optimizing...", 0);
            this.startExperiment();
        }
    }

    private void checkForObjectives(String w) {
        System.out.println("I'm currently " + w);
        System.out.print("Original Problem is ");
        if (this.state.originalProblem.isMultiObjective()) {
            System.out.println("multi-objective.");
        } else {
            System.out.println("single-objective.");
        }
        System.out.print("Current Problem is ");
        if (this.state.currentProblem.isMultiObjective()) {
            System.out.println("multi-objective.");
        } else {
            System.out.println("single-objective.");
        }
        if (this.state.backupProblem != null) {
            System.out.print("Backup Problem is ");
            if (this.state.backupProblem.isMultiObjective()) {
                System.out.println("multi-objective.");
            } else {
                System.out.println("single-objective.");
            }
        } else {
            System.out.println("No Backup Problem");
        }
    }

    private void checktForMOSO(String w) {
        String s;
        System.out.println("I'm currently at " + w);
        InterfaceMOSOConverter moso = ((AbstractMultiObjectiveOptimizationProblem) this.state.currentProblem).getMOSOConverter();
        System.out.println("MOSO selected: " + moso.getName());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem) this.state.currentProblem).getProblemObjectives();
        s = "Objectives: {";
        for (int i = 0; i < obj.length; i++) {
            s += obj[i].getIdentName();
            if (i < (obj.length - 1)) {
                s += "; ";
            }
        }
        s += "}";
        System.out.println("" + s);
        if (moso instanceof MOSOWeightedFitness) {
            PropertyDoubleArray prop = ((MOSOWeightedFitness) moso).getWeights();
            s = "Weights   : {";
            for (int i = 0; i < prop.getNumRows(); i++) {
                s += prop.getValue(i, 0);
                if (i < (prop.getNumRows() - 1)) {
                    s += "; ";
                }
            }
            s += "}";
            System.out.println("" + s);
        }

    }
/*
    public void test() {
        for (int a = 1; a < 10; a++) {
            ArrayList ProxyList = new ArrayList();
            ArrayList NormalList = new ArrayList();
            // Create a list of ThreadProxies of Something objects --> ProxyList
            // Create a list of Something objects --> NormalList
            for (int i = 0; i < a; i++) {
                ProxyList.add(ThreadProxy.newInstance(new Something(1000)));
                NormalList.add(new Something(1000));
            }
            long ProxyTime = System.currentTimeMillis();
            // calling a method of Something, everytime a call is performed a
            // new Thread is started processing this call.
            for (int i = 0; i < a; i++)
                ((SomethingInterface)(ProxyList.get(i))).doit();
            long CallingProxyTime = ProxyTime-System.currentTimeMillis();
            // calling a second method of Something but the first call (doit)
            // is not finished until now. So the opeartion sequence waits
            // here until the first call is finished.
            for (int i = 0; i < a; i++)
                ((SomethingInterface)(ProxyList.get(i))).nothing();
            ProxyTime = System.currentTimeMillis() - ProxyTime;
            System.out.print ("ProxyTime :" + ProxyTime + " CallingProxyTime :" +CallingProxyTime);
            long NormalTime = System.currentTimeMillis();
            for (int i = 0; i < a; i++)
                ((SomethingInterface)(NormalList.get(i))).doit();
            NormalTime = System.currentTimeMillis() - NormalTime;
            System.out.print (" NormalTime :" + NormalTime);
            double frac = ((double)NormalTime)/((double)ProxyTime);
            System.out.println(" Number of Threads=" + a+ " frac = "+frac);
        }
    }*/

    /**
     * This is the main method
     *
     * @param args
     */
    public static void main(String[] args) {
        MOCCOStandalone go = new MOCCOStandalone();
        if (args.length == 0) {
            // start GUI
            go.initMOCCOFrame();
        } else {
            String file = args[0];
            go.openObject(file);
            if (go.state == null) {
                System.out.println("No valid input state!");
                System.exit(0);
            } else {
                if (go.state.optimizer.getPopulation().getFunctionCalls() == 0) {
                    // start to optimize
                    go.startExperiment();
                    file = file.replaceAll(".ser", "");
                    go.saveObject(file + "_Finished.ser");
                } else {
                    // start GUI
                    go.initMOCCOFrame();
                }
            }
        }
    }

    /**
     * This methods loads the current state of MOCO from a serialized file
     *
     * @param loadFrom The name of the serialized file
     * @return The new state of MOCO
     */
    public Object openObject(String loadFrom) {
        File selected = new File(loadFrom);
        try {
            ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
            Object obj = oi.readObject();
            oi.close();
            if (!(obj instanceof MOCCOState)) {
                throw new Exception("Object not of type MOCCOState");
            }
            return obj;
        } catch (Exception ex) {
            if (this.mainFrame != null) {
                JOptionPane.showMessageDialog(this.mainFrame, "Couldn't read object: " + selected.getName() + "\n" + ex.getMessage(), "Open object file", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Couldn't read object: " + selected.getName() + "\n" + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * This method saves the current MOCOData into a serialized file
     *
     * @param saveAs The name of the outputfile
     */
    public void saveObject(String saveAs) {
        File sFile = new File(saveAs);
        try {
            ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
            oo.writeObject(this.state);
            oo.close();
        } catch (Exception ex) {
            if (this.mainFrame != null) {
                JOptionPane.showMessageDialog(this.mainFrame, "Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage(), "Save object", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage());
            }
        }
    }

    /***********************************************************************************************
     * InterfaceStandaloneOptimization
     */

    /**
     * This method starts the actual optimization procedure
     */
    @Override
    public void startExperiment() {
        if (this.mainFrame != null) {
        }
        this.stillWorking = true;
        this.state.optimizer.setProblem(this.state.currentProblem);
        if (this.debug) {
            System.out.println("" + this.state.optimizer.getStringRepresentation());
        }
        this.state.currentProblem.evaluate(this.state.optimizer.getPopulation());
        this.state.optimizer.getPopulation().setFunctionCalls(0);
        if (this.state.optimizer.getPopulation().size() == 0) {
            this.state.optimizer.initialize();
        }
        this.state.optimizer.addPopulationChangedEventListener(this);
        worker = new SwingWorker() {
            @Override
            public Object construct() {
                return doWork();
            }

            @Override
            public void finished() {
                Population[] pop = null;
                if (state.optimizer instanceof IslandModelEA) {
                    InterfaceOptimizer[] opt = ((IslandModelEA) state.optimizer).getOptimizers();
                    pop = new Population[opt.length];
                    for (int i = 0; i < opt.length; i++) {
                        pop[i] = opt[i].getPopulation();
                    }
                }
                state.restore();
                if (pop == null) {
                    state.addPopulation2History(state.optimizer.getPopulation());
                } else {
                    for (int i = 0; i < pop.length; i++) {
                        state.currentProblem.evaluate(pop[i]);
                        state.addPopulation2History(pop[i]);
                    }
                }
                if (view != null) {
                    view.problemChanged(true);
                }
                stillWorking = false;
            }
        };
        worker.start();
    }

    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
    void updateStatus(final String t, final int i) {
        if (this.progressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue(i);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
        this.currentState.setText(t);
    }

    /**
     * This method represents the application code that we'd like to
     * run on a separate thread.  It simulates slowly computing
     * a value, in this case just a string 'All Done'.  It updates the
     * progress bar every half second to remind the user that
     * we're still busy.
     */
    Object doWork() {
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            while (!this.state.terminator.isTerminated(this.state.optimizer.getPopulation())) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                this.state.optimizer.optimize();
            }
            System.gc();
        } catch (InterruptedException e) {
            updateStatus("Interrupted", 0);
            return "Interrupted";
        }
        updateStatus("All Done", 0);
        return "All Done";
    }

    @Override
    public void setShow(boolean t) {
        // i guess this is not necessary in this environment
    }

    /***********************************************************************************************
     * InterfacePopulationChangedEventListener
     */
    /**
     * This method allows an optimizer to register a change in the optimizer.
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        int currentProgress;
        if (name.equals(Population.NEXT_GENERATION_PERFORMED)) {
            if (this.state.isVisible) {
                Population population = ((InterfaceOptimizer) source).getPopulation();
                double x = 100;
                if (this.state.terminator instanceof EvaluationTerminator) {
                    double y = x / (double) ((EvaluationTerminator) this.state.terminator).getFitnessCalls();
                    currentProgress = (int) (population.getFunctionCalls() * y);
                } else {
                    currentProgress = (int) (0);
                }
                updateStatus("Optimizing...", currentProgress);
            } else {
                // perhaps i could write it to file!?
            }
        }
    }

    public JFrame getMainFrame() {
        return this.mainFrame;
    }
}
