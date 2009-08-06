package eva2.server.go.tools;


import java.io.*;

import eva2.gui.GraphPointSet;
import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.archiving.ArchivingAllDominating;
import eva2.server.go.populations.Population;
import eva2.tools.chart2d.DPoint;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 12.08.2005
 * Time: 11:26:22
 * To change this template use File | Settings | File Templates.
 */
public class ImpactOfDimensionOnMOEAs {

    public static void main(String[] args) {
        ImpactOfDimensionOnMOEAs ikel = new ImpactOfDimensionOnMOEAs();
        int         maxDim      = 21;
        int         multiRuns   = 25;
        int         popSize     = 100;
        int         numberofVariables = 30;
        double      mean;
        double[][]  log;
        log         = new double[maxDim][5];
        Plot        mPlot;
        GraphPointSet mySet;
        DPoint      myPoint;
        Population  pop;
        double[] tmpD = new double[2];
        tmpD[0] = 1;
        tmpD[1] = 1;
        mPlot = new eva2.gui.Plot("ImpactOfDimensionOnMOEAs", "ProblemDimensin", "No. of Pareto-opt solutions", true);
        for (int l = 0; l < 5; l++) {
            popSize = 100 + (l*100);
            mySet = new GraphPointSet(l, mPlot.getFunctionArea());
            mySet.setConnectedMode(true);
            myPoint = new DPoint(0, 0);
            mySet.addDPoint(myPoint);
            myPoint = new DPoint(1, 1/(double)popSize);
            mySet.addDPoint(myPoint);
            log[0][l] = 0;
            log[1][l] = 1/(double)popSize;
            for (int i = 2; i < maxDim; i++) {
                mean = 0;
                for (int j = 0; j < multiRuns; j++) {
                    pop = new Population();
                    ikel.initPopulation(pop, popSize, numberofVariables);
                    ikel.evaluatePopulation(pop, i);
                    mean += ikel.numberOfParetoOptimalSolutions(pop)/(double)pop.size();
                }
                mean = mean/(double)multiRuns;
                myPoint = new DPoint(i, mean);
                log[i][l] = mean;
                mySet.addDPoint(myPoint);
            }
        }
        ikel.save(log);
    }

    public void save(double[][] log) {
        String tmpS;
        BufferedWriter m_OutputFile = null;
        try {
            m_OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("Impact_Of_Dimension_On_MOEAs.dat")));
        } catch (FileNotFoundException e) {
            System.out.println("Could not open output file! Filename: Impact_Of_Dimension_On_MOEAs.dat");
        }
        try {
            tmpS = "Dim;P100;P200;P300;P400;P500\n";
            m_OutputFile.write(tmpS);
            for (int i = 0; i < log.length; i++) {
                tmpS = ""+i+";";
                for (int j = 0; j < log[i].length; j++) {
                    tmpS += log[i][j];
                    if (j < log[i].length-1) tmpS += ";";
                }
                tmpS += "\n";
                m_OutputFile.write(tmpS);
            }
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
        try {
            m_OutputFile.close();
        } catch (IOException e) {

        }
    }

    public void initPopulation(Population pop, int popSize, int numberOfVariables) {
        AbstractEAIndividual tmpIndy, template;
        pop.clear();
        template = new ESIndividualDoubleData();
        ((InterfaceDataTypeDouble)template).setDoubleDataLength(numberOfVariables);
        for (int i = 0; i < popSize; i++) {
            tmpIndy = (AbstractEAIndividual)template.clone();
            tmpIndy.init(null);
            pop.add(tmpIndy);
        }
    }

    public void evaluatePopulation(Population pop, int objectives) {
        double[]    fitness;
        double[]    x;

        for (int i = 0; i < pop.size(); i++) {
            x = ((InterfaceDataTypeDouble)pop.get(i)).getDoubleData();
            fitness = new double[objectives];
            for (int j = 0; j < objectives; j++) {
                fitness[j] = 1;
                for (int k = 0; k < x.length; k++) {
                    if (k != j) {
                        fitness[j] += x[k];
                    }
                }
                if (j < x.length) fitness[j] =1/((double)fitness[j]) + x[j];
                else  fitness[j] =1/((double)fitness[j]) + x[j%objectives] + x[(j+1)%objectives];
            }
            ((AbstractEAIndividual)pop.get(i)).SetFitness(fitness);
        }
    }

    public int numberOfParetoOptimalSolutions(Population pop) {
        ArchivingAllDominating arch = new ArchivingAllDominating();
        pop.SetArchive(new Population());
        arch.addElementsToArchive(pop);
        return pop.getArchive().size();
    }
}
