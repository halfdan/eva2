package eva2.optimization.operators.postprocess;

import eva2.optimization.enums.PostProcessMethod;

/**
 * Parameters for an optional post processing of found solutions. Mainly contains
 * parameters for a hill climbing step, namely the number of evaluations and
 * the sigma condition for clustering. The idea is to optimize the best
 * individual within each cluster.
 * 
 * @author mkron
 *
 */
public interface InterfacePostProcessParams {
    public int getPostProcessSteps();
    public void setPostProcessSteps(int ppSteps);
    public String postProcessStepsTipText();
    
    public boolean isDoPostProcessing();
	public void setDoPostProcessing(boolean postProcess);
	public String doPostProcessingTipText();
	
	public double getPostProcessClusterSigma();
	public void setPostProcessClusterSigma(double postProcessClusterSigma);
	public String postProcessClusterSigmaTipText();
	
	public int getPrintNBest();
	public void setPrintNBest(int nBest);
	public String printNBestTipText();
	
	public void setPPMethod(PostProcessMethod meth);
	public PostProcessMethod getPPMethod();
	public String PPMethodTipText();
	
	public boolean isWithPlot();
	public void setWithPlot(boolean withPlot);
}
