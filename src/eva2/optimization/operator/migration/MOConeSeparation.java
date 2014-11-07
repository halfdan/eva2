package eva2.optimization.operator.migration;

import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.operator.archiving.InterfaceArchiving;
import eva2.optimization.operator.constraint.ConstObjectivesInEqualityBiggerThanLinear;
import eva2.optimization.operator.constraint.ConstObjectivesInEqualityBiggerThanSurface;
import eva2.optimization.operator.constraint.ConstObjectivesInEqualityLesserThanLinear;
import eva2.optimization.operator.constraint.ConstObjectivesInEqualitySmallerThanSurface;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.chart2d.Chart2DDPointIconCircle;
import eva2.tools.chart2d.Chart2DDPointIconText;
import eva2.tools.chart2d.DPoint;
import eva2.util.annotation.Description;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This method implements the cone separation subdivision
 * scheme, this method rearranges the populations and may
 * impose area constraints on the subpopulations.
 */
@Description("This is migration scheme, which implements a cone separation based partitioning.")
public class MOConeSeparation implements InterfaceMigration, java.io.Serializable {

    public boolean debug = false;
    private boolean useAllToDetermineR = false;  // since i'm only interessted in the pareto-front this should be set to false!!
    private boolean useConstraints = true;
    private InterfaceSelection selection = new SelectRandom();
    private double[][] bounds3D;

    public MOConeSeparation() {

    }

    public MOConeSeparation(MOConeSeparation b) {
        this.debug = b.debug;
        this.useConstraints = b.useConstraints;
        this.useAllToDetermineR = b.useAllToDetermineR;
        if (b.selection != null) {
            this.selection = (InterfaceSelection) b.selection.clone();
        }
    }

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new MOConeSeparation(this);
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
        InterfaceArchiving allDom = new ArchivingAllDominating();

        // collect the populations
        for (int i = 0; i < islands.length; i++) {
            oldIPOP[i] = islands[i].getPopulation();
            if (this.debug) {
                System.out.println("Got population from " + i + " of size " + oldIPOP[i].size());
            }
            collector.addPopulation((Population) oldIPOP[i].clone());
            newIPOP[i] = new Population();
        }
        memory = (Population) collector.clone();

