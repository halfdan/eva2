package eva2.optimization.mocco;

import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.optimization.problems.InterfaceOptimizationObjective;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.MultiObjectiveEA;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 21.10.2005
 * Time: 14:40:44
 * To change this template use File | Settings | File Templates.
 */
public class MOCCOState {

    public transient boolean isVisible = false;
    public InterfaceOptimizer m_Optimizer = new MultiObjectiveEA();
    public InterfaceOptimizer m_BackupOptimizer;
    public InterfaceTerminator m_Terminator = new EvaluationTerminator();
    public InterfaceOptimizationProblem m_OriginalProblem = null;
    public InterfaceOptimizationProblem m_CurrentProblem;
    public InterfaceOptimizationProblem m_BackupProblem;
    public int m_InitialPopulationSize = 50;
    // the population history
    public Population m_ParetoFront;
    public Population[] m_PopulationHistory = new Population[0];
    public boolean[] m_Show;
    public boolean[] m_Use;
    public Color[] m_Color;
    // the fitness cache for fast plotting
    public ArrayList m_FitnessCache = new ArrayList();
    public ArrayList m_ObjectiveCache = new ArrayList();
    public ArrayList m_ConstraintCache = new ArrayList();

    public MOCCOState() {
    }

    public void restore() {
        Population pop = this.m_Optimizer.getPopulation();
        if (this.m_BackupProblem != null) {
            this.m_CurrentProblem = this.m_BackupProblem;
            this.m_BackupProblem = null;
        }
        if (this.m_BackupOptimizer != null) {
            this.m_Optimizer = this.m_BackupOptimizer;
            this.m_BackupOptimizer = null;
        }
        this.m_Optimizer.setPopulation(pop);
        this.m_Optimizer.setProblem(this.m_CurrentProblem);
        this.m_CurrentProblem.evaluate(this.m_Optimizer.getPopulation());
    }

    public void makeBackup() {
        this.m_BackupProblem = (InterfaceOptimizationProblem) this.m_CurrentProblem.clone();
        this.m_BackupOptimizer = (InterfaceOptimizer) this.m_Optimizer.clone();
        this.m_BackupOptimizer.setProblem(null);
    }

