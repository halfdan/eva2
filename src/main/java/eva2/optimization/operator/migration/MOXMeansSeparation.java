package eva2.optimization.operator.migration;


import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.operator.cluster.ClusteringXMeans;
import eva2.optimization.operator.constraint.ConstBelongsToDifferentClass;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.chart2d.DPointIconCircle;
import eva2.tools.chart2d.DPointIconText;
import eva2.tools.chart2d.DPoint;
import eva2.util.annotation.Description;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This method implements the clustering based subdivision
 * scheme suited to identify uni- and multi-modal search spaces
 * under development and currently defunct.
 */
@Description("This is migration scheme, which implements a clustering based partitioning.")
public class MOXMeansSeparation implements InterfaceMigration, java.io.Serializable {

    public boolean debug = false;
    private ClusteringXMeans xMeans = new ClusteringXMeans();
    private ArchivingNSGAII NSGAII = new ArchivingNSGAII();
    private boolean useConstraints = true;
    private InterfaceSelection selection = new SelectRandom();

    public MOXMeansSeparation() {

    }

    public MOXMeansSeparation(MOXMeansSeparation b) {
        this.debug = b.debug;
        this.useConstraints = b.useConstraints;
        if (b.xMeans != null) {
            this.xMeans = (ClusteringXMeans) b.xMeans.clone();
        }
        if (b.NSGAII != null) {
            this.NSGAII = (ArchivingNSGAII) b.NSGAII.clone();
        }
        if (b.selection != null) {
            this.selection = (InterfaceSelection) b.selection.clone();
        }
    }

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new MOXMeansSeparation(this);
    }

    /**
     * Typically i'll need some initialization method for
     * every bit of code i write....
     */
    @Override
    public void initializeMigration(InterfaceOptimizer[] islands) {
        // pff at a later stage i could initialize a topology here
    }

    /**
     * The migrate method can be called asychnronously or
     * sychronously. Basically it allows migration of individuals
     * between multiple EA islands and since there are so many
     * different possible strategies i've introduced this
     * interface which is mostlikely subject to numerous changes..
     * Note: Since i use the RMIRemoteThreadProxy everything done
     * to the islands will use the serialization method, so if
     * you call getPopulation() on an island it is not a reference
     * to the population but a serialized copy of the population!!
     */
    @Override
    public void migrate(InterfaceOptimizer[] islands) {
        Population[] oldIPOP = new Population[islands.length];
        Population[] newIPOP = new Population[islands.length];
        Population collector = new Population(), memory;
        AbstractEAIndividual indy;

        // collect the populations
        for (int i = 0; i < islands.length; i++) {
            oldIPOP[i] = islands[i].getPopulation();
            if (this.debug) {
                System.out.println("Got population from " + i + " of size " + oldIPOP[i].size());
            }
            collector.addPopulation((Population) oldIPOP[i].clone());
            //if (oldIPOP[i].getArchive() != null) collector.addPopulation((Population)oldIPOP[i].getArchive().clone());
            newIPOP[i] = new Population();
        }
        memory = (Population) collector.clone();

//        if (this.debug) {
//            // let's see how they arrive here
//            Plot        plot;
//            double[]    tmpD = new double[2];
//            tmpD[0] = 0;
//            tmpD[1] = 0;
//            plot = new eva2.gui.plot.Plot("Debugging Clustering Separation", "Y1", "Y2", tmpD, tmpD);
//            GraphPointSet           mySet;
//            DPoint                  myPoint;
//            DPointIconText   tmp;
//            for (int i = 0; i < oldIPOP.length; i++) {
//                mySet = new GraphPointSet(10+1, plot.getFunctionArea());
//                mySet.setConnectedMode(false);
//                for (int j = 0; j < oldIPOP[i].size(); j++) {
//                    indy = (AbstractEAIndividual)oldIPOP[i].get(j);
//                    myPoint = new DPoint(indy.getFitness()[0], indy.getFitness()[1]);
//                    tmp = new DPointIconText(""+i);
//                    if (i % 2 == 0) tmp.setIcon(new DPointIconCircle());
//                    myPoint.setIcon(tmp);
//                    mySet.addDPoint(myPoint);
//                }
//            }
//        }

        // Now lets cluster this stuff
        Population[] archives = this.NSGAII.getNonDominatedSortedFronts(collector);
        Population toCluster = new Population();
        int currentFront = 0;
        toCluster.addPopulation(archives[currentFront]);
        while (toCluster.size() < islands.length) {
            currentFront++;
            toCluster.addPopulation(archives[currentFront]);
        }

        // first set the K to the K-Means
        this.xMeans.setMaxK(islands.length);
        this.xMeans.cluster(toCluster, (Population) null);
        double[][] c = this.xMeans.getC();
        //@todo Hier muss ich mal denk machen und weniger click...
        newIPOP = this.xMeans.cluster(collector, c);
        for (int i = 0; i < islands.length; i++) {
            islands[i].getPopulation().clear();
            islands[i].getPopulation().setTargetSize(0);
        }
        if (this.debug) {
            Plot plot;
            double[] tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            plot = new Plot("Debugging Clustering Separation", "Y1", "Y2", tmpD, tmpD);
            GraphPointSet mySet;
            DPoint myPoint;
            DPointIconText tmp;
            for (int i = 0; i < newIPOP.length; i++) {
                mySet = new GraphPointSet(10 + 1, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                for (int j = 0; j < newIPOP[i].size(); j++) {
                    indy = newIPOP[i].get(j);
                    myPoint = new DPoint(indy.getFitness()[0], indy.getFitness()[1]);
                    tmp = new DPointIconText("" + i);
                    //if (i % 2 == 0) tmp.setIcon(new DPointIconCircle());
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            mySet = new GraphPointSet(10 + 2, plot.getFunctionArea());
            mySet.setConnectedMode(false);
            for (int i = 0; i < c.length; i++) {
                myPoint = new DPoint(c[i][0], c[i][1]);
                tmp = new DPointIconText("" + i);
                tmp.setIcon(new DPointIconCircle());
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }
        }

        if ((this.useConstraints) && (c.length > 1)) {
            // i should set the constraints to the optimizers
            InterfaceOptimizationProblem prob;

            for (int i = 0; (i < islands.length) && (i < c.length); i++) {
                prob = islands[i].getProblem();
                if (prob instanceof AbstractMultiObjectiveOptimizationProblem) {
                    // set the boundaries to perform the constrained
                    // domain principle introduced by Deb et al.
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.clear();
                    double[] myClass = c[i];
                    double[][] myOtherClass = new double[c.length - 1][];
                    int index = 0;
                    for (int j = 0; j < myOtherClass.length; j++) {
                        if (index == i) {
                            index++;
                        }
                        myOtherClass[j] = c[index];
                        index++;
                    }
                    ConstBelongsToDifferentClass b = new ConstBelongsToDifferentClass(myClass, myOtherClass, this.xMeans.getUseSearchSpace());
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.add(b);
//                    if (this.debug) {
//                        String out = "";
//                        out += i+ ". MyClass: {";
//                        for (int j = 0; j < myClass.length; j++) out += myClass[j]+"; ";
//                        out += "}\n Other Classes:";
//                        for (int k = 0; k < myOtherClass.length; k++) {
//                            out += "\n {";
//                            for (int l = 0; l < myOtherClass[k].length; l++) {
//                                out += myOtherClass[k][l]+"; ";
//                            }
//                            out += "}";
//                        }
//                        if (this.xMeans.getUsePhenotype()) out += "\n Using phenotype.";
//                        else out += "\n Using objective space.";
//                        System.out.println(""+out);
//                    }
                    islands[i].setProblem(prob);
                }
            }
        }

        // set the population back to the islands
        //System.out.println("islands.length = "+islands.length + " /newIPOP.length = "+newIPOP.length);
        for (int i = 0; (i < islands.length) && (i < newIPOP.length); i++) {
            oldIPOP[i].clear();
            oldIPOP[i].addPopulation(newIPOP[i]);
            // todo remove this for nice pictures
            if (!oldIPOP[i].targetSizeReached()) {
                oldIPOP[i].addPopulation(this.selection.selectFrom(memory, oldIPOP[i].getFreeSlots()));
            }
            if (this.debug) {
                System.out.println("Setting " + i + " to population size " + oldIPOP[i].size());
            }
            islands[i].setPopulation(oldIPOP[i]);
            islands[i].getPopulation().setTargetSize(oldIPOP[i].size());
        }
    }

//    public static void main(String[] args) {
//        MOXMeansSeparation cluster = new MOXMeansSeparation();
//        cluster.setUseConstraints(false);
//
////        double[] tmpDir = new double[3];
////        double[] tmpVec = new double[3];
////        double[] result;
////        double   alpha = 45;
////        tmpDir[0] = 0;
////        tmpDir[1] = 0;
////        tmpDir[2] = 1;
////        tmpVec[0] = 0;
////        tmpVec[1] = 1;
////        tmpVec[2] = 0;
////        for (int i = 0; i < 5; i++) {
////            result = cone.rot(tmpVec, tmpDir, Math.toRadians(i*alpha));
////            System.out.println("Rotating ("+tmpVec[0]+", "+tmpVec[1]+", "+tmpVec[2]+") by " +
////                    (i*alpha)+ " degree around ("+tmpDir[0]+", "+tmpDir[1]+", "+tmpDir[2]+")");
////            System.out.println("Results in ("+result[0]+", "+result[1]+", "+result[2]+")");
////        }
//
//        TFPortfolioSelectionProblem problem = new TFPortfolioSelectionProblem();
//        problem.getOptimizationTargets().removeTarget(0);
//        problem.getOptimizationTargets().removeTarget(0);
//        OptTargetPortfolioReturn ret = new OptTargetPortfolioReturn();
//        ret.setNormalizeTarget(true);
//        problem.getOptimizationTargets().addTarget(ret);
//        OptTargetPortfolioRisk ris = new OptTargetPortfolioRisk();
//        ris.setNormalizeTarget(true);
//        problem.getOptimizationTargets().addTarget(ris);
//        OptTargetPortfolioDuration dur = new OptTargetPortfolioDuration();
//        dur.setNormalizeTarget(true);
//        problem.getOptimizationTargets().addTarget(dur);
//
//        int n = 4;
//        MultiObjectiveEA[] islands = new MultiObjectiveEA[n];
//        for (int i = 0; i < islands.length; i++) {
//            islands[i] = new MultiObjectiveEA();
//            islands[i].SetProblem(problem);
//            islands[i].initialize();
//        }
//
//        cluster.xMeans.setUseSearchSpace(true);
//
//        for (int i = 0; i < 20; i++) {
//            for (int j = 0; j < islands.length; j++) {
//                islands[j].optimize();
//            }
//            if (i % 4 == 0) cluster.migrate(islands);
//        }
//
//
//        cluster.migrate(islands);
//
//        BufferedWriter outfile = null;
//        double[] fitness;
//        ArchivingAllDominating arch = new ArchivingAllDominating();
//
//        for (int i = 0; i < islands.length; i++) {
//            try {
//                outfile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOCluster"+i+".txt")));
//            } catch (FileNotFoundException e) {
//                System.out.println("Could not open output file! Filename: ");
//            }
//            //cone.writeToFile(outfile, " x\t y\t z");
//            islands[i].getPopulation().SetArchive(new Population());
//            arch.addElementsToArchive(islands[i].getPopulation());
//            for (int j = 0; j < islands[i].getPopulation().getArchive().size(); j++) {
//                fitness = ((AbstractEAIndividual)islands[i].getPopulation().getArchive().get(j)).getFitness();
//                cluster.writeToFile(outfile, ""+fitness[0]+"\t"+fitness[1]+"\t"+fitness[2]);
//            }
//            try{
//                outfile.close();
//            } catch (IOException e) {
//                System.out.println("Couldn'T even close the stuff");
//            }
//        }
//        try {
//            outfile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOClusterCenters.txt")));
//        } catch (FileNotFoundException e) {
//            System.out.println("Could not open output file! Filename: ");
//        }
//
//        // finally write the cluster centroids
//        double[][] centroids = cluster.getXMeans().getC();
//        for (int i = 0; i < centroids.length; i++) {
//            cluster.writeToFile(outfile, ""+centroids[i][0]+"\t"+centroids[i][1]+"\t"+centroids[i][2]);
//        }
//        try{
//            outfile.close();
//        } catch (IOException e) {
//            System.out.println("Couldn't even close the stuff");
//        }
//        Population pop = new Population();
//        for (int i = 0; i < islands.length; i++) {
//            pop.addPopulation(islands[i].getPopulation());
//            pop.addPopulation(islands[i].getPopulation().getArchive());
//        }
//        System.out.println("S-Metric: "+problem.getMetric().calculateMetricOn(pop, problem));
//        System.exit(0);
//    }

    /**
     * This method writes Data to file.
     *
     * @param line The line that is to be added to the file
     */
    private void writeToFile(BufferedWriter out, String line) {
        String write = line + "\n";
        write.replaceAll(",", ".");
        if (out == null) {
            return;
        }
        try {
            out.write(write, 0, write.length());
            out.flush();
        } catch (IOException e) {
            System.out.println("Problems writing to output file!");
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "MOClusteringSeparation";
    }

    /**
     * This method allows you to set/get the clustering algorithm.
     *
     * @return The clustering algorithm method
     */
    public ClusteringXMeans getXMeans() {
        return this.xMeans;
    }

    public void setXMeans(ClusteringXMeans b) {
        this.xMeans = b;
    }

    public String xMeansTipText() {
        return "Parameterize the clustering algorithm.";
    }

    /**
     * This method allows you to toogle the use of constraints,
     * which enable the algorithm to limit each island to a
     * specific area of the search space.
     *
     * @return The modus of constraints.
     */
    public boolean getUseConstraints() {
        return this.useConstraints;
    }

    public void setUseConstraints(boolean b) {
        this.useConstraints = b;
    }

    public String useConstraintsTipText() {
        return "If activated constraints are used to limit each island to a local area.";
    }
}