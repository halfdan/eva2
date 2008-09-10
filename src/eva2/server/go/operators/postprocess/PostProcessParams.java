package eva2.server.go.operators.postprocess;

import java.io.Serializable;

import eva2.gui.GenericObjectEditor;


public class PostProcessParams implements InterfacePostProcessParams, Serializable {
	
	protected int				postProcessSteps = 5000;
	private boolean 			postProcess = false;
	protected double 			postProcessClusterSigma = 0.05;
	protected int				printNBest = 10;
	
	public PostProcessParams() {
		postProcessSteps = 5000;
		postProcess = false;
		postProcessClusterSigma = 0.05;
		printNBest = 10;
	}
	
	public PostProcessParams(boolean doPP) {
		postProcessSteps = 5000;
		postProcess = doPP;
		postProcessClusterSigma = 0.05;
	}
	
	public PostProcessParams(int steps, double clusterSigma) {
		this(steps, clusterSigma, 10);
	}
	
	public PostProcessParams(int steps, double clusterSigma, int nBest) {
		postProcessSteps = steps;
		postProcess = true;
		postProcessClusterSigma = clusterSigma;
		printNBest = nBest;
	}
	
	public void hideHideable() {
		setDoPostProcessing(isDoPostProcessing());
	}
	
	/**
	 * @return the postProcess
	 */
	public boolean isDoPostProcessing() {
		return postProcess;
	}
	/**
	 * @param postProcess the postProcess to set
	 */
	public void setDoPostProcessing(boolean postProcess) {
		this.postProcess = postProcess;
		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessSteps", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessClusterSigma", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "printNBest", postProcess);
	}
	public String doPostProcessingTipText() {
		return "Toggle post processing of the solutions.";
	}
	/**
	 * @return the postProcessClusterSigma
	 */
	public double getPostProcessClusterSigma() {
		return postProcessClusterSigma;
	}
	/**
	 * @param postProcessClusterSigma the postProcessClusterSigma to set
	 */
	public void setPostProcessClusterSigma(double postProcessClusterSigma) {
		this.postProcessClusterSigma = postProcessClusterSigma;
	}
	public String postProcessClusterSigmaTipText() {
		return "Set the sigma parameter for clustering during post processing; set to 0 for no clustering.";
	}
	
	public String postProcessStepsTipText() {
		return "The number of HC post processing steps in fitness evaluations.";
	}
	public int getPostProcessSteps() {
		return postProcessSteps;
	}
	public void setPostProcessSteps(int ppSteps) {
		postProcessSteps = ppSteps;
	}
	
	public int getPrintNBest() {
		return printNBest;
	}
	public void setPrintNBest(int nBest) {
		printNBest = nBest;
	}
	public String printNBestTipText() {
		return "Print as many solutions at max; set to -1 to print all";  
	}
	//////////////////////// GUI
	
	public String getName() {
		return "PostProcessing " + (postProcess ? (postProcessSteps + "/" + postProcessClusterSigma) : "off");
	}
	
	public String globalInfo() {
		return "Combined clustering and hill-climbing for post-processing of solutions.";
	}
}
