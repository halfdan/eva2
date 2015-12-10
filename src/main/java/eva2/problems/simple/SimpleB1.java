package eva2.problems.simple;

import eva2.util.annotation.Description;

import java.util.BitSet;

@Description("A simple B1 implementation, minimize bits in a binary vector.")
public class SimpleB1 extends SimpleProblemBinary {

    @Override
    public double[] evaluate(BitSet b) {
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
