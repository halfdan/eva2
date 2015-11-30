package eva2.gui.plot;

public interface PlotInterface {
    void setConnectedPoint(double x, double y, int GraphLabel);

    /**
     * Add two graphs to form an average graph
     *
     * @param g1       graph object one
     * @param g2       graph object two
     * @param forceAdd if the graph mismatch in point counts, try to add them anyway in a useful manner.
     */
    void addGraph(int g1, int g2, boolean forceAdd);

    void setUnconnectedPoint(double x, double y, int GraphLabel);

    void clearAll();

    void clearGraph(int GraphNumber);

    void setInfoString(int GraphLabel, String Info, float stroke);

    void jump();

    String getName();

    int getPointCount(int graphLabel);

    //  public FunctionArea getFunctionArea(); // this is bad for RMI
    boolean isValid();

    void init();
}

