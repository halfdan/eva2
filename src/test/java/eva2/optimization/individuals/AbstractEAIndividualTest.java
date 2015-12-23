package eva2.optimization.individuals;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class AbstractEAIndividualTest {

    @Test
    public void testIsDominatingFitness() throws Exception {
        // Single objective
        double[] fit1 = new double[]{405.231322};
        double[] fit2 = new double[]{123.128371293};

        assertTrue(AbstractEAIndividual.isDominatingFitness(fit2, fit1));
        assertFalse(AbstractEAIndividual.isDominatingFitness(fit1, fit2));

        // Multi objective
        fit1 = new double[]{12.0, 10.0, 8.0};
        fit2 = new double[]{11.0, 10.0, 9.0};

        assertFalse(AbstractEAIndividual.isDominatingFitness(fit1, fit2));
        assertFalse(AbstractEAIndividual.isDominatingFitness(fit2, fit1));

        fit2 = new double[]{11.0, 9.9, 0.9};
        assertTrue(AbstractEAIndividual.isDominatingFitness(fit2, fit1));

        // Is dominating itself
        assertTrue(AbstractEAIndividual.isDominatingFitness(fit1, fit1));
    }

    @Test
    public void testIsDominatingFitnessNotEqual() throws Exception {
        // Single objective
        double[] fit1 = new double[]{415.231322};

        // Is not dominating itself
        assertFalse(AbstractEAIndividual.isDominatingFitnessNotEqual(fit1, fit1));
    }

    @Test
    public void testGetStringRepresentation() throws Exception {

    }

    @Test
    public void testGetDefaultStringRepresentation() throws Exception {

    }

    @Test
    public void testGetDefaultDataString() throws Exception {

    }

    @Test
    public void testGetDefaultDataObject() throws Exception {

    }

    @Test
    public void testGetDefaultDataString1() throws Exception {

    }

    @Test
    public void testGetDoublePositionShallow() throws Exception {

    }

    @Test
    public void testGetDoublePosition() throws Exception {

    }
}