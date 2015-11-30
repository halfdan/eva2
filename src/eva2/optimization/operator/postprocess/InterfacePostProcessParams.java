package eva2.optimization.operator.postprocess;

import eva2.optimization.enums.PostProcessMethod;

/**
 * Parameters for an optional post processing of found solutions. Mainly contains
 * parameters for a hill climbing step, namely the number of evaluations and
 * the sigma condition for clustering. The idea is to optimize the best
 * individual within each cluster.
 *
 * @author mkron
 */
public interface InterfacePostProcessParams {
    int getPostProcessSteps();

    void setPostProcessSteps(int ppSteps);

    String postProcessStepsTipText();

    boolean isDoPostProcessing();

    void setDoPostProcessing(boolean postProcess);

    String doPostProcessingTipText();

    double getPostProcessClusterSigma();

    void setPostProcessClusterSigma(double postProcessClusterSigma);

    String postProcessClusterSigmaTipText();

    int getPrintNBest();

    void setPrintNBest(int nBest);

    String printNBestTipText();

    void setPPMethod(PostProcessMethod meth);

    PostProcessMethod getPPMethod();

    String PPMethodTipText();

    boolean isWithPlot();

    void setWithPlot(boolean withPlot);
}