    public void addPopulation2History(Population pop) {
        InterfaceOptimizationObjective[] tmpObj = null;

        if (this.m_Show == null) {
            this.m_Use = new boolean[1];
            this.m_Use[0] = true;
            this.m_Show = new boolean[1];
            this.m_Show[0] = true;
            this.m_Color = new Color[1];
            this.m_Color[0] = this.getColor4Index(0);
        } else {
            boolean[] newUse = new boolean[this.m_Show.length + 1];
            boolean[] newShow = new boolean[this.m_Show.length + 1];
            Color[] newColor = new Color[this.m_Show.length + 1];
            for (int i = 0; i < this.m_Show.length; i++) {
                newUse[i] = this.m_Use[i];
                newShow[i] = this.m_Show[i];
                newColor[i] = this.m_Color[i];
            }
            newUse[m_Show.length] = true;
            newShow[m_Show.length] = true;
            newColor[m_Show.length] = this.getColor4Index(this.m_PopulationHistory.length);
            this.m_Use = newUse;
            this.m_Show = newShow;
            this.m_Color = newColor;
        }

        Population[] newPop = new Population[this.m_PopulationHistory.length + 1];
        for (int i = 0; i < this.m_PopulationHistory.length; i++) {
            newPop[i] = this.m_PopulationHistory[i];
        }
        newPop[newPop.length - 1] = (Population) pop.clone();
        newPop[newPop.length - 1].addPopulation(newPop[newPop.length - 1].getArchive());
        newPop[newPop.length - 1].SetArchive(null);
        this.m_PopulationHistory = newPop;
        ArrayList fitness = new ArrayList();
        ArrayList objectives = new ArrayList();
        ArrayList constraint = new ArrayList();
        if (this.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.m_CurrentProblem).getProblemObjectives();
        }
        for (int j = 0; j < newPop[newPop.length - 1].size(); j++) {
            if (tmpObj != null) {
                double[] tmoF = new double[tmpObj.length];
                for (int k = 0; k < tmpObj.length; k++) {
                    tmoF[k] = ((Double) ((AbstractEAIndividual) newPop[newPop.length - 1].get(j)).getData(tmpObj[k].getIdentName())).doubleValue();
                }
                objectives.add(tmoF);
            }
            fitness.add(((AbstractEAIndividual) newPop[newPop.length - 1].get(j)).getFitness());
            constraint.add(new Double(((AbstractEAIndividual) newPop[newPop.length - 1].get(j)).getConstraintViolation()));
        }
        if (this.m_ObjectiveCache != null) {
            this.m_ObjectiveCache.add(objectives);
        }
        this.m_FitnessCache.add(fitness);
        this.m_ConstraintCache.add(constraint);
    }

    /**
     * Simple method to choose a color
     *
     * @param i The index to choose a color for
     * @return A nice color...
     */
    public Color getColor4Index(int i) {
        switch (i % 6) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.CYAN;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.ORANGE;
        }
        return Color.RED;
    }

    public void reduce2ParetoFront(int i) {
        ArchivingAllDominating arch = new ArchivingAllDominating();
        arch.addElementsToArchive(this.m_PopulationHistory[i]);
        this.m_PopulationHistory[i] = this.m_PopulationHistory[i].getArchive();
        this.m_PopulationHistory[i].SetArchive(null);
        this.makeFitnessCache(false);
    }

    /**
     * This method return the currently selected populations
     *
     * @return the selected populations
     */
    public Population getSelectedPopulations() {
        Population result = new Population();
        for (int i = 0; i < this.m_PopulationHistory.length; i++) {
            if (this.m_Use[i]) {
                result.addPopulation(this.m_PopulationHistory[i]);
            }
        }
        this.m_CurrentProblem.evaluate(result);
        return result;
    }

    /**
     * This method establishes a fitness cache to give the plot methods
     * easier and faster access to the data
     *
     * @param reevaluate
     */
    public void makeFitnessCache(boolean reevaluate) {
        InterfaceOptimizationObjective[] tmpObj = null;
        if (reevaluate) {
            // clear all archives, since problem dimension may have changed
            for (int i = 0; i < this.m_PopulationHistory.length; i++) {
                if (this.m_PopulationHistory[i].getArchive() != null) {
                    this.m_PopulationHistory[i].addPopulation(this.m_PopulationHistory[i].getArchive());
                    this.m_PopulationHistory[i].SetArchive(null);
                }
            }
            Population pop = this.m_Optimizer.getPopulation();
            if (pop.getArchive() != null) {
                pop.addPopulation(pop.getArchive());
                pop.SetArchive(null);
            }
            this.m_CurrentProblem.evaluate(pop);
        }
        this.m_FitnessCache = new ArrayList();
        this.m_ObjectiveCache = null;
        this.m_ConstraintCache = new ArrayList();
        if (this.m_CurrentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            this.m_ObjectiveCache = new ArrayList();
            tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.m_CurrentProblem).getProblemObjectives();
        }
        this.m_ParetoFront = new Population();
        for (int i = 0; i < this.m_PopulationHistory.length; i++) {
            if (reevaluate) {
                ((AbstractMultiObjectiveOptimizationProblem) this.m_CurrentProblem).resetParetoFront();
                this.m_CurrentProblem.evaluate(this.m_PopulationHistory[i]);
            }
            this.m_ParetoFront.addPopulation(this.m_PopulationHistory[i]);
            ArrayList fitness = new ArrayList();
            ArrayList objectives = new ArrayList();
            ArrayList constraint = new ArrayList();
            for (int j = 0; j < this.m_PopulationHistory[i].size(); j++) {
                if (tmpObj != null) {
                    double[] tmoF = new double[tmpObj.length];
                    for (int k = 0; k < tmpObj.length; k++) {
                        if (this.m_PopulationHistory[i].get(j) == null) {
                            System.out.println("Individual " + i + " == null!");
                        }
                        if (tmpObj[k] == null) {
                            System.out.println("Objective " + k + " == null!");
                        }
                        if (((AbstractEAIndividual) this.m_PopulationHistory[i].get(j)).getData(tmpObj[k].getIdentName()) == null) {
                            System.out.println("User Data " + k + " " + tmpObj[k].getIdentName() + " == null!");
                        }
                        tmoF[k] = ((Double) ((AbstractEAIndividual) this.m_PopulationHistory[i].get(j)).getData(tmpObj[k].getIdentName())).doubleValue();
                    }
                    objectives.add(tmoF);
                }
                fitness.add(((AbstractEAIndividual) this.m_PopulationHistory[i].get(j)).getFitness());
                constraint.add(new Double(((AbstractEAIndividual) this.m_PopulationHistory[i].get(j)).getConstraintViolation()));
            }
            if (this.m_ObjectiveCache != null) {
                this.m_ObjectiveCache.add(objectives);
            }
            this.m_FitnessCache.add(fitness);
            this.m_ConstraintCache.add(constraint);
        }
        ArchivingAllDominating arch = new ArchivingAllDominating();
        arch.addElementsToArchive(this.m_ParetoFront);
        this.m_ParetoFront = this.m_ParetoFront.getArchive();
        this.m_ParetoFront.SetArchive(null);
    }
}
