package eva2.optimization.mocco;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.InterfaceMultiObjectiveDeNovoProblem;
import eva2.problems.InterfaceOptimizationObjective;
import eva2.problems.InterfaceOptimizationProblem;
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
    public InterfaceOptimizer optimizer = new MultiObjectiveEA();
    public InterfaceOptimizer backupOptimizer;
    public InterfaceTerminator terminator = new EvaluationTerminator();
    public InterfaceOptimizationProblem originalProblem = null;
    public InterfaceOptimizationProblem currentProblem;
    public InterfaceOptimizationProblem backupProblem;
    public int initialPopulationSize = 50;
    // the population history
    public Population paretoFront;
    public Population[] populationHistory = new Population[0];
    public boolean[] show;
    public boolean[] use;
    public Color[] colors;
    // the fitness cache for fast plotting
    public ArrayList fitnessCache = new ArrayList();
    public ArrayList objectiveCache = new ArrayList();
    public ArrayList constraintCache = new ArrayList();

    public MOCCOState() {
    }

    public void restore() {
        Population pop = this.optimizer.getPopulation();
        if (this.backupProblem != null) {
            this.currentProblem = this.backupProblem;
            this.backupProblem = null;
        }
        if (this.backupOptimizer != null) {
            this.optimizer = this.backupOptimizer;
            this.backupOptimizer = null;
        }
        this.optimizer.setPopulation(pop);
        this.optimizer.setProblem(this.currentProblem);
        this.currentProblem.evaluate(this.optimizer.getPopulation());
    }

    public void makeBackup() {
        this.backupProblem = (InterfaceOptimizationProblem) this.currentProblem.clone();
        this.backupOptimizer = (InterfaceOptimizer) this.optimizer.clone();
        this.backupOptimizer.setProblem(null);
    }

    public void addPopulation2History(Population pop) {
        InterfaceOptimizationObjective[] tmpObj = null;

        if (this.show == null) {
            this.use = new boolean[1];
            this.use[0] = true;
            this.show = new boolean[1];
            this.show[0] = true;
            this.colors = new Color[1];
            this.colors[0] = this.getColor4Index(0);
        } else {
            boolean[] newUse = new boolean[this.show.length + 1];
            boolean[] newShow = new boolean[this.show.length + 1];
            Color[] newColor = new Color[this.show.length + 1];
            for (int i = 0; i < this.show.length; i++) {
                newUse[i] = this.use[i];
                newShow[i] = this.show[i];
                newColor[i] = this.colors[i];
            }
            newUse[show.length] = true;
            newShow[show.length] = true;
            newColor[show.length] = this.getColor4Index(this.populationHistory.length);
            this.use = newUse;
            this.show = newShow;
            this.colors = newColor;
        }

        Population[] newPop = new Population[this.populationHistory.length + 1];
        System.arraycopy(this.populationHistory, 0, newPop, 0, this.populationHistory.length);
        newPop[newPop.length - 1] = (Population) pop.clone();
        newPop[newPop.length - 1].addPopulation(newPop[newPop.length - 1].getArchive());
        newPop[newPop.length - 1].SetArchive(null);
        this.populationHistory = newPop;
        ArrayList fitness = new ArrayList();
        ArrayList objectives = new ArrayList();
        ArrayList constraint = new ArrayList();
        if (this.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.currentProblem).getProblemObjectives();
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
        if (this.objectiveCache != null) {
            this.objectiveCache.add(objectives);
        }
        this.fitnessCache.add(fitness);
        this.constraintCache.add(constraint);
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
        arch.addElementsToArchive(this.populationHistory[i]);
        this.populationHistory[i] = this.populationHistory[i].getArchive();
        this.populationHistory[i].SetArchive(null);
        this.makeFitnessCache(false);
    }

    /**
     * This method return the currently selected populations
     *
     * @return the selected populations
     */
    public Population getSelectedPopulations() {
        Population result = new Population();
        for (int i = 0; i < this.populationHistory.length; i++) {
            if (this.use[i]) {
                result.addPopulation(this.populationHistory[i]);
            }
        }
        this.currentProblem.evaluate(result);
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
            for (int i = 0; i < this.populationHistory.length; i++) {
                if (this.populationHistory[i].getArchive() != null) {
                    this.populationHistory[i].addPopulation(this.populationHistory[i].getArchive());
                    this.populationHistory[i].SetArchive(null);
                }
            }
            Population pop = this.optimizer.getPopulation();
            if (pop.getArchive() != null) {
                pop.addPopulation(pop.getArchive());
                pop.SetArchive(null);
            }
            this.currentProblem.evaluate(pop);
        }
        this.fitnessCache = new ArrayList();
        this.objectiveCache = null;
        this.constraintCache = new ArrayList();
        if (this.currentProblem instanceof InterfaceMultiObjectiveDeNovoProblem) {
            this.objectiveCache = new ArrayList();
            tmpObj = ((InterfaceMultiObjectiveDeNovoProblem) this.currentProblem).getProblemObjectives();
        }
        this.paretoFront = new Population();
        for (int i = 0; i < this.populationHistory.length; i++) {
            if (reevaluate) {
                ((AbstractMultiObjectiveOptimizationProblem) this.currentProblem).resetParetoFront();
                this.currentProblem.evaluate(this.populationHistory[i]);
            }
            this.paretoFront.addPopulation(this.populationHistory[i]);
            ArrayList fitness = new ArrayList();
            ArrayList objectives = new ArrayList();
            ArrayList constraint = new ArrayList();
            for (int j = 0; j < this.populationHistory[i].size(); j++) {
                if (tmpObj != null) {
                    double[] tmoF = new double[tmpObj.length];
                    for (int k = 0; k < tmpObj.length; k++) {
                        if (this.populationHistory[i].get(j) == null) {
                            System.out.println("Individual " + i + " == null!");
                        }
                        if (tmpObj[k] == null) {
                            System.out.println("Objective " + k + " == null!");
                        }
                        if (((AbstractEAIndividual) this.populationHistory[i].get(j)).getData(tmpObj[k].getIdentName()) == null) {
                            System.out.println("User Data " + k + " " + tmpObj[k].getIdentName() + " == null!");
                        }
                        tmoF[k] = ((Double) ((AbstractEAIndividual) this.populationHistory[i].get(j)).getData(tmpObj[k].getIdentName())).doubleValue();
                    }
                    objectives.add(tmoF);
                }
                fitness.add(((AbstractEAIndividual) this.populationHistory[i].get(j)).getFitness());
                constraint.add(new Double(((AbstractEAIndividual) this.populationHistory[i].get(j)).getConstraintViolation()));
            }
            if (this.objectiveCache != null) {
                this.objectiveCache.add(objectives);
            }
            this.fitnessCache.add(fitness);
            this.constraintCache.add(constraint);
        }
        ArchivingAllDominating arch = new ArchivingAllDominating();
        arch.addElementsToArchive(this.paretoFront);
        this.paretoFront = this.paretoFront.getArchive();
        this.paretoFront.SetArchive(null);
    }
}
