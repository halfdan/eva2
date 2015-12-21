package eva2.optimization.operator.terminators;

import eva2.optimization.population.Population;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MaximumTimeTerminatorTest {
    MaximumTimeTerminator terminator;

    @Before
    public void setUp() throws Exception {
        terminator = new MaximumTimeTerminator();
        terminator.setMaximumTime(1);
    }

    @Test(timeout = 2500)
    public void testIsTerminated() throws Exception {
        terminator.initialize(null);

        while(!terminator.isTerminated(new Population()));
    }

    @Test
    public void testLastTerminationMessage() throws Exception {
        assertNotNull(terminator.lastTerminationMessage());
    }

    @Test
    public void testInitialize() throws Exception {
        terminator.initialize(null);
        // Should not be terminated after reinitialization
        assertFalse(terminator.isTerminated(new Population()));
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(terminator.toString());
    }

    @Test
    public void testGetMaximumTime() throws Exception {
        assertEquals(1, terminator.getMaximumTime());
    }

    @Test
    public void testSetMaximumTime() throws Exception {
        terminator.setMaximumTime(10);
        assertEquals(10, terminator.getMaximumTime());
    }
}