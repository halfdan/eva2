package simpleprobs;


public class SimpleF1 extends SimpleProblemDouble {
    public static String globalInfo() {
        return "A simple F1 implementation, find the minimum of a hyper parabola.";
    }

    @Override
    public double[] eval(double[] x) {
        double res[] = new double[1];
        // this defines the dimension of the fitness vector, which should be always the same

        double sum = 0;
        // calculate the fitness value
        for (int i = 0; i < getProblemDimension(); i++) {
            sum += (x[i] * x[i]);
        }

        // setting the return vector and return it
        res[0] = Math.sqrt(sum);
        return res;
    }

    @Override
    public int getProblemDimension() {
        return 20;
    }
}
