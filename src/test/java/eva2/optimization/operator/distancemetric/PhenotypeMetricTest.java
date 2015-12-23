package eva2.optimization.operator.distancemetric;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.ESIndividualIntegerData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PhenotypeMetricTest {
    private PhenotypeMetric metric;

    @Before
    public void setUp() throws Exception {
        metric = new PhenotypeMetric();
    }

    @Test
    public void testDistance() throws Exception {
        // DOUBLE
        ESIndividualDoubleData dindy1, dindy2;
        dindy1 = mock(ESIndividualDoubleData.class);
        when(dindy1.getDoubleData()).thenReturn(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        when(dindy1.getDoubleRange()).thenReturn(new double[][]{
                {0.0, 10.0},{0.0, 10.0},{0.0, 10.0},{0.0, 10.0},{0.0, 10.0}
        });
        dindy2 = mock(ESIndividualDoubleData.class);
        when(dindy2.getDoubleData()).thenReturn(new double[]{2.0, 2.0, 2.0, 2.0, 2.0});
        when(dindy2.getDoubleRange()).thenReturn(new double[][]{
                {0.0, 10.0},{0.0, 10.0},{0.0, 10.0},{0.0, 10.0},{0.0, 10.0}
        });

        // Should be zero for distance to itself
        assertEquals(0.0, metric.distance(dindy1, dindy1), 0.0);

        assertEquals(0.2236, metric.distance(dindy1, dindy2), 1E-4);

        // INTEGER
        ESIndividualIntegerData iindy1, iindy2;
        iindy1 = mock(ESIndividualIntegerData.class);
        when(iindy1.getIntegerData()).thenReturn(new int[]{1, 1, 1, 1, 1});
        when(iindy1.getIntRange()).thenReturn(new int[][]{
                {0, 10},{0, 10},{0, 10},{0, 10},{0, 10}
        });
        iindy2 = mock(ESIndividualIntegerData.class);
        when(iindy2.getIntegerData()).thenReturn(new int[]{3, 3, 3, 3, 3});
        when(iindy2.getIntRange()).thenReturn(new int[][]{
                {0, 10},{0, 10},{0, 10},{0, 10},{0, 10}
        });

        // Should be zero for distance to itself
        assertEquals(0.0, metric.distance(iindy1, iindy1), 0.0);

        assertEquals(0.4472, metric.distance(iindy1, iindy2), 1E-4);

    }

    @Test
    public void testNorm() throws Exception {

    }
}