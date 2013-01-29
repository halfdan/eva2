package eva2.server.go.operators.postprocess;

import java.io.Serializable;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.enums.PostProcessMethod;


public class PostProcessParams implements InterfacePostProcessParams, Serializable {
	
	protected int				postProcessSteps = 5000;
	private boolean 			postProcess = false;
	protected double 			postProcessClusterSigma = 0.05;
	protected int				printNBest = 10;
	protected PostProcessMethod	method = PostProcessMethod.nelderMead;
	protected double[]			accuracies = new double[]{0.01};//{0.01, 0.001, 0.0001};
	protected double			accAssumeConv = 1e-8;
	protected int				accMaxEval = -1;

	private boolean 			withPlot = false;
	
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
	
	public PostProcessParams(PostProcessMethod meth, int steps, double clusterSigma, int nBest) {
		reset(meth, steps, clusterSigma, nBest, false);
	}
	
	public PostProcessParams(PostProcessMethod meth, int steps, double clusterSigma, int nBest, boolean doPlot) {
		reset(meth, steps, clusterSigma, nBest, doPlot);
	}
	
	public PostProcessParams reset(PostProcessMethod meth, int steps, double clusterSigma, int nBest, boolean doPlot) {
		method = meth;
		postProcessSteps = steps;
		postProcess = true;
		postProcessClusterSigma = clusterSigma;
		printNBest = nBest;
		withPlot = doPlot;
		return this;
	}
	
	public void hideHideable() {
		setDoPostProcessing(isDoPostProcessing());
	}
	
	/**
	 * @return the postProcess
	 */
    @Override
	public boolean isDoPostProcessing() {
		return postProcess;
	}
	/**
	 * @param postProcess the postProcess to set
	 */
    @Override
	public void setDoPostProcessing(boolean postProcess) {
		this.postProcess = postProcess;
		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessSteps", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "postProcessClusterSigma", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "printNBest", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "PPMethod", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "withPlot", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "accuracies", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "accAssumeConv", postProcess);
		GenericObjectEditor.setShowProperty(this.getClass(), "accMaxEval", postProcess);
	}
    @Override
	public String doPostProcessingTipText() {
		return "Toggle post processing of the solutions.";
	}
	/**
	 * @return the postProcessClusterSigma
	 */
    @Override
	public double getPostProcessClusterSigma() {
		return postProcessClusterSigma;
	}
	/**
	 * @param postProcessClusterSigma the postProcessClusterSigma to set
	 */
    @Override
	public void setPostProcessClusterSigma(double postProcessClusterSigma) {
		this.postProcessClusterSigma = postProcessClusterSigma;
	}
    @Override
	public String postProcessClusterSigmaTipText() {
		return "Set the sigma parameter for clustering during post processing; set to 0 for no clustering.";
	}
	
    @Override
	public String postProcessStepsTipText() {
		return "The number of HC post processing steps in fitness evaluations.";
	}
    @Override
	public int getPostProcessSteps() {
		return postProcessSteps;
	}
    @Override
	public void setPostProcessSteps(int ppSteps) {
		postProcessSteps = ppSteps;
	}
	
    @Override
	public int getPrintNBest() {
		return printNBest;
	}
    @Override
	public void setPrintNBest(int nBest) {
		printNBest = nBest;
	}
    @Override
	public String printNBestTipText() {
		return "Print as many solutions at max; set to -1 to print all";  
	}
	//////////////////////// GUI
	
	public String getName() {
		return "PostProcessing " + (postProcess ? (postProcessSteps + "/" + postProcessClusterSigma) : "off");
	}
	
	public static String globalInfo() {
		return "Combined clustering and local search post-processing of solutions. Additionally, accuracy checks can be performed on the " +
				"returned solutions with different thresholds.";
	}

    @Override
	public PostProcessMethod getPPMethod() {
		return method;
	}
    @Override
	public String PPMethodTipText() {
		return "The method to use for post-processing.";
	}
    @Override
	public void setPPMethod(PostProcessMethod meth) {
		method=meth;
	}

    @Override
	public boolean isWithPlot() {
		return withPlot;
	}
    @Override
	public void setWithPlot(boolean withPlot) {
		this.withPlot = withPlot;
	}

	public double[] getAccuracies() {
		return accuracies;
	}
	public void setAccuracies(double[] accuracies) {
		this.accuracies = accuracies;
	}
	public String accuraciesTipText() {
		return "The accuracy thresholds to be tested";
	}
	
	public double getAccAssumeConv() {
		return accAssumeConv;
	}
	public void setAccAssumeConv(double accAssumeConv) {
		this.accAssumeConv = accAssumeConv;
	}
	public String accAssumeConvTipText() {
		return "The local search refinement is stopped earlier if the fitness changes less than this value"; 
	}

	public int getAccMaxEval() {
		return accMaxEval;
	}
	public void setAccMaxEval(int accMaxEval) {
		this.accMaxEval = accMaxEval;
	}
	public String accMaxEvalTipText() {
		return "The maximal number of evaluations (times dimension) for accuracy check or -1 to use the default.";
	}
	

}
