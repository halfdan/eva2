package eva2.optimization.operator.terminators;

import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FitnessValueTerminatorTest {
    private FitnessValueTerminator terminator;

    @Before
    public void setUp() throws Exception {
        terminator = new FitnessValueTerminator();
    }

    @Test
    public void testIsTerminated() throws Exception {
        Population pop = mock(Population.class);
        when(pop.getBestFitness()).thenReturn(new double[]{10E-3});

        assertFalse(terminator.isTerminated(pop));


        when(pop.getBestFitness()).thenReturn(new double[]{10E-5});
        assertTrue(terminator.isTerminated(pop));

        SolutionSet solSet = mock(SolutionSet.class);
        when(solSet.getCurrentPopulation()).thenReturn(pop);

        assertTrue(terminator.isTerminated(pop));
    }

    @Test
    public void testSetFitnessValue() throws Exception {
        assertArrayEquals(new double[]{10E-4}, terminator.getFitnessValue(), 0.0);

        double[] newFitness = {10E-4, 10E-4, 10E-4};
        terminator.setFitnessValue(newFitness);
        assertEquals(3, terminator.getFitnessValue().length);
        assertArrayEquals(newFitness, terminator.getFitnessValue(), 0.0);
    }
}