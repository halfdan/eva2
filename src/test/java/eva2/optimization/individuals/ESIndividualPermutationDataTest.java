package eva2.optimization.individuals;

import org.junit.Test;

import static org.junit.Assert.*;

public class ESIndividualPermutationDataTest {

    @Test
    public void testMapXToY() throws Exception {
        double[][] matrix = new double[][]{
                {1.2, 2.3, 3.4, 1.2},
                {0.2, 0.3, 0.4}
        };

        // Properly constructs vector from matrix
        assertArrayEquals(
                new double[]{1.2, 2.3, 3.4, 1.2, 0.2, 0.3, 0.4},
                ESIndividualPermutationData.mapMatrixToVector(matrix), 0.0
        );

        // Properly converts to vector and back to matrix
        assertArrayEquals(
                matrix,
                ESIndividualPermutationData.mapVectorToMatrix(
                        ESIndividualPermutationData.mapMatrixToVector(matrix), new int[]{4,3}
                )
        );
    }
}