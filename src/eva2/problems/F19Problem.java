package eva2.problems;

import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.Arrays;
import java.util.Random;

/**
 * Fletcher-Powell function with up to 2^n optima from Shir&Baeck, PPSN 2006,
 * after Bäck 1996. Alphas and Matrices A and B are randomly created with a fixed seed.
 *
 */
@Description("Fletcher-Powell function")
public class F19Problem extends AbstractProblemDouble implements
        InterfaceMultimodalProblem, InterfaceInterestingHistogram, InterfaceFirstOrderDerivableProblem {
    int problemDimension = 10;
    transient private double[] alphas, As;
    transient private int[] A, B;
    private long randSeed = 23;

    public F19Problem() {
        alphas = null;
        problemDimension = 10;
        setDefaultRange(Math.PI);
    }

    public F19Problem(F19Problem other) {
        problemDimension = other.problemDimension;
        alphas = null;
    }

    public F19Problem(int d) {
        this();
        setProblemDimension(d);
    }

    @Override
    public void initializeProblem() {
        super.initializeProblem();
        // create static random data
        Random rand = new Random();
        rand.setSeed(randSeed);
        alphas = RNG.randomDoubleArray(rand, -Math.PI, Math.PI, problemDimension);
        A = RNG.randomIntArray(rand, -100, 100, problemDimension * problemDimension);
        B = RNG.randomIntArray(rand, -100, 100, problemDimension * problemDimension);
        As = transform(alphas);
    }

    private double[] transform(double[] x) {
        double[] v = new double[problemDimension];
        Arrays.fill(v, 0.);
        for (int i = 0; i < problemDimension; i++) {
            for (int j = 0; j < problemDimension; j++) {
                v[i] += get(A, i, j) * Math.sin(x[j]) + get(B, i, j) * Math.cos(x[j]);
            }
        }
        return v;
    }

    /**
     * Calculate partial derivation of the B_i function by the j-th coordinate
     *
     * @param x
     * @param i
     * @return
     */
    private double derivedTransform(double[] x, int i, int j) {
        double v = get(A, i, j) * Math.cos(x[j]) - get(B, i, j) * Math.sin(x[j]);
        return v;
    }

    /**
     * Get a value in row i, col j, from matrix M (represented as vector).
     *
     * @param M
     * @param i
     * @param j
     * @return
     */
    private int get(int[] M, int i, int j) {
        return M[i * problemDimension + j];
    }

    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] res = new double[1];
        double[] Bs = transform(x);

        double sum = 0;
        for (int i = 0; i < getProblemDimension(); i++) {
            sum += Math.pow(As[i] - Bs[i], 2);
        }
        res[0] = sum;
        return res;
    }

    public void setProblemDimension(int newDim) {
        problemDimension = newDim;
        if (alphas != null && (newDim > alphas.length)) { // only recreate if really necessary
            alphas = null;
            A = null;
            B = null;
        }
    }

    @Override
    public Object clone() {
        return new F19Problem(this);
    }

    @Override
    public String getName() {
        return "F19-Problem";
    }

    @Override
    public SolutionHistogram getHistogram() {
        if (getProblemDimension() < 15) {
            return new SolutionHistogram(0, 8, 16);
        } else {
            return new SolutionHistogram(0, 40000, 16);
        }
    }

    @Override
    public double[] getFirstOrderGradients(double[] x) {
        x = rotateMaybe(x);
        double[] res = new double[x.length];
        double[] Bs = transform(x);

        for (int k = 0; k < getProblemDimension(); k++) {
            double sum = 0;
            for (int i = 0; i < getProblemDimension(); i++) {
                sum += (-2 * As[i] * derivedTransform(x, i, k) + 2 * Bs[i] * derivedTransform(x, i, k));
            }
            res[k] = sum;
        }
        return res;
    }

}

