package eva2.server.go;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.05.2003
 * Time: 13:14:06
 * To change this template use Options | File Templates.
 */
public interface InterfaceGOParameters {

    /** This method returns a global info string
     * @return description
     */
//    public String globalInfo();

    /** This method allows you to serialize the current parameters into a *.ser file
     */
    public void saveInstance();

    /** This method returns the name
     * @return string
     */
    public String getName();

    /** This methods allow you to set and get the Seed for the Random Number Generator.
     * @param x     Long seed.
     */
    public void setSeed(long x);
    public long getSeed();
    public String seedTipText();

    /** This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     * @param term  The new terminator
     */
    public void setTerminator(InterfaceTerminator term);
    public InterfaceTerminator getTerminator();
    public String terminatorTipText();

    /** This method allows you to set the current optimizing algorithm
     * @param optimizer The new optimizing algorithm
     */
    public void setOptimizer(InterfaceOptimizer optimizer);
    public InterfaceOptimizer getOptimizer();
//    public String optimizerTipText();

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void setProblem (InterfaceOptimizationProblem problem);
    public InterfaceOptimizationProblem getProblem ();
    public String problemTipText();

    /** This method will set the output filename
     * @param name
     * TODO invalidate these!
     */
//    public void setOutputFileName (String name);
//    public String getOutputFileName ();
//    public String outputFileNameTipText();
    
    public InterfacePostProcessParams getPostProcessParams();
    public void setPostProcessParams(InterfacePostProcessParams ppp);
    public String postProcessParamsTipText();
    public void setDoPostProcessing(boolean doPP);
//    public int getPostProcessSteps();
//    public void setPostProcessSteps(int ppSteps);
//    public String postProcessStepsTipText();
//    
//    public boolean isPostProcess();
//	public void setPostProcess(boolean postProcess);
//	public String postProcessTipText();
//	
//	public double getPostProcessClusterSigma();
//	public void setPostProcessClusterSigma(double postProcessClusterSigma);
//	public String postProcessClusterSigmaTipText();
}
