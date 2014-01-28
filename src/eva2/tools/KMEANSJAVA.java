package eva2.tools;

import eva2.tools.math.RNG;

import java.util.Comparator;

/**
 *
 */
public class KMEANSJAVA {
    static public boolean TRACE = false;
    protected double[][] c;
    protected int[] indices;

    /**
     *
     */
    public double[][] getC() {
        return c;
    }

    /**
     *
     */
    public int[] getIDX() {
        return indices;
    }

    /**
     *
     */
    private double dist(double[] x1, double[] x2) {
        double ret = 0;
        for (int i = 0; i < x1.length; i++) {
            ret += (x1[i] - x2[i]) * (x1[i] - x2[i]);
        }
        return Math.sqrt(ret);
    }

    /**
     *
     */
    public KMEANSJAVA(double[][] samples, int K, int iterations) {
        //System.out.print("in");
        if (TRACE) {
            System.out.println("K" + K);
        }
        if (K > samples.length) {
            K = samples.length;
        }
        int counter = 0;
        c = new double[K][];
        for (int i = 0; i < K; i++) {
            c[i] = (double[]) samples[i].clone();
        }
        indices = new int[samples.length];
        while (counter++ < iterations) {
            // determine indices start
            for (int i = 0; i < indices.length; i++) {
                int index_nc = 0; // index of nearest cluster
                double mindist = 999999999;
                for (int j = 0; j < c.length; j++) {
                    if (mindist > dist(samples[i], c[j])) {
                        mindist = dist(samples[i], c[j]);
                        index_nc = j;
                    }
                }
                indices[i] = index_nc;
            }
            // determine indices end !
            // determine the new centers
            for (int indexofc = 0; indexofc < c.length; indexofc++) {
                double[] newcenter = new double[samples[0].length];
                int treffer = 0;
                for (int j = 0; j < indices.length; j++) { //System.out.println("j="+j);
                    if (indices[j] == indexofc) {
                        treffer++;
                        for (int d = 0; d < newcenter.length; d++) {
                            newcenter[d] += c[indices[j]][d];
                            //newcenter[d] = newcenter[d] + samples[j][d];
                        }
                    }
                }
                for (int d = 0; d < newcenter.length; d++) {
                    newcenter[d] /= treffer;
                }
                c[indexofc] = newcenter;
            }
            // determine the new centers
        }
        //System.out.println("out");
    }

    /**
     * Just a test function.
     */
    public static void main(String[] args) {
        int k = 3;
        int samples = 10;
        int d = 2;
        double[][] test = new double[samples][d];
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < d; j++) {
                test[i][j] = RNG.randomDouble(0, 10);
            }
        }
        KMEANSJAVA app = new KMEANSJAVA(test, k, 5);
        double[][] c = app.getC();
        int[] idx = app.getIDX();
        System.out.println("c");
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c[i].length; j++) {
                System.out.print(c[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("test");
        for (int i = 0; i < test.length; i++) {
            for (int j = 0; j < test[i].length; j++) {
                System.out.print(test[i][j] + " ");
            }
            System.out.println("");
        }
    }
}

/**
 *
 */
class ClusterComp implements Comparator {
    /**
     *
     */
    public ClusterComp() {
    }

    /**
     *
     */
    @Override
    public int compare(Object p1, Object p2) {
        int x1 = ((Cluster) p1).samplesInCluster;
        int x2 = ((Cluster) p2).samplesInCluster;
        if (x1 > x2) {
            return -1;
        }
        if (x1 <= x2) {
            return 1;
        }
        return 0;
    }

    /**
     *
     */
    @Override
    public boolean equals(Object x) {
        return false;
    }
}