        if (collector.get(0).getFitness().length == 2) {
            this.coneSeparation2D(collector, newIPOP, islands);
        } else {
            if (collector.get(0).getFitness().length == 3) {
                this.coneSeparation3D(collector, newIPOP, islands);
            } else {
                if (collector.get(0).getFitness().length >= 4) {
                    System.out.println("*Pff*");
                }
            }
        }
        // set the population back to the islands
        for (int i = 0; i < islands.length; i++) {
            oldIPOP[i].clear();
            oldIPOP[i].addPopulation(newIPOP[i]);
            // todo remove this for nice pictures
            if (!oldIPOP[i].targetSizeReached()) {
                oldIPOP[i].addPopulation(this.selection.selectFrom(memory, oldIPOP[i].getTargetSize() - oldIPOP[i].size()));
            }
            if (this.debug) {
                System.out.println("Setting island " + i + " to population size " + oldIPOP[i].size());
            }
            allDom.addElementsToArchive(oldIPOP[i]);
            islands[i].setPopulation(oldIPOP[i]);
        }
    }

    /**
     * This method performs cone separation in 2D
     *
     * @param collector This is a aggregated population;
     * @param newIPOP   The new population on the islands after separation
     * @param islands   The optimizer required to get the problem to set the constraints.
     */
    private void coneSeparation2D(Population collector, Population[] newIPOP, InterfaceOptimizer[] islands) {
        AbstractEAIndividual indy;

//        if (this.debug) {
//            // let's see how they arrive here
//            // This shows that the Drecksbeutels
//            // indeed spread out, even within
//            // two generations!!!
//            Plot        plot;
//            double[]    tmpD = new double[2];
//            tmpD[0] = 0;
//            tmpD[1] = 0;
//            plot = new eva2.gui.plot.Plot("Debugging Cone Separation", "Y1", "Y2", tmpD, tmpD);
//            GraphPointSet           mySet;
//            DPoint                  myPoint;
//            Chart2DDPointIconText   tmp;
//            for (int i = 0; i < oldIPOP.length; i++) {
//                mySet = new GraphPointSet(10+1, plot.getFunctionArea());
//                mySet.setConnectedMode(false);
//                for (int j = 0; j < oldIPOP[i].size(); j++) {
//                    indy = (AbstractEAIndividual)oldIPOP[i].get(j);
//                    myPoint = new DPoint(indy.getFitness()[0], indy.getFitness()[1]);
//                    tmp = new Chart2DDPointIconText(""+i);
//                    if (indy.areaConst4ParallelViolated) tmp.setIcon(new Chart2DDPointIconCircle());
//                    myPoint.setIcon(tmp);
//                    mySet.addDPoint(myPoint);
//                }
//            }
//        }

        // now let's find the reference point first just in 2D
        InterfaceArchiving allDom = new ArchivingAllDominating();
        allDom.addElementsToArchive(collector);
        int y1Big = 0, y2Big = 0;
        Population archive = collector.getArchive();
        Population ref;

        if (this.useAllToDetermineR) {
            ref = collector;
        } else {
            ref = archive;
        }

        for (int i = 1; i < ref.size(); i++) {
            if (ref.get(i).getFitness()[0] > ref.get(y1Big).getFitness()[0]) {
                y1Big = i;
            }
            if (ref.get(i).getFitness()[1] > ref.get(y2Big).getFitness()[1]) {
                y2Big = i;
            }
        }
        double[] r = new double[2];
        double alpha = 90.0 / (double) islands.length;
        double[][] boundaries = new double[islands.length - 1][2];
        r[0] = ref.get(y1Big).getFitness()[0];
        r[1] = ref.get(y2Big).getFitness()[1];
        for (int i = 0; i < boundaries.length; i++) {
            boundaries[i][0] = 1 / Math.tan(Math.toRadians(alpha * (i + 1)));
            boundaries[i][1] = r[1] - boundaries[i][0] * r[0];
//            System.out.println("Boundary"+i+" f(x)="+boundaries[i][0]+"*x + "+ boundaries[i][1]);
        }

        collector.SetArchive(new Population());

        // Now i got the cone's let's separate
        for (int i = 0; i < boundaries.length; i++) {
            for (int j = 0; j < collector.size(); j++) {
                indy = collector.get(j);
                if (indy.getFitness()[1] < boundaries[i][0] * indy.getFitness()[0] + boundaries[i][1]) {
                    // this guy belongs to cone i
                    newIPOP[i].add(indy);
                    collector.remove(j);
                    j--;
                }
            }
        }
        // the rest belongs to newIPOP.length-1
        newIPOP[newIPOP.length - 1].addPopulation(collector);

        if (this.debug) {
            Plot plot;
            double[] tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            plot = new Plot("Debugging Cone Separation", "Y1", "Y2", tmpD, tmpD);
            GraphPointSet mySet;
            DPoint myPoint;
            Chart2DDPointIconText tmp;
            mySet = new GraphPointSet(9, plot.getFunctionArea());
            mySet.setConnectedMode(false);
            // now plot the region boundaries
            for (int i = 0; i < 250; i++) {
                myPoint = new DPoint(0, 0);
                mySet.addDPoint(myPoint);
            }
            for (int i = 0; i < newIPOP.length; i++) {
                mySet = new GraphPointSet(10 + i, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                for (int j = 0; j < newIPOP[i].size(); j++) {
                    indy = newIPOP[i].get(j);
                    myPoint = new DPoint(indy.getFitness()[0], indy.getFitness()[1]);
                    tmp = new Chart2DDPointIconText("" + i);
                    if (i % 2 == 0) {
                        tmp.setIcon(new Chart2DDPointIconCircle());
                    }
                    myPoint.setIcon(tmp);
                    mySet.addDPoint(myPoint);
                }
            }
            mySet = new GraphPointSet(10, plot.getFunctionArea());
            mySet.setConnectedMode(false);
            // now plot the region boundaries
            for (int i = 0; i < islands.length - 1; i++) {
                myPoint = new DPoint(r[0], r[1]);
                mySet.addDPoint(myPoint);
                myPoint = new DPoint(r[0], r[1]);
                mySet.addDPoint(myPoint);
                if (boundaries[i][1] > 0) {
                    myPoint = new DPoint(0.0, boundaries[i][1]);
                    mySet.addDPoint(myPoint);
                    myPoint = new DPoint(0.0, boundaries[i][1]);
                    mySet.addDPoint(myPoint);
                } else {
                    myPoint = new DPoint(-boundaries[i][1] / boundaries[i][0], 0);
                    mySet.addDPoint(myPoint);
                    myPoint = new DPoint(-boundaries[i][1] / boundaries[i][0], 0);
                    mySet.addDPoint(myPoint);
                }
            }
        }

        if (this.useConstraints) {
            // i should set the constraints to the optimizers
            InterfaceOptimizationProblem prob;
            for (int i = 0; i < islands.length; i++) {
                prob = islands[i].getProblem();
                if (prob instanceof AbstractMultiObjectiveOptimizationProblem) {
                    // set the boundaries to perform the constrained
                    // domain principle introduced by Deb et al.
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.clear();
                    if (i > 0) {
                        // add the lower boundary
                        ConstObjectivesInEqualityBiggerThanLinear b = new ConstObjectivesInEqualityBiggerThanLinear(boundaries[i - 1][0], boundaries[i - 1][1]);

                        ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.add(b);
                    }
                    if (i < islands.length - 1) {
                        // add the upper boundary
                        ConstObjectivesInEqualityLesserThanLinear b = new ConstObjectivesInEqualityLesserThanLinear(boundaries[i][0], boundaries[i][1]);

                        ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.add(b);
                    }
                    islands[i].setProblem(prob);
                }
            }
        }
    }

    /**
     * This method performs cone separation in 2D
     *
     * @param collector This is a aggregated population;
     * @param newIPOP   The new population on the islands after separation
     * @param islands   The optimizer required to get the problem to set the constraints.
     */
    private void coneSeparation3D(Population collector, Population[] newIPOP, InterfaceOptimizer[] islands) {
        AbstractEAIndividual indy;

        // now let's find the reference point first just in 2D
        InterfaceArchiving allDom = new ArchivingAllDominating();
        allDom.addElementsToArchive(collector);
        int y1Big = 0, y2Big = 0, y3Big = 0;
        Population archive = collector.getArchive();
        Population ref;

        if (this.useAllToDetermineR) {
            ref = collector;
        } else {
            ref = archive;
        }

        for (int i = 1; i < ref.size(); i++) {
            if (ref.get(i).getFitness()[0] > ref.get(y1Big).getFitness()[0]) {
                y1Big = i;
            }
            if (ref.get(i).getFitness()[1] > ref.get(y2Big).getFitness()[1]) {
                y2Big = i;
            }
            if (ref.get(i).getFitness()[2] > ref.get(y3Big).getFitness()[2]) {
                y3Big = i;
            }
        }
        // now build a 3D bounding rule
        double[] distopian = new double[3], zE = new double[3];
        double[][] normals = new double[islands.length][3];
        double angIncr = 360.0 / (double) islands.length;

        distopian[0] = ref.get(y1Big).getFitness()[0];
        distopian[1] = ref.get(y2Big).getFitness()[1];
        distopian[2] = ref.get(y3Big).getFitness()[2];

        zE[0] = 0;
        zE[1] = 0;
        zE[2] = 1;

        double[] firstVec = this.getCrossProduct(distopian, zE);
        firstVec = this.getNormalized(firstVec);
        double[] normDist = this.getNormalized(distopian);

        this.bounds3D = new double[normals.length + 2][3];
        this.bounds3D[0] = distopian;

        for (int i = 0; i < normals.length; i++) {
            normals[i] = this.rotVector(firstVec, normDist, Math.toRadians(i * angIncr));
            this.bounds3D[i + 1] = normals[i];
        }

        // now i got the bounding planes
        double[][] lastBoundingPlane = new double[2][3]; // first double[3] gives a point on the plane, the second gives the normal on the plane
        double[][] curBoundingPlane = new double[2][3];
        double[] fitness;
        InterfaceOptimizationProblem prob;
        lastBoundingPlane[0] = distopian;
        lastBoundingPlane[1] = normals[normals.length - 1];
        curBoundingPlane[0] = distopian;
        curBoundingPlane[1] = normals[0];

        collector.SetArchive(new Population());

        // Now i got the cone's let's separate
        for (int i = 0; i < normals.length; i++) {
            for (int j = 0; j < collector.size(); j++) {
                indy = collector.get(j);
                fitness = indy.getFitness();
                if ((this.getScalarProduct(curBoundingPlane[1], this.getVectorSub(fitness, curBoundingPlane[0])) < 0) &&
                        (this.getScalarProduct(lastBoundingPlane[1], this.getVectorSub(fitness, lastBoundingPlane[0])) >= 0)) {
                    // this guy belongs to cone i
                    newIPOP[i].add(indy);
//                    collector.remove(j);
//                    j--;
                }
            }
            if (this.useConstraints) {
                prob = islands[i].getProblem();
                if (prob instanceof AbstractMultiObjectiveOptimizationProblem) {
                    // set the boundaries to perform the constrained
                    // domain principle introduced by Deb et al.
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.clear();
                    ConstObjectivesInEqualitySmallerThanSurface sts = new ConstObjectivesInEqualitySmallerThanSurface(curBoundingPlane[0], curBoundingPlane[1]);
                    ConstObjectivesInEqualityBiggerThanSurface bts = new ConstObjectivesInEqualityBiggerThanSurface(lastBoundingPlane[0], lastBoundingPlane[1]);
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.add(sts);
                    ((AbstractMultiObjectiveOptimizationProblem) prob).areaConst4Parallelization.add(bts);
                }
                islands[i].setProblem(prob);
//                if (true) {
//                    prob.evaluate(newIPOP[i]);
//                    System.out.println("Invalid Individual in Island "+i+" ("+newIPOP[i].size()+"): ");
//                    for (int j = 0; j < newIPOP[i].size(); j++) {
//                        if(((AbstractEAIndividual)newIPOP[i].get(j)).areaConst4ParallelViolated) {
//                            System.out.print(j+", ");
//                            ((AbstractEAIndividual)newIPOP[i].get(j)).checkAreaConst4Parallelization(((AbstractMultiObjectiveOptimizationProblem)prob).areaConst4Parallelization);
//                        }
//                    }
//                    System.out.println("");
//                }
            }
            lastBoundingPlane[0] = curBoundingPlane[0];
            lastBoundingPlane[1] = curBoundingPlane[1];
            curBoundingPlane[0] = distopian;
            if (i + 1 < normals.length) {
                curBoundingPlane[1] = normals[i + 1];
            }
//            else curBoundingPlane[1]     = normals[0];
        }
//        System.out.println("collector.size() "+ collector.size());
//        // the rest belongs to newIPOP.length-1
//        curBoundingPlane[0]     = distopian;
//        curBoundingPlane[1]     = normals[normals.length-1];
//        lastBoundingPlane[0]    = distopian;
//        lastBoundingPlane[1]    = normals[0];
//
//        int last = newIPOP.length-1;
//        newIPOP[last].addPopulation(collector);
//        if (this.useConstraints) {
//            prob = (InterfaceOptimizationProblem) islands[last].getProblem();
//            if (prob instanceof AbstractMultiObjectiveOptimizationProblem) {
//                // set the boundaries to perform the constrained
//                // domain principle introduced by Deb et al.
//                ((AbstractMultiObjectiveOptimizationProblem)prob).areaConst4Parallelization.clear();
//                ConstObjectivesInEqualitySmallerThanSurface sts = new ConstObjectivesInEqualitySmallerThanSurface(curBoundingPlane[0], curBoundingPlane[1]);
//                ConstObjectivesInEqualityBiggerThanSurface bts = new ConstObjectivesInEqualityBiggerThanSurface(lastBoundingPlane[0], lastBoundingPlane[1]);
//                ((AbstractMultiObjectiveOptimizationProblem)prob).areaConst4Parallelization.add(sts);
//                ((AbstractMultiObjectiveOptimizationProblem)prob).areaConst4Parallelization.add(bts);
//                }
//            islands[last].SetProblem(prob);
//        }
//        System.out.println("collector.size() "+ collector.size());
//        for (int i = 0; i < newIPOP.length; i++) {
//            System.out.println("newIPOP["+i+ "]: "+ newIPOP[i].size());
//        }
    }

    /**
     * This method returns the scalar product of two vectors
     *
     * @param a The first vector
     * @param b The second vector
     * @return The scalar product of a and b
     */
    private double getScalarProduct(double[] a, double[] b) {
        return (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
    }

    /**
     * This method returns the cross product of two 3D vectors
     *
     * @param a The first vector
     * @param b The second vector
     * @return The cross product
     */
    private double[] getCrossProduct(double[] a, double[] b) {
        double[] result = new double[3];

        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];

        return result;
    }

    private double[] getVectorAdd(double[] a, double[] b) {
        double[] result = new double[3];

        result[0] = a[0] + b[0];
        result[1] = a[1] + b[1];
        result[2] = a[2] + b[2];

        return result;
    }

    private double[] getVectorSub(double[] a, double[] b) {
        double[] result = new double[3];

        result[0] = a[0] - b[0];
        result[1] = a[1] - b[1];
        result[2] = a[2] - b[2];

        return result;
    }

    private double[] getScalarMultiplication(double a, double[] b) {
        double[] result = new double[3];

        result[0] = a * b[0];
        result[1] = a * b[1];
        result[2] = a * b[2];

        return result;
    }

    /**
     * This method will return a normalized vector
     *
     * @param a The vector to normalize
     * @return A normalized version of the input vector
     */
    private double[] getNormalized(double[] a) {
        double[] result = new double[a.length];
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i], 2);
        }
        sum = Math.sqrt(sum);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] / sum;
        }
        return result;
    }

    /**
     * Quaternion rotation
     *
     * @param s The first quaternion
     * @param v The second quaternion
     * @return The resulting quaternion
     */
    private double[] qMult(double[] s, double[] v) {
        double[] r = new double[4];
        r[0] = s[0] * v[0] - s[1] * v[1] - s[2] * v[2] - s[3] * v[3];
        r[1] = s[0] * v[1] + s[1] * v[0] + s[2] * v[3] - s[3] * v[2];
        r[2] = s[0] * v[2] - s[1] * v[3] + s[2] * v[0] + s[3] * v[1];
        r[3] = s[0] * v[3] + s[1] * v[2] - s[2] * v[1] + s[3] * v[0];
        return r;
    }

    /**
     * This method will perform a quaterion rotation
     *
     * @param v     The vector to rotate
     * @param u     The vector to rotate aroung
     * @param alpha The rotation angle in RAD!
     * @return The resulting rotated vector
     */
    private double[] rotQuad(double[] v, double[] u, double alpha) {
        double[] result = new double[3];
        double[] q = new double[4];
        double[] qt = new double[4];
        double[] p = new double[4];
        double s, f;

        p[0] = 0;
        p[1] = v[0];
        p[2] = v[1];
        p[3] = v[2];
        s = Math.cos(alpha / 2.0);
        f = Math.sin(alpha / 2.0);
        q[0] = s;
        q[1] = u[0] * f;
        q[2] = u[1] * f;
        q[3] = u[2] * f;
        // ok bis hier hin
        qt[0] = s;
        qt[1] = -q[1];
        qt[2] = -q[2];
        qt[3] = -q[3];
        p = this.qMult(q, p);
        p = this.qMult(p, qt);
        f = 1 / (q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        result[0] = p[1];// * f;
        result[1] = p[2]; // * f;
        result[2] = p[3]; // * f;
        result = this.getNormalized(result);
        return result;
    }

    private double[] rotVector(double[] p, double[] w, double a) {
        double[] result, tmp1, tmp2, tmp3;

        tmp1 = this.getScalarMultiplication(Math.cos(a), p);
        tmp2 = this.getScalarMultiplication(this.getScalarProduct(w, p), p);
        tmp2 = this.getScalarMultiplication((1 - Math.cos(a)), tmp2);
        tmp3 = this.getScalarMultiplication(Math.sin(a), this.getCrossProduct(w, p));
        result = this.getVectorAdd(tmp1, tmp2);
        result = this.getVectorAdd(result, tmp3);

        return result;
    }


//    public static void main(String[] args) {
//        MOConeSeparation    cone    = new MOConeSeparation();
//        cone.setUseConstraints(true);
//        BufferedWriter      outfile = null, outfile1 = null, outfile2 = null;
//
///**
//        double[] tmpDir = new double[3];
//        double[] tmpVec = new double[3];
//        double[] result;
//        int      n = 8;
//        double   alpha = 0;
//        double   incr = 360 /(double)n;
//        incr = 10;
//        tmpDir[0] = 0;
//        tmpDir[1] = 0;
//        tmpDir[2] = 1;
//        tmpVec[0] = 0;
//        tmpVec[1] = 1;
//        tmpVec[2] = 0;
//
//        // First the reference vector
//        try {
//            outfile1 = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOTestSurfVec.txt")));
//            outfile2 = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOTestSurf.txt")));
//        } catch (FileNotFoundException e) {
//            System.out.println("Could not open output file! Filename: ");
//        }
//        cone.writeToFile(outfile1, "0\t 0\t 0");
//        cone.writeToFile(outfile1, ""+tmpDir[0]+"\t"+tmpDir[1]+"\t"+tmpDir[2]);
//
//        for (int i = 0; i < n; i++) {
//            result = cone.rotQuad(tmpVec, tmpDir, Math.toRadians(i*incr));
//            System.out.println("Alpha: " + alpha);
//            System.out.println(result[0]+"\t"+result[1]+"\t"+result[2]);
//            //cone.writeToFile(outfile2, ""+tmpDir[0]+"\t"+tmpDir[1]+"\t"+tmpDir[2]);
//            cone.writeToFile(outfile2, "0\t 0\t 0");
//            cone.writeToFile(outfile2, ""+(result[0])+"\t"+(result[1])+"\t"+(result[2]));
//            alpha += incr;
//        }
//
//        try{
//            outfile1.close();
//            outfile2.close();
//        } catch (IOException e) {
//            System.out.println("Couldn't even close the stuff");
//        }
// **/
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
//        for (int i = 0; i < 20; i++) {
//            for (int j = 0; j < islands.length; j++) {
//                islands[j].optimize();
//            }
//            if (i % 4 == 0) cone.migrate(islands);
//        }
//
//        cone.migrate(islands);
//
//        double[] fitness;
//        ArchivingAllDominating arch = new ArchivingAllDominating();
//
//        for (int i = 0; i < islands.length; i++) {
//            try {
//                outfile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOCone"+i+".txt")));
//            } catch (FileNotFoundException e) {
//                System.out.println("Could not open output file! Filename: ");
//            }
//            //cone.writeToFile(outfile, " x\t y\t z");
//            islands[i].getPopulation().SetArchive(new Population());
//            arch.addElementsToArchive(islands[i].getPopulation());
//            for (int j = 0; j < islands[i].getPopulation().getArchive().size(); j++) {
//                fitness = ((AbstractEAIndividual)islands[i].getPopulation().getArchive().get(j)).getFitness();
//                cone.writeToFile(outfile, ""+fitness[0]+"\t"+fitness[1]+"\t"+fitness[2]);
//            }
//            try{
//                outfile.close();
//            } catch (IOException e) {
//                System.out.println("Couldn'T even close the stuff");
//            }
//        }
//
//        // First the reference vector
//        try {
//            outfile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOConeSurfVec.txt")));
//        } catch (FileNotFoundException e) {
//            System.out.println("Could not open output file! Filename: ");
//        }
//        double[][] surf = cone.bounds3D;
//        cone.writeToFile(outfile, "0\t 0\t 0");
//        cone.writeToFile(outfile, ""+surf[0][0]+"\t"+surf[0][1]+"\t"+surf[0][2]);
//        cone.writeToFile(outfile, ""+surf[0][0]+"\t"+surf[0][1]+"\t"+surf[0][2]);
//        cone.writeToFile(outfile, "0\t 0\t 0");
//        try{
//            outfile.close();
//        } catch (IOException e) {
//            System.out.println("Couldn't even close the stuff");
//        }
//
//        // now the surface normals
//        try {
//            outfile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream ("C://Programme//MATLAB6p5p1//work//MOConeSurf.txt")));
//        } catch (FileNotFoundException e) {
//            System.out.println("Could not open output file! Filename: ");
//        }
//        for (int i = 1; i < surf.length; i++) {
//            cone.writeToFile(outfile, ""+surf[0][0]+"\t"+surf[0][1]+"\t"+surf[0][2]);
//            surf[i] = cone.getScalarMultiplication(0.02, surf[i]);
//            cone.writeToFile(outfile, ""+(surf[i][0]+surf[0][0])+"\t"+(surf[i][1]+surf[0][1])+"\t"+(surf[i][2]+surf[0][2]));
//            cone.writeToFile(outfile, ""+surf[0][0]+"\t"+surf[0][1]+"\t"+surf[0][2]);
//        }
//        try{
//            outfile.close();
//        } catch (IOException e) {
//            System.out.println("Couldn't even close the stuff");
//        }
//
//        Population pop = new Population();
//        for (int i = 0; i < islands.length; i++) {
//            pop.addPopulation(islands[i].getPopulation());
//            pop.addPopulation(islands[i].getPopulation().getArchive());
//        }
//        //System.out.println("S-Metric: "+problem.getMetric().calculateMetricOn(pop, problem));
//
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
        return "MOConeSeparation";
    }

    /**
     * This method allows you to toggle which elements are
     * to be used to calculate the reference point. If false
     * all individuals are used to calculate the reference point.
     * This can cause the algorithm to build cone segments, which
     * are actually all dominated.
     *
     * @return The modus to calculate the reference point.
     */
    public boolean getUseAllToDetermineR() {
        return this.useAllToDetermineR;
    }

    public void setUseAllToDetermineR(boolean b) {
        this.useAllToDetermineR = b;
    }

    public String useAllToDetermineRTipText() {
        return "If true all individuals are used to calculate the reference point (may reduce efficiency).";
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
