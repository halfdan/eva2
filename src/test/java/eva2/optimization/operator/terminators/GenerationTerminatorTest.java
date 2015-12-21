package eva2.optimization.operator.terminators;

import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GenerationTerminatorTest {
    GenerationTerminator terminator;

    @Before
    public void setUp() throws Exception {
        terminator = new GenerationTerminator();
    }

    @Test
    public void testIsTerminated() throws Exception {
        Population pop = new Population();
        pop.setGeneration(100);
        assertFalse(terminator.isTerminated(pop));
        pop.incrGeneration();
        assertTrue(terminator.isTerminated(pop));

        pop.setGeneration(100);
        SolutionSet sset = new SolutionSet(pop);
        assertFalse(terminator.isTerminated(sset));
        pop.incrGeneration();
        assertTrue(terminator.isTerminated(sset));
    }

    @Test
    public void testLastTerminationMessage() throws Exception {
        assertNotNull(terminator.toString());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("Generation calls = 100", terminator.toString());
    }

    @Test
    public void testSetGenerations() throws Exception {
        // Defaults to 100 generations
        assertEquals(100, terminator.getGenerations());

        terminator.setGenerations(42);

        assertEquals(42, terminator.getGenerations());
    }

    @Test
    public void testGetGenerations() throws Exception {
        // Defaults to 100 generations
        assertEquals(100, terminator.getGenerations());
    }
}