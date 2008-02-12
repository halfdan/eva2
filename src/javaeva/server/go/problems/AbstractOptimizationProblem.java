package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.individuals.InterfaceDataTypeInteger;
import javaeva.server.go.individuals.InterfaceDataTypePermutation;
import javaeva.server.go.individuals.InterfaceDataTypeProgram;
import javaeva.server.go.individuals.codings.gp.InterfaceProgram;
import javaeva.server.go.operators.moso.MOSONoConvert;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;


import javax.swing.*;
import java.util.BitSet;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 29.08.2003
 * Time: 13:40:12
 * To change this template use Options | File Templates.
 */
public abstract class AbstractOptimizationProblem implements InterfaceOptimizationProblem, java.io.Serializable {

    protected 	AbstractEAIndividual      m_Template;

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public abstract Object clone();

    /** This method inits the Problem to log multiruns
     */
    public abstract void initProblem();

    /******************** The most important methods ****************************************/

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public abstract void initPopulation(Population population);

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;

        // @todo This is the position to implement a granular
        // @todo paralliziation scheme
        evaluatePopulationStart(population);
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
        evaluatePopulationEnd(population);
    }
    
    /**
     * Empty thunk for implementation in subclasses. This is called right before a population is evaluated.
     * Made public because some steady-state optimizers do not call evaluate(Population).
     *
     * @param population
     */   
    
    public void evaluatePopulationStart(Population population) {
	}

	/**
     * Empty thunk for implementation in subclasses. This is called after a population was evaluated.
     *
     * @param population
     */
    public void evaluatePopulationEnd(Population population) {
    }
    
    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public abstract void evaluate(AbstractEAIndividual individual);

    /** This method should be used to calculate the distance between two
     * individuals. Per default i implemented a phenotypic distance metric.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return The distance.
     */
    public double distanceBetween(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double result = 1;
        if ((indy1 instanceof InterfaceDataTypeBinary) && (indy2 instanceof InterfaceDataTypeBinary)) {
            BitSet  b1, b2;
            b1      = ((InterfaceDataTypeBinary)indy1).getBinaryData();
            b2      = ((InterfaceDataTypeBinary)indy2).getBinaryData();
            if (b1.length() != b2.length()) return Math.max(b1.length(), b2.length());
            result  = b1.length();
            for (int i = 0; i < b1.length(); i++) {
                if (b1.get(i) == b2.get(i)) result--;
            }
            result = result/((double)b1.length());
        }
        if ((indy1 instanceof InterfaceDataTypeInteger) && (indy2 instanceof InterfaceDataTypeInteger)) {
            int[]   b1, b2;
            int[][] range;
            int     max = 0;
            b1      = ((InterfaceDataTypeInteger)indy1).getIntegerData();
            b2      = ((InterfaceDataTypeInteger)indy2).getIntegerData();
            if (b1.length != b2.length) return Math.max(b1.length, b2.length);
            range   = ((InterfaceDataTypeInteger)indy2).getIntRange();
            result  = 0;
            for (int i = 0; i < b1.length; i++) {
                result += Math.abs(b1[i]-b2[i]);
                max    += range[i][1]-range[i][0];
            }
            result = result/((double)max);
        }
        if ((indy1 instanceof InterfaceDataTypeDouble) && (indy2 instanceof InterfaceDataTypeDouble)) {
            double[]   b1, b2;
            double[][] range;
            double     max = 0;
            b1      = ((InterfaceDataTypeDouble)indy1).getDoubleData();
            b2      = ((InterfaceDataTypeDouble)indy2).getDoubleData();
            if (b1.length != b2.length) return Math.max(b1.length, b2.length);
            result  = 0;
            range   = ((InterfaceDataTypeDouble)indy1).getDoubleRange();
            for (int i = 0; i < (Math.min(b1.length, b2.length)); i++) {
                result += Math.abs(b1[i]-b2[i]);
                max    += range[i][1]-range[i][0];
            }
            result = result/max;
        }
        if ((indy1 instanceof InterfaceDataTypePermutation) && (indy2 instanceof InterfaceDataTypePermutation)) {
            int[]   b1, b2;
            b1      = ((InterfaceDataTypePermutation)indy1).getPermutationData()[0];
            b2      = ((InterfaceDataTypePermutation)indy2).getPermutationData()[0];
            if (b1.length != b2.length) return Math.max(b1.length, b2.length);
            result  = b1.length;
            int     tmp;
            for (int i = 0; i < b1.length; i++) {
                tmp = b1.length;
                for (int j = 0; j < b1.length; j++) {
                    if (b1[j] != b2[(j+i)%b1.length]) tmp++;
                }
                result = Math.min(result, tmp);
            }
            result = result/((double)b1.length);
        }
        return result;
    }

    /******************** Some output methods *******************************************/

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suiteable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
        StringBuffer sb = new StringBuffer("Individual:\n");
        if (individual instanceof InterfaceDataTypeBinary) {
            sb.append("Binary data     : {");
            BitSet b = ((InterfaceDataTypeBinary)individual).getBinaryData();
            for (int i = 0; i < b.length(); i++) {
                if (b.get(i)) sb.append("1");
                else sb.append("0");
            }
            sb.append("}\n");
        }
        if (individual instanceof InterfaceDataTypeInteger) {
            sb.append("Integer data    : {");
            int[] b = ((InterfaceDataTypeInteger)individual).getIntegerData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}\n");
        }
        if (individual instanceof InterfaceDataTypeDouble) {
            sb.append("Double data     : {");
            double[] b = ((InterfaceDataTypeDouble)individual).getDoubleData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}\n");
        }
        if (individual instanceof InterfaceDataTypePermutation) {
            sb.append("Permutation data: {");
            int[] b = ((InterfaceDataTypePermutation)individual).getPermutationData()[0];
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}\n");
        }
        if (individual instanceof InterfaceDataTypeProgram) {
            sb.append("Program data    : ");
            InterfaceProgram[] b = ((InterfaceDataTypeProgram)individual).getProgramData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i].getStringRepresentation());
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}");
        }
        double[]    fitness = individual.getFitness();
        sb.append("Fitness         : {");
        for (int i = 0; i < fitness.length; i++) {
            sb.append(fitness[i]);
            if ((i+1) < fitness.length) sb.append("; ");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /** This method returns a single line representation of the solution
     * @param individual  The individual
     * @return The string
     */
    public String getSolutionDataFor(AbstractEAIndividual individual) {
        StringBuffer sb = new StringBuffer("");
        if (individual instanceof InterfaceDataTypeBinary) {
            sb.append("{");
            BitSet b = ((InterfaceDataTypeBinary)individual).getBinaryData();
            for (int i = 0; i < b.length(); i++) {
                if (b.get(i)) sb.append("1");
                else sb.append("0");
            }
            sb.append("}");
        }
        if (individual instanceof InterfaceDataTypeInteger) {
            sb.append("{");
            int[] b = ((InterfaceDataTypeInteger)individual).getIntegerData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}");
        }
        if (individual instanceof InterfaceDataTypeDouble) {
            sb.append("{");
            double[] b = ((InterfaceDataTypeDouble)individual).getDoubleData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}");
        }
        if (individual instanceof InterfaceDataTypePermutation) {
            sb.append("{");
            int[] b = ((InterfaceDataTypePermutation)individual).getPermutationData()[0];
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}");
        }
        if (individual instanceof InterfaceDataTypeProgram) {
            sb.append("{");
            InterfaceProgram[] b = ((InterfaceDataTypeProgram)individual).getProgramData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i].getStringRepresentation());
                if ((i+1) < b.length) sb.append("; ");
            }
            sb.append("}");
        }
        return sb.toString();
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentation() {
        return "AbstractOptimizationProblem: programmer failed to give further details";
    }

    /** This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     * @param pop   The population that is to be refined.
     * @return Double value
     */
    public Double getDoublePlotValue(Population pop) {
        return new Double(pop.getBestEAIndividual().getFitness(0));
    }

    /** This method returns the header for the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringHeader(Population pop) {
        return "Solution";
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(Population pop) {
        return this.getSolutionDataFor(pop.getBestEAIndividual());
    }

    /** This method allows you to request a graphical represenation for a given
     * individual.
     * @return JComponent
     */
    public JComponent drawIndividual(AbstractEAIndividual indy) {
        JPanel      result  = new JPanel();
        result.setLayout(new BorderLayout());
        JTextArea   area    = new JTextArea();
        JScrollPane scroll  = new JScrollPane(area);
        area.setText("Best Solution:\n"+this.getSolutionRepresentationFor(indy));
        area.setEditable(false);
        result.add(scroll, BorderLayout.CENTER);
        return result;
    }

    /** This method will report whether or not this optimization problem is truly
     * multi-objective
     * @return True if multi-objective, else false.
     */
    public boolean isMultiObjective() {
        if (this instanceof AbstractMultiObjectiveOptimizationProblem) {
            if (((AbstractMultiObjectiveOptimizationProblem)this).getMOSOConverter() instanceof MOSONoConvert) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * TODO
     * @param opt
     */
    public void informAboutOptimizer(InterfaceOptimizer opt) {
    	
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "AbstractOptimizationProblem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The programmer failed to give further details.";
    }
}
