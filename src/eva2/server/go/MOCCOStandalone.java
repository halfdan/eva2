package eva2.server.go;


import javax.swing.*;

import eva2.gui.*;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.mocco.InterfaceProcessElement;
import eva2.server.go.mocco.MOCCOChooseMOStrategy;
import eva2.server.go.mocco.MOCCOChooseReferencePoint;
import eva2.server.go.mocco.MOCCOChooseReferenceSolution;
import eva2.server.go.mocco.MOCCOInitialPopulationSize;
import eva2.server.go.mocco.MOCCOParameterizeGDF;
import eva2.server.go.mocco.MOCCOParameterizeMO;
import eva2.server.go.mocco.MOCCOParameterizeRefPoint;
import eva2.server.go.mocco.MOCCOParameterizeSO;
import eva2.server.go.mocco.MOCCOParameterizeSTEP;
import eva2.server.go.mocco.MOCCOParameterizeTchebycheff;
import eva2.server.go.mocco.MOCCOProblemInitialization;
import eva2.server.go.mocco.MOCCOProblemRedefinition;
import eva2.server.go.mocco.MOCCOState;
import eva2.server.go.mocco.paretofrontviewer.MOCCOViewer;
import eva2.server.go.operators.moso.InterfaceMOSOConverter;
import eva2.server.go.operators.moso.MOSOWeightedFitness;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.server.go.problems.InterfaceOptimizationObjective;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.problems.TF1Problem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.IslandModelEA;
import eva2.server.go.tools.GeneralGOEProperty;
import eva2.tools.jproxy.ThreadProxy;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 21.10.2005
 * Time: 14:31:56
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOStandalone implements InterfaceGOStandalone, InterfacePopulationChangedEventListener, java.io.Serializable {

    public volatile MOCCOState  m_State;
    private SwingWorker         worker;
    private volatile boolean    m_StillWorking = false;
    public int                  m_Iteration = -1;
    public JFrame               m_JFrame;
    //public ParetoFrontView      n_ParetoFrontView;
    public boolean              m_Debug = false;
    public MOCCOViewer          m_View;
    public JPanel               m_JPanelMain, m_JPanelParameters, m_JPanelControl, m_JPanelButtom;
    private JLabel              m_CurrentState;
    private JProgressBar        m_ProgressBar;

    public MOCCOStandalone() {
        this.m_State = new MOCCOState();
    }

    /** This method will init the main MOCCO GUI
     * frame
     */
    public void initMOCCOFrame() {
        JParaPanel paraPanel = new JParaPanel(this, "MyGUI");

        this.m_State.isVisible = true;
        this.m_JFrame = new JFrame();
        this.m_JFrame.setTitle("MOCCO - Interactive Multi-Objective Optimization");
        this.m_JFrame.setSize(1200, 750);
        this.m_JFrame.setLocation(50, 50);
        this.m_JFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        // init basic panel structure
        this.m_JPanelMain       = new JPanel();
        this.m_JPanelParameters = new JPanel();
        this.m_JPanelParameters.setPreferredSize(new Dimension(500, 300));
        this.m_JPanelParameters.setMinimumSize(new Dimension(500, 300));
        this.m_JPanelControl    = new JPanel();
        this.m_View             = new MOCCOViewer(this);
        this.m_JFrame.getContentPane().add(this.m_JPanelMain);
        this.m_JPanelMain.setLayout(new BorderLayout());
        this.m_JPanelMain.add(this.m_JPanelParameters, BorderLayout.WEST);
        this.m_JPanelMain.add(this.m_View, BorderLayout.CENTER);
        this.m_JPanelButtom = new JPanel();
        this.m_JPanelButtom.setLayout(new BorderLayout());
        JPanel tmpP = new JPanel();
        tmpP.setLayout(new GridLayout(2,1));
        this.m_CurrentState = new JLabel("Problem Initialization");
        tmpP.add(this.m_CurrentState);
        this.m_ProgressBar = new JProgressBar();
        tmpP.add(this.m_ProgressBar);
        this.m_JPanelButtom.add(tmpP, BorderLayout.CENTER);
        this.m_JPanelControl.setMinimumSize(new Dimension(400,0));
        this.m_JPanelControl.setPreferredSize(new Dimension(400,0));
        this.m_JPanelButtom.add(this.m_JPanelControl, BorderLayout.EAST);
        this.m_JPanelMain.add(this.m_JPanelButtom, BorderLayout.SOUTH);
        this.m_JFrame.validate();
        // everything is prepared let's start the main loop
        this.MOCCOOptimization();
    }

    public void MOCCOOptimization() {
        boolean                 cont = true;
        InterfaceProcessElement tmpP;
        while (cont) {
            this.m_Iteration++;
            while (m_StillWorking) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
            if (this.m_State.m_OriginalProblem == null) {
                this.m_State.m_OriginalProblem = new TF1Problem();
                tmpP = new MOCCOProblemInitialization(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { }}
                this.m_State.m_CurrentProblem = (InterfaceOptimizationProblem)this.m_State.m_OriginalProblem.clone();
                this.m_View.problemChanged(true);
                this.m_JPanelParameters.removeAll();
                tmpP = new MOCCOInitialPopulationSize(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { }}
                this.m_State.m_InitialPopulationSize = Math.max(1, this.m_State.m_InitialPopulationSize);
                Population pop = new Population();
                pop.setTargetSize(this.m_State.m_InitialPopulationSize);
                this.m_State.m_CurrentProblem = (InterfaceOptimizationProblem) this.m_State.m_OriginalProblem.clone();
                this.m_State.m_CurrentProblem.initPopulation(pop);
                this.m_State.m_CurrentProblem.evaluate(pop);
                this.m_State.addPopulation2History(pop);
                this.m_View.problemChanged(true);
            }
            ((InterfaceMultiObjectiveDeNovoProblem)this.m_State.m_CurrentProblem).deactivateRepresentationEdit();
            this.updateStatus("Analysis/Redefinition", 33);
            tmpP = new MOCCOProblemRedefinition(this);
            tmpP.initProcessElementParametrization();
            while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
            this.m_State.makeFitnessCache(true);            
            this.m_State.m_CurrentProblem.initProblem();
            this.m_State.makeBackup();
            this.m_View.problemChanged(true);
            if (this.m_State.m_CurrentProblem.isMultiObjective()) {
                this.updateStatus("MO Strategy Selection", 50);
                tmpP = new MOCCOChooseMOStrategy(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                switch (((MOCCOChooseMOStrategy)tmpP).getMOStrategy()) {
                    case MOCCOChooseMOStrategy.STRATEGY_MOEA : {
                        this.updateStatus("MOEA Parameterization", 75);
                        tmpP = new MOCCOParameterizeMO(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_STEP : {
                        this.updateStatus("Reference Solution...", 75);
                        tmpP = new MOCCOChooseReferenceSolution(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        AbstractEAIndividual reference = ((MOCCOChooseReferenceSolution)tmpP).getReferenceSolution();
                        this.updateStatus("STEP Parameterization...", 90);
                        tmpP = new MOCCOParameterizeSTEP(this);
                        ((MOCCOParameterizeSTEP)tmpP).setReferenceSolution(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_REFP : {
                        this.updateStatus("Reference Point...", 75);
                        tmpP = new MOCCOChooseReferencePoint(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        double[] reference = ((MOCCOChooseReferencePoint)tmpP).getReferencePoint();
                        this.updateStatus("Reference Point Parameterization...", 90);
                        tmpP = new MOCCOParameterizeRefPoint(this);
                        ((MOCCOParameterizeRefPoint)tmpP).setReferencePoint(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_TBCH : {
                        this.updateStatus("Reference Point...", 75);
                        tmpP = new MOCCOChooseReferencePoint(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        double[] reference = ((MOCCOChooseReferencePoint)tmpP).getReferencePoint();
                        this.updateStatus("Tchebycheff Method Parameterization...", 90);
                        tmpP = new MOCCOParameterizeTchebycheff(this);
                        ((MOCCOParameterizeTchebycheff)tmpP).setReferencePoint(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                    case MOCCOChooseMOStrategy.STRATEGY_GDF : {
                        this.updateStatus("Reference Solution...", 75);
                        tmpP = new MOCCOChooseReferenceSolution(this);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        AbstractEAIndividual reference = ((MOCCOChooseReferenceSolution)tmpP).getReferenceSolution();
                        this.updateStatus("Geoffrion-Dyer-Feinberg Method Parameterization...", 90);
                        tmpP = new MOCCOParameterizeGDF(this);
                        ((MOCCOParameterizeGDF)tmpP).setReferenceSolution(reference);
                        tmpP.initProcessElementParametrization();
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                    default : {
                        tmpP = new MOCCOParameterizeMO(this);
                        while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
                        break;
                    }
                }
            } else {
                this.updateStatus("SO-Optimizer Parameterization", 66);
                tmpP = new MOCCOParameterizeSO(this);
                tmpP.initProcessElementParametrization();
                while (!tmpP.isFinished()) { try { Thread.sleep(1000); } catch (java.lang.InterruptedException e) { } }
            }
            // now optimize
            this.updateStatus("Optimizing...", 0);
            this.startExperiment();
        }
    }

    private void checkForObjectives(String w) {
        System.out.println("I'm currently "+w);
        System.out.print("Original Problem is ");
        if (this.m_State.m_OriginalProblem.isMultiObjective()) System.out.println("multi-objective.");
        else System.out.println("single-objective.");
        System.out.print("Current Problem is ");
        if (this.m_State.m_CurrentProblem.isMultiObjective()) System.out.println("multi-objective.");
        else System.out.println("single-objective.");
        if (this.m_State.m_BackupProblem != null) {
            System.out.print("Backup Problem is ");
            if (this.m_State.m_BackupProblem.isMultiObjective()) System.out.println("multi-objective.");
            else System.out.println("single-objective.");
        } else {
            System.out.println("No Backup Problem");
        }
    }

    private void checktForMOSO(String w) {
        String s;
        System.out.println("I'm currently at "+w);
        InterfaceMOSOConverter moso = ((AbstractMultiObjectiveOptimizationProblem)this.m_State.m_CurrentProblem).getMOSOConverter();
        System.out.println("MOSO selected: "+moso.getName());
        InterfaceOptimizationObjective[] obj = ((InterfaceMultiObjectiveDeNovoProblem)this.m_State.m_CurrentProblem).getProblemObjectives();
        s = "Objectives: {";
        for (int i = 0; i < obj.length; i++) {
            s += obj[i].getIdentName();
            if (i < (obj.length-1)) s += "; ";
        }
        s += "}";
        System.out.println(""+s);
        if (moso instanceof MOSOWeightedFitness) {
            PropertyDoubleArray prop = ((MOSOWeightedFitness)moso).getWeights();
            s = "Weights   : {";
            for (int i = 0; i < prop.getNumRows(); i++) {
                s += prop.getValue(i,0);
                if (i < (prop.getNumRows()-1)) s += "; ";
            }
            s += "}";
            System.out.println(""+s);
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

    /** This is the main method
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
            if (go.m_State == null) {
                System.out.println("No valid input state!");
                System.exit(0);
            } else {
                if (go.m_State.m_Optimizer.getPopulation().getFunctionCalls() == 0) {
                    // start to optimize
                    go.startExperiment();
                    file = file.replaceAll(".ser","");
                    go.saveObject(file+"_Finished.ser");
                } else {
                    // start GUI
                    go.initMOCCOFrame();
                }
            }
        }
    }

    /** This methods loads the current state of MOCO from a serialized file
     * @param loadFrom  The name of the serialized file
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
            } return obj;
	    } catch (Exception ex) {
            if (this.m_JFrame != null)
	            JOptionPane.showMessageDialog(this.m_JFrame, "Couldn't read object: " + selected.getName() + "\n" + ex.getMessage(), "Open object file", JOptionPane.ERROR_MESSAGE);
            else
                System.out.println("Couldn't read object: " + selected.getName() + "\n" + ex.getMessage());
	    }
        return null;
    }

    /** This method saves the current MOCOData into a serialized file
     * @param saveAs    The name of the outputfile
     */
    public void saveObject(String saveAs) {
	    File sFile = new File(saveAs);
	    try {
	        ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
            oo.writeObject(this.m_State);
            oo.close();
	    } catch (Exception ex) {
            if (this.m_JFrame != null)
	            JOptionPane.showMessageDialog(this.m_JFrame, "Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage(), "Save object", JOptionPane.ERROR_MESSAGE);
            else
                System.out.println("Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage());
	    }
    }

    /***********************************************************************************************
     * InterfaceGOStandalone
     */

    /** This method starts the actual optimization procedure
     */
    public void startExperiment() {
        if (this.m_JFrame != null) {
        }
        this.m_StillWorking = true;
        this.m_State.m_Optimizer.SetProblem(this.m_State.m_CurrentProblem);
        if (this.m_Debug) System.out.println(""+this.m_State.m_Optimizer.getStringRepresentation());
        this.m_State.m_CurrentProblem.evaluate(this.m_State.m_Optimizer.getPopulation());
        this.m_State.m_Optimizer.getPopulation().SetFunctionCalls(0);
        if (this.m_State.m_Optimizer.getPopulation().size() == 0) this.m_State.m_Optimizer.init();
        this.m_State.m_Optimizer.addPopulationChangedEventListener(this);
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }
            public void finished() {
                Population[] pop = null;
                if (m_State.m_Optimizer instanceof IslandModelEA) {
                    InterfaceOptimizer[] opt = ((IslandModelEA)m_State.m_Optimizer).getOptimizers();
                    pop = new Population[opt.length];
                    for (int i = 0; i < opt.length; i++) {
                        pop[i] = opt[i].getPopulation();
                    }
                }
                m_State.restore();
                if (pop == null) {
                    m_State.addPopulation2History(m_State.m_Optimizer.getPopulation());
                } else {
                    for (int i = 0; i < pop.length; i++) {
                        m_State.m_CurrentProblem.evaluate(pop[i]);
                        m_State.addPopulation2History(pop[i]);
                    }
                }
                if (m_View != null) m_View.problemChanged(true);
                m_StillWorking = false;
            }
        };
        worker.start();
    }

    /** When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
    void updateStatus(final String t, final int i) {
        if (this.m_ProgressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                public void run() {
                    m_ProgressBar.setValue(i);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
        this.m_CurrentState.setText(t);
    }

    /** This method represents the application code that we'd like to
     * run on a separate thread.  It simulates slowly computing
     * a value, in this case just a string 'All Done'.  It updates the
     * progress bar every half second to remind the user that
     * we're still busy.
     */
    Object doWork() {
        try {
            if (Thread.interrupted()) throw new InterruptedException();
            while (!this.m_State.m_Terminator.isTerminated(this.m_State.m_Optimizer.getPopulation())) {
                if (Thread.interrupted()) throw new InterruptedException();
                this.m_State.m_Optimizer.optimize();
            }
            System.gc();
        }
        catch (InterruptedException e) {
            updateStatus("Interrupted", 0);
            return "Interrupted";
        }
        updateStatus("All Done", 0);
        return "All Done";
    }

    public void setShow(boolean t) {
        // i guess this is not necessary in this environment
    }

    /***********************************************************************************************
     * InterfacePopulationChangedEventListener
     */
    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
    	int currentProgress;
    	if (name.equals(Population.nextGenerationPerformed)) {
    		if (this.m_State.isVisible) {
    			Population population = ((InterfaceOptimizer)source).getPopulation();
    			double x = 100;
    			if (this.m_State.m_Terminator instanceof EvaluationTerminator) {
    				double y = x/(double)((EvaluationTerminator)this.m_State.m_Terminator).getFitnessCalls();
    				currentProgress = (int)(population.getFunctionCalls()*y);
    			} else {
    				currentProgress = (int)(0);
    			}
    			updateStatus("Optimizing...",currentProgress);
    		} else {
    			// perhaps i could write it to file!?
    		}
    	}
    }
}
