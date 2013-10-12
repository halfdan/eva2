package simpleprobs;

import java.util.BitSet;

public class SimpleB1 extends SimpleProblemBinary {
    public static String globalInfo() {
        return "A simple B1 implementation, minimize bits in a binary vector.";
    }

    @Override
    public double[] eval(BitSet b) {
        double[] result = new double[1];
        int fitness = 0;

        for (int i = 0; i < getProblemDimension(); i++) {
            if (b.get(i)) {
                fitness++;
            }
        }
        result[0] = fitness;
        return result;
    }

    @Override
    public int getProblemDimension() {
        return 20;
    }

}
