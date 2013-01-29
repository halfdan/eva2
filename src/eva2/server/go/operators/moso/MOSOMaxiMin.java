package eva2.server.go.operators.moso;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 06.08.2004
 * Time: 15:30:52
 * To change this template use File | Settings | File Templates.
 */
public class MOSOMaxiMin implements InterfaceMOSOConverter, java.io.Serializable {

    private int             m_OutputDimension   = 2;
    transient protected     eva2.gui.Plot    m_Plot = null;

    public MOSOMaxiMin() {
    }
    public MOSOMaxiMin(MOSOMaxiMin b) {
        this.m_OutputDimension          = b.m_OutputDimension;
    }
    @Override
    public Object clone() {
        return (Object) new MOSOMaxiMin(this);
    }

    /** This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     * @param pop       The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        AbstractEAIndividual tmpIndy;
        double[][]  fitnessArray, minArray;
        double[]    result, tmpFit, resultFit;
        double      tmpResult;

        tmpIndy         = (AbstractEAIndividual) pop.get(0);
        fitnessArray    = new double[pop.size()][tmpIndy.getFitness().length];
        minArray        = new double[pop.size()][tmpIndy.getFitness().length];
        result          = new double[pop.size()];
        resultFit       = new double[1];
        for (int i = 0; i < pop.size(); i++) {
            fitnessArray[i] = ((AbstractEAIndividual) pop.get(i)).getFitness();
        }
        for (int i = 0; i < fitnessArray.length; i++) {
            result[i] = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < fitnessArray.length; k++)  {
                if (i != k) {
                    tmpResult = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < fitnessArray[k].length; j++) {
                        tmpResult = Math.min(tmpResult, fitnessArray[i][j] - fitnessArray[k][j]);
                    }
                    result[i] = Math.max(result[i], tmpResult);
                }
            }
            // result[i] is now negative and big for good individuals
            //result[i] = Math.exp(this.m_ScalingFactor * result[i]);
            // write the result to the individuals
            tmpIndy     = (AbstractEAIndividual) pop.get(i);
            tmpFit      = tmpIndy.getFitness();
            tmpIndy.putData("MOFitness", tmpFit);
            resultFit   = new double[1];
            resultFit[0] = result[i];
            tmpIndy.setFitness(resultFit);
        }
        ////////////////////////////////////////////////////////////////////////////////////
//        if (false) {
//            this.m_Plot = new eva2.gui.Plot("Debug MaxiMin", "Y1", "Y2");
//            this.m_Plot.setUnconnectedPoint(0, 0, 11);
//            this.m_Plot.setUnconnectedPoint(1.2, 2.0, 11);
//            double[][] trueFitness, moFitness;
//            GraphPointSet   mySet = new GraphPointSet(10, this.m_Plot.getFunctionArea());
//            DPoint          myPoint;
//            double          tmp1, tmp2;
//            Chart2DDPointIconText tmp;
//            trueFitness = new double[pop.size()][];
//            moFitness   = new double[pop.size()][];
//            for (int i = 0; i < pop.size(); i++) {
//                trueFitness[i]  = ((AbstractEAIndividual)pop.get(i)).getFitness();
//                moFitness[i]    = (double[])((AbstractEAIndividual)pop.get(i)).getData("MOFitness");
//            }
//            mySet.setConnectedMode(false);
//            for (int i = 0; i < trueFitness.length; i++) {
//                myPoint = new DPoint(moFitness[i][0], moFitness[i][1]);
//                tmp1 = Math.round(trueFitness[i][0] *100)/100.0;
//                tmp = new Chart2DDPointIconText(""+tmp1);
//                tmp.setIcon(new Chart2DDPointIconCircle());
//                myPoint.setIcon(tmp);
//                mySet.addDPoint(myPoint);
//            }
//        }
    }

    /** This method processes a single individual
     * @param indy      The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[]    resultFit = new double[1];
        double[]    tmpFit;

        tmpFit          = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        System.err.println("The MaxiMin MOSO can not be applied to single individuals! I default to random criterion.");
        resultFit[0]    = tmpFit[RNG.randomInt(0, tmpFit.length)];
        indy.setFitness(resultFit);
    }

    /** This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     * @param dim       Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {
        this.m_OutputDimension = dim;
    }

    /** This method returns a description of the objective
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        return this.getName()+"\n";
    }


/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "MaxiMin Criterium";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method calculate the maximum of minimum distance over all criterias over all individuals.";
    }
//    /** This method allows you to choose the ScalingFactor for
//     * the exp() function
//     * @param goals     The scaling factor.
//     */
//    public void setScalingFactor(double goals) {
//        this.m_ScalingFactor = goals;
//    }
//    public double getScalingFactor() {
//        return this.m_ScalingFactor;
//    }
//    public String scalingFactorTipText() {
//        return "Choose the scaling factor for the exp() function.";
//    }
}