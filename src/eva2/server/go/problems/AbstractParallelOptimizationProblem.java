package eva2.server.go.problems;

import eva2.gui.PropertyRemoteServers;
import eva2.tools.jproxy.RMIProxyRemoteThread;

/**
 * This class is under construction. 
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 17.12.2004
 * Time: 14:43:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractParallelOptimizationProblem extends AbstractOptimizationProblem {

    private PropertyRemoteServers                   m_Servers           = new PropertyRemoteServers();
    private int                                     m_LocalCPUs         = 4;
    private boolean                                 m_Parallelize       = false;
    private AbstractOptimizationProblem[]           m_Slaves;

    @Override
    public void initProblem() {
        if (this.m_Parallelize) {
            // this is running on remote maschines
            String[] nodesList = this.m_Servers.getCheckedServerNodes();
            if ((nodesList == null) || (nodesList.length == 0)) {
                return;
            }
            this.m_Slaves = new AbstractOptimizationProblem[nodesList.length];
            for (int i = 0; i < nodesList.length; i++) {
                this.m_Slaves[i] = (AbstractOptimizationProblem) RMIProxyRemoteThread.newInstance(this, nodesList[i]);
            }
        } else {
            // this is running on the local machine
            this.m_Slaves = new AbstractOptimizationProblem[this.m_LocalCPUs];
            for (int i = 0; i < this.m_LocalCPUs; i++) {
                this.m_Slaves[i] = (AbstractOptimizationProblem) this.clone();
            }
        }
    }

//    /** This method evaluates a given population and set the fitness values
//     * accordingly
//     * @param population    The population that is to be evaluated.
//     */
//    public void evaluate(Population population) {
//        AbstractEAIndividual    tmpIndy;
//        int                     curIndex = 0;
//
//        while (curIndex < population.size()) {
//            what the ?? is this??
//        }
//
//    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a framework for parallelizing expensive optimization problems.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "Parallel Optimization Problem";
    }

    /** This method allows you to toggle between a truly parallel
     * and a serial implementation.
     * @return The current optimzation mode
     */
    public boolean getParallelize() {
        return this.m_Parallelize;
    }
    public void setParallelize(boolean b){
        this.m_Parallelize = b;
    }
    public String parallelizeTipText() {
        return "Toggle between parallel and serial implementation.";
    }

    /** This method allows you to managae the available servers
     * @return The current servers
     */
    public PropertyRemoteServers getServers() {
        return this.m_Servers;
    }
    public void setServers(PropertyRemoteServers b){
        this.m_Servers = b;
    }
    public String serversTipText() {
        return "Choose and manage the servers (only active in parallelized mode).";
    }

    /** This method allows you to set the number of processors in local mode
     * @param n     Number of processors.
     */
    public void setNumberLocalCPUs(int n) {
        this.m_LocalCPUs = n;
    }
    public int getNumberLocalCPUs() {
        return this.m_LocalCPUs;
    }
    public String numberLocalCPUsTipText() {
        return "Set the number of local CPUS (only active in non-parallelized mode).";
    }
}